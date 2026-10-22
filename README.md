# Status — Mobile

Kotlin Multiplatform (KMP) shared module for **Status**, a service status page
app (component health, uptime history, incidents).

Part of [Chaowalit Greepoke](https://bookchaowalit.com)'s 101 Portfolio Projects.

## Canonical template

This repo **is the canonical Kotlin Multiplatform template** in
`bookchaowalit-mobile`: a `shared` KMP module (JVM + iOS targets) with
`commonTest` suites, a committed Gradle wrapper (checksum-pinned), JDK 17, and
fail-closed CI. Copies built from the same scaffold:

- [`portfolio-techspace-mobile`](https://github.com/bookchaowalit-mobile/portfolio-techspace-mobile)

Each copy owns its code (no shared package). Consolidation or archiving of the
copies is pending owner decision D5 (BD-033), per
`repository-catalog/portfolio/MOBILE-CONSOLIDATION-2026-10.md` in solo-empire.
What exists today: [PRODUCT.md](PRODUCT.md).

## What is here today

Only the shared business-logic module exists. There is **no Android or iOS UI
project yet** — those are on the backlog in [docs/UPGRADE-PLAN.md](docs/UPGRADE-PLAN.md).

`shared/` (package `com.bookchaowalit.status`):

- `StatusModels.kt` — components, component status, health checks, incidents
  with an update timeline
- `StatusCalculator.kt`
  - overall page status (worst component wins; outages outrank maintenance)
  - uptime % over a time window, returning `null` (not 100%) when there is no data
  - daily uptime bars for an N-day history strip
  - average latency of successful checks
  - incident ordering (open first) and affected-component lookup
  - `formatUptime()` display helper

Tests live in `shared/src/commonTest` and run on every target.

## Tech Stack

- **Language:** Kotlin 2.0 (Kotlin Multiplatform)
- **Targets:** JVM (tests, and consumable by an Android app module),
  iosX64 / iosArm64 / iosSimulatorArm64 (static `shared` framework)
- **Build:** Gradle 8.10 wrapper (committed)

## Getting Started

Requires JDK 17+.

```bash
./gradlew :shared:jvmTest   # fast unit tests on the JVM
./gradlew :shared:build     # full build; iOS targets are skipped on non-macOS hosts
./gradlew :shared:iosSimulatorArm64Test   # macOS only
```

CI (`.github/workflows/build.yml`) runs `:shared:build :shared:allTests` on
`ubuntu-24.04` (JDK 17), the iOS simulator tests on `macos-14`, and the shared
`security.yml` scan (gitleaks, Semgrep, OSV-Scanner, Trivy) from
`bookchaowalit/platform-ci-workflows` v1.1.1. Any failing job fails the
workflow (no `|| true`).

## Related

- **Frontend:** [bookchaowalit-website/status-frontend](https://github.com/bookchaowalit-website/bookchaowalit-status-frontend)
- **Portfolio:** [bookchaowalit.com](https://bookchaowalit.com)

## License

MIT
