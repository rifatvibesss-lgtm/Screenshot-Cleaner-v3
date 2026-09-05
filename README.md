# Screenshot Cleaner — publish-ready Android source

## Included
- Screenshot discovery using MediaStore
- Exact duplicate detection using local MD5 hashes
- Thumbnail review list
- Select all / clear / delete with confirmation
- Storage totals and duplicate savings estimate
- Android 13/14+ media permission handling
- Google Mobile Ads SDK integration using Google's test App ID placeholder
- Google Play Billing 9.1.0 scaffold for a one-time `remove_ads_lifetime` product
- Local-only photo analysis (no photo upload in this code)
- Privacy policy template

## Important before Google Play release
1. Replace the test AdMob App ID in `AndroidManifest.xml` with your real AdMob App ID and create your real ad unit(s). The current ID is Google's test ID.
2. Create the Google Play one-time product with ID `remove_ads_lifetime`, or change the ID in `BillingManager.kt` to match your Play Console product.
3. Add your real developer/support details to the privacy policy and host it on a public HTTPS webpage. Do not use the bundled text file as the Play policy URL.
4. Complete Play Console Data Safety and Ads declarations accurately for the exact release and SDKs used.
5. Build a signed Android App Bundle (`.aab`) for Google Play. New apps/updates submitted from 31 August 2026 must target Android 16 / API 36 or higher.
6. Test on Android 13, 14, 15 and 16 devices, including devices where photo access is limited/selected.
7. Create a proper launcher icon, screenshots, feature graphic and store listing assets before production release.
8. Run Play Console internal/closed testing and fix policy/build warnings before production.

## Monetisation
The app is structured for a free version with ads and a lifetime premium purchase to remove ads. Actual ad revenue only starts after your own AdMob account/app/ad units are configured and the app has users.

## Build
Open this folder in Android Studio with a current JDK/Android SDK. Sync Gradle, then Build > Generate Signed Bundle/APK.
