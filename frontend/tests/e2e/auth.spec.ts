import { expect, test, type Page } from "@playwright/test";

const USER = { email: process.env.NEXT_PUBLIC_DEMO_USER_EMAIL, password: process.env.NEXT_PUBLIC_DEMO_USER_PASSWORD };
const ADMIN = { email: process.env.NEXT_PUBLIC_DEMO_ADMIN_EMAIL, password: process.env.NEXT_PUBLIC_DEMO_ADMIN_PASSWORD };

async function backendIsUp(baseURL: string | undefined, request: import("@playwright/test").APIRequestContext) {
  try {
    const response = await request.get(`${baseURL}/api/proxy/incident-categories/all`, { timeout: 8_000 });
    return response.ok();
  } catch {
    return false;
  }
}

async function login(page: Page, credentials: { email?: string; password?: string }) {
  await page.goto("/login");
  await page.getByLabel("Email address").fill(credentials.email ?? "");
  await page.getByLabel("Password", { exact: true }).fill(credentials.password ?? "");
  await page.getByRole("button", { name: /^Sign In/ }).click();
}

test.describe("with the backend running and demo accounts configured", () => {
  test.beforeEach(async ({ request, baseURL }) => {
    test.skip(!USER.email || !ADMIN.email, "Demo credentials are not in .env.local");
    test.skip(!(await backendIsUp(baseURL, request)), "SafeWalk backend is not reachable");
  });

  test("a user signs in, gets an httpOnly cookie and is kept out of /admin", async ({ page, context }) => {
    await login(page, USER);
    await expect(page).toHaveURL(/\/dashboard$/);
    await expect(page.getByRole("link", { name: "Report an Incident" })).toBeVisible();

    const cookie = (await context.cookies()).find((c) => c.name === "safewalk_token");
    expect(cookie?.httpOnly).toBe(true);
    expect(await page.evaluate(() => document.cookie)).not.toContain("safewalk_token");

    await page.goto("/admin");
    await expect(page).toHaveURL(/\/forbidden$/);
  });

  test("an admin lands on the admin overview", async ({ page }) => {
    await login(page, ADMIN);
    await expect(page).toHaveURL(/\/admin$/);
    await expect(page.getByRole("heading", { name: "Safety operations" })).toBeVisible();
    await expect(page.getByRole("navigation", { name: "Admin" })).toBeVisible();
  });

  test("a wrong password shows the server message and stays on login", async ({ page }) => {
    await login(page, { email: USER.email, password: "definitely-wrong-1" });
    await expect(page).toHaveURL(/\/login/);
    await expect(page.getByRole("alert").first()).toBeVisible();
  });

  test("logging out returns to a signed-out state", async ({ page }) => {
    await login(page, USER);
    await page.getByRole("button", { name: "Account menu" }).click();
    await page.getByRole("menuitem", { name: "Log out" }).click();
    await expect(page.getByRole("link", { name: "Create Account" })).toBeVisible();
    await page.goto("/dashboard");
    await expect(page).toHaveURL(/\/login/);
  });

  test("the map loads and shows incidents", async ({ page }) => {
    test.skip(!process.env.NEXT_PUBLIC_MAPBOX_TOKEN, "Mapbox token is not configured");
    await page.goto("/map");
    await expect(page.locator("canvas.mapboxgl-canvas")).toBeVisible({ timeout: 30_000 });
    await expect(page.getByText(/incidents in view/)).toBeVisible({ timeout: 30_000 });
  });

  test("the reports explorer lists reports as a table and switches to the map", async ({ page }) => {
    await page.goto("/reports");
    await expect(page.getByRole("columnheader", { name: "Report", exact: true })).toBeVisible({ timeout: 30_000 });
    await page.getByRole("button", { name: "Map" }).click();
    await expect(page.locator("canvas.mapboxgl-canvas")).toBeVisible({ timeout: 30_000 });
  });
});
