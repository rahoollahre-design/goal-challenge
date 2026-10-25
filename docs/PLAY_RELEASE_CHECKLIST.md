# Google Play Release Checklist

## Build
- [ ] `compileSdk = 36`
- [ ] `targetSdk = 36`
- [ ] Release AAB built and verified as signed with your upload key
- [ ] Play App Signing enabled
- [ ] Unique permanent application ID confirmed before first publish
- [ ] Release versionCode increased for every upload
- [ ] Test on Android 16/API 36, Android 15/API 35, and at least one older supported Android version
- [ ] Verify the home/player screens respect system bars and display cutouts on edge-to-edge devices

## Quality
- [ ] Run `:game-core:test`
- [ ] Test portrait, landscape, tablet/foldable aspect ratios
- [ ] Verify no score changes while app is backgrounded
- [ ] Verify TTS missing/unavailable does not crash the game
- [ ] Verify sound/vibration settings persist
- [ ] Verify Reset Score and Back/Home behavior
- [ ] Review Android Vitals after internal/closed testing

## Store / policy
- [ ] Confirm rights to all player images, names, likenesses and trademarks
- [ ] If rights are not available, replace celebrity branding before upload
- [ ] Complete Data Safety accurately
- [ ] Publish a privacy policy appropriate to the final app/SDK set
- [ ] Complete content rating questionnaire
- [ ] Prepare screenshots, feature graphic and app icon that do not imply unauthorized official endorsement

## Privacy baseline in this generated project
- No INTERNET permission
- No login
- No analytics
- No ads
- No location/camera/microphone/contacts/storage access
- `VIBRATE` is the only declared permission and is a normal permission used for optional winner haptics

## Signing a Play upload bundle
The release bundle task intentionally fails unless an upload keystore is configured. Keep the keystore and passwords outside the project, for example in `~/.gradle/gradle.properties`:

```properties
GOALCHALLENGE_RELEASE_STORE_FILE=/absolute/path/to/upload-key.jks
GOALCHALLENGE_RELEASE_STORE_PASSWORD=your-store-password
GOALCHALLENGE_RELEASE_KEY_ALIAS=your-key-alias
GOALCHALLENGE_RELEASE_KEY_PASSWORD=your-key-password
```

Then run `./gradlew :app:bundleRelease` (or `gradlew.bat :app:bundleRelease` on Windows). Create and securely back up your own upload key before configuring it; never commit the keystore or its passwords. If Play App Signing is enabled, Google manages the app signing key while you sign uploads with this upload key.

Building the AAB does not complete Play Console setup. Supply a real privacy-policy URL and support contact, complete Data Safety and content-rating forms, verify image/name/likeness rights, and finish store listing assets before publishing.
