# 🛡️ SafeGuard Family — Consent-First Child Safety Platform

A transparent, consent-based child safety and digital wellbeing platform.

## Project Structure

```
Child Protection/
├── backend/                  # Spring Boot REST API
│   ├── src/main/java/com/childprotection/api/
│   │   ├── config/           # Security, CORS config
│   │   ├── controller/       # REST controllers
│   │   ├── dto/              # Request/Response DTOs
│   │   ├── exception/        # Global error handler
│   │   ├── model/            # JPA entities & enums
│   │   ├── repository/       # Data access layer
│   │   ├── security/         # JWT token provider & filter
│   │   └── service/          # Business logic
│   └── src/main/resources/
│       └── application.yml   # App configuration
├── parent-app/               # Parent dashboard (Web)
│   ├── index.html
│   ├── css/style.css
│   └── js/
│       ├── api.js            # API client
│       └── app.js            # App logic & routing
├── parent-android-app/        # Parent Android app (Kotlin)
│   ├── app/src/main/java/com/childprotection/parent/
│   │   ├── ui/auth/          # Login & signup
│   │   ├── ui/dashboard/     # Family dashboard overview
│   │   ├── ui/family/        # Create family, invite, members
│   │   ├── ui/controls/      # Screen time, app blocking rules
│   │   ├── ui/alerts/        # SOS & safety alert management
│   │   ├── network/          # Retrofit API + FCM service
│   │   └── data/             # Encrypted preferences
│   └── app/build.gradle.kts
├── child-app/                # Android child app (Kotlin)
│   ├── app/src/main/java/com/childprotection/child/
│   │   ├── ui/onboarding/    # Join family via invite code
│   │   ├── ui/consent/       # Consent walkthrough
│   │   ├── ui/dashboard/     # Child dashboard + SOS
│   │   ├── service/          # Monitoring, usage tracking
│   │   ├── network/          # Retrofit API client
│   │   └── data/             # Encrypted preferences
│   ├── PLAY_STORE_GUIDE.md   # Play Store submission guide
│   └── app/build.gradle.kts
├── PRIVACY_POLICY.md         # Privacy policy (Play Store)
└── AGENTS.md                 # AI agent rules
```

## Quick Start

### 1. Start the Backend

```bash
cd backend
mvn spring-boot:run
```

The API runs on `http://localhost:8080` with an in-memory H2 database (dev mode).

### 2. Open Parent Dashboard

Open `parent-app/index.html` in your browser, or serve it:

```bash
cd parent-app
npx serve .
```

### 3. Android Child App

Open `child-app/` in Android Studio, sync Gradle, and run on a device/emulator.

```bash
cd child-app
./gradlew assembleDebug
# Install: adb install app/build/outputs/apk/debug/app-debug.apk
```

### 4. Use the App

1. **Sign up** as a parent on the web dashboard
2. **Create a family** and get the invite code
3. **Install the child app** on the child's Android device
4. **Enter the invite code** in the child app to link
5. **Review consents** — child approves each feature
6. **Monitor & protect** from the parent dashboard

## Phase 1 Features (MVP)

### ✅ Account & Family Setup
- Parent signup/login with JWT auth
- Family creation with invite codes
- Child joining via invite code
- Role-based access (PARENT, CHILD)

### ✅ Consent Management
- Granular per-feature consent (screen time, location, etc.)
- Consent granting & revocation
- Policy versioning
- Full audit trail

### ✅ Core Visibility
- Screen time dashboard
- App usage tracking & timeline
- Device status & battery level
- Location sharing (with consent)

### ✅ Parental Controls
- Screen time limits
- App blocking
- Bedtime mode
- Study mode schedules

### ✅ Safety Alerts
- SOS alerts
- Screen time exceeded alerts
- Geofence alerts
- Alert acknowledgment

### ✅ Trust Layer
- Persistent monitoring indicators
- Consent audit logs
- Clear disconnect flow
- Transparent data collection

### ✅ Android Child App
- Onboarding with invite code
- Consent walkthrough with why/what-not explanations
- Dashboard with screen time display
- SOS emergency button
- Foreground service with persistent notification
- Usage tracking via UsageStatsManager
- Encrypted local token storage
- Device heartbeat & battery monitoring
- Boot receiver for auto-restart

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Backend | Spring Boot 3.2.5, Java 17 |
| Database | H2 (dev) / PostgreSQL (prod) |
| Auth | JWT (access + refresh tokens) |
| Parent Web | Vanilla HTML/CSS/JS (dashboard) |
| Parent Android | Kotlin, Material 3, Bottom Navigation |
| Child Android | Kotlin, Material 3, Foreground Service |
| Networking | Retrofit 2.9 + OkHttp 4 |
| Storage | EncryptedSharedPreferences |
| Push | Firebase Cloud Messaging |

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/auth/signup` | Parent registration |
| POST | `/api/v1/auth/login` | Login |
| POST | `/api/v1/auth/refresh` | Refresh JWT token |
| POST | `/api/v1/families` | Create family |
| GET | `/api/v1/families` | Get my families |
| POST | `/api/v1/families/join` | Child joins family |
| POST | `/api/v1/consents/grant` | Grant consent |
| POST | `/api/v1/consents/revoke` | Revoke consent |
| GET | `/api/v1/consents` | List consents |
| POST | `/api/v1/devices/register` | Register device |
| POST | `/api/v1/events/usage` | Report usage events |
| POST | `/api/v1/events/location` | Report location |
| GET | `/api/v1/children/{id}/screen-time` | Get screen time |
| POST | `/api/v1/policies` | Create policy |
| GET | `/api/v1/policies` | List policies |
| GET | `/api/v1/alerts` | List alerts |
| PUT | `/api/v1/alerts/{id}/acknowledge` | Ack alert |

## Compliance

- ✅ Consent-first: Nothing works without explicit opt-in
- ✅ Transparent: All monitoring is visible to the child
- ✅ Revocable: All consents can be revoked at any time
- ✅ Auditable: Full audit trail for all consent changes
- ✅ No spyware: No hidden tracking or surveillance
- ✅ Play Store safe: Designed for policy compliance
