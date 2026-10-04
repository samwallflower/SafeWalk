"""SYNTHETIC end-to-end demo. Produces NO real findings -- it only proves the pipeline runs
and lets you see what every table/figure looks like before real data exists.

Usage: python run_demo.py        (writes into ../demo/, never touches ../data or ../results)
"""
import json
import random
import shutil
from pathlib import Path
import numpy as np
import pandas as pd
import config as C
from common import encode_polyline, polyline_length_m, haversine_m

DEMO = C.ROOT / "demo"
if DEMO.exists():
    shutil.rmtree(DEMO)
C.DATA_RAW = DEMO / "data" / "raw"
C.DATA_CACHE = DEMO / "data" / "cache" / "directions"
C.DATA_PROC = DEMO / "data" / "processed"
C.RES_TABLES = DEMO / "results" / "tables"
C.RES_FIGS = DEMO / "results" / "figures"
C.CITIES = {
    "demoA": {"label": "Synthetic A (high density)", "raw_dir": C.DATA_RAW / "demoA",
              "bbox": (47.50, 21.58, 47.55, 21.66), "t1": ("2024-01", "2024-12"),
              "t2": ("2025-01", "2025-06"), "seed": 1, "n_target_pairs": 60,
              "od_min_m": 1000, "od_max_m": 3000},
    "demoB": {"label": "Synthetic B (low density)", "raw_dir": C.DATA_RAW / "demoB",
              "bbox": (47.50, 21.58, 47.55, 21.66), "t1": ("2024-01", "2024-12"),
              "t2": ("2025-01", "2025-06"), "seed": 2, "n_target_pairs": 60,
              "od_min_m": 1000, "od_max_m": 3000},
}
import prepare_data, sample_od, fetch_routes, score, analyze, figures  # noqa: E402

TYPES = ["Robbery", "Violence and sexual offences", "Public order", "Anti-social behaviour",
         "Criminal damage and arson", "Bicycle theft", "Burglary"]


def make_raw(city, n_per_month, n_hot, seed):
    rng = np.random.default_rng(seed)
    bb = C.CITIES[city]["bbox"]
    hot = np.column_stack((rng.uniform(bb[0], bb[2], n_hot), rng.uniform(bb[1], bb[3], n_hot)))
    d = C.CITIES[city]["raw_dir"]
    d.mkdir(parents=True, exist_ok=True)
    months = [f"2024-{m:02d}" for m in range(1, 13)] + [f"2025-{m:02d}" for m in range(1, 7)]
    for mo in months:
        k = rng.poisson(n_per_month)
        h = hot[rng.integers(0, n_hot, k)]
        lat = h[:, 0] + rng.normal(0, 0.0035, k)
        lng = h[:, 1] + rng.normal(0, 0.005, k)
        pd.DataFrame({"Month": mo, "Longitude": lng, "Latitude": lat,
                      "Crime type": rng.choice(TYPES, k, p=[.07, .28, .18, .22, .12, .06, .07])}
                     ).to_csv(d / f"{mo}-demo-street.csv", index=False)


def make_directions_cache(city, seed):
    rng = random.Random(seed)
    od = pd.read_csv(C.DATA_PROC / f"{city}_od_pairs.csv")
    cd = C.DATA_CACHE / city
    cd.mkdir(parents=True, exist_ok=True)
    for r in od.itertuples():
        k = 1 if rng.random() < .12 else rng.choice([2, 3])
        routes = []
        for j, off in enumerate([0, 0.0035, -0.0035][:k]):
            off *= rng.uniform(.6, 1.4)
            pts = []
            for t in np.linspace(0, 1, 28):
                bow = np.sin(np.pi * t) * off
                lat = r.o_lat + (r.d_lat - r.o_lat) * t + bow * (r.d_lng - r.o_lng) / 0.05
                lng = r.o_lng + (r.d_lng - r.o_lng) * t - bow * (r.d_lat - r.o_lat) / 0.05
                pts.append((lat + rng.gauss(0, .0002), lng + rng.gauss(0, .0002)))
            dist = int(polyline_length_m(pts) * 1.15)
            routes.append({"legs": [{"distance": {"value": dist}, "duration": {"value": int(dist / 1.3)},
                                     "steps": [{"polyline": {"points": encode_polyline(pts[:14])}},
                                               {"polyline": {"points": encode_polyline(pts[13:])}}]}],
                           "overview_polyline": {"points": encode_polyline(pts)}})
        (cd / f"{r.od_id}.json").write_text(json.dumps({"status": "OK", "routes": routes}))


if __name__ == "__main__":
    make_raw("demoA", 380, 14, 5)
    make_raw("demoB", 110, 14, 6)
    for c in ("demoA", "demoB"):
        prepare_data.prepare(c)
        sample_od.sample(c)
        make_directions_cache(c, 9)
        fetch_routes.run(c, parse_only=True)
        score.score(c)
    analyze.main(["demoA", "demoB"])
    figures.main()
