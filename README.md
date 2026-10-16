# Zunévio
### Your music. Your flow.

A focused Android music experience with clean lyrics, personal discovery and settings that stay out of your way.

[**Download the latest release →**](https://github.com/amitsami/zunevio-releases/releases/latest) · [All versions & changelogs](https://github.com/amitsami/zunevio-releases/releases) · [Privacy](PRIVACY.md)

---

## Made for everyday listening

- **Clean player** — single-line lyrics and a visible favorite heart.
- **Personal discovery** — refreshed Explore recommendations and a guest-friendly Library.
- **Focused settings** — individual category pages, readable switches and remembered scroll positions.
- **Updates inside the app** — changelog, verified APK download and Android's installation confirmation. No GitHub visit needed for future updates.
- **Optional update alerts** — allow Android notifications to hear when a new stable version is found.

## Install

1. Open the latest release and download **Zunevio-…-universal.apk**.
2. Open the APK and allow installation for the app you downloaded it with, if Android asks.
3. Keep Play Protect enabled. Review any Android warning; never bypass it blindly.

**Requirements:** Android 8.0 or later. One universal APK supports ARM, ARM64, x86 and x86_64; individual device compatibility and provider availability can vary.

### Moving from a preview?

The old debug-signed preview is not the production signing chain. The production package is `com.zunevio.music`, separate from `preview3`. Install the production app separately and keep the preview until you have migrated any needed data. Future official updates use the same production certificate and increasing version codes.

## How updates work

Zunévio checks this public release channel on app open, on demand, and approximately every 12 hours in the background. Android permission, battery policies and network availability affect phone notifications; this is not instant push. A repository commit alone is not an app update—a newer stable release with its APK must be published.

Downloads happen only when you choose. The app verifies the SHA-256 digest, package, signing certificate and newer version code before opening Android's installer. Android always controls installation and requires your confirmation.

## Transparency

Every release includes the APK, changelog, SHA-256 checksums, certificate details, validation report and **corresponding-source ZIP** with build scripts and required notices. This repository's Git history contains release documentation, not the development tree. The source ZIP remains publicly available under its applicable licenses.

See [License](LICENSE), [Notices](NOTICE.md), [Third-party notices](THIRD_PARTY_NOTICES.md) and [Privacy](PRIVACY.md). Required upstream credits are retained in those notices and the corresponding source. Zunévio is independent and is not endorsed by music providers or upstream projects.

No warranty, Play Protect certification, silent install, universal device smoothness guarantee or on-device future-update certification is claimed. Consult each release's validation report and known limitations.

---

**Zunévio** · Android music app · Maintained by [Amit Hasan Sami](https://github.com/amitsami)
