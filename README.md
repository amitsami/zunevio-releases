![Zunevio for Android — Your music. Your flow.](assets/gallery/banner.png)

# Zunevio
### Your music. Your flow.

An Android music experience with personal discovery, immersive artwork, lyrics and playback settings.

[**Download latest release →**](https://github.com/amitsami/zunevio-releases/releases/latest) · [All versions](https://github.com/amitsami/zunevio-releases/releases) · [Privacy](PRIVACY.md)

## Latest update — 1.3.1 (Build 64)

- Correct repeated-track seeking after Next and audio/video handovers.
- Keep seek state with its active track and clock; clean up cancelled gestures.
- Separate the landscape “This phone” caption from the headphones button.
- Soften the video's right edge without reducing the video pane.
- Show only the selected footer icon in brand red.
- Remove redundant video compositing; no measured FPS guarantee.

[Changelog and known limits](RELEASE_NOTES.md) · [Release assets and source](https://github.com/amitsami/zunevio-releases/releases/tag/v1.3.1)

## Download and install

Choose **Zunevio-1.3.1-build64-arm64.apk** for a compatible ARM64 device, or **Zunevio-1.3.1-build64-universal.apk** if unsure. Android 8.0+ is required.

1. Download only from this repository’s official release page.
2. Open the APK and review Android’s installation prompts. Keep Play Protect enabled.
3. To update the existing production app, keep it installed. Do not uninstall or clear data just to update. Preview/debug apps may use a separate package or signing chain.

Package: `com.zunevio.music`. Updates retain the production signing identity. The app checks update SHA-256, package identity, signer and a newer version code before opening Android's installer. Device/network/battery policies affect update checks.

## App gallery

The images below are historical gallery screenshots from v1.2.6, **not screenshots of Build 64**.

### Discover your next favorite
![Listen Now, Explore and Library](assets/gallery/discover.png)

### Stay in the music
![Now Playing and Track Controls](assets/gallery/listen.png)

### Make it yours
![Settings](assets/gallery/settings.png)

## Source and transparency

The exact 1.3.1 application/build sources are supplied in the explicitly named **Zunevio-1.3.1-build64-corresponding-source.zip** release asset, alongside build instructions, license notices, validation summary, signer information and SHA-256 checksums. The source tree already present in this repository and GitHub’s automatic tag source archives are historical/download-repository material; use the named corresponding-source asset for the current APK.

Public files exclude owner signing keys, passwords, private CI configuration and internal test reports. Full development/review bundles are not mirrored here.

Validation: 1,419/1,420 unit tests passed; one known external Genius HTTP 403 test failure. Zero Error/Fatal lint findings, 701 warnings. Offline seek/decoder tests passed on Android API 28 and 35 emulators. No physical-device/FPS certification, full security audit or all-provider/bug-free guarantee.

## Credits and license

Maintained by [Amit Hasan Sami](https://github.com/amitsami). Derived from [BitChord](https://github.com/kushagrasinghx/BitChord). Required source, copyright and third-party notices are retained.

[License](LICENSE) · [Notices](NOTICE.md) · [Third-party notices](THIRD_PARTY_NOTICES.md) · [App privacy](PRIVACY.md)

Zunevio is independent and is not endorsed by music providers or upstream projects. Media, lyrics and artwork remain with their respective rights holders. Provider availability, account requirements and device compatibility vary.
