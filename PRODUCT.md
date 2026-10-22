# Status (mobile) — product

## What it is

The shared business logic for a service status page app (component health,
uptime history, incidents), written as a Kotlin Multiplatform module. It is
also the canonical KMP template for the `bookchaowalit-mobile` organisation
(see README).

## What exists today

Only the `shared/` module. **There is no Android or iOS app and no UI.**
Nothing can be installed on a phone yet.

`shared/` (package `com.bookchaowalit.status`) provides:

- Models: components and their status, health checks, incidents with an update
  timeline.
- Overall page status (worst component wins; outages outrank maintenance).
- Uptime % over a time window — `null` (shown as "No data") when there are no
  checks. The display helper never rounds a window with failures up to
  "100.00%" or a window with some successes down to "0.00%".
- Daily uptime bars for an N-day history strip.
- Average latency of successful checks.
- Incident ordering (open first; same-timestamp updates ordered by stage) and
  affected-component lookup.

Data is passed in by the caller. There is no network client, no backend, no
persistence, no accounts and no analytics.

Covered by 17 tests in 3 `commonTest` files, run on the JVM in CI and on the
iOS simulator (macOS runner).

## Distribution

Not published anywhere (no store listing, no Maven artifact).

## Not done yet

See [docs/UPGRADE-PLAN.md](docs/UPGRADE-PLAN.md): Android/iOS UI projects that
consume `shared`, and a data source for real status checks.
