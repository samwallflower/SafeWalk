"""Parity check: print what the offline scorer computes for ONE OD pair at ONE radius,
so you can compare with the app's POST /routing/recommend response (same origin/destination,
same app.routing.segment-buffer-meters and penalty-meters-per-severity-point).

Usage: python parity.py cityA cityA_0007 100 50 [T1]
Compare: distance_m, n_incidents, severity, penalty_m, virtual_m per route (order = Google order).
Small differences are expected if the app's DB holds different incidents than the CSV
(the app counts ACTIVE incidents of ANY date; the scorer filters by period).
"""
import sys
import pandas as pd
import config as C

city, od, radius, scale = sys.argv[1], sys.argv[2], float(sys.argv[3]), float(sys.argv[4])
period = sys.argv[5] if len(sys.argv) > 5 else "T1"
w = C.WEIGHT_SCHEMES["hand"]
routes = pd.read_csv(C.DATA_PROC / f"{city}_routes.csv")
routes = routes[routes.od_id == od]
inc = pd.read_csv(C.DATA_PROC / f"{city}_incidents.csv")
ri = pd.read_csv(C.DATA_PROC / f"{city}_route_incidents.csv")
ri = ri[(ri.od_id == od) & (ri.min_dist_m <= radius)].merge(inc[["incident_id", "period", "category"]], on="incident_id")
ri = ri[ri.period == period]
ri["w"] = ri.category.map(w)
g = ri.groupby("route_idx").agg(n_incidents=("incident_id", "size"), severity=("w", "sum"))
out = routes[["route_idx", "distance_m"]].merge(g, on="route_idx", how="left").fillna(0)
out["penalty_m"] = out.severity * scale
out["virtual_m"] = out.distance_m + out.penalty_m
print(out.to_string(index=False))
