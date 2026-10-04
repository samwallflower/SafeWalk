"""Step 5: all statistics. Reads processed CSVs, writes results/tables/*.csv

Usage: python analyze.py cityA cityB
Experiment A  radius sensitivity     -> table_A_radius.csv
Experiment B  penalty-scale          -> table_B_scale.csv
Experiment C  weighting scheme       -> table_C_scheme.csv
Experiment D  strategy comparison    -> table_D_strategies.csv  (held-out T2 incidents + in-sample T1)
"""
import sys
import numpy as np
import pandas as pd
from scipy.stats import kendalltau
import config as C

KEY = ["city", "od_id"]
RKEY = ["city", "od_id", "route_idx"]


# ----------------------------------------------------------------- loading
def load(cities):
    routes, inc, ri = [], [], []
    for c in cities:
        r = pd.read_csv(C.DATA_PROC / f"{c}_routes.csv")
        r["city"] = c
        routes.append(r[["city", "od_id", "route_idx", "distance_m", "duration_s"]])
        i = pd.read_csv(C.DATA_PROC / f"{c}_incidents.csv")
        i["city"] = c
        inc.append(i)
        ri.append(pd.read_csv(C.DATA_PROC / f"{c}_route_incidents.csv"))
    routes = pd.concat(routes, ignore_index=True)
    inc = pd.concat(inc, ignore_index=True)
    ri = pd.concat(ri, ignore_index=True)
    ri = ri.merge(inc[["city", "incident_id", "period", "category"]], on=["city", "incident_id"])
    n_alt = routes.groupby(KEY).size().rename("n_alt")
    routes = routes.merge(n_alt, on=KEY)
    routes = routes[routes.n_alt >= C.MIN_ALTERNATIVES].reset_index(drop=True)
    return routes, inc, ri


# ----------------------------------------------------------------- core
def exposure(routes, ri, radius, weights, period):
    """Per route: n incidents and weighted severity within radius, in the given period."""
    sub = ri[(ri.period == period) & (ri.min_dist_m <= radius)].copy()
    sub["w"] = sub.category.map(weights)
    g = sub.groupby(RKEY).agg(n=("incident_id", "size"), sev=("w", "sum")).reset_index()
    out = routes.merge(g, on=RKEY, how="left")
    out[["n", "sev"]] = out[["n", "sev"]].fillna(0)
    return out


def choose(rt, strategy, scale):
    """rt: output of exposure(). strategy in shortest|count|severity. Ties -> shorter route."""
    rt = rt.copy()
    pen = {"shortest": 0.0, "count": rt.n, "severity": rt.sev}[strategy]
    rt["virtual"] = rt.distance_m + scale * pen
    rt = rt.sort_values(KEY + ["virtual", "distance_m", "route_idx"])
    return rt.groupby(KEY).head(1).reset_index(drop=True)


def shortest_of(routes):
    return routes.sort_values(KEY + ["distance_m", "route_idx"]).groupby(KEY).head(1).reset_index(drop=True)


def boot_ci(x, n=C.BOOTSTRAP_N, seed=C.BOOTSTRAP_SEED):
    x = np.asarray(x, dtype=float)
    if len(x) == 0:
        return (np.nan, np.nan)
    rng = np.random.default_rng(seed)
    idx = rng.integers(0, len(x), size=(n, len(x)))
    m = x[idx].mean(axis=1)
    return tuple(np.percentile(m, [2.5, 97.5]))


# ----------------------------------------------------------------- A
def exp_a(routes, inc, ri, weights, scale):
    rows = []
    for city in sorted(routes.city.unique()):
        rc = routes[routes.city == city]
        ric = ri[ri.city == city]
        t1_total = ((inc.city == city) & (inc.period == "T1")).sum()
        ref = choose(exposure(rc, ric, C.REFERENCE_RADIUS, weights, "T1"), "severity", scale)
        ref_pick = ref.set_index(KEY).route_idx
        ref_v = exposure(rc, ric, C.REFERENCE_RADIUS, weights, "T1")
        ref_v["virtual"] = ref_v.distance_m + scale * ref_v.sev
        short = shortest_of(rc).set_index(KEY).route_idx
        for r in C.RADII:
            ex = exposure(rc, ric, r, weights, "T1")
            ex["pen"] = scale * ex.sev
            ex["virtual"] = ex.distance_m + ex.pen
            pick = choose(ex, "severity", scale).set_index(KEY).route_idx
            captured = ric[(ric.period == "T1") & (ric.min_dist_m <= r)].incident_id.nunique()
            spread = ex.groupby(KEY).pen.agg(lambda s: s.max() - s.min())
            shortest_d = ex.groupby(KEY).distance_m.min()
            taus = []
            m = ex.merge(ref_v[RKEY + ["virtual"]], on=RKEY, suffixes=("", "_ref"))
            for _, g in m.groupby(KEY):
                if len(g) >= 2 and g.virtual.nunique() > 1 and g.virtual_ref.nunique() > 1:
                    taus.append(kendalltau(g.virtual, g.virtual_ref)[0])
            rows.append({
                "city": city, "radius_m": r, "n_od": len(pick),
                "mean_incidents_per_route": ex.n.mean(),
                "mean_severity_per_route": ex.sev.mean(),
                "saturation_pct": 100 * captured / max(t1_total, 1),
                "penalty_spread_m_mean": spread.mean(),
                "penalty_spread_pct_of_shortest": 100 * (spread / shortest_d).mean(),
                "top_ne_shortest_pct": 100 * (pick != short.reindex(pick.index)).mean(),
                "top1_agree_with_ref_pct": 100 * (pick == ref_pick.reindex(pick.index)).mean(),
                "kendall_tau_vs_ref": float(np.mean(taus)) if taus else np.nan,
                "tau_n": len(taus)})
    return pd.DataFrame(rows)


# ----------------------------------------------------------------- B / C
def _pick_stats(rc, ric, radius, weights, scale, strategy="severity"):
    ex = exposure(rc, ric, radius, weights, "T1")
    pick = choose(ex, strategy, scale)
    short = shortest_of(rc)
    m = pick.merge(short[KEY + ["distance_m", "route_idx"]], on=KEY, suffixes=("", "_short"))
    detour = 100 * (m.distance_m / m.distance_m_short - 1)
    return {"n_od": len(m),
            "top_ne_shortest_pct": 100 * (m.route_idx != m.route_idx_short).mean(),
            "mean_detour_pct": detour.mean(),
            "mean_detour_pct_when_diverted": detour[m.route_idx != m.route_idx_short].mean()}


def exp_b(routes, ri, weights):
    rows = []
    for city in sorted(routes.city.unique()):
        rc, ric = routes[routes.city == city], ri[ri.city == city]
        for s in C.PENALTY_SCALES:
            rows.append({"city": city, "scale_m_per_point": s,
                         **_pick_stats(rc, ric, C.STRATEGY_ROUTING_RADIUS, weights, s)})
    return pd.DataFrame(rows)


def exp_c(routes, ri, scale):
    rows = []
    for city in sorted(routes.city.unique()):
        rc, ric = routes[routes.city == city], ri[ri.city == city]
        for name, w in C.available_schemes().items():
            # uniform weights = 1/point, so rescale to keep penalty magnitudes comparable
            sc = scale * (np.mean(list(C.WEIGHT_SCHEMES["hand"].values())) / np.mean(list(w.values())))
            rows.append({"city": city, "scheme": name, "scale_used": round(sc, 2),
                         **_pick_stats(rc, ric, C.STRATEGY_ROUTING_RADIUS, w, sc)})
    return pd.DataFrame(rows)


# ----------------------------------------------------------------- D
def exp_d(routes, ri, weights, scale):
    """Choose with T1 knowledge, evaluate on T2 (held out) and T1 (in-sample)."""
    rows = []
    uniform = C.WEIGHT_SCHEMES["uniform"]
    for city in sorted(routes.city.unique()):
        rc, ric = routes[routes.city == city], ri[ri.city == city]
        ex_t1 = exposure(rc, ric, C.STRATEGY_ROUTING_RADIUS, weights, "T1")
        ex_t1_u = exposure(rc, ric, C.STRATEGY_ROUTING_RADIUS, uniform, "T1")
        picks = {
            "shortest": shortest_of(rc),
            "count_only": choose(ex_t1_u, "count", scale * np.mean(list(weights.values()))),
            "severity_weighted": choose(ex_t1, "severity", scale)}
        # count_only uses the same average penalty per incident as severity => fair comparison
        short = shortest_of(rc).set_index(KEY)
        for period in ("T2", "T1"):
            for er in C.EVAL_RADII:
                ev_h = exposure(rc, ric, er, weights, period)
                ev_u = exposure(rc, ric, er, uniform, period)
                base = None
                for name, p in picks.items():
                    sel = p[KEY + ["route_idx"]]
                    h = sel.merge(ev_h[RKEY + ["n", "sev"]], on=RKEY).set_index(KEY)
                    u = sel.merge(ev_u[RKEY + ["sev"]], on=RKEY, suffixes=("", "_u")).set_index(KEY)
                    dist = p.set_index(KEY).distance_m.reindex(h.index)
                    detour = 100 * (dist / short.distance_m.reindex(h.index) - 1)
                    if name == "shortest":
                        base = (h.n.copy(), h.sev.copy())
                    d_n = (h.n - base[0].reindex(h.index))
                    d_s = (h.sev - base[1].reindex(h.index))
                    ci_n, ci_s = boot_ci(d_n), boot_ci(d_s)
                    rows.append({
                        "city": city, "strategy": name, "eval_period": period, "eval_radius_m": er,
                        "n_od": len(h), "mean_detour_pct": detour.mean(),
                        "mean_exposure_count": h.n.mean(), "mean_exposure_severity": h.sev.mean(),
                        "diff_count_vs_shortest": d_n.mean(),
                        "diff_count_ci_lo": ci_n[0], "diff_count_ci_hi": ci_n[1],
                        "diff_severity_vs_shortest": d_s.mean(),
                        "diff_severity_ci_lo": ci_s[0], "diff_severity_ci_hi": ci_s[1],
                        "pct_change_severity_vs_shortest": 100 * d_s.mean() / max(base[1].mean(), 1e-9),
                        "share_od_diverted_pct": 100 * (p.set_index(KEY).route_idx.reindex(h.index)
                                                        != short.route_idx.reindex(h.index)).mean()})
    return pd.DataFrame(rows)


# ----------------------------------------------------------------- main
def main(cities):
    routes, inc, ri = load(cities)
    C.RES_TABLES.mkdir(parents=True, exist_ok=True)
    w, sc = C.WEIGHT_SCHEMES["hand"], C.DEFAULT_SCALE
    out = {"table_A_radius": exp_a(routes, inc, ri, w, sc),
           "table_B_scale": exp_b(routes, ri, w),
           "table_C_scheme": exp_c(routes, ri, sc),
           "table_D_strategies": exp_d(routes, ri, w, sc)}
    for k, df in out.items():
        df.round(3).to_csv(C.RES_TABLES / f"{k}.csv", index=False)
        print(f"\n== {k} ==")
        print(df.round(2).to_string(index=False))
    print(f"\nOD pairs used: {routes.groupby('city').od_id.nunique().to_dict()}")


if __name__ == "__main__":
    main(sys.argv[1:])
