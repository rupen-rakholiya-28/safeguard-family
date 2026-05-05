# Quick Start Guide

## Prerequisites

- **Java 17** (for backend)
- **Maven 3.x** (for backend)
- **Node.js + npm** (for parent web dashboard)
- **Android Studio** (for Android apps)
- **Android SDK** (API 34, minSdk 26)

---

## 1. Start Backend

```bash
cd backend
mvn spring-boot:run
```

- Runs on `http://localhost:8080`
- Uses H2 in-memory database (dev mode)
- API prefix: `/api/v1`

---

## 2. Run Parent Web Dashboard

```bash
cd parent-app
npx serve .
```

Open `http://localhost:3000` in your browser.

---

## 3. Build & Run Child App

### Build APK
```bash
cd child-app
./gradlew assembleDebug
```

### Install on Emulator
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Install on Physical Device
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```
Note: For physical device, use your machine's IP instead of `localhost` in the app's API settings.

---

## 4. Build & Run Parent Android App

```bash
cd parent-android-app
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## Usage Flow

1. **Parent**: Sign up on web dashboard → Create family → Get invite code
2. **Child**: Install app → Enter invite code → Review & grant consents
3. **Parent**: View dashboard → Set screen time limits, app blocks, schedules

---

## Troubleshooting

- **Emulator**: Use `http://10.0.2.127:8080/api/v1` (Android emulator maps localhost to 10.0.2.127)
- **Physical Device**: Find your machine IP with `ifconfig` (macOS) or `ipconfig` (Windows)
- **Port already in use**: Check for other processes on port 8080

---

## Configuration for Testing

### Local Testing (Phone on Same WiFi)

The Android apps are configured to connect to your machine's IP:
- **Child App**: `http://192.168.88.6:8080/api/v1`
- **Parent App**: `http://192.168.88.6:8080/api/v1`

If your IP changes, update these files:
- `child-app/app/build.gradle.kts` (line 19)
- `parent-android-app/app/build.gradle.kts` (line 20)

Then rebuild:
```bash
cd child-app && ./gradlew assembleDebug
cd parent-android-app && ./gradlew assembleDebug
```

### Deploying to Production

When ready to deploy:

1. **Backend**: Deploy to Render/Railway/Vercel
2. **Update API URLs**:
   - In `child-app/app/build.gradle.kts` → release block (line 30): Use `https://api.safeguardfamily.com/api/v1`
   - In `parent-android-app/app/build.gradle.kts` → release block (line 31): Use `https://api.safeguardfamily.com/api/v1`

3. **Build release APK**:
```bash
cd child-app && ./gradlew assembleRelease
cd parent-android-app && ./gradlew assembleRelease
```

---

## Backend Test

```bash
cd backend && mvn test
```