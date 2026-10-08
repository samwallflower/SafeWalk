// Runs Lighthouse (mobile emulation) against a running production server and prints a score table.
// Usage: pnpm build && pnpm start   (in one terminal)   then   pnpm lighthouse
// Optional: LH_BASE_URL, LH_MIN (fail if any score is below it, default 0 = report only), CHROME_PATH.
import { mkdir, writeFile } from "node:fs/promises";

import * as chromeLauncher from "chrome-launcher";
import lighthouse from "lighthouse";

const BASE = process.env.LH_BASE_URL ?? "http://localhost:3000";
const MIN = Number(process.env.LH_MIN ?? 0);
const PAGES = ["/", "/map", "/reports", "/plan", "/login", "/about"];
const CATEGORIES = ["performance", "accessibility", "best-practices", "seo"];

const chrome = await chromeLauncher.launch({ chromeFlags: ["--headless=new", "--no-sandbox"] });
const rows = [];
const failures = [];

try {
  await mkdir("lighthouse-reports", { recursive: true });
  for (const path of PAGES) {
    const result = await lighthouse(`${BASE}${path}`, { port: chrome.port, output: "html", onlyCategories: CATEGORIES, logLevel: "error" });
    if (!result) continue;
    const name = path === "/" ? "home" : path.slice(1);
    await writeFile(`lighthouse-reports/${name}.html`, result.report);
    const scores = Object.fromEntries(CATEGORIES.map((c) => [c, Math.round((result.lhr.categories[c]?.score ?? 0) * 100)]));
    const audit = (id) => result.lhr.audits[id]?.displayValue ?? "-";
    rows.push({ page: path, ...scores, FCP: audit("first-contentful-paint"), LCP: audit("largest-contentful-paint"), TBT: audit("total-blocking-time"), CLS: audit("cumulative-layout-shift") });
    for (const [category, score] of Object.entries(scores)) {
      if (score < MIN) failures.push(`${path} ${category}: ${score} < ${MIN}`);
    }
  }
} finally {
  await chrome.kill();
}

console.table(rows);
console.log("Full reports: lighthouse-reports/*.html");
if (failures.length > 0) {
  console.error(`Below LH_MIN=${MIN}:\n${failures.join("\n")}`);
  process.exit(1);
}
