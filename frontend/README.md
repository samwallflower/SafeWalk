# SafeWalk Web

Safety-aware walking navigation for pedestrians. Crowd-sourced incident reports become a live map, a searchable
reports explorer and safety-ranked route suggestions. Next.js (App Router) + shadcn/ui front end for the SafeWalk
Spring Boot backend.

## What is in it

| Area | Pages | Notes |
|---|---|---|
| Public | `/map`, `/reports`, `/plan`, `/incidents/[id]`, `/about`, `/privacy`, `/terms` | Map with incident dots and filters, reports as table or map, route comparison |
| Auth | `/login`, `/register`, `/verify` | Email verification flow, demo login buttons (opt-in) |
| Signed in | `/report`, `/dashboard`, `/dashboard/{reports,sessions,contacts,settings}` | Report form with pin picker, voting, own reports, walk history, emergency contacts, profile |
| Admin | `/admin`, `/admin/{incidents,users,categories,emergencies,sessions,authorities}` | Overview, moderation, CRUD for categories and authorities |

Walk-session tracking, buddy mode, SOS and anomaly detection live in the mobile app. The web app shows session and
emergency history read-only.

## Run it

Requirements: Node 20+, pnpm, the SafeWalk backend running (default `http://localhost:8080`).

```bash
pnpm install
cp .env.example .env.local   # then fill in the values below
pnpm dev                     # http://localhost:3000
```

### Environment

| Variable | Where | Purpose |
|---|---|---|
| `BACKEND_URL` | server only | Spring base URL, default `http://localhost:8080` |
| `API_PREFIX` | server only | Backend `api.prefix`, default `/api/v1` |
| `NEXT_PUBLIC_MAPBOX_TOKEN` | browser | Mapbox **public** token (restrict it by URL in the Mapbox dashboard) |
| `NEXT_PUBLIC_DEMO_LOGIN_ENABLED` | browser | `true` shows the "Use demo user / admin" buttons on `/login` |
| `NEXT_PUBLIC_DEMO_USER_EMAIL`, `..._PASSWORD`, `NEXT_PUBLIC_DEMO_ADMIN_EMAIL`, `..._PASSWORD` | browser | Credentials the demo buttons fill in |

Anything `NEXT_PUBLIC_*` is readable by every visitor. Keep demo login **off** for a public deployment.

### Scripts

| Command | Does |
|---|---|
| `pnpm dev` / `pnpm build` / `pnpm start` | Next.js |
| `pnpm lint` | ESLint (Next + React Compiler rules) |
| `pnpm typecheck` | `tsc --noEmit` |
| `pnpm test` | Vitest + Testing Library |
| `pnpm check` | lint + typecheck + test + build |
| `pnpm e2e` | Playwright end-to-end tests against the production server (`pnpm build` first) |
| `pnpm lighthouse` | Lighthouse (mobile) for the public pages against a running production server; reports in `lighthouse-reports/` |

## How it is organised

```
app/                 routes only (thin pages that render a feature screen)
features/<name>/     one folder per feature: components/, hooks/, api/, schemas/, lib/, types.ts
components/ui/       generated shadcn primitives, never edited
components/shared/   our wrappers around shadcn (DataTable, ConfirmDialog, StatCard, ...)
lib/                 http client, env, auth/JWT helpers, geo helpers, Mapbox geocoding, formatters
hooks/               hooks shared by several features
backend-contract/    copy of the backend controllers, DTOs, requests and enums (source of truth for the API)
docs/                API contract, plans and DECISIONS.md
```

Conventions: no `any`; API functions are typed from the Java DTOs; every data view has loading, empty and error
states; `userId` always comes from the session, never from the URL or a form.

## Backend contract

Endpoints and response shapes come from `backend-contract/`. If the backend changes, update that folder first. See
[docs/DECISIONS.md](docs/DECISIONS.md) for why things are built the way they are, including the data-size limits.

## Testing

**End-to-end (Playwright).** One-time: `pnpm exec playwright install chromium`. Then `pnpm build && pnpm e2e`
(it starts `pnpm start` itself, or reuses a server already on port 3000). Tests that need the backend and the demo
accounts from `.env.local` skip themselves when either is missing, so the rest still run. They cover: home page layout,
skip link, mobile menu without horizontal scroll, route guards, proxy hardening, login validation, sign-in with an
httpOnly cookie, user kept out of `/admin`, admin overview, logout, map, and the reports explorer.

**Lighthouse.** Start the production server, then `pnpm lighthouse`. Set `LH_MIN=90` to make it fail below a score.

**Unit and component (Vitest).**

`pnpm test` covers the pure logic (filters, aggregation, polyline decoding, vote counts, route guard, JWT decoding,
Zod schemas that mirror the Java validation) and key components (login demo buttons, vote buttons, data-table
states). Manual checks against a running backend are listed per feature in the phase notes.
