"""Step 1: raw police.uk CSVs -> cleaned incidents with category + period (T1/T2).

Usage:  python prepare_data.py cityA
Output: data/processed/<city>_incidents.csv
        data/processed/<city>_incidents_import.sql   (optional DB import, see guide)
"""
import sys
import pandas as pd
import config as C


def load_raw(raw_dir):
    files = sorted(raw_dir.rglob("*.csv"))
    if not files:
        sys.exit(f"No CSV files under {raw_dir}")
    frames = [pd.read_csv(f, usecols=lambda c: c in {C.COL_MONTH, C.COL_LNG, C.COL_LAT, C.COL_TYPE})
              for f in files]
    return pd.concat(frames, ignore_index=True)


def prepare(city):
    cfg = C.get_city(city)
    df = load_raw(cfg["raw_dir"])
    n0 = len(df)
    df = df.dropna(subset=[C.COL_LNG, C.COL_LAT, C.COL_TYPE, C.COL_MONTH]).copy()
    df["ctype"] = df[C.COL_TYPE].str.strip().str.lower()
    unknown = set(df["ctype"]) - set(C.CRIME_TYPE_TO_CATEGORY) - C.EXCLUDED_TYPES
    if unknown:
        print("WARNING unmapped crime types (dropped):", sorted(unknown))
    df["category"] = df["ctype"].map(C.CRIME_TYPE_TO_CATEGORY)
    df = df.dropna(subset=["category"])
    df = df.rename(columns={C.COL_MONTH: "month", C.COL_LNG: "lng", C.COL_LAT: "lat"})

    bbox = cfg["bbox"]
    if bbox is None:
        lo, hi = df[["lat", "lng"]].quantile(0.02), df[["lat", "lng"]].quantile(0.98)
        m = C.BBOX_MARGIN_DEG
        bbox = (lo.lat - m, lo.lng - m, hi.lat + m, hi.lng + m)
        print(f"bbox not set in config -> derived {tuple(round(x, 4) for x in bbox)} (set it explicitly!)")
    df = df[(df.lat >= bbox[0]) & (df.lat <= bbox[2]) & (df.lng >= bbox[1]) & (df.lng <= bbox[3])]

    def period(m):
        if cfg["t1"][0] <= m <= cfg["t1"][1]:
            return "T1"
        if cfg["t2"][0] <= m <= cfg["t2"][1]:
            return "T2"
        return None
    df["period"] = df["month"].map(period)
    df = df.dropna(subset=["period"]).reset_index(drop=True)
    df.insert(0, "incident_id", range(1, len(df) + 1))
    out = C.DATA_PROC / f"{city}_incidents.csv"
    C.DATA_PROC.mkdir(parents=True, exist_ok=True)
    df[["incident_id", "month", "period", "category", "lat", "lng"]].to_csv(out, index=False)
    print(f"{city}: raw rows {n0} -> kept {len(df)}  (T1={sum(df.period=='T1')}, T2={sum(df.period=='T2')})")
    print(df.groupby(["period", "category"]).size().unstack(0).fillna(0).astype(int))
    print("wrote", out)
    return df


if __name__ == "__main__":
    prepare(sys.argv[1])
