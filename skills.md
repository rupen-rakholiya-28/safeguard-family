# Skills

Specialized task workflows for this project.

## Run Backend Tests

Runs Spring Boot unit tests.

```bash
cd backend && mvn test
```

## Build Child App APK

Builds debug APK for the child Android app.

```bash
cd child-app && ./gradlew assembleDebug
```

Install on connected device:
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Build Parent App APK

Builds debug APK for the parent Android app.

```bash
cd parent-android-app && ./gradlew assembleDebug
```

## Start Dev Server

Runs backend with H2 in-memory database on port 8080.

```bash
cd backend && mvn spring-boot:run
```

## Run Parent Web Dashboard

Serves the vanilla HTML/CSS/JS dashboard.

```bash
cd parent-app && npx serve .
```

## Analyze Consent Flow

Traces consent grant/revoke flow through backend:
1. `ConsentController` → `ConsentService` → `ConsentRepository`
2. Checks `ConsentAudit` table for audit trail

## Debug Child App Service

The foreground service runs in `com.childprotection.child.service.MonitoringService`.
It publishes a persistent notification and uses:
- `UsageStatsManager` for app usage
- `WorkManager` for periodic sync
- `EncryptedSharedPreferences` for token storage