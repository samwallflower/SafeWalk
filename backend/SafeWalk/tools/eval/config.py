"""Central configuration for the SafeWalk evaluation pipeline.

Everything you are expected to edit lives in this file.
Search for 'TODO' to find the values you MUST fill in.
"""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DATA_RAW = ROOT / "data" / "raw"
DATA_CACHE = ROOT / "data" / "cache" / "directions"
DATA_PROC = ROOT / "data" / "processed"
RES_TABLES = ROOT / "results" / "tables"
RES_FIGS = ROOT / "results" / "figures"
RES_LOGS = ROOT / "results" / "logs"

# --------------------------------------------------------------------------
# Cities. Two cities with clearly different crime levels.
# bbox = (min_lat, min_lng, max_lat, max_lng). Use the SAME physical size
# for both cities (e.g. ~6 km x 6 km) so densities are comparable.
# t1 = "routing" period (what the algorithm knows when it chooses a route)
# t2 = "evaluation" period (held-out future incidents used to judge routes)
# Format of periods: ("YYYY-MM", "YYYY-MM") inclusive, matching police.uk 'Month'.
# --------------------------------------------------------------------------
CITIES = {
    "cityA": {
        "label": "Nottingham",
        "raw_dir": DATA_RAW / "cityA",
        "bbox": (52.9278, -1.2028, 52.9818, -1.1134),
        "t1": ("2025-01", "2025-12"),      # TODO
        "t2": ("2026-01", "2026-06"),      # TODO
        "seed": 11,
        "n_target_pairs": 100,
        "od_min_m": 1000,
        "od_max_m": 3000,
    },
    "cityB": {
        "label": "Cambridge",
        "raw_dir": DATA_RAW / "cityB",
        "bbox": (52.1783, 0.0778, 52.2323, 0.1658),
        "t1": ("2025-01", "2025-12"),
        "t2": ("2026-01", "2026-06"),
        "seed": 22,
        "n_target_pairs": 100,
        "od_min_m": 1000,
        "od_max_m": 3000,
    },
    # dense, high-crime city (Greater Manchester returned no data from police.uk, so Birmingham / West Midlands)
    "cityC": {
        "label": "Birmingham",
        "raw_dir": DATA_RAW / "cityC",
        "bbox": (52.4527, -1.9469, 52.5067, -1.8583),
        "t1": ("2025-01", "2025-12"),
        "t2": ("2026-01", "2026-06"),
        "seed": 33,
        "n_target_pairs": 100,
        "od_min_m": 1000,
        "od_max_m": 3000,
    },
    # small, low-crime city
    "cityD": {
        "label": "York",
        "raw_dir": DATA_RAW / "cityD",
        "bbox": (53.9321, -1.1273, 53.9861, -1.0357),
        "t1": ("2025-01", "2025-12"),
        "t2": ("2026-01", "2026-06"),
        "seed": 44,
        "n_target_pairs": 100,
        "od_min_m": 1000,
        "od_max_m": 3000,
    },
}
BBOX_MARGIN_DEG = 0.03          # extra margin when auto-deriving a bbox from data

# Id of the dataset-import user in the SafeWalk DB (used only by the SQL export)
IMPORT_USER_ID = None           # TODO

# --------------------------------------------------------------------------
# Google Directions
# --------------------------------------------------------------------------
GOOGLE_DIRECTIONS_URL = "https://maps.googleapis.com/maps/api/directions/json"
GOOGLE_MODE = "walking"
MAX_API_CALLS_PER_CITY = 400    # hard safety cap per run
API_SLEEP_SECONDS = 0.1
# Which polyline the app scores. 'overview' = route.overview_polyline (what a
# simple implementation uses); 'steps' = concatenated per-step polylines (more
# detailed). MUST match what RoutingService decodes -> check and set!
POLYLINE_SOURCE = "overview"
MIN_ALTERNATIVES = 2            # OD pairs with fewer routes are excluded

# --------------------------------------------------------------------------
# Experiment grid
# --------------------------------------------------------------------------
RADII = [25, 50, 100, 150, 200, 300, 500, 1000]
# Real police data is dense, so 50 m/point swamps trip length (penalties of 100+ km on a
# 4 km walk). The sweep therefore includes small scales to find where distance matters again.
PENALTY_SCALES = [0.05, 0.1, 0.2, 0.5, 1, 2, 5, 10, 25, 50, 100]
DEFAULT_SCALE = 50
REFERENCE_RADIUS = 100          # radius other radii are compared against
STRATEGY_ROUTING_RADIUS = 100   # radius used by strategies when choosing
STRATEGY_SCALES = [0.2, 1, 5]    # penalty scales at which the three strategies are compared
EVAL_RADII = [50, 100]          # radii used to measure exposure on T2 incidents
DEFAULT_EVAL_RADIUS = 50
BOOTSTRAP_N = 2000
BOOTSTRAP_SEED = 12345

# --------------------------------------------------------------------------
# Categories & weights
# --------------------------------------------------------------------------
CATEGORIES = ["robbery", "physical assault", "harassment",
              "suspicious activity", "road accident", "vandalism"]

WEIGHT_SCHEMES = {
    "uniform": {c: 1.0 for c in CATEGORIES},
    # the weights currently seeded in SafeWalk (hand-chosen, NOT literature)
    "hand": {"robbery": 20, "physical assault": 18, "harassment": 15,
             "suspicious activity": 10, "road accident": 8, "vandalism": 6},
    # ONS Crime Severity Score weights (datatool.xls, "List of weights"), log-scaled
    # to 1-20 and ROUNDED to integers (IncidentCategory.severityWeight is an Integer
    # validated 1..20): w = 1 + 19*(ln x - ln 7.576)/(ln 993.55 - ln 7.576).
    # Raw ONS: robbery of personal property 993.55; physical assault = unweighted
    # mean of assault with injury (203.07) and without injury (12.99) = 108.03;
    # harassment 39.64; suspicious activity (ASB, no ONS weight) = public fear,
    # alarm or distress 7.58; criminal damage 9.16. Road accident has no ONS
    # weight and no police.uk rows, so the hand value is kept (unused).
    "literature": {"robbery": 20, "physical assault": 11, "harassment": 7,
                   "suspicious activity": 1, "road accident": 8, "vandalism": 2},
}

# --------------------------------------------------------------------------
# police.uk street-level CSV
# --------------------------------------------------------------------------
COL_MONTH, COL_LNG, COL_LAT, COL_TYPE = "Month", "Longitude", "Latitude", "Crime type"
CRIME_TYPE_TO_CATEGORY = {
    "robbery": "robbery",
    "violence and sexual offences": "physical assault",
    "violent crime": "physical assault",
    "possession of weapons": "physical assault",
    "public order": "harassment",
    "anti-social behaviour": "suspicious activity",
    "criminal damage and arson": "vandalism",
}
EXCLUDED_TYPES = {"bicycle theft", "burglary", "drugs", "other crime", "other theft",
                  "shoplifting", "theft from the person", "vehicle crime"}


# --------------------------------------------------------------------------
# Live-endpoint experiment (run_experiment.py)
# --------------------------------------------------------------------------
import os
API_BASE = os.environ.get("SAFEWALK_API_BASE", "http://localhost:8080/api/v1")
# Admin used only to change category weights between schemes. Defaults are the
# dev seed account created by DataInitializer; override via env if you changed it.
ADMIN_EMAIL = os.environ.get("SAFEWALK_ADMIN_EMAIL", "admin1@email.com")
ADMIN_PASSWORD = os.environ.get("SAFEWALK_ADMIN_PASSWORD", "123456")
RES_RAW = ROOT / "results" / "raw"
# schemes run against the endpoint ("hand" = seeded weights, "ons" = literature)
RUN_SCHEMES = {"hand": "hand", "ons": "literature"}
MAIN_SCHEME = "hand"
# categories that actually occur in the police.uk data (road accident has 0 rows);
# used for the mean weight that scales the count-only (UNIFORM) strategy
DATA_CATEGORIES = [c for c in CATEGORIES if c != "road accident"]


def mean_weight(scheme_key):
    w = WEIGHT_SCHEMES[RUN_SCHEMES[scheme_key]]
    return sum(w[c] for c in DATA_CATEGORIES) / len(DATA_CATEGORIES)


def available_schemes():
    return {k: v for k, v in WEIGHT_SCHEMES.items() if v is not None}


def get_city(name):
    return CITIES[name]


def label_for(name):
    return CITIES[name]["label"]
