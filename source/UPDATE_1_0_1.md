# Zunéviu 1.0.1 — Settings performance correction

This update targets the reported Settings lag in v1. Android version code 32; package and private test signing lineage remain unchanged.

## Targeted changes

- Remove the Settings-only background recording layer and the blur/shadow renderer invoked for every card. These were plausible high-cost rendering paths on the scrolling Settings page; no phone trace was available to establish their measured share of frame time.
- Retain rounded cards, a lightweight black/white glass-style gradient and rim, original icons/switches, all options and developer links. This is **painted glass styling**, not live background blur/refraction. Other app screens and footer are unchanged by this correction.
- Collect Reduce blur once for Settings, not once per card. Remember each card's brushes/modifier chain across ordinary control updates.
- Map playback telemetry to the Atmos Boolean Settings actually displays and filter unchanged values, avoiding a whole Settings recomposition for unrelated telemetry changes.
- Keep actual account data, back-navigation and translation/startup fixes.

## Validation limits

Source-level regression tests enforce the absence of Settings backdrop/blur render calls, a single Reduce blur collector and the narrow playback observation. These guards do not measure GPU time or prove real-device smoothness. Existing unit/resource tests are run; the enabled live Genius lookup test is reported honestly if it fails. Full Lint has inherited unresolved issues and is not a quality gate passed by this update.

No connected physical Android phone or emulator is available. No measured FPS, before/after frame time, visual screenshot parity or on-phone playback result is claimed.

## Install and check

Install the ARM64 APK over the delivered v1 on the POCO. Do not uninstall/clear app data to resolve an update conflict. Confirm App info shows 1.0.1, scroll Settings quickly in both directions, and try switches, sliders, search and returning from a subpage. Check with playback active as well as stopped. Glass styling is deliberately less GPU-intensive than v1.

If lag remains, report whether it happens while opening Settings, scrolling, typing in search or changing a control; whether music is playing; and whether Reduce blur changes it. A short recording is useful; redact account information. The eager Settings column is retained to keep this a small rendering fix rather than a full list/navigation rewrite.
