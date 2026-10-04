"""Geometry helpers: haversine, Google polyline encode/decode, projection, midpoints."""
import math
import numpy as np

R_EARTH = 6371000.0


def haversine_m(lat1, lng1, lat2, lng2):
    p1, p2 = math.radians(lat1), math.radians(lat2)
    dphi = p2 - p1
    dl = math.radians(lng2 - lng1)
    a = math.sin(dphi / 2) ** 2 + math.cos(p1) * math.cos(p2) * math.sin(dl / 2) ** 2
    return 2 * R_EARTH * math.asin(math.sqrt(a))


def decode_polyline(s):
    """Decode a Google encoded polyline into [(lat, lng), ...]."""
    pts, i, lat, lng = [], 0, 0, 0
    while i < len(s):
        for k in (0, 1):
            shift = result = 0
            while True:
                b = ord(s[i]) - 63
                i += 1
                result |= (b & 0x1F) << shift
                shift += 5
                if b < 0x20:
                    break
            d = ~(result >> 1) if result & 1 else result >> 1
            if k == 0:
                lat += d
            else:
                lng += d
        pts.append((lat / 1e5, lng / 1e5))
    return pts


def encode_polyline(pts):
    out, plat, plng = [], 0, 0
    for lat, lng in pts:
        ilat, ilng = round(lat * 1e5), round(lng * 1e5)
        for v in (ilat - plat, ilng - plng):
            v = ~(v << 1) if v < 0 else v << 1
            while v >= 0x20:
                out.append(chr((0x20 | (v & 0x1F)) + 63))
                v >>= 5
            out.append(chr(v + 63))
        plat, plng = ilat, ilng
    return "".join(out)


def midpoints(pts):
    """Midpoints of consecutive points -- what RoutingService queries."""
    return [((a[0] + b[0]) / 2, (a[1] + b[1]) / 2) for a, b in zip(pts[:-1], pts[1:])]


def polyline_length_m(pts):
    return sum(haversine_m(a[0], a[1], b[0], b[1]) for a, b in zip(pts[:-1], pts[1:]))


class Projector:
    """Local equirectangular projection (metres) around a reference point.
    Error is < 0.1% at city scale, which is far below the data's own noise."""

    def __init__(self, lat0, lng0):
        self.lat0, self.lng0 = lat0, lng0
        self.kx = math.radians(1) * R_EARTH * math.cos(math.radians(lat0))
        self.ky = math.radians(1) * R_EARTH

    def xy(self, lat, lng):
        lat = np.asarray(lat, dtype=float)
        lng = np.asarray(lng, dtype=float)
        return np.column_stack(((lng - self.lng0) * self.kx, (lat - self.lat0) * self.ky))
