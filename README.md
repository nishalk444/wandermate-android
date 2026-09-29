# WanderMate Android

A guest-first travel companion built with Kotlin and Jetpack Compose. Explore a small curated destination catalog, plan a trip, and keep practical details together offline.

## Get started

1. Clone this repository and open its root folder in Android Studio.
2. Select JDK 17 for Gradle. Install Android SDK 35 and Build Tools 35.0.0 using SDK Manager.
3. Sync Gradle, select the `app` configuration, and run on an Android 8.0+ device or emulator.
4. Explore a destination or choose **Help me plan a trip**. No login or API keys are required for the included guest experience.

```sh
./gradlew assembleDebug testDebugUnitTest lintDebug
# With a connected device or emulator:
./gradlew connectedDebugAndroidTest
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`. GitHub Actions uploads a `wandermate-debug` artifact after successful builds.

## Included experience

| Feature | Implementation |
| --- | --- |
| Explore and destination guides | Four bundled starter guides: NYC, Virginia Beach, Washington DC, Blackwater Falls. Thirteen attractions, practical tips, seasons, transport guidance, official source links. |
| Search and filters | City/name/description search; category, family, and free-estimate filters; distance filter from destination center. |
| Favorites | Persistent per-place bookmarks in Room. |
| Trip wizard | Dates, travelers, interests, family preference, trip budget, and optional automatic starter itinerary. |
| Smart suggestions | Local deterministic planner: category/family filtering, group admission estimates, nearest-next-stop ordering, up to three stops and seven hours including buffers per day. |
| Daily itinerary | Add, remove, move earlier, move to next day, view visit-duration totals and straight-line distances. |
| Maps and nearby essentials | Open Google Maps search/directions externally. Search restrooms, pharmacies, hospitals, food, and transit near a destination. No location permission. |
| Offline guide | All bundled text and saved trip details are local from the start. Offline readiness confirmation; successful weather responses cached. |
| Weather-aware planning | Seven-day Open-Meteo forecast with cached/offline state, date-matched itinerary weather, and indoor-activity prompts. |
| Budget | Exact integer-cent storage, spending categories, remaining/over-budget state, separate admission planning estimate. USD only. |
| Packing checklist | Default checklist plus custom additions, completion state, and removal. |
| Booking organizer | Local confirmation references, HTTPS booking links, and document-provider PDF/image attachments. |
| Shared trips | JSON export/import through Android's document picker; imported copies are independently editable. Plain-text itinerary sharing through Android Sharesheet. |
| Appearance/accessibility | System/light/dark themes, Material components, scrollable screens, text labels and action descriptions. English only. |
| Engineering | Hilt, Room, StateFlow, Navigation Compose, unit tests, device smoke tests, CI APK and test-report artifacts. |

## What this version does not claim

This is a functional starter release, not a worldwide production travel service:

- The catalog is curated and limited to four destinations. Add entries in `Catalog.kt` to expand it.
- Prices, durations, family suitability, and accessibility notes are illustrative planning guidance. **Opening hours and admission availability are not live-verified.** Official links are shown throughout.
- The planner reserves 30% of total trip budget for attraction estimates and multiplies estimated entry cost by group size. It does not account for discounts, per-vehicle fees, actual road routing, opening hours, or booking availability. The 30-minute buffer is not a route-time estimate.
- Maps/directions open an external app or browser; there is no embedded map or offline map tile download.
- Text guides and trip records are offline. Photos are network-loaded and may only exist in image cache. Tickets remain with the chosen document provider and must be made offline there.
- Shared trip files are snapshots, not cloud accounts, invitations, or real-time multi-user collaboration. No backend is configured. A production collaborative feature needs authenticated server-side access rules and sync conflict handling.
- Weather is for the next seven days. Future trip dates outside that window show an unavailable message, not invented forecasts.
- Scenic photos are illustrative and are not guaranteed to depict the exact attraction.

## Architecture

`MainActivity` → Compose screens → `TravelViewModel` → `TravelRepository` → Room / Open-Meteo.

- `data/Models.kt`: domain and persistence models; versioned share envelope.
- `data/Catalog.kt`: offline editorial catalog and source links.
- `data/Database.kt`: Room tables for trip JSON, favorites, forecast cache. Schema exports are version controlled.
- `data/TravelRepository.kt`: persistence, validated import/export, theme preferences, forecast I/O.
- `domain/Planner.kt`: deterministic planner, distance, money/date and import validation.
- `ui/`: screens, reusable components, theme and state holder.
- `di/AppModule.kt`: application-scoped database, HTTP client, and injection.

Trip mutations are serialized and read the latest database snapshot to avoid lost updates during quick edits. Trips use JSON payloads inside Room for a small initial schema; any domain payload changes need backward-compatible decoding or explicit migrations.

## Services and privacy

No secrets are committed or required for the included experience.

- Open-Meteo receives destination coordinates, not the user's current location. Requests use HTTPS and timeouts. Data attribution is visible in the app. Review [Open-Meteo terms](https://open-meteo.com/en/terms) before commercial deployment; the public endpoint is not a blanket commercial-use entitlement.
- Unsplash image URLs receive normal network requests. Replace illustrative photos with licensed, maintained production assets before launch.
- Maps, official attraction pages, and booking links open externally.
- Trip records remain in app-private Room storage. Automatic Android backup is disabled. Data is removed on uninstall unless exported.
- Exported JSON includes budgets, expenses, and booking references. A confirmation explains this before export. Ticket URI permissions are stripped from exports/imports.
- Do not store passport numbers, payment-card data, or passwords in booking references. There are no analytics or advertising SDKs.

## Toolchain

Pinned for reproducibility: AGP 8.9.2, Gradle 8.11.1, Kotlin/Compose compiler 2.1.20, JDK 17, compile/target SDK 35, min SDK 26. These are a compatible baseline, not a claim to be the latest releases. Reassess the target SDK against Play submission requirements before publishing.

References: [AGP 8.9 compatibility](https://developer.android.com/build/releases/agp-8-9-0-release-notes), [Compose compiler plugin](https://kotlinlang.org/docs/compose-compiler-migration-guide.html), [Open-Meteo forecast API](https://open-meteo.com/en/docs).

## Validation

The Android workflow builds the debug APK, runs planner/validation unit tests, runs lint, and executes Compose smoke tests on an API 35 emulator. See the PR checks for actual results; merely defining a workflow is not evidence of a passing build.

See [manual QA](docs/QA.md) for device checks and [next milestones](docs/ROADMAP.md) for production expansion.

## License

MIT. See [LICENSE](LICENSE). Third-party libraries, data, and photos retain their own terms.
