"""Step 3: call Google Directions (walking, alternatives=true), cache raw JSON, build routes table.

Usage:
  export GOOGLE_MAPS_API_KEY=...        (Windows PowerShell: $env:GOOGLE_MAPS_API_KEY="...")
  python fetch_routes.py cityA          # fetch missing OD pairs (cached ones are skipped), then parse
  python fetch_routes.py cityA --parse-only   # no API calls, only rebuild routes table from cache

Output: data/cache/directions/<city>/<od_id>.json   (raw responses, never re-fetched)
        data/processed/<city>_routes.csv
Stops once the OD pairs with >= MIN_ALTERNATIVES routes reach n_target_pairs.
"""
import json
import os
import sys
import time
import urllib.parse
import urllib.request
import pandas as pd
import config as C
from common import decode_polyline, polyline_length_m


def call_directions(row, key):
    q = urllib.parse.urlencode({
        "origin": f"{row.o_lat},{row.o_lng}", "destination": f"{row.d_lat},{row.d_lng}",
        "mode": C.GOOGLE_MODE, "alternatives": "true", "key": key})
    with urllib.request.urlopen(f"{C.GOOGLE_DIRECTIONS_URL}?{q}", timeout=30) as r:
        return json.loads(r.read().decode())


def parse_response(js):
    """-> list of dicts, one per route alternative."""
    out = []
    for i, rt in enumerate(js.get("routes", [])):
        legs = rt["legs"]
        dist = sum(l["distance"]["value"] for l in legs)
        dur = sum(l["duration"]["value"] for l in legs)
        overview = rt["overview_polyline"]["points"]
        step_pts = []
        for l in legs:
            for s in l["steps"]:
                step_pts.extend(decode_polyline(s["polyline"]["points"]))
        out.append({"route_idx": i, "distance_m": dist, "duration_s": dur,
                    "overview_polyline": overview,
                    "steps_points": json.dumps(step_pts)})
    return out


def run(city, parse_only=False):
    cfg = C.get_city(city)
    od = pd.read_csv(C.DATA_PROC / f"{city}_od_pairs.csv")
    cache_dir = C.DATA_CACHE / city
    cache_dir.mkdir(parents=True, exist_ok=True)
    key = os.environ.get("GOOGLE_MAPS_API_KEY")
    calls, good = 0, 0
    rows, kept_od = [], []
    for row in od.itertuples():
        f = cache_dir / f"{row.od_id}.json"
        if f.exists():
            js = json.loads(f.read_text())
        elif parse_only:
            continue
        else:
            if good >= cfg["n_target_pairs"]:
                break
            if not key:
                sys.exit("Set GOOGLE_MAPS_API_KEY first")
            if calls >= C.MAX_API_CALLS_PER_CITY:
                print("API call cap reached"); break
            js = call_directions(row, key)
            calls += 1
            f.write_text(json.dumps(js))
            time.sleep(C.API_SLEEP_SECONDS)
        if js.get("status") != "OK":
            continue
        routes = parse_response(js)
        if len(routes) >= C.MIN_ALTERNATIVES:
            good += 1
            kept_od.append(row.od_id)
            for r in routes:
                r.update({"city": city, "od_id": row.od_id})
                rows.append(r)
    df = pd.DataFrame(rows)
    out = C.DATA_PROC / f"{city}_routes.csv"
    df.to_csv(out, index=False)
    print(f"{city}: API calls this run={calls}; OD pairs with >={C.MIN_ALTERNATIVES} routes={good}; "
          f"route rows={len(df)} -> {out}")
    if len(df):
        print("alternatives per OD:\n", df.groupby("od_id").size().value_counts().sort_index())


if __name__ == "__main__":
    run(sys.argv[1], parse_only="--parse-only" in sys.argv)
