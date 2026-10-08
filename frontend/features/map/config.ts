/** Default view: Nottingham, where the seeded reports are. Users can search or geolocate. */
export const DEFAULT_VIEW = { latitude: 52.9548, longitude: -1.1581, zoom: 12 } as const;

/** Above this viewport radius no incident data is requested (payload size guard, ~67k reports). */
export const MAX_HEATMAP_RADIUS_M = 30_000;
/** Individual clickable points (full DTOs) are only fetched at street level. */
export const MAX_POINTS_RADIUS_M = 3_000;
/** Category/time filters need full DTOs, so they only apply at neighbourhood level. */
export const MAX_FILTER_RADIUS_M = 5_000;

/** Fetched area is the viewport circle times this, so small pans do not refetch. */
export const AREA_PADDING = 1.4;
/** Refetch with a tighter area once the cached one is this much bigger than needed. */
export const AREA_MAX_OVERSIZE = 2.5;
export const MOVE_DEBOUNCE_MS = 400;
export const FALLBACK_MAX_SEVERITY = 10;
export const LIST_MAX_ITEMS = 50;
