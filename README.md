# Kolab — Android App (Developer Guide)

Kolab connects **creatives** (models, photographers, videographers, 3D artists,
stylists) with **brands** that want to collaborate. It is a native Android
(Compose) client for the Kolab NestJS backend living in the sibling folder
`kolab-backend-updated`.

This document is the single source of truth for building, running, securing and
contributing to the app. It is written for developers, not end users.

---

## 1. Tech stack

| Layer      | Choice                                                                 |
| ---------- | ---------------------------------------------------------------------- |
| Language   | Kotlin 2.2.10                                                          |
| UI         | Jetpack Compose (BOM 2024.09.00, Material 3, Material icons extended)  |
| DI         | Manual service-locator (`AppGraph`) — no Hilt/Koin                     |
| State      | ViewModels over `StateFlow` (MVVM)                                     |
| Networking | Retrofit 2 + OkHttp 4 (Gson), Socket.IO client for chat                |
| Crypto     | androidx.security-crypto (EncryptedSharedPreferences), JCA (ECDH/AES)  |
| Biometrics | androidx.biometric 1.1.0                                               |
| Notify     | Firebase Messaging (push only; no DB dependency)                       |
| Build      | AGP 9.1.1, KSP 2.3.5, Gradle 8.13+, JDK 17+                            |

Key app identifiers:
- `namespace` = `com.example`, `applicationId` = `com.aistudio.kolab.creatives`
- `minSdk` 24 · `targetSdk` 36 · `compileSdk` 36
- `versionName` 1.0 / `versionCode` 1

---

## 2. Repository layout

```
app/
  build.gradle.kts                     Dependencies, signing, BuildConfig.BASE_URL
  proguard-rules.pro                   R8 rules (minify currently disabled)
  src/main/AndroidManifest.xml         Permissions, deep link, backup hardening
  src/main/java/
    com/kolab/data/
      TokenManager.kt                  Encrypted prefs: refresh token, profile, public key
      FCMService.kt                    Push receiver (creates channel, deep-links)
      api/ApiClient.kt                 OkHttp/Retrofit wiring + safeCall()
      api/KolabApi.kt                  Retrofit endpoint definitions
      api/dto/*.kt                     Request/response models
      api/interceptor/AuthInterceptor.kt  Bearer injection + TokenAuthenticator (refresh)
      chat/ChatSocketManager.kt        Socket.IO client (joinDeal, sendMessage, readReceipts)
    com/example/
      KolabApplication.kt              Bootstrap: ApiClient, AppGraph, socket
      MainActivity.kt                  Kotlin Compose host, deep link, app-lock overlay
      data/
        AppGraph.kt                    Service locator singletons
        KolabRepository.kt             All backend calls + search/follow/security/social API
        Models.kt                      UI models (CreativeProfile, FeedPost, Hotspot, …)
        security/ChatCrypto.kt         E2EE chat crypto (ECDH + AES-256-GCM)
        security/BiometricAuth.kt      BiometricPrompt wrapper
        security/AppLockManager.kt     App-lock enable flag
      ui/
        KolabViewModel.kt              Top-level state machine (auth, navigation, data)
        auth/LoginScreen.kt, SignUpScreen.kt, AccountCreatedScreen.kt, SocialAuthScreen.kt
        home/HomeScreen.kt             Feed with Following / For-You toggle
        discover/DiscoverScreen.kt     Search + creators + follow
        places/PlacesScreen.kt         Location hotspots
        profile/ProfileScreen.kt       Profile, deals, portfolio, withdraw
        profile/SecurityScreen.kt      Password, 2FA, app lock, logout-all, E2EE status
        components/*.kt                CollabRequestDialog, CreateCollabSheet, dialogs, bars
        theme/                         Kolab dark glassmorphism theme
    com/kolab/ui/chat/
      ChatScreen.kt, ChatViewModel.kt, ChatViewModelFactory.kt
  src/test/                            Robolectric unit tests (JVM, no emulator)
gradle/libs.versions.toml              Version catalog
.env / .env.example                    Secrets for the Secrets Gradle Plugin
```

A Gradle **wrapper is committed** (`gradlew` / `gradlew.bat`, Gradle 9.3.1), so
any machine with JDK 17 can run `./gradlew` directly — no Android Studio, no
system-wide Gradle. The wrapper downloads its exact Gradle distribution the
first time it runs. See §4.6 for building with neither Android Studio nor a
lot of RAM.

---

## 3. Architecture & data flow

Everything funnels through one `KolabRepository` (backend calls) composed with
`ChatSocketManager` (realtime) and `TokenManager` (secure session storage),
created once in `KolabApplication.onCreate` and exposed as singletons on
`AppGraph`.

```
Compose screens ──collect──▶ KolabViewModel (StateFlow state machines)
                                   │
                                   ▼
                          KolabRepository
                           ├── Retrofit / KolabApi  (REST + auth refresh)
                           └── ChatSocketManager    (Socket.IO chat events)
                                   │
                              TokenManager (EncryptedSharedPreferences)
```

- **Auth state machine**: `KolabViewModel` drives a `AuthStep` enum
  (`LOGIN`, `SIGN_UP`, `ACCOUNT_CREATED`, `SPLASH`, `AUTHENTICATED`, social
  sheets…). All screens are hosted in `MainActivity`'s `setContent` and swap
  on `AuthStep`.
- **Token refresh**: OkHttp `Authenticator` (`TokenAuthenticator`) transparently
  refreshes on 401, single-flight under a lock, and clears the session on
  refresh failure.
- **Socket**: `ChatSocketManager` derives the Socket.IO base URL from
  `BuildConfig.BASE_URL` by stripping the trailing `/api`, authenticates with
  the bearer token both in Socket.IO `auth` and the `Authorization` header, and
  emits `newMessage` / `readReceipt` events into `SharedFlow`s that
  `ChatViewModel` collects.

---

## 4. Build & run

### 4.1 Prerequisites
- JDK 17+ (AGP 9.1.1 requires JDK 17, Gradle 9.3.1, Build Tools 36.0.0 — the
  committed wrapper pins Gradle 9.3.1 for you).
- An Android SDK: Android Studio **or** the standalone command-line tools
  (see §4.6 for the no-Studio path).
- The backend running on the host machine (see §5).

### 4.2 Default configuration
- `BuildConfig.BASE_URL` defaults to `http://10.0.2.2:3000/api/` (the emulator's
  alias for the host's localhost).
- Override for a physical device:
  ```
  ./gradlew assembleDebug -PKOLAB_BASE_URL=http://192.168.1.50:3000/api/
  ```
  (or `KOLAB_BASE_URL` as an environment variable).

### 4.3 Cleartext (dev-only) policy
`res/xml/network_security_config.xml` forbids cleartext everywhere **except**
`10.0.2.2`, `localhost` and `127.0.0.1`. If you run on a physical device over
LAN HTTP, add your machine's IP to that file (or use HTTPS in production).

### 4.4 Run
1. Open the folder in Android Studio → let Gradle sync.
2. Start your backend, then run `app` on an emulator (API 24–36).
3. Log in or register (role: CREATIVE) — or use the backend seed account.

### 4.5 Functional checks after first launch
- Discover search box works (debounced, ranked) and shows a results section.
- Home has a Following / For-You toggle.
- Creative cards show a follow pill; profiles show it in the detail sheet.
- Chat with a deal opens with an "End-to-end encrypted" badge once both devices
  publish keys (second device: backend seeded or a second emulator).
- Profile → lock icon opens Security & Privacy (password, 2FA, app lock,
  logout-all).

### 4.6 Building without Android Studio
You do **not** need Android Studio (it costs several GB of RAM and disk). Two
lighter alternatives:

**Option A — GitHub Actions (recommended; zero local resources).**
The repo ships with `.github/workflows/build-apk.yml`.
1. Create a free GitHub repository and push this folder to `main` — or use the
   GitHub web drag-and-drop upload (`.gitignore` already excludes secrets).
2. The workflow installs JDK 17 + the Android SDK on GitHub's machines and runs
   `./gradlew assembleDebug`.
3. Open the **Actions** tab → latest run → **Artifacts** → download
   `kolab-debug-apk` → unzip → `app-debug.apk`.
4. Transfer that APK to your phone (email/Drive/USB) and open it to install
   (enable "Install unknown apps" for the file manager you use).

**Option B — Gradle CLI on any machine with ~4 GB free disk.**
| Download | Approx. size |
| --- | --- |
| Temurin JDK 17 | ~180 MB |
| Android `commandlinetools-win-*.zip` | ~150 MB |
| Gradle 9.3.1 (wrapper, first build) | ~130 MB |
| SDK: `platforms;android-36` + `build-tools;36.0.0` | ~500 MB |

```powershell
# 1. Install JDK 17 and put its \bin on PATH
# 2. Unzip commandlinetools to C:\Android\cmdline-tools\latest  (folder MUST be "latest")
# 3. Accept licenses and install components
yes | sdkmanager --sdk_root="C:\Android" --licenses
sdkmanager --sdk_root="C:\Android" --install "platform-tools" "platforms;android-36" "build-tools;36.0.0"
# 4. Point the project at the SDK
Set-Content local.properties "sdk.dir=C\:\\Android"
# 5. Build (wrapper downloads Gradle 9.3.1 automatically)
#    Lower Gradle's heap if your machine is tight:
$env:GRADLE_OPTS = "-Xmx2g -Dorg.gradle.jvmargs=-Xmx2g"
.\gradlew assembleDebug
# 6. APK: app\build\outputs\apk\debug\app-debug.apk
```

Low-RAM tips: add `org.gradle.workers.max=2` to `gradle.properties`; keep
`configuration-cache` on (already enabled); the app is single-module so builds
stay modest.

---

## 5. Backend

The app expects the NestJS API in `../kolab-backend-updated`:

| Setting            | Value                                      |
| ------------------ | ------------------------------------------ |
| Base path          | `/api` (CORS enabled)                      |
| Default port       | `3000`                                     |
| DB                 | MongoDB (via Mongoose)                     |
| Real-time          | Socket.IO gateway at the same origin       |

Essential env vars (see its `.env.example`): `PORT`, `MONGODB_URI`,
`JWT_SECRET`/`JWT_REFRESH_SECRET`, and for social login:
`INSTAGRAM_CLIENT_ID`, `INSTAGRAM_CLIENT_SECRET`, `X_CLIENT_ID`,
`X_CLIENT_SECRET`, `SOCIAL_REDIRECT_URL=kolab://auth/callback`.

```
cd ../kolab-backend-updated
npm install          # or pnpm install
npm run start:dev    # adjust seed/dev script per its package.json
```

### Endpoints the app uses
| Area      | Endpoints                                                                  |
| --------- | -------------------------------------------------------------------------- |
| Auth      | `POST /auth/register`, `/auth/login`, `/auth/refresh`, `/auth/logout`, `/auth/change-password`, `/auth/2fa/setup`, `/auth/2fa/enable`, `/auth/2fa/disable`, `/auth/logout-all` |
| Social    | `GET /social/:provider/start`, `POST /social/:provider/exchange`           |
| Users     | `GET /users`, `GET /users/me`, `GET /users/:id`, `PATCH /users/me` (name, avatar, publicKey), `POST /users/me/portfolio`, `POST|DELETE /users/:id/follow`, `GET /users/:id/followers`, `/following` |
| Posts     | `GET /posts/feed` (with `search`, `category`, `tags`), `GET /posts/feed/for-you`, `POST /posts`, `POST /posts/:id/like`, `POST /posts/:id/view` |
| Deals     | `POST /deals`, `GET /deals`, `GET /deals/:id`, `PATCH /deals/:id/status`   |
| Pricing   | `GET /rate-cards`                                                          |
| Hotspots  | `GET /hotspots`                                                            |
| Payments  | `POST /payments/webhook/payfast`, `POST /payments/webhook/ozow` (server-side funding) |
| Withdraw  | `POST /withdrawals`, `GET /withdrawals/me`; admin: `GET /admin/withdrawals`, `POST /admin/withdrawals/:id/approve` |
| Notify    | `POST /notifications/devices` (register FCM token)                         |
| Uploads   | `POST /uploads`, `GET /uploads/presign`                                    |
| Chat      | Socket.IO events: `joinDeal`, `sendMessage`, `markRead`, `readReceipt`, `newMessage`; history via `GET /deals/:id` |

---

## 6. Authentication flows

### 6.1 Email
`POST /auth/register` → `POST /auth/login` → the returned `accessToken` +
`refreshToken` are stored (see §7.1). A 401 anywhere triggers `POST /auth/refresh`
silently. `logout` clears tokens and returns to the login step.

### 6.2 Instagram / X (social)
Behind-the-scenes PKCE OAuth relayed by the backend:
1. App calls `GET /social/:provider/start` → backend returns
   `{ configured, authUrl, state }`.
2. App opens the `authUrl` in the browser (`MainActivity.openAuthUrl`).
3. Provider redirects to `kolab://auth/callback?provider=…&code=…&state=…`,
   delivered as an Android intent (manifest intent-filter on `MainActivity`,
   scheme `kolab`, host `auth`, pathPrefix `/callback`).
4. `MainActivity.onNewIntent`/`onCreate` parse it into `SocialCallback` and
   `KolabViewModel.completeSocial` runs `POST /social/:provider/exchange`, which
   returns a token pair; the app then fetches its own profile via `GET /users/me`.

If the backend reports `configured=false`, the buttons show a "not configured"
notice and fall back to email login.

---

## 7. Security model

The app was designed "security first" for a messaging + payments workspace.

### 7.1 Session storage
- `accessToken` lives **only in memory** (`TokenManager`) — never persisted.
- `refreshToken`, user profile, and the E2EE key fingerprint persist in
  `EncryptedSharedPreferences` (`kolab_secure_prefs`), AES256_SIV key names,
  AES256_GCM values, master key in the Android Keystore (non-exportable).
- `clearTokens()` (on refresh failure / logout) wipes the refresh token;
  `clearAll()` additionally wipes profile and the public key.

### 7.2 End-to-end encrypted chat (`ChatCrypto.kt`)
Protocol per install:
1. Each device generates an **EC P-256 (secp256r1)** keypair. The private half
   is stored in `EncryptedSharedPreferences` (`kolab_e2ee_keys`), encrypted by
   the Keystore master key — it never leaves the device.
2. The public key (SPKI DER, base64) is published to `PATCH /users/me`
   (`publicKey`) whenever it changes (`ensurePublicKeyPublished`).
3. To send: derive `secret = ECDH(myPrivate, peerPublic)`, HKDF-lite
   `HMAC-SHA256(secret, "kolab-e2ee-v1")` → 32-byte AES key. Encrypt with
   **AES-256-GCM**, 128-bit tag, fresh 12-byte IV per message.
4. Wire format: `{"v":1,"iv":"<b64>","ct":"<b64>"}`. The backend socket layer
   only ever relays these opaque strings.
5. To read: same derivation from `(myPrivate, senderPublic)`. GCM authentication
   means tampered ciphertext decrypts to `null` → displayed as
   "Encrypted message — key unavailable on this device".
6. Fallbacks: no peer key → plaintext send (marked, prompts key exchange);
   decrypt failure → locked placeholder, never crashes.

**Known limitations (documented by design, not defects):**
- No forward secrecy — both parties keep their long-term EC keys; a stolen
  private half would reveal past messages. Rotating devices via key
  re-publication eventually rolls the channel.
- Keys are per-install, so reinstalling the app starts a fresh keypair (old
  messages become "locked").
- The server still sees metadata (who talks to whom, timestamps, message
  lengths).

### 7.3 App lock & account security
- **App lock** (optional, `SecurityScreen`): gates the whole app behind a
  `BiometricPrompt` (biometric **or** device credential; `BIOMETRIC_WEAK` on
  API 30+). The app re-locks when backgrounded (`onStop`), re-prompts on
  return, and shows an on-screen "Unlock" fallback. Enabled flag stored in
  `AppLockManager` prefs.
- **2FA**: TOTP setup (`/auth/2fa/setup` returns a secret + otpauth URL),
  enable/disable with a 6-digit code.
- **Change password**: requires the current password; enforced server-side.
- **Log out of all devices**: `/auth/logout-all` then local sign-out.

### 7.4 Network
- Production must use HTTPS; cleartext is disabled by default
  (`network_security_config.xml`).
- OkHttp logging is **`BODY` in debug builds only**; `NONE` in release — tokens
  and bodies never hit logcat in a release build.
- Socket.IO sends the bearer token in the standard `Authorization` header and
  in the Socket.IO `auth` payload; TLS in production.

### 7.5 Data at rest / backup
- `android:allowBackup="false"` (and no `dataExtractionRules`): encrypted
  prefs are **not** eligible for OS cloud backup or device-to-device transfer.
  Rationale: the Keystore master key is never backed up, so a restored copy
  would be cryptographically dead — and shipping the ciphertext to Google's
  servers needlessly lengthens data at rest. Trade-off: no backup/restore of
  session, feed prefs, or chat keys (they are device-bound by design anyway).

### 7.6 App surface hardening
- Only one exported component: `MainActivity` (launcher + `kolab://` schemes).
  `FCMService` is `android:exported="false"`.
- The `kolab://auth/callback` deep link is deliberately narrow (specific
  host/path); arbitrary links simply fail OAuth exchange server-side (state is
  validated on the backend).
- No secrets in code or git. `.env` is gitignored; the zip/artifacts exclude it.
- Release signing reads `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD` from
  the environment — never committed.

### 7.7 Before shipping to production
- Enable R8/minify for release (`isMinifyEnabled = true` + keep rules) to
  obfuscate and shrink.
- Enable Firebase **App Check** (deps are commented in `libs.versions.toml`) to
  block calls from non-app clients; add `google-services.json`.
- Consider adding a wallet ledger module on the backend — the in-app balance is
  currently a demo value (`R12 450.00`), withdrawals are real requests to
  `/withdrawals` but settle via the admin UI.
- Review push payloads: FCM `message.data` should not contain message bodies
  for E2EE channels.

---

## 8. Configuration reference

| File | Purpose |
| ---- | ------- |
| `.env` / `.env.example` | Secrets plugin inputs. `GEMINI_API_KEY` is a placeholder for a future Gemini integration; **not** referenced by current code. |
| `app/build.gradle.kts` | `KOLAB_BASE_URL` env → `BuildConfig.BASE_URL`; signing env vars. |
| `google-services.json` | User-provided Firebase config. The google-services Gradle plugin is set to **WARN/passthrough** so builds succeed without it; FCM won't deliver until it's present. |

---

## 9. Testing

- Unit tests: `./gradlew testDebugUnitTest` (Robolectric, JVM-only — no
  emulator). Existing: `KolabAppRobolectricTest` (app name + BASE_URL smoke).
- `roborazzi` (Compose screenshot testing) test deps + plugin are present for
  future visual regression tests.
- Manual E2EE test: two emulators, two accounts in the same deal; confirm the
  badge shows, send a message, then kill the app on one device and send from
  the other — history must still decrypt.

---

## 10. Releasing

- Keystore: place your `.jks` where `KEYSTORE_PATH` points (default
  `<project>/my-upload-key.jks`, never committed), alias `upload`.
- Build: `./gradlew assembleRelease` with `KEYSTORE_PATH`, `STORE_PASSWORD`,
  `KEY_PASSWORD` in the environment.
- Upload the AAB to Play Console (`./gradlew bundleRelease`).

---

## 11. Known limitations

- Google/Apple sign-in not implemented (no backend endpoints).
- IG/X social login inert until real provider credentials are set (see §6.2).
- FCM silent without `google-services.json`.
- Wallet balance is demo currency; real funding flows (PayFast/Ozow webhooks)
  exist server-side but aren't driven by the app UI.
- No offline cache/Room (local persistence is prefs + encrypted prefs only).
- Release builds are currently **not minified** (see §7.7).

---

## 12. Troubleshooting

| Symptom | Fix |
| ------- | --- |
| "No internet connection" in debug | Backend not running; or wrong IP for physical device (set `KOLAB_BASE_URL` + allow cleartext for that IP). |
| Socket never connects | `BuildConfig.BASE_URL` must end in `/api/` (stripped to compute the origin); check the bearer token is still valid. |
| "google-services plugin" warnings | Expected without `google-services.json`; add your file to enable FCM. |
| Chat shows locked messages | Peer hasn't published a key (`PATCH /users/me` with `publicKey`) or you changed installs; re-send once both keys exist. |
| App-lock prompt missing | Device has neither biometrics nor PIN set up; the overlay's "Unlock" button still works with a PIN configured. |
| No Android Studio / low resources | Use §4.6: cloud build via GitHub Actions, or Gradle CLI with the committed wrapper (needs only JDK 17 + ~4 GB). |
| Incorrect line endings | Keep LF line endings in `.kt`/`.kts`/`gradlew`; never re-sign with a template keystore. |