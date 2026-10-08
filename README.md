# Penny

Penny is an offline-first personal finance app for Android and iOS, built with Kotlin Multiplatform and Compose Multiplatform. Financial records live on the device in a Room database. The database is the source of truth; screens react to its flows, and there is no backend, account, or network dependency.

## What’s here

- Shared Compose dashboard for Android and iOS
- Local income and expense entry with input validation
- Room Multiplatform persistence backed by bundled SQLite
- Reactive balance, income, expenses, recent activity, and cash-flow summary
- Domain use cases and repository boundary
- Koin dependency wiring
- Kotlin serialization-ready transaction models
- Common tests for balance calculations, validation, and input normalization

## Architecture

```text
shared/src/commonMain/
├── data/
│   ├── local/database/   Room entities, DAO, database
│   └── repository/       Room-backed repository implementation
├── di/                   Koin module
├── domain/
│   ├── model/            Finance models
│   ├── repository/       Repository contract
│   └── usecase/          Dashboard calculation and transaction rules
└── presentation/         StateFlow-backed dashboard state and Compose UI
```

Only database file selection is platform-specific. Android stores `penny.db` in its app database directory; iOS stores it in Application Support. Both use the same Room schema, DAO, repository, and business logic.

## Run

- Android: `./gradlew :androidApp:assembleDebug`
- iOS: open `iosApp/iosApp.xcodeproj` in Xcode and run the `iosApp` scheme.
- Common tests: `./gradlew :shared:allTests`

## Design notes

Amounts are represented as integer minor units (for example, cents) to avoid floating-point arithmetic in financial totals. Currency presentation is currently USD. The starter experience is intentionally empty; add a transaction to populate the dashboard.

Penny does not request internet access and does not send financial data off-device.
