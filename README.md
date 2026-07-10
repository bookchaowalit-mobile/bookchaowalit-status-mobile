# Status — Mobile

Kotlin Multiplatform (KMP) shared codebase for **Status**.

Part of [Chaowalit Greepoke](https://bookchaowalit.com)'s 101 Portfolio Projects.

## Tech Stack

- **Framework:** Kotlin Multiplatform (KMP)
- **Language:** Kotlin 2.0
- **Targets:** Android + iOS
- **Pattern:** Shared business logic, native UI

## Structure

```
shared/       — Common Kotlin code (business logic, data)
androidApp/   — Android-specific UI (Jetpack Compose)
iosApp/       — iOS-specific UI (SwiftUI)
```

## Getting Started

```bash
./gradlew :shared:build
```

## Related

- **Frontend:** [bookchaowalit-website/status-frontend](https://github.com/bookchaowalit-website/status-frontend)
- **Portfolio:** [bookchaowalit.com](https://bookchaowalit.com)

## License

MIT
