import { defineConfig, devices } from "@playwright/test";

// Demo credentials and the Mapbox token come from .env.local (same file the app uses).
try {
  process.loadEnvFile(".env.local");
} catch {
  // No .env.local: tests that need credentials or a backend skip themselves.
}

const PORT = 3000;

export default defineConfig({
  testDir: "./tests/e2e",
  // Generous: login (bcrypt) and cold pages can be slow on a dev machine.
  timeout: 90_000,
  expect: { timeout: 20_000 },
  fullyParallel: true,
  retries: process.env.CI ? 1 : 0,
  reporter: [["list"], ["html", { open: "never" }]],
  use: {
    baseURL: `http://localhost:${PORT}`,
    trace: "retain-on-failure",
    screenshot: "only-on-failure",
  },
  projects: [{ name: "chromium", use: { ...devices["Desktop Chrome"] } }],
  // Tests run against the production build: `pnpm build` first, then `pnpm e2e`.
  webServer: {
    command: "pnpm start",
    url: `http://localhost:${PORT}`,
    reuseExistingServer: true,
    timeout: 120_000,
  },
});
