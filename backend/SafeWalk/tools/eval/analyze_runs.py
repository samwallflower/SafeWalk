"""Step 5 (live endpoint): all statistics from the runs produced by run_experiment.py.

Usage: python analyze_runs.py cityA cityB
Reads   results/raw/<city>_runs.csv   (what the real endpoint returned)
        data/processed/<city>_incidents.csv   (T1 totals; T2 = held-out incidents)
Writes  results/tables/table_A_radius.csv, table_B_scale.csv, table_C_scheme.csv,
        table_D_strategies.csv, table_E_contrasts.csv (paired severity-weighted vs random / count-only)   (every table has a `scheme` column: hand | ons)

A, B, C come straight from the endpoint output. D picks each strategy's route per
OD pair from the runs (shortest = min distance; count_only = UNIFORM run; severity
= default run) and then scores the chosen polylines offline against incidents the
router never saw (T2), plus T1 in-sample, with the midpoint logic of score.py.
"""
import sys
import numpy as np
import pandas as pd
from scipy.spatial import cKDTree
from scipy.stats import kendalltau
import config as C
from common import Projector, decode_polyline, midpoints
from analyze import boot_ci
from run_experiment import configs_for

KEY = ["city", "od_id"]
DEF_CFG = f"R{C.REFERENCE_RADIUS}_S{C.DEFAULT_SCALE:g}_CAT"   # default configuration


def load(cities):
    runs, inc = [], []
    for c in cities:
        r = pd.read_csv(C.RES_RAW / f"{c}_runs.csv")
        runs.append(r)
        i = pd.read_csv(C.DATA_PROC / f"{c}_incidents.csv")
        i["city"] = c
        inc.append(i)
    runs = pd.concat(runs, ignore_index=True)
    inc = pd.concat(inc, ignore_index=True)
    runs["incident_ids"] = runs["incident_ids"].fillna("").astype(str)
    # drop configs the harness no longer defines (e.g. left over from earlier smoke runs)
    valid = {(sch, cfg[0]) for sch in C.RUN_SCHEMES for cfg in configs_for(sch)}
    runs = runs[[(s, c) in valid for s, c in zip(runs.scheme, runs.config_id)]]
    n_alt = runs[runs.config_id == DEF_CFG].groupby(["city", "scheme", "od_id"]).size().rename("n_alt")
    runs = runs.merge(n_alt.reset_index(), on=["city", "scheme", "od_id"], how="left")
    runs = runs[runs.n_alt >= C.MIN_ALTERNATIVES].reset_index(drop=True)
    return runs, inc


def top(df):
    """The route the endpoint recommends (rank 1) per scheme/config/OD."""
    return df[df["rank"] == 1]


def shortest(df):
    """Shortest Google route per OD (distance is identical in every config)."""
    d = df[df.config_id == DEF_CFG] if (df.config_id == DEF_CFG).any() else df
    return (d.sort_values(["city", "od_id", "distance_m", "route_idx"])
            .groupby(KEY).head(1).set_index(KEY))


def _cfg(df, scheme, cfg):
    return df[(df.scheme == scheme) & (df.config_id == cfg)]


def _detour(pick, short):
    m = pick.set_index(KEY)
    s = short.distance_m.reindex(m.index)
    return 100 * (m.distance_m / s - 1), m.route_idx != short.route_idx.reindex(m.index)


# ------------------------------------------------------------------ A
def exp_a(runs, inc):
    rows = []
    for scheme in sorted(runs.scheme.unique()):
        for city in sorted(runs.city.unique()):
            rc = runs[(runs.city == city) & (runs.scheme == scheme)]
            t1_total = ((inc.city == city) & (inc.period == "T1")).sum()
            short = shortest(rc)
            ref = _cfg(rc, scheme, DEF_CFG)
            ref_pick = top(ref).set_index(KEY).route_idx
            for r in C.RADII:
                ex = _cfg(rc, scheme, f"R{r}_S{C.DEFAULT_SCALE:g}_CAT")
                if ex.empty:
                    continue
                pick = top(ex).set_index(KEY).route_idx
                ids = set(";".join(ex.incident_ids).split(";")) - {""}
                spread = ex.groupby(KEY).penalty_m.agg(lambda s: s.max() - s.min())
                shortest_d = ex.groupby(KEY).distance_m.min()
                taus = []
                m = ex.merge(ref[KEY + ["route_idx", "virtual_m"]], on=KEY + ["route_idx"], suffixes=("", "_ref"))
                for _, g in m.groupby(KEY):
                    if len(g) >= 2 and g.virtual_m.nunique() > 1 and g.virtual_m_ref.nunique() > 1:
                        taus.append(kendalltau(g.virtual_m, g.virtual_m_ref)[0])
                rows.append({
                    "city": city, "scheme": scheme, "radius_m": r, "n_od": len(pick),
                    "mean_incidents_per_route": ex.n_incidents.mean(),
                    "mean_severity_per_route": (ex.penalty_m / ex.penalty_scale).mean(),
                    "saturation_pct": 100 * len(ids) / max(t1_total, 1),
                    "penalty_spread_m_mean": spread.mean(),
                    "penalty_spread_pct_of_shortest": 100 * (spread / shortest_d).mean(),
                    "top_ne_shortest_pct": 100 * (pick != short.route_idx.reindex(pick.index)).mean(),
                    "top1_agree_with_ref_pct": 100 * (pick == ref_pick.reindex(pick.index)).mean(),
                    "kendall_tau_vs_ref": float(np.mean(taus)) if taus else np.nan,
                    "tau_n": len(taus)})
    return pd.DataFrame(rows)


# ------------------------------------------------------------------ B / C
def _stats(pick, short):
    detour, diverted = _detour(pick, short)
    return {"n_od": len(detour),
            "top_ne_shortest_pct": 100 * diverted.mean(),
            "mean_detour_pct": detour.mean(),
            "mean_detour_pct_when_diverted": detour[diverted].mean()}


def exp_b(runs):
    rows = []
    for scheme in sorted(runs.scheme.unique()):
        for city in sorted(runs.city.unique()):
            rc = runs[(runs.city == city) & (runs.scheme == scheme)]
            short = shortest(rc)
            for s in C.PENALTY_SCALES:
                ex = _cfg(rc, scheme, f"R{C.REFERENCE_RADIUS}_S{s:g}_CAT")
                if not ex.empty:
                    rows.append({"city": city, "scheme": scheme, "scale_m_per_point": s,
                                 **_stats(top(ex), short)})
    return pd.DataFrame(rows)


def exp_c(runs):
    """Weighting-scheme sensitivity at the reference radius: every scheme x scale,
    plus the count-only (UNIFORM) run, compared with the hand/default pick."""
    rows = []
    for city in sorted(runs.city.unique()):
        rcity = runs[runs.city == city]
        short = shortest(rcity)
        ref = top(_cfg(rcity, C.MAIN_SCHEME, DEF_CFG)).set_index(KEY).route_idx
        for scheme in sorted(rcity.scheme.unique()):
            rs = rcity[(rcity.scheme == scheme) & (rcity.buffer_m == C.REFERENCE_RADIUS)]
            for cfg_id, g in rs.groupby("config_id"):
                p = top(g).set_index(KEY)
                rows.append({"city": city, "scheme": scheme, "config_id": cfg_id,
                             "weighting": g.weighting.iloc[0], "scale_used": g.penalty_scale.iloc[0],
                             **_stats(top(g), short),
                             "top1_agree_with_hand_default_pct":
                                 100 * (p.route_idx == ref.reindex(p.index)).mean()})
    return pd.DataFrame(rows)


# ------------------------------------------------------------------ D
class RouteScorer:
    """Distance from every incident of a city to a polyline's midpoints (<= max eval radius)."""

    def __init__(self, inc):
        self.inc = inc.reset_index(drop=True)
        self.proj = Projector(self.inc.lat.mean(), self.inc.lng.mean())
        self.xy = self.proj.xy(self.inc.lat.values, self.inc.lng.values)
        self.rmax = max(C.EVAL_RADII)
        self.cache = {}

    def hits(self, polyline):
        if polyline not in self.cache:
            mids = midpoints(decode_polyline(polyline))
            mxy = self.proj.xy([m[0] for m in mids], [m[1] for m in mids])
            d, _ = cKDTree(mxy).query(self.xy, k=1, distance_upper_bound=self.rmax)
            ok = np.isfinite(d)
            self.cache[polyline] = (np.flatnonzero(ok), d[ok])
        return self.cache[polyline]

    def exposure(self, polyline, radius, period, weights):
        idx, d = self.hits(polyline)
        sel = idx[d <= radius]
        sub = self.inc.iloc[sel]
        sub = sub[sub.period == period]
        return len(sub), float(sub.category.map(weights).sum())


def exp_d(runs, inc, contrasts=None):
    """Strategies compared at each scale in STRATEGY_SCALES (plus a random-alternative control). severity = CATEGORY run at that
    scale; count_only = UNIFORM run whose scale is base x mean weight (same avg penalty/incident)."""
    rows = []
    for scheme in sorted(runs.scheme.unique()):
        weights = C.WEIGHT_SCHEMES[C.RUN_SCHEMES[scheme]]
        mw = C.mean_weight(scheme)
        for city in sorted(runs.city.unique()):
            rc = runs[(runs.city == city) & (runs.scheme == scheme)]
            short = shortest(rc)
            sc = RouteScorer(inc[inc.city == city])
            lookup = rc.drop_duplicates(KEY + ["route_idx"]).set_index(KEY + ["route_idx"]).polyline
            for base in C.STRATEGY_SCALES:
                sev_cfg = f"R{C.STRATEGY_ROUTING_RADIUS}_S{base:g}_CAT"
                cnt_cfg = f"R{C.STRATEGY_ROUTING_RADIUS}_S{round(base * mw, 2):g}_UNI"
                if _cfg(rc, scheme, sev_cfg).empty or _cfg(rc, scheme, cnt_cfg).empty:
                    continue
                cols = KEY + ["route_idx", "distance_m"]
                picks = {"shortest": short.reset_index()[cols],
                         "count_only": top(_cfg(rc, scheme, cnt_cfg))[cols],
                         "severity_weighted": top(_cfg(rc, scheme, sev_cfg))[cols]}
                allr = rc.drop_duplicates(KEY + ["route_idx"])[KEY + ["route_idx", "distance_m"]]
                for period in ("T2", "T1"):
                    for er in C.EVAL_RADII:
                        base_exp = None
                        # route -> (count, severity) once per route, reused by every strategy
                        ex_all = {(c, o, ri): sc.exposure(lookup[(c, o, ri)], er, period, weights)
                                  for c, o, ri in zip(allr.city, allr.od_id, allr.route_idx)}

                        def evaluate(name):
                            if name == "random_alternative":
                                # expected value of picking one Google alternative uniformly at random:
                                # the mean over the routes of each OD (deterministic, no seed needed)
                                t = allr.copy()
                                t["n"] = [ex_all[(c, o, ri)][0] for c, o, ri in zip(t.city, t.od_id, t.route_idx)]
                                t["sev"] = [ex_all[(c, o, ri)][1] for c, o, ri in zip(t.city, t.od_id, t.route_idx)]
                                t["detour"] = 100 * (t.distance_m / short.distance_m.reindex(
                                    pd.MultiIndex.from_frame(t[KEY])).values - 1)
                                t["div"] = (t.route_idx != short.route_idx.reindex(
                                    pd.MultiIndex.from_frame(t[KEY])).values).astype(float)
                                g = t.groupby(KEY)[["n", "sev", "detour", "div"]].mean()
                                return g.n, g.sev, g.detour, 100 * g["div"]
                            p = picks[name].set_index(KEY)
                            n = pd.Series([ex_all[(c, o, ri)][0] for (c, o), ri in zip(p.index, p.route_idx)],
                                          index=p.index, dtype=float)
                            sev = pd.Series([ex_all[(c, o, ri)][1] for (c, o), ri in zip(p.index, p.route_idx)],
                                            index=p.index, dtype=float)
                            detour = 100 * (p.distance_m / short.distance_m.reindex(p.index) - 1)
                            div = 100 * (p.route_idx != short.route_idx.reindex(p.index)).astype(float)
                            return n, sev, detour, div

                        ev = {}
                        for name in ["shortest", "count_only", "severity_weighted", "random_alternative"]:
                            n, sev, detour, div = evaluate(name)
                            ev[name] = (n, sev, detour)
                            if name == "shortest":
                                base_exp = (n.copy(), sev.copy())
                            d_n, d_s = n - base_exp[0].reindex(n.index), sev - base_exp[1].reindex(n.index)
                            ci_n, ci_s = boot_ci(d_n), boot_ci(d_s)
                            rows.append({
                                "city": city, "scheme": scheme, "scale_m_per_point": base, "strategy": name,
                                "eval_period": period, "eval_radius_m": er, "n_od": len(n),
                                "mean_detour_pct": detour.mean(),
                                "mean_exposure_count": n.mean(), "mean_exposure_severity": sev.mean(),
                                "diff_count_vs_shortest": d_n.mean(),
                                "diff_count_ci_lo": ci_n[0], "diff_count_ci_hi": ci_n[1],
                                "diff_severity_vs_shortest": d_s.mean(),
                                "diff_severity_ci_lo": ci_s[0], "diff_severity_ci_hi": ci_s[1],
                                "pct_change_severity_vs_shortest": 100 * d_s.mean() / max(base_exp[1].mean(), 1e-9),
                                "share_od_diverted_pct": div.mean()})
                        if contrasts is not None:
                            for a, b in [("severity_weighted", "random_alternative"),
                                         ("severity_weighted", "count_only")]:
                                for metric, i in (("count", 0), ("severity", 1), ("detour_pct", 2)):
                                    d = ev[a][i] - ev[b][i].reindex(ev[a][i].index)
                                    lo, hi = boot_ci(d)
                                    contrasts.append({
                                        "city": city, "scheme": scheme, "scale_m_per_point": base,
                                        "eval_period": period, "eval_radius_m": er, "n_od": len(d),
                                        "a": a, "b": b, "metric": metric,
                                        "mean_diff_a_minus_b": d.mean(), "ci_lo": lo, "ci_hi": hi,
                                        "ci_excludes_0": bool(lo > 0 or hi < 0)})
    return pd.DataFrame(rows)


def exp_f(runs, inc):
    """Does seriousness weighting help specifically against SERIOUS crime? Held-out exposure counting only
    robbery, or only robbery + physical assault, for each strategy (paired CI vs shortest and vs count_only)."""
    subsets = {"robbery_only": {"robbery"}, "robbery+assault": {"robbery", "physical assault"}}
    rows = []
    for scheme in sorted(runs.scheme.unique()):
        mw = C.mean_weight(scheme)
        for city in sorted(runs.city.unique()):
            rc = runs[(runs.city == city) & (runs.scheme == scheme)]
            short = shortest(rc)
            sc = RouteScorer(inc[inc.city == city])
            lookup = rc.drop_duplicates(KEY + ["route_idx"]).set_index(KEY + ["route_idx"]).polyline
            allr = rc.drop_duplicates(KEY + ["route_idx"])[KEY + ["route_idx", "distance_m"]]
            for base in C.STRATEGY_SCALES:
                sev_cfg = f"R{C.STRATEGY_ROUTING_RADIUS}_S{base:g}_CAT"
                cnt_cfg = f"R{C.STRATEGY_ROUTING_RADIUS}_S{round(base * mw, 2):g}_UNI"
                if _cfg(rc, scheme, sev_cfg).empty or _cfg(rc, scheme, cnt_cfg).empty:
                    continue
                picks = {"shortest": short.reset_index()[KEY + ["route_idx"]],
                         "count_only": top(_cfg(rc, scheme, cnt_cfg))[KEY + ["route_idx"]],
                         "severity_weighted": top(_cfg(rc, scheme, sev_cfg))[KEY + ["route_idx"]]}
                for sname, cats in subsets.items():
                    w = {c: (1.0 if c in cats else 0.0) for c in C.CATEGORIES}
                    er = C.DEFAULT_EVAL_RADIUS
                    ex_all = {(c, o, ri): sc.exposure(lookup[(c, o, ri)], er, "T2", w)[1]
                              for c, o, ri in zip(allr.city, allr.od_id, allr.route_idx)}
                    ev = {}
                    for name, p in picks.items():
                        p = p.set_index(KEY)
                        ev[name] = pd.Series([ex_all[(c, o, ri)] for (c, o), ri in zip(p.index, p.route_idx)],
                                             index=p.index, dtype=float)
                    t = allr.copy()
                    t["x"] = [ex_all[(c, o, ri)] for c, o, ri in zip(t.city, t.od_id, t.route_idx)]
                    ev["random_alternative"] = t.groupby(KEY).x.mean()
                    for name in ["shortest", "random_alternative", "count_only", "severity_weighted"]:
                        d = ev[name] - ev["shortest"].reindex(ev[name].index)
                        lo, hi = boot_ci(d)
                        row = {"city": city, "scheme": scheme, "scale_m_per_point": base, "subset": sname,
                               "strategy": name, "n_od": len(d), "mean_exposure": ev[name].mean(),
                               "diff_vs_shortest": d.mean(), "ci_lo": lo, "ci_hi": hi,
                               "pct_change_vs_shortest": 100 * d.mean() / max(ev["shortest"].mean(), 1e-9)}
                        if name == "severity_weighted":
                            dc = ev[name] - ev["count_only"].reindex(ev[name].index)
                            clo, chi = boot_ci(dc)
                            row.update({"diff_vs_count_only": dc.mean(), "vs_count_ci_lo": clo, "vs_count_ci_hi": chi})
                        rows.append(row)
    return pd.DataFrame(rows)


def main(cities):
    runs, inc = load(cities)
    C.RES_TABLES.mkdir(parents=True, exist_ok=True)
    contrasts = []
    out = {"table_A_radius": exp_a(runs, inc), "table_B_scale": exp_b(runs),
           "table_C_scheme": exp_c(runs), "table_D_strategies": exp_d(runs, inc, contrasts)}
    out["table_E_contrasts"] = pd.DataFrame(contrasts)
    out["table_F_serious"] = exp_f(runs, inc)
    for k, df in out.items():
        df.round(3).to_csv(C.RES_TABLES / f"{k}.csv", index=False)
        print(f"\n== {k} ==")
        print(df.round(2).to_string(index=False))
    print(f"\nOD pairs used: {runs.groupby('city').od_id.nunique().to_dict()}")


if __name__ == "__main__":
    main(sys.argv[1:])
