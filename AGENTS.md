# AGENTS.md — SafeGuard Family

## Project Structure
```
backend/              # Spring Boot API (port 8080)
  → mvn spring-boot:run    # H2 in-memory DB for dev
child-app/            # Android child app (Kotlin, minSdk 26)
  → ./gradlew assembleDebug
parent-android-app/   # Android parent app (Kotlin)
  → ./gradlew assembleDebug
parent-app/           # Web dashboard (vanilla HTML/CSS/JS)
  → serve with any static server
```

## Developer Commands

### Backend
```bash
cd backend && mvn spring-boot:run      # Dev server on :8080
cd backend && mvn test                  # Run tests
```

### Android Apps
```bash
cd child-app && ./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Architecture Notes

- **Backend**: Controller → Service → Repository layers; JWT auth (access + refresh)
- **Child App**: Foreground service with persistent notification; UsageStatsManager for screen time; EncryptedSharedPreferences for token storage
- **Parent App**: Material 3; Retrofit + OkHttp networking

## Key Constraints

- Consent-first: No feature works without explicit opt-in
- All consents revocable; audit trail required
- Send signals only (risk level, type, confidence), NOT raw data (chat logs, images, audio)
- No hidden monitoring, silent recording, or stealth behavior
- Android: UsageStats API, Notification Listener, Location (with consent) allowed; Accessibility abuse, background recording restricted

## API Base URLs

- Dev (Android emulator): `http://10.0.2.127:8080/api/v1` (localhost from emulator)
- Dev (Android device): Use machine IP, not localhost
- Prod: `https://api.safeguardfamily.com/api/v1`