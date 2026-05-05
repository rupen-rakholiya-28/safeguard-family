# 🛡️ SafeGuard Child — Play Store Submission Guide

## App Information

| Field | Value |
|-------|-------|
| **App Name** | SafeGuard Child — Family Safety |
| **Package** | com.childprotection.child |
| **Category** | Parenting |
| **Content Rating** | Everyone |
| **Target Age** | 13+ (requires parent) |
| **Pricing** | Free |

## Play Store Listing

### Short Description (80 chars)
Consent-first family safety app — transparent monitoring with child awareness.

### Full Description
SafeGuard Child is a transparent safety companion for families. Unlike traditional parental control apps, SafeGuard puts consent and transparency first.

**How it works:**
1. Parent creates a family and shares an invite code
2. Child installs the app and joins using the code
3. Child reviews and approves each monitoring feature
4. A persistent notification always shows what's being monitored
5. Child can view their monitoring status anytime
6. SOS button for emergency situations

**Key Features:**
🔒 Consent-first — Nothing is tracked without explicit approval
📊 Screen time tracking — See daily device usage
📱 App usage insights — Aggregated usage data (no content reading)
📍 Location sharing — Optional, with clear consent
🆘 SOS button — Send emergency alert to parent
🔔 Persistent notification — Always shows what's being monitored
🚫 No hidden tracking — Everything is transparent

**What we DON'T do:**
❌ No reading messages or chats
❌ No recording audio or video
❌ No keylogging or screen capture
❌ No hidden background tracking
❌ No accessing private content

**Privacy First:**
- All data is encrypted in transit
- Minimum data collection (signals, not raw data)
- All consents are revocable at any time
- Full audit trail of all consent changes

## Required Play Store Declarations

### Permissions Justification

| Permission | Justification |
|------------|--------------|
| INTERNET | API communication with backend |
| PACKAGE_USAGE_STATS | Screen time tracking (with explicit consent) |
| ACCESS_FINE_LOCATION | Location sharing (with explicit consent) |
| FOREGROUND_SERVICE | Persistent notification showing monitoring status |
| POST_NOTIFICATIONS | Show monitoring indicator (transparency) |
| RECEIVE_BOOT_COMPLETED | Restart monitoring after device reboot |

### Data Safety Form

**Data Collected:**
- Device usage statistics (screen time, app usage duration)
- Approximate location (if consent granted)
- Device identifiers (for family linking)
- Account info (email, display name)

**Data NOT Collected:**
- Messages, calls, contacts content
- Photos, videos, files
- Browsing history content
- Keystroke data
- Audio/video recordings

**Data Sharing:**
- Data is shared only with the parent account linked through family invite code
- No data is shared with third parties
- No data is sold

**Data Retention:**
- Usage data retained for 30 days
- Account data retained until account deletion
- All data deleted on family disconnect

### Families Policy Compliance
- ✅ App is designed for families (child with parent oversight)
- ✅ All monitoring requires explicit user consent
- ✅ Persistent notification indicates active monitoring
- ✅ Child can view what is being tracked at any time
- ✅ Disconnect request available to child
- ✅ No ads in the app
- ✅ Age-appropriate content and language
- ✅ Privacy policy clearly describes data practices

### Device Admin / Special Permissions
- App does NOT request Device Administrator privileges
- Usage Stats permission requires user to manually enable in Settings
- Location permission follows standard Android permission flow

## Build & Release

### Debug Build
```bash
./gradlew assembleDebug
# APK at: app/build/outputs/apk/debug/app-debug.apk
```

### Release Build
```bash
# 1. Create signing keystore (one time)
keytool -genkey -v -keystore safeguard-release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias safeguard

# 2. Build release AAB (required for Play Store)
./gradlew bundleRelease

# 3. AAB at: app/build/outputs/bundle/release/app-release.aab
```

### Play Store Upload
1. Go to [Google Play Console](https://play.google.com/console)
2. Create new app → Select "App" → Category: "Parenting"
3. Complete the Data Safety form using the info above
4. Upload the `.aab` file to Production track
5. Fill store listing with description above
6. Submit for review

## Privacy Policy (Required)

Host the privacy policy at a public URL and link it in Play Console.
See `PRIVACY_POLICY.md` in the project root.
