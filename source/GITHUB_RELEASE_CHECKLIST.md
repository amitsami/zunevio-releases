# Public GitHub release checklist

This bundle has not been published. A GitHub profile is known, but no owner-approved repository/release destination has been selected.

- [ ] Review the current `VALIDATION_1_2_0.json`, full Lint report and live-provider test result; distinguish warnings/front-end limitations from real defects and fix actionable release blockers.
- [ ] Install and exercise the **exact signed universal APK** on representative supported devices. Verify security prompts using their exact wording; do not instruct users to turn Play Protect off.
- [ ] Establish the repository, final package/version, support/privacy documentation and owner-controlled signing arrangements. Preserve signing continuity for installed users or explicitly document a migration plan. Do not rotate identity to evade security classification.
- [ ] Keep the private signing key and credentials outside Git. Do not upload `local.properties`, `keystore.properties`, `.keystore`, `.jks`, auth cookies or account backups.
- [ ] Retain GPLv3, upstream copyrights and attribution; provide the complete corresponding source and rebuild instructions for the shipped binary.
- [ ] Publish the universal APK, SHA-256 checksums, release notes and corresponding source as release assets, clearly labelled with supported Android/API/ABI ranges and known limitations.
- [ ] If Google wrongly identifies the exact APK as harmful, use Google's official developer appeal process. Supply the requested accurate APK/package/certificate/version evidence privately to Google; do not claim approval until it is actually granted.

Official guidance: https://developers.google.com/android/play-protect/warning-dev-guidance

“App scan recommended / Play Protect hasn't seen this app before” is not the same classification as “Harmful app blocked.” No build-only change can guarantee a permanent absence of warnings. No external APK scan or appeal was submitted from this task.

## 1.2.0 updater-specific checks

- [ ] Read `GITHUB_UPDATES.md`; set the actual new public repository in Settings only after creating it.
- [ ] Upload a signed, same-track `*-universal.apk` to a **published stable GitHub Release**, with a greater versionName and versionCode, release description and GitHub SHA-256 asset digest.
- [ ] Test notification permission/tap, manual check, changelog, cancel/retry, checksum/signer/package/version rejection and Android installation confirmation from an older build.
- [ ] Review the deliberately restored `REQUEST_INSTALL_PACKAGES` permission and narrow `cache/updates/` FileProvider path for the chosen distribution channel. Never claim that the permission prevents Play Protect warnings.
- [ ] Never publish the test or production keystore. This preview3 test identity is not the final production identity.
