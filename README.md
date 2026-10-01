# Status — Mobile

Kotlin Multiplatform (KMP) shared module for **Status**, a service status page
app (component health, uptime history, incidents).

Part of [Chaowalit Greepoke](https://bookchaowalit.com)'s 101 Portfolio Projects.

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

CI (`.github/workflows/build.yml`) runs `:shared:build` on Ubuntu and the iOS
simulator tests on macOS. Failures fail the workflow.

## Related

- **Frontend:** [bookchaowalit-website/status-frontend](https://github.com/bookchaowalit-website/bookchaowalit-status-frontend)
- **Portfolio:** [bookchaowalit.com](https://bookchaowalit.com)

## License

MIT
