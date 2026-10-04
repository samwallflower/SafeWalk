"""Step 2: sample origin-destination pairs (seeded, reproducible).

Usage:  python sample_od.py cityA
Output: data/processed/<city>_od_pairs.csv
Oversamples (x1.6) because some pairs will later be dropped (<2 alternatives).
"""
import sys
import random
import pandas as pd
import config as C
from common import haversine_m


def sample(city):
    cfg = C.get_city(city)
    bbox = cfg["bbox"]
    if bbox is None:
        inc = pd.read_csv(C.DATA_PROC / f"{city}_incidents.csv")
        m = C.BBOX_MARGIN_DEG
        bbox = (inc.lat.quantile(.02) - m, inc.lng.quantile(.02) - m,
                inc.lat.quantile(.98) + m, inc.lng.quantile(.98) + m)
    rng = random.Random(cfg["seed"])
    target = int(cfg["n_target_pairs"] * 1.6)
    rows, tries = [], 0
    while len(rows) < target and tries < 200000:
        tries += 1
        a = (rng.uniform(bbox[0], bbox[2]), rng.uniform(bbox[1], bbox[3]))
        b = (rng.uniform(bbox[0], bbox[2]), rng.uniform(bbox[1], bbox[3]))
        d = haversine_m(*a, *b)
        if cfg["od_min_m"] <= d <= cfg["od_max_m"]:
            rows.append({"city": city, "od_id": f"{city}_{len(rows):04d}",
                         "o_lat": round(a[0], 6), "o_lng": round(a[1], 6),
                         "d_lat": round(b[0], 6), "d_lng": round(b[1], 6),
                         "straight_m": round(d, 1)})
    out = C.DATA_PROC / f"{city}_od_pairs.csv"
    pd.DataFrame(rows).to_csv(out, index=False)
    print(f"{city}: {len(rows)} OD pairs -> {out}")


if __name__ == "__main__":
    sample(sys.argv[1])
