import type { LayerProps } from "react-map-gl/mapbox";

export const MARKER_COLOR = "#c4161c";

export const HEAT_LAYER_ID = "incident-density";
export const HALO_LAYER_ID = "incident-halo";
export const DOT_LAYER_ID = "incident-dot";
export const HIT_LAYER_ID = "incident-hit";
export const SELECTED_HALO_LAYER_ID = "incident-selected-halo";
export const SELECTED_DOT_LAYER_ID = "incident-selected-dot";

/** Dots take over from the density wash at this zoom. */
const DOT_MIN_ZOOM = 12.5;

/** Single-hue density wash for zoomed-out views, so thousands of reports do not become a red blob. */
export const densityLayer: LayerProps = {
  id: HEAT_LAYER_ID,
  type: "heatmap",
  maxzoom: 15,
  paint: {
    "heatmap-weight": 0.6,
    "heatmap-intensity": ["interpolate", ["linear"], ["zoom"], 9, 0.08, 12, 0.25, 15, 0.6],
    "heatmap-radius": ["interpolate", ["linear"], ["zoom"], 9, 4, 12, 14, 15, 26],
    "heatmap-color": [
      "interpolate",
      ["linear"],
      ["heatmap-density"],
      0,
      "rgba(196,22,28,0)",
      0.25,
      "rgba(196,22,28,0.10)",
      0.6,
      "rgba(196,22,28,0.28)",
      1,
      "rgba(196,22,28,0.55)",
    ],
    "heatmap-opacity": ["interpolate", ["linear"], ["zoom"], 12, 1, 14.5, 0],
  },
};

/** Dots (halo + red dot + hit area) that appear from `minZoom`. The white border grows in as dots get big. */
export function haloLayerAt(minZoom: number): LayerProps {
  return {
    id: HALO_LAYER_ID,
    type: "circle",
    minzoom: minZoom,
    paint: {
      "circle-radius": ["interpolate", ["linear"], ["zoom"], minZoom, 5, 15, 12, 18, 20],
      "circle-color": MARKER_COLOR,
      "circle-opacity": ["interpolate", ["linear"], ["zoom"], minZoom, 0.12, 15, 0.16],
    },
  };
}

export function dotLayerAt(minZoom: number): LayerProps {
  return {
    id: DOT_LAYER_ID,
    type: "circle",
    minzoom: minZoom,
    paint: {
      "circle-radius": ["interpolate", ["linear"], ["zoom"], minZoom, 2.5, 15, 4.5, 18, 8],
      "circle-color": MARKER_COLOR,
      "circle-stroke-color": "#ffffff",
      "circle-stroke-width": ["interpolate", ["linear"], ["zoom"], minZoom, 0.5, 14, 0.8, 16, 1.8],
    },
  };
}

/** Invisible, larger hit area so small dots are easy to hover and click. */
export function hitLayerAt(minZoom: number): LayerProps {
  return {
    id: HIT_LAYER_ID,
    type: "circle",
    minzoom: minZoom,
    paint: { "circle-radius": 12, "circle-color": MARKER_COLOR, "circle-opacity": 0 },
  };
}

export const DEFAULT_DOT_MIN_ZOOM = DOT_MIN_ZOOM;
export const haloLayer: LayerProps = haloLayerAt(DOT_MIN_ZOOM);
export const dotLayer: LayerProps = dotLayerAt(DOT_MIN_ZOOM);
export const hitLayer: LayerProps = hitLayerAt(DOT_MIN_ZOOM);

export function selectedHaloLayer(selectedId: number | null): LayerProps {
  return {
    id: SELECTED_HALO_LAYER_ID,
    type: "circle",
    filter: ["==", ["get", "id"], selectedId ?? -1],
    paint: { "circle-radius": 22, "circle-color": MARKER_COLOR, "circle-opacity": 0.25 },
  };
}

export function selectedDotLayer(selectedId: number | null): LayerProps {
  return {
    id: SELECTED_DOT_LAYER_ID,
    type: "circle",
    filter: ["==", ["get", "id"], selectedId ?? -1],
    paint: {
      "circle-radius": 9,
      "circle-color": MARKER_COLOR,
      "circle-stroke-color": "#ffffff",
      "circle-stroke-width": 3,
    },
  };
}
