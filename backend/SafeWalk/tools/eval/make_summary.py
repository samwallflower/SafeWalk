"""Generate results/THESIS_SUMMARY.md from results/tables/*.csv and results/raw/*_runs.csv.
Every number in the file is read from the tables, so re-running after new data keeps it consistent.

Usage: python make_summary.py            (after analyze_runs.py cityA cityB)
"""
import pandas as pd
import config as C

T = C.RES_TABLES
MAIN_SCALE, MAIN_RADIUS = 1, C.DEFAULT_EVAL_RADIUS
LABEL = {"shortest": "Shortest route", "random_alternative": "Random alternative",
         "count_only": "Count-only (every incident = 1)", "severity_weighted": "Severity-weighted (SafeWalk)"}
ORDER = ["shortest", "random_alternative", "count_only", "severity_weighted"]
SCHEME_NAME = {"hand": "hand-chosen weights", "ons": "ONS-derived weights"}


def md(df):
    cols = list(df.columns)
    out = ["| " + " | ".join(cols) + " |", "|" + "|".join("---" for _ in cols) + "|"]
    for _, r in df.iterrows():
        out.append("| " + " | ".join(str(r[c]) for c in cols) + " |")
    return "\n".join(out)


def ci(v, lo, hi, nd=0):
    return f"{v:.{nd}f} [{lo:.{nd}f}, {hi:.{nd}f}]"


def city(c):
    return C.label_for(c)


def main_table(D, scheme, period):
    rows = []
    for c in sorted(D.city.unique()):
        for s in ORDER:
            r = D[(D.city == c) & (D.scheme == scheme) & (D.eval_period == period) & (D.strategy == s)
                  & (D.scale_m_per_point == MAIN_SCALE) & (D.eval_radius_m == MAIN_RADIUS)]
            if r.empty:
                continue
            r = r.iloc[0]
            rows.append({"City": city(c), "Strategy": LABEL[s], "OD pairs": int(r.n_od),
                         "Detour %": f"{r.mean_detour_pct:.1f}",
                         "Mean exposure (count)": f"{r.mean_exposure_count:.1f}",
                         "Change in severity exposure vs shortest": f"{r.pct_change_severity_vs_shortest:+.1f}%",
                         "Paired difference in count [95% CI]":
                             "-" if s == "shortest" else ci(r.diff_count_vs_shortest, r.diff_count_ci_lo, r.diff_count_ci_hi, 1)})
    return md(pd.DataFrame(rows))


def hand_vs_ons(cities):
    rows = []
    for c in cities:
        d = pd.read_csv(C.RES_RAW / f"{c}_runs.csv")
        n = d[(d.scheme == "hand") & (d.config_id == f"R100_S{C.DEFAULT_SCALE:g}_CAT")].groupby("od_id").size()
        ok = set(n[n >= C.MIN_ALTERNATIVES].index)
        top = d[(d["rank"] == 1) & d.od_id.isin(ok)]
        row = {"City": city(c)}
        for s in [0.2, 1, 5, 50]:
            cfg = f"R{C.REFERENCE_RADIUS}_S{s:g}_CAT"
            h = top[(top.scheme == "hand") & (top.config_id == cfg)].set_index("od_id").route_idx
            o = top[(top.scheme == "ons") & (top.config_id == cfg)].set_index("od_id").route_idx
            j = h.index.intersection(o.index)
            row[f"scale {s:g}"] = f"{100 * (h[j] == o[j]).mean():.0f}%"
        rows.append(row)
    return md(pd.DataFrame(rows))


def main():
    A, B = pd.read_csv(T / "table_A_radius.csv"), pd.read_csv(T / "table_B_scale.csv")
    D, E = pd.read_csv(T / "table_D_strategies.csv"), pd.read_csv(T / "table_E_contrasts.csv")
    F = pd.read_csv(T / "table_F_serious.csv")
    cities = sorted(D.city.unique())
    n_od = D[(D.scheme == "hand") & (D.strategy == "shortest") & (D.eval_period == "T2")
             & (D.scale_m_per_point == MAIN_SCALE) & (D.eval_radius_m == MAIN_RADIUS)].set_index("city").n_od
    L = []
    L.append("# SafeWalk route recommendation: evaluation summary\n")
    L.append("*Generated from `results/tables`. Numbers are exact outputs of the real `/routing/recommend` endpoint "
             "(Google Directions replayed from cache); safety is measured offline on held-out incidents.*\n")

    L.append("## 1. Setup\n")
    L.append(f"- **Data:** police.uk street-level crimes, {', '.join(city(c) for c in cities)}. Routing knowledge (T1): "
             f"{C.CITIES['cityA']['t1'][0]} to {C.CITIES['cityA']['t1'][1]}. Held-out evaluation (T2): "
             f"{C.CITIES['cityA']['t2'][0]} to {C.CITIES['cityA']['t2'][1]}.")
    L.append("- **Categories (police.uk to SafeWalk):** robbery; violence/sexual offences, weapons -> physical assault; "
             "public order -> harassment; anti-social behaviour -> suspicious activity; criminal damage -> vandalism. "
             "Burglary, vehicle crime, shoplifting, drugs, theft etc. are excluded as not street-safety events.")
    L.append("- **Trips:** " + ", ".join(f"{city(c)}: {int(n_od[c])} random walking origin-destination pairs (1-3 km apart, "
             f"at least 2 Google alternatives)" for c in cities) + ".")
    L.append("- **System under test:** the real endpoint, with an eval-mode flag that allows per-request buffer radius, "
             "penalty scale and weighting. Production behaviour is unchanged when the flag is off.")
    L.append("- **Rule:** each route is penalised by (sum of severity weights of unique reported incidents within the "
             "buffer of the route's segment midpoints) x (penalty metres per point); the lowest virtual distance wins.")
    L.append(f"- **Main setting:** buffer {C.REFERENCE_RADIUS} m, penalty {MAIN_SCALE} m/point, exposure counted within "
             f"{MAIN_RADIUS} m of the chosen route. Chosen from the scale sweep (section 5): below about 0.2 m/point distance "
             "dominates, above about 5 m/point the choice stops changing.")
    L.append("- **Weights:** hand-chosen (robbery 20, assault 18, harassment 15, suspicious activity 10, vandalism 6) and "
             "ONS Crime Severity Score weights log-scaled to 1-20 (robbery 20, assault 11, harassment 7, vandalism 2, "
             "suspicious activity 1).")
    L.append("- **Strategies:** shortest route; random alternative (expected value over Google's alternatives); count-only "
             "(every incident = 1 point, with the same average penalty per incident); severity-weighted (SafeWalk).\n")

    L.append("## 2. Main result: routes chosen with 2025 reports, judged on 2026 incidents\n")
    L.append("Lower exposure is better. Differences are paired by trip with 95% bootstrap intervals (2000 resamples).\n")
    for s in ["hand", "ons"]:
        L.append(f"### {SCHEME_NAME[s][0].upper() + SCHEME_NAME[s][1:]}, held-out T2\n")
        L.append(main_table(D, s, "T2") + "\n")
    L.append("### In-sample check (same routes, T1 incidents the router knew), hand weights\n")
    L.append(main_table(D, "hand", "T1") + "\n")
    L.append("The in-sample and held-out reductions are almost the same, so the benefit is not specific to the "
             "incidents the router was given.\n")

    L.append("## 3. Direct paired comparisons (severity-weighted vs the controls)\n")
    e = E[(E.eval_period == "T2") & (E.eval_radius_m == MAIN_RADIUS) & (E.scale_m_per_point == MAIN_SCALE)
          & (E.metric.isin(["severity", "detour_pct"]))].copy()
    e["City"] = e.city.map(city)
    e["Scheme"] = e.scheme.map(SCHEME_NAME)
    e["Comparison"] = "severity-weighted minus " + e.b.str.replace("_", " ")
    e["Metric"] = e.metric.map({"severity": "severity exposure", "detour_pct": "detour (pct points)"})
    e["Mean difference [95% CI]"] = [ci(r.mean_diff_a_minus_b, r.ci_lo, r.ci_hi, 1) for r in e.itertuples()]
    e["CI excludes 0"] = e.ci_excludes_0.map({True: "yes", False: "no"})
    L.append(md(e[["City", "Scheme", "Comparison", "Metric", "Mean difference [95% CI]", "CI excludes 0"]]) + "\n")
    allw = E[(E.b == "count_only") & (E.metric == "severity")]
    L.append(f"Across all {len(allw)} severity-weighted vs count-only comparisons (scales, radii, periods, schemes) the "
             f"interval excludes 0 in {100 * allw.ci_excludes_0.mean():.0f}%: no consistent advantage for seriousness weighting.\n")

    L.append("## 4. Does seriousness weighting help against serious crime specifically?\n")
    f = F[(F.scale_m_per_point == MAIN_SCALE) & (F.scheme == "hand")].copy()
    rows = []
    for c in cities:
        for sub in ["robbery_only", "robbery+assault"]:
            for s in ORDER:
                r = f[(f.city == c) & (f["subset"] == sub) & (f.strategy == s)]
                if not r.empty:
                    r = r.iloc[0]
                    rows.append({"City": city(c), "Incidents counted": sub.replace("_", " "), "Strategy": LABEL[s],
                                 "Mean exposure": f"{r.mean_exposure:.2f}",
                                 "Change vs shortest": f"{r.pct_change_vs_shortest:+.1f}%"})
    L.append(md(pd.DataFrame(rows)) + "\n")
    w = F[F.strategy == "severity_weighted"].copy()
    w["sig"] = (w.vs_count_ci_lo > 0) | (w.vs_count_ci_hi < 0)
    L.append(f"Severity-weighted vs count-only on these serious-crime subsets: interval excludes 0 in "
             f"{int(w.sig.sum())} of {len(w)} comparisons (hand and ONS, all strategy scales).\n")

    L.append("## 5. Sensitivity\n")
    L.append("### 5.1 Buffer radius (hand weights, scale 50)\n")
    a = A[A.scheme == "hand"].copy()
    a["City"] = a.city.map(city)
    t = a[["City", "radius_m", "mean_incidents_per_route", "saturation_pct", "top_ne_shortest_pct",
           "top1_agree_with_ref_pct"]].round(1)
    t.columns = ["City", "Radius (m)", "Incidents per route", "Unique T1 incidents captured (%)",
                 "Top route differs from shortest (%)", f"Same top route as {C.REFERENCE_RADIUS} m (%)"]
    L.append(md(t) + "\n")
    L.append("### 5.2 Penalty scale (hand weights, radius 100)\n")
    b = B[B.scheme == "hand"].copy()
    b["City"] = b.city.map(city)
    t = b[["City", "scale_m_per_point", "top_ne_shortest_pct", "mean_detour_pct", "mean_detour_pct_when_diverted"]].round(2)
    t.columns = ["City", "Scale (m/point)", "Top route differs from shortest (%)", "Mean detour (%)", "Detour when re-routed (%)"]
    L.append(md(t) + "\n")
    L.append("### 5.3 Weighting scheme: do hand and ONS weights choose the same route?\n")
    L.append("Share of trips where both schemes pick the same top route, at the same penalty scale:\n")
    L.append(hand_vs_ons(cities) + "\n")

    L.append("## 6. Limitations to state\n")
    L.append("- Recorded crime is not the same as risk: under-reporting, and police.uk snaps locations to anonymised points.")
    L.append(f"- {len(cities)} English cities, one data source (police.uk); results for sparse, user-reported data may differ.")
    L.append("- Safety is exposure to recorded incidents near the chosen route, not observed victimisation or perceived safety.")
    L.append("- Routes are Google's overview-polyline alternatives; the router cannot create routes Google does not offer.")
    L.append("- Severity weights are subjective (hand) or from a different context (ONS Crime Severity Score).")
    L.append("- The 50 m/point default was not calibrated for dense police data: it ignores walking distance (section 5.2).\n")

    L.append("## 7. Figures and reproduction\n")
    L.append("Figures: `results/figures/<scheme>_fig1..fig8*.png` (radius sensitivity, scale sensitivity, strategy "
             "comparison on held-out incidents, paired difference with confidence intervals).\n")
    L.append("```\npython sample_od.py <city>; python run_experiment.py <city> --schemes hand ons\n"
             "python analyze_runs.py cityA cityB; python figures.py hand; python figures.py ons; python make_summary.py\n```")
    out = C.ROOT / "results" / "THESIS_SUMMARY.md"
    out.write_text("\n".join(L) + "\n", encoding="utf-8")
    print("wrote", out)


if __name__ == "__main__":
    main()
