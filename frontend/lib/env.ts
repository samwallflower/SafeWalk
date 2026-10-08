/** Static `process.env.NEXT_PUBLIC_*` access is required so Next inlines the values. */
export const env = {
  mapboxToken: process.env.NEXT_PUBLIC_MAPBOX_TOKEN ?? "",
  demoLoginEnabled: process.env.NEXT_PUBLIC_DEMO_LOGIN_ENABLED === "true",
  demoUser: {
    email: process.env.NEXT_PUBLIC_DEMO_USER_EMAIL ?? "",
    password: process.env.NEXT_PUBLIC_DEMO_USER_PASSWORD ?? "",
  },
  demoAdmin: {
    email: process.env.NEXT_PUBLIC_DEMO_ADMIN_EMAIL ?? "",
    password: process.env.NEXT_PUBLIC_DEMO_ADMIN_PASSWORD ?? "",
  },
} as const;
