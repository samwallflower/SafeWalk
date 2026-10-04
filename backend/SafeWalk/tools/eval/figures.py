"""Step 6: figures from results/tables/*.csv  -> results/figures/*.png and *.pdf

Usage: python figures.py [scheme]      (scheme = hand | ons; default hand)
"""
import sys
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import config as C

plt.rcParams.update({"figure.dpi": 130, "font.size": 10, "axes.spines.top": False,
                     "axes.spines.right": False, "axes.grid": True, "grid.alpha": .25})
COL = {}
PREFIX = ""


def _color(i):
    return ["#0969da", "#cf222e", "#1a7f37", "#9a6700"][i % 4]


PRETTY = {"shortest": "Shortest route", "random_alternative": "Random alternative",
          "count_only": "Count-only", "severity_weighted": "Severity-weighted (SafeWalk)"}
STRAT_COLOR = {"shortest": "#0969da", "random_alternative": "#cf222e",
               "count_only": "#1a7f37", "severity_weighted": "#9a6700"}
MAIN_SCALE = 1   # penalty scale used for the main strategy figures (see THESIS_SUMMARY section 1)


def _save(fig, name, rect=None):
    C.RES_FIGS.mkdir(parents=True, exist_ok=True)
    fig.tight_layout(rect=rect)
    fig.savefig(C.RES_FIGS / f"{PREFIX}{name}.png")
    fig.savefig(C.RES_FIGS / f"{PREFIX}{name}.pdf")
    plt.close(fig)
    print("wrote", C.RES_FIGS / f"{PREFIX}{name}.png")


def _lab(city):
    try:
        return C.label_for(city)
    except KeyError:
        return city


def line_by_city(df, ycol, ylabel, name, title, ref=None):
    fig, ax = plt.subplots(figsize=(6, 3.8))
    for i, (city, g) in enumerate(df.groupby("city")):
        ax.plot(g.radius_m, g[ycol], marker="o", color=_color(i), label=_lab(city))
    if ref is not None:
        ax.axvline(ref, ls="--", color="grey", lw=1)
    ax.set_xscale("log")
    ax.set_xticks(C.RADII)
    ax.set_xticklabels([str(r) for r in C.RADII])
    ax.set_xlabel("Buffer radius (m, log scale)")
    ax.set_ylabel(ylabel)
    ax.set_title(title)
    ax.legend()
    _save(fig, name)


def main(scheme=C.MAIN_SCHEME):
    global PREFIX
    PREFIX = f"{scheme}_"
    A = pd.read_csv(C.RES_TABLES / "table_A_radius.csv")
    B = pd.read_csv(C.RES_TABLES / "table_B_scale.csv")
    D = pd.read_csv(C.RES_TABLES / "table_D_strategies.csv")
    A, B, D = (t[t.scheme == scheme] for t in (A, B, D))

    line_by_city(A, "mean_incidents_per_route", "Mean incidents captured per route",
                 "fig1_captured_vs_radius", "Incidents captured vs buffer radius")
    line_by_city(A, "saturation_pct", "Unique incidents captured (% of city total)",
                 "fig2_saturation_vs_radius", "Saturation of the incident set")
    A3 = A.assign(spread_km=A.penalty_spread_m_mean / 1000)
    line_by_city(A3, "spread_km", f"Mean penalty gap between alternatives (km, at {C.DEFAULT_SCALE} m/point)",
                 "fig3_penalty_spread_vs_radius", "Penalty gap between a trip's routes vs buffer radius")
    line_by_city(A, "top_ne_shortest_pct", "Top route differs from shortest (% of OD pairs)",
                 "fig4_diverted_vs_radius", "Share of trips re-routed vs buffer radius")
    line_by_city(A, "top1_agree_with_ref_pct",
                 f"Same top route as {C.REFERENCE_RADIUS} m (% of OD pairs)",
                 "fig5_rank_stability_vs_radius", "Rank stability", ref=C.REFERENCE_RADIUS)

    # scale sensitivity
    fig, ax = plt.subplots(figsize=(6, 3.8))
    for i, (city, g) in enumerate(B.groupby("city")):
        ax.plot(g.scale_m_per_point, g.top_ne_shortest_pct, marker="o", color=_color(i), label=_lab(city))
    ax.set_xscale("log")
    ax.axvspan(1, 5, color="#1a7f37", alpha=0.10, lw=0)
    ax.text(2.2, ax.get_ylim()[0] + 1.5, "useful range", ha="center", fontsize=8, color="#1a7f37")
    ax.axvline(C.DEFAULT_SCALE, ls="--", color="grey", lw=1)
    ax.text(C.DEFAULT_SCALE * 1.15, ax.get_ylim()[0] + 1.5, "current default", fontsize=8, color="grey")
    ax.set_xlabel("Penalty scale (metres of virtual distance per severity point, log scale)")
    ax.set_ylabel("Top route differs from shortest (%)")
    ax.set_title(f"Sensitivity to penalty scale ({C.REFERENCE_RADIUS} m buffer)")
    ax.legend(loc="center right")
    _save(fig, "fig6_scale_sensitivity")

    # strategy comparison on held-out data
    for sc_, er in [(x, y) for x in C.STRATEGY_SCALES for y in C.EVAL_RADII]:
        d = D[(D.eval_period == "T2") & (D.eval_radius_m == er) & (D.scale_m_per_point == sc_)]
        cities = sorted(d.city.unique())
        strategies = ["shortest", "random_alternative", "count_only", "severity_weighted"]
        fig, axes = plt.subplots(1, 2, figsize=(10, 4.2))
        for ax, (col, ttl, yl) in zip(axes, [
                ("mean_exposure_severity", "Exposure to held-out incidents", f"Severity points within {er} m of route"),
                ("mean_detour_pct", "Extra walking", "Detour vs shortest route (%)")]):
            w = 0.2
            for j, st in enumerate(strategies):
                vals = [d[(d.city == c) & (d.strategy == st)][col].iloc[0] for c in cities]
                ax.bar(np.arange(len(cities)) + (j - 1.5) * w, vals, w, label=PRETTY[st], color=STRAT_COLOR[st])
            ax.set_xticks(range(len(cities)))
            ax.set_xticklabels([_lab(c) for c in cities])
            ax.set_ylabel(yl)
            ax.set_title(ttl, fontsize=10)
        h, l = axes[0].get_legend_handles_labels()
        fig.legend(h, l, loc="lower center", ncol=4, frameon=False)
        fig.suptitle(f"Strategies on held-out 2026 incidents (penalty {sc_:g} m/point, {C.REFERENCE_RADIUS} m buffer)",
                     fontsize=10)
        _save(fig, f"fig7_strategies_T2_r{er}_s{sc_:g}", rect=[0, 0.07, 1, 0.95])

    # headline figure: % change in severity exposure vs the shortest route, with 95% bootstrap CI
    d = D[(D.eval_period == "T2") & (D.eval_radius_m == C.DEFAULT_EVAL_RADIUS) & (D.scale_m_per_point == MAIN_SCALE)]
    base = d[d.strategy == "shortest"].set_index("city").mean_exposure_severity
    cities = sorted(d.city.unique())
    order = ["severity_weighted", "count_only", "random_alternative"]
    fig, ax = plt.subplots(figsize=(7.5, 0.5 * len(cities) * len(order) + 1.6))
    y, ticks, labels = 0, [], []
    for c in cities:
        for st in order:
            r = d[(d.city == c) & (d.strategy == st)].iloc[0]
            b0 = base[c]
            ax.errorbar(100 * r.diff_severity_vs_shortest / b0, y,
                        xerr=[[100 * (r.diff_severity_vs_shortest - r.diff_severity_ci_lo) / b0],
                              [100 * (r.diff_severity_ci_hi - r.diff_severity_vs_shortest) / b0]],
                        fmt="o", color=STRAT_COLOR[st], capsize=3, label=PRETTY[st] if c == cities[0] else None)
            ticks.append(y)
            labels.append(f"{_lab(c)}: {PRETTY[st]}")
            y += 1
        y += 0.7
    ax.axvline(0, color="black", lw=1)
    ax.set_yticks(ticks)
    ax.set_yticklabels(labels, fontsize=8)
    ax.invert_yaxis()
    ax.set_xlabel("Change in exposure to held-out incidents vs shortest route (%)\n"
                  "95% bootstrap CI; below 0 = safer than shortest route")
    ax.set_title(f"Effect of each strategy ({MAIN_SCALE:g} m/point, {C.REFERENCE_RADIUS} m buffer)", fontsize=10)
    _save(fig, "fig8_paired_difference")

    # paired difference with bootstrap CI (what the thesis should actually claim)
    d = D[(D.eval_period == "T2") & (D.eval_radius_m == C.DEFAULT_EVAL_RADIUS) & (D.strategy != "shortest")]
    fig, ax = plt.subplots(figsize=(7.5, 0.35 * max(len(d), 4) + 1.5))
    labels, y = [], 0
    for i, (c, g) in enumerate(d.groupby("city")):
        for _, r in g.iterrows():
            ax.errorbar(r.diff_severity_vs_shortest, y,
                        xerr=[[r.diff_severity_vs_shortest - r.diff_severity_ci_lo],
                              [r.diff_severity_ci_hi - r.diff_severity_vs_shortest]],
                        fmt="o", color=_color(i), capsize=3)
            labels.append(f"{_lab(c)} - {r.strategy} (scale {r.scale_m_per_point:g})")
            y += 1
    ax.axvline(0, color="black", lw=1)
    ax.set_yticks(range(len(labels)))
    ax.set_yticklabels(labels, fontsize=8)
    ax.set_xlabel("Change in severity exposure vs shortest\n(95% bootstrap CI; <0 = safer than shortest)")
    ax.set_title("Paired effect on held-out incidents")
    _save(fig, "fig8b_paired_difference_all_scales")



if __name__ == "__main__":
    main(*sys.argv[1:2])
