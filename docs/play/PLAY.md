# Google Play submission pack

Everything here is ready except the items only the account owner can supply
(Play Console account, Firebase project, screenshots from a real device).

## 1. Accounts and access

| Item | Status |
|---|---|
| Signed AAB (`release.yml`) | Ready – uploaded by CI |
| Play Console account (US$25, once) | **Needs owner** |
| Privacy Policy URL | `https://wiz-cmd.github.io/kolab/legal/privacy.html` |
| Account deletion URL | `https://wiz-cmd.github.io/kolab/legal/delete-account.html` |
| Terms URL | `https://wiz-cmd.github.io/kolab/legal/terms.html` |
| Support email | `support@kolab.co.za` |
| Demo login for reviewers | `brand@kolab.co.za` / `Password123!` |

## 2. Store listing (copy/paste)

**App name:** Kolab

**Short description (80 chars):**
`Find, book and pay South African creative talent in one place.`

**Full description:**

```
Kolab is the marketplace for South African creators and the brands that hire them.

Discover photographers, models, videographers, stylists and other creatives by city, category and rate. Browse real portfolios, agree the details in a secure deal, chat in the app and pay through PayFast – without chasing invoices over WhatsApp.

For creators:
• Publish a profile and portfolio
• Set your rates and availability
• Receive booking requests and collaborate on deals
• Track active work, payments and reviews in one dashboard

For brands:
• Search verified local talent
• Compare portfolios, rates and reviews
• Agree deliverables and milestones in a deal
• Pay securely and keep every contract in one place

Kolab is built for the South African creative industry. You must be 18 or older to use Kolab.
```

**Category:** Business (or Social)
**Tags:** creator marketplace, freelancers, bookings, South Africa
**Contact email:** support@kolab.co.za

## 3. Graphics

| Asset | Requirement | Status |
|---|---|---|
| App icon | 512×512 PNG, 32-bit | Source icon exists; export a 512×512 PNG |
| Feature graphic | 1024×500 PNG/JPEG | **To create** |
| Phone screenshots | 2–8, 16:9 or 9:16, min 320px | **Capture on a device** |
| Tablet screenshots | Optional | Skip for first release |

Suggested screenshots (in order): Discover feed, creator profile/portfolio,
deal detail, chat, payments/wallet, profile.

## 4. Data safety form (answers)

- **Does your app collect or share user data?** Yes.
- **Is all data encrypted in transit?** Yes (HTTPS only).
- **Can users request deletion?** Yes – in-app and via the deletion URL.
- **Data types collected:**
  - Personal info: name, email, phone (if added), user IDs.
  - Photos and videos: profile, portfolio, chat attachments.
  - Messages: in-app messages (end-to-end encrypted).
  - Financial info: payment references, payout details. Processed by PayFast.
  - Location: approximate location entered by the user; precise location only with permission.
  - App activity: searches, likes, follows, deal activity.
  - Device or other IDs: push token (only if notifications enabled).
  - App info and performance: crash logs, diagnostics.
- **Purpose:** app functionality, account management, payments, fraud prevention, analytics.
- **Shared with third parties?** Yes – hosting/database, object storage, PayFast, push provider.
- **Ephemeral processing?** No.
- **Data deletion:** users can delete in-app; financial records retained for 5 years as required by tax law (disclosed in the policy).

## 5. Content rating questionnaire

- Violence: none.
- Sexuality: none; user-generated portfolio images may include swimwear/fashion – moderate.
- Language: user-generated; moderation and reporting are in place.
- Controlled substances: none.
- Gambling: none.
- User interaction: yes – users can chat, follow and transact. Blocking/reporting is available.
- Shares location: no precise location sharing with other users; city/province only.
- Digital purchases: yes – deals and payouts via PayFast.
- Expected rating: **Teen / 16+** (user-generated content, online interaction).

## 6. App access instructions (for reviewers)

```
Use the brand demo account:
Email: brand@kolab.co.za
Password: Password123!

The app talks to https://kolab-api-7r6i.onrender.com/api/ (production).
If the API is asleep, the first request can take up to 60 seconds.
A creator account is also available: model@kolab.co.za / Password123!
```

## 7. Release checklist

1. [ ] Play Console account created and identity verified (owner).
2. [ ] App created with package `com.aistudio.kolab.creatives`.
3. [ ] AAB from the `v1.0.0` GitHub Release uploaded to Internal testing.
4. [ ] Store listing, graphics and screenshots added.
5. [ ] Data safety and content rating forms completed with the answers above.
6. [ ] Privacy Policy and deletion URLs entered.
7. [ ] Demo account credentials added under App access.
8. [ ] Internal test on a real device (API must be live).
9. [ ] Closed testing (12 testers, 14 days) if the account is new – Google policy.
10. [ ] Production rollout.

## 8. Blockers to fix before submitting

- **The production API must respond.** It currently hangs because the database
  is gone. Set a Neon `DATABASE_URL` and redeploy (see `docs/STABILITY.md` in the
  backend repo).
- **Push notifications** need `google-services.json` from a Firebase project.
  The app already runs without it, but notifications will not arrive.
- **Crash-free rate** cannot be measured until the app is live and either
  Firebase Crashlytics or Sentry is configured. The app's startup crash paths
  have already been hardened.
