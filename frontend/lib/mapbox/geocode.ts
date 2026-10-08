import { z } from "zod";

import { env } from "@/lib/env";

const responseSchema = z.object({
  features: z.array(
    z.object({
      properties: z.object({
        name: z.string(),
        place_formatted: z.string().optional(),
      }),
      geometry: z.object({ coordinates: z.tuple([z.number(), z.number()]) }),
    }),
  ),
});

export interface GeocodeResult {
  id: string;
  label: string;
  latitude: number;
  longitude: number;
}

/** Mapbox Geocoding v6 forward search. Coordinates come back as [lng, lat]. */
export async function searchPlaces(
  query: string,
  signal?: AbortSignal,
  proximity?: { latitude: number; longitude: number },
): Promise<GeocodeResult[]> {
  const params = new URLSearchParams({
    q: query,
    limit: "5",
    autocomplete: "true",
    access_token: env.mapboxToken,
  });
  // Bias (not restrict) results toward where the user is looking.
  if (proximity) params.set("proximity", `${proximity.longitude},${proximity.latitude}`);
  const response = await fetch(`https://api.mapbox.com/search/geocode/v6/forward?${params}`, { signal });
  if (!response.ok) throw new Error("Place search failed");
  const parsed = responseSchema.parse(await response.json());
  return parsed.features.map((f, index) => {
    const [longitude, latitude] = f.geometry.coordinates;
    const label = [f.properties.name, f.properties.place_formatted].filter(Boolean).join(", ");
    return { id: `${index}-${longitude}-${latitude}`, label, latitude, longitude };
  });
}

const reverseSchema = z.object({
  features: z.array(
    z.object({
      properties: z.object({
        name: z.string(),
        place_formatted: z.string().optional(),
      }),
    }),
  ),
});

/** Human-readable address for a coordinate (Mapbox Geocoding v6 reverse). */
export async function reversePlace(
  latitude: number,
  longitude: number,
  signal?: AbortSignal,
  types?: string,
): Promise<string | null> {
  const params = new URLSearchParams({
    latitude: String(latitude),
    longitude: String(longitude),
    limit: "1",
    access_token: env.mapboxToken,
  });
  if (types) params.set("types", types);
  const response = await fetch(`https://api.mapbox.com/search/geocode/v6/reverse?${params}`, { signal });
  if (!response.ok) throw new Error("Reverse geocoding failed");
  const feature = reverseSchema.parse(await response.json()).features[0];
  if (!feature) return null;
  return [feature.properties.name, feature.properties.place_formatted].filter(Boolean).join(", ");
}

/** Street (or nearest address) name only, e.g. "Upper Parliament Street". Null when nothing is found. */
export async function reverseStreet(latitude: number, longitude: number, signal?: AbortSignal): Promise<string | null> {
  const street = await reversePlace(latitude, longitude, signal, "street");
  if (street) return street.split(",")[0];
  const address = await reversePlace(latitude, longitude, signal, "address");
  return address ? address.split(",")[0] : null;
}
