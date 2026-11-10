# Releasing Kolab (Android)

The release pipeline lives in `.github/workflows/release.yml` and produces:

- `kolab-release-apk` — signed APK, sideloadable on any phone
- `kolab-release-aab` — signed App Bundle for Google Play

## 1. Back up the signing key first

The release keystore is **not** in this repository (and must never be).

- Local copy: `C:\Users\Wiz'\Documents\KOLAB\keys\kolab-release.jks`
- Alias: `kolab`
- Password: stored next to it in `keys\password.txt`

Back both files up somewhere safe (password manager + offline copy). **If you
lose the keystore you can never update the app under the same application ID.**
Never commit the `.jks` or its passwords.

## 2. Store the signing secrets in GitHub

Settings → Secrets and variables → Actions → New repository secret:

| Secret | Value |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | `base64 -w0 kolab-release.jks` |
| `ANDROID_KEYSTORE_PASSWORD` | keystore password |
| `ANDROID_KEY_ALIAS` | `kolab` |
| `ANDROID_KEY_PASSWORD` | key password (same for PKCS12) |

PowerShell (Windows):

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("C:\Users\Wiz'\Documents\KOLAB\keys\kolab-release.jks")) | Set-Clipboard
```

Or use the helper in the backend repo:

```powershell
$env:GITHUB_TOKEN = "..."   # repo scope / Secrets: write
./scripts/set-github-secrets.ps1 -Repo Wiz-cmd/kolab -EnvFile ..\keys\github.env `
  -Keys ANDROID_KEYSTORE_BASE64,ANDROID_KEYSTORE_PASSWORD,ANDROID_KEY_ALIAS,ANDROID_KEY_PASSWORD
```

## 3. Build a release

- **Manual:** Actions → *Release APK/AAB* → *Run workflow*. Optionally set a
  version name and backend URL. Artifacts appear on the run page.
- **Tagged:** `git tag v1.0.1 && git push origin v1.0.1`. A GitHub Release is
  created with the APK and AAB attached.

`versionCode` is the workflow run number (monotonically increasing);
`versionName` comes from the tag (`v1.0.1` → `1.0.1`) or the manual input.

## 4. Local release build (optional)

`keystore.properties` in the repo root (gitignored) points at the keystore:

```properties
storeFile=C:/path/to/kolab-release.jks
storePassword=...
keyAlias=kolab
keyPassword=...
```

```bash
./gradlew assembleRelease bundleRelease
```

Without a keystore the release build still compiles, but the APK is unsigned.

## 5. Google Play specifics

- Upload the `.aab`, not the APK.
- The application ID is `com.aistudio.kolab.creatives`. It cannot be changed
  after the first Play upload.
- Google Play requires a paid developer account (one-time US$25).
- Add `google-services.json` (Firebase) if you want push notifications; the app
  builds and runs without it.
