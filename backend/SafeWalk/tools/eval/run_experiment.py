"""Step 3 (live endpoint): run every OD pair x configuration through the REAL
POST /routing/recommend endpoint and append one row per returned route.

Prerequisites
  * backend running with the eval profile:  ./mvnw spring-boot:run -Dspring-boot.run.profiles=eval
    (run from backend/SafeWalk so the Directions replay cache lands in tools/data/...)
  * incidents imported (import_incidents.sql), <city>_od_pairs.csv from sample_od.py
  * GOOGLE_MAPS_API_KEY is only needed by the backend process, never by this script

Usage:  python run_experiment.py cityA [--limit 5] [--schemes hand ons]
Output: results/raw/<city>_runs.csv  (resumable: finished scheme/config/OD rows are skipped)

Category weights live in the DB, so for each scheme the script logs in as admin,
PUTs the scheme's weights via /incident-categories/{id}/update, verifies them,
runs all configurations, and finally restores the weights it found at the start.
"""
import argparse
import csv
import sys
import time
import pandas as pd
import requests
import config as C

COLUMNS = ["city", "od_id", "scheme", "config_id", "buffer_m", "penalty_scale", "weighting",
           "route_idx", "rank", "distance_m", "penalty_m", "virtual_m", "n_incidents",
           "incident_ids", "polyline"]
SKIPPED = ("No routes found", "No usable routes")


def configs_for(scheme):
    """(config_id, buffer_m, penalty_scale, weighting) for one scheme."""
    out, seen = [], set()

    def add(buf, scale, weighting):
        key = (buf, scale, weighting)
        if key not in seen:
            seen.add(key)
            tag = "UNI" if weighting == "UNIFORM" else "CAT"
            out.append((f"R{buf}_S{scale:g}_{tag}", buf, scale, weighting))

    for r in C.RADII:                                    # A: radius sweep
        add(r, C.DEFAULT_SCALE, "CATEGORY")
    for s in C.PENALTY_SCALES:                           # B: scale sweep
        add(C.REFERENCE_RADIUS, s, "CATEGORY")
    # count-only strategy: every incident = 1 point, same mean penalty per incident as the
    # severity-weighted run at the same base scale (so scale = base x mean category weight)
    for s in C.STRATEGY_SCALES:
        add(C.STRATEGY_ROUTING_RADIUS, round(s * C.mean_weight(scheme), 2), "UNIFORM")
    return out


class Api:
    def __init__(self):
        self.s = requests.Session()
        self.token = None

    def login(self):
        r = self.s.post(f"{C.API_BASE}/auth/login",
                        json={"email": C.ADMIN_EMAIL, "password": C.ADMIN_PASSWORD}, timeout=30)
        if r.status_code != 200:
            sys.exit(f"admin login failed: {r.status_code} {r.text[:300]}")
        self.token = r.json()["data"]["token"]
        self.s.headers["Authorization"] = f"Bearer {self.token}"

    def categories(self):
        r = self.s.get(f"{C.API_BASE}/incident-categories/all", timeout=30)
        r.raise_for_status()
        return {c["name"].lower(): c for c in r.json()["data"]}

    def set_weights(self, weights):
        cats = self.categories()
        for name, w in weights.items():
            if name not in cats:
                sys.exit(f"category '{name}' not found in DB (found: {sorted(cats)})")
            if cats[name]["severityWeight"] == w:
                continue
            r = self.s.put(f"{C.API_BASE}/incident-categories/{cats[name]['id']}/update",
                           json={"severityWeight": int(w)}, timeout=30)
            if r.status_code != 200:
                sys.exit(f"weight update failed for {name}: {r.status_code} {r.text[:300]}")
        now = {k: v["severityWeight"] for k, v in self.categories().items()}
        bad = {k: (now.get(k), w) for k, w in weights.items() if now.get(k) != w}
        if bad:
            sys.exit(f"weights not applied (db, wanted): {bad}")

    def recommend(self, od, buf, scale, weighting):
        body = {"originLatitude": od.o_lat, "originLongitude": od.o_lng,
                "destinationLatitude": od.d_lat, "destinationLongitude": od.d_lng,
                "bufferMeters": buf, "penaltyMetersPerPoint": scale, "weighting": weighting}
        return self.s.post(f"{C.API_BASE}/routing/recommend", json=body, timeout=300)


def done_keys(path):
    if not path.exists():
        return set()
    d = pd.read_csv(path, usecols=["od_id", "scheme", "config_id"])
    return set(zip(d.scheme, d.config_id, d.od_id))


def run(city, limit, schemes):
    ods = pd.read_csv(C.DATA_PROC / f"{city}_od_pairs.csv")
    if limit:
        ods = ods.head(limit)
    C.RES_RAW.mkdir(parents=True, exist_ok=True)
    out = C.RES_RAW / f"{city}_runs.csv"
    bad_file = C.RES_RAW / f"{city}_skipped_od.csv"
    skipped = set(pd.read_csv(bad_file).od_id) if bad_file.exists() else set()
    done = done_keys(out)
    new_file = not out.exists()

    api = Api()
    api.login()
    original = {k: v["severityWeight"] for k, v in api.categories().items()}
    print("original DB weights:", original)

    try:
        with open(out, "a", newline="", encoding="utf-8") as fh:
            w = csv.DictWriter(fh, fieldnames=COLUMNS)
            if new_file:
                w.writeheader()
            for scheme in schemes:
                api.set_weights(C.WEIGHT_SCHEMES[C.RUN_SCHEMES[scheme]])
                cfgs = configs_for(scheme)
                total = len(ods) * len(cfgs)
                n = 0
                t0 = time.time()
                print(f"\n[{city}] scheme={scheme}: {len(cfgs)} configs x {len(ods)} OD = {total} calls")
                for cfg_id, buf, scale, weighting in cfgs:
                    for od in ods.itertuples():
                        n += 1
                        if (scheme, cfg_id, od.od_id) in done or od.od_id in skipped:
                            continue
                        r = api.recommend(od, buf, scale, weighting)
                        if r.status_code != 200:
                            if any(m in r.text for m in SKIPPED):
                                skipped.add(od.od_id)
                                pd.DataFrame({"od_id": sorted(skipped)}).to_csv(bad_file, index=False)
                                print(f"  {od.od_id}: Google returned no route, skipped")
                                continue
                            sys.exit(f"STOP {r.status_code} on {od.od_id} {cfg_id}: {r.text[:500]}")
                        routes = r.json()["data"]
                        for rt in routes:
                            if not rt.get("polyline"):
                                sys.exit(f"empty polyline for {od.od_id} {cfg_id}")
                            w.writerow({
                                "city": city, "od_id": od.od_id, "scheme": scheme, "config_id": cfg_id,
                                "buffer_m": buf, "penalty_scale": scale, "weighting": weighting,
                                "route_idx": rt["googleIndex"], "rank": rt["rank"],
                                "distance_m": rt["actualDistanceMeters"],
                                "penalty_m": rt["safetyPenaltyMeters"],
                                "virtual_m": rt["virtualDistanceMeters"],
                                "n_incidents": rt["incidentCount"],
                                "incident_ids": ";".join(map(str, rt["incidentIds"])),
                                "polyline": rt["polyline"]})
                        fh.flush()
                        if n % 25 == 0:
                            el = time.time() - t0
                            print(f"  {n}/{total}  {cfg_id}  ({el:.0f}s elapsed)")
    finally:
        api.set_weights(original)
        print("restored original DB weights")
    print("wrote", out)


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("city")
    ap.add_argument("--limit", type=int, default=0, help="only the first N OD pairs (smoke test)")
    ap.add_argument("--schemes", nargs="+", default=list(C.RUN_SCHEMES), choices=list(C.RUN_SCHEMES))
    a = ap.parse_args()
    run(a.city, a.limit, a.schemes)
