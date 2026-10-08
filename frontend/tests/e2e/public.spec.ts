import { expect, test } from "@playwright/test";

test.describe("home page", () => {
  test("renders the hero and a full-width feature section", async ({ page }) => {
    await page.goto("/");
    await expect(page.getByRole("heading", { level: 1 })).toContainText("confidence");
    await expect(page.getByRole("link", { name: "Explore the map" })).toBeVisible();

    // Regression: the feature cards once collapsed into narrow slivers.
    const section = page.locator("section", { has: page.getByRole("heading", { name: "Built for people on foot" }) });
    const sectionBox = await section.boundingBox();
    const viewport = page.viewportSize();
    expect(sectionBox?.width ?? 0).toBeGreaterThan((viewport?.width ?? 0) * 0.9);
    for (const title of ["Live safety map", "Safer routes", "Community powered", "Private by default"]) {
      const box = await page.getByRole("heading", { name: title }).boundingBox();
      expect(box?.width ?? 0).toBeGreaterThan(100);
    }
  });

  test("has a skip link that moves focus to the main content", async ({ page }) => {
    await page.goto("/");
    await page.keyboard.press("Tab");
    await expect(page.getByRole("link", { name: "Skip to content" })).toBeFocused();
    await page.keyboard.press("Enter");
    await expect(page.locator("#main")).toBeFocused();
  });

  test("navigates to the about page", async ({ page }) => {
    await page.goto("/");
    await page.getByRole("navigation", { name: "Main" }).getByRole("link", { name: "About" }).click();
    await expect(page).toHaveURL(/\/about$/);
    await expect(page.getByRole("heading", { name: "About SafeWalk" })).toBeVisible();
  });
});

test.describe("mobile layout", () => {
  test.use({ viewport: { width: 375, height: 812 } });

  test("collapses the navigation into a menu without horizontal scrolling", async ({ page }) => {
    await page.goto("/");
    await page.getByRole("button", { name: "Open menu" }).click();
    await expect(page.getByRole("link", { name: "Plan a route" }).last()).toBeVisible();
    await page.keyboard.press("Escape");
    const overflow = await page.evaluate(() => document.documentElement.scrollWidth - window.innerWidth);
    expect(overflow).toBeLessThanOrEqual(0);
  });
});

test.describe("routing guards", () => {
  for (const path of ["/dashboard", "/dashboard/reports", "/report", "/admin"]) {
    test(`${path} sends guests to login and keeps the destination`, async ({ page }) => {
      await page.goto(path);
      await expect(page).toHaveURL(new RegExp(`/login\\?redirect=${encodeURIComponent(path).replace(/%/g, "%")}`));
    });
  }

  test("the proxy refuses to expose the login endpoint", async ({ request }) => {
    const response = await request.post("/api/proxy/auth/login", { data: {} });
    expect(response.status()).toBe(404);
  });
});

test.describe("states that need no backend", () => {
  test("an invalid incident link shows not-found, not a spinner", async ({ page }) => {
    await page.goto("/incidents/abc");
    await expect(page.getByText("Incident not found")).toBeVisible();
  });

  test("unknown routes show the 404 page", async ({ page }) => {
    await page.goto("/definitely-not-a-page");
    await expect(page.getByText("Page not found")).toBeVisible();
  });

  test("the login form validates before submitting", async ({ page }) => {
    await page.goto("/login");
    await page.getByRole("button", { name: /^Sign In/ }).click();
    await expect(page.getByText("Email is required")).toBeVisible();
    await expect(page.getByText("Password is required")).toBeVisible();
  });

  test("security headers are set", async ({ request }) => {
    const response = await request.get("/");
    expect(response.headers()["x-content-type-options"]).toBe("nosniff");
    expect(response.headers()["x-frame-options"]).toBe("DENY");
  });
});
