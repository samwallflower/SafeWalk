"""Step 4: for every route, find every incident within MAX(RADII) of the polyline
and store the minimum distance. Any radius/weights/scale can then be applied
afterwards without touching the data again.

Mirrors RoutingService: polyline -> midpoints of consecutive points -> incidents
within radius of a midpoint (an incident is counted once per route = dedup by id).
For radius r:  incident counted  <=>  min distance to any midpoint <= r.

Usage:  python score.py cityA
Output: data/processed/<city>_route_incidents.csv   (city, od_id, route_idx, incident_id, min_dist_m)
"""
import json
import sys
import numpy as np
import pandas as pd
from scipy.spatial import cKDTree
import config as C
from common import Projector, decode_polyline, midpoints


def route_points(row):
    if C.POLYLINE_SOURCE == "steps":
        return [tuple(p) for p in json.loads(row.steps_points)]
    return decode_polyline(row.overview_polyline)


def score(city):
    inc = pd.read_csv(C.DATA_PROC / f"{city}_incidents.csv")
    routes = pd.read_csv(C.DATA_PROC / f"{city}_routes.csv")
    proj = Projector(inc.lat.mean(), inc.lng.mean())
    inc_xy = proj.xy(inc.lat.values, inc.lng.values)
    rmax = max(C.RADII)
    out = []
    for row in routes.itertuples():
        mids = midpoints(route_points(row))
        if not mids:
            continue
        mid_xy = proj.xy([m[0] for m in mids], [m[1] for m in mids])
        d, _ = cKDTree(mid_xy).query(inc_xy, k=1, distance_upper_bound=rmax)
        hit = np.isfinite(d)
        if hit.any():
            out.append(pd.DataFrame({"city": city, "od_id": row.od_id, "route_idx": row.route_idx,
                                     "incident_id": inc.incident_id.values[hit],
                                     "min_dist_m": np.round(d[hit], 2)}))
    res = pd.concat(out, ignore_index=True) if out else pd.DataFrame(
        columns=["city", "od_id", "route_idx", "incident_id", "min_dist_m"])
    f = C.DATA_PROC / f"{city}_route_incidents.csv"
    res.to_csv(f, index=False)
    print(f"{city}: {len(routes)} routes, {len(res)} (route, incident) pairs within {rmax} m -> {f}")


if __name__ == "__main__":
    score(sys.argv[1])
