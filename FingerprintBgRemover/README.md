# Fingerprint BG Remover

A minimal LSPosed module that does exactly one thing: it removes the background circle drawn
behind the fingerprint (device entry) icon on the lock screen. The icon itself, its position, its
touch target and the UDFPS sensor area are left untouched.

## What it hooks

| Android | Class | Field |
|---|---|---|
| 14+ | `com.android.systemui.keyguard.ui.view.DeviceEntryIconView` | `bgView` |
| 12 / 13 | `com.android.systemui.statusbar.phone.LockIconView` | `mBgView` |

Both are `ImageView`s, so the module simply sets their image alpha to `0` right after the view is
built (constructor on 14+, `onFinishInflate()` on 12/13) and re-applies it on every layout pass,
because SystemUI re-binds the background whenever the device entry state changes. Nothing is
resized, hidden or re-laid-out, so no other lock screen behaviour changes.

Scope is `com.android.systemui` only.

## Build

Needs the Android SDK (`ANDROID_HOME`, or a `local.properties` with `sdk.dir=...`):

```bash
cd FingerprintBgRemover
./gradlew assembleRelease
```

The APK lands in `app/build/outputs/apk/release/`. Release builds are signed with the local debug
key so the output installs without extra setup - swap in your own signing config if you care.

## Install

1. Install the APK.
2. Enable the module in LSPosed and tick **System UI** in its scope (it is the only scoped app).
3. Force-stop SystemUI or reboot.

The module has no UI - there is nothing to configure, so it ships without a launcher activity and
is only visible in LSPosed's module list.

## Notes

- "Home screen" here means the lock screen: that is where Android draws the fingerprint affordance.
  There is no fingerprint button on the launcher.
- Logs are written to the LSPosed log under the tag `FingerprintBgRemover`, and only when a
  SystemUI build turns out not to have the expected field.
- This is standalone - it is not wired into the PixelXpert Gradle build and does not affect it.
  PixelXpert's own `UDFPSManager` covers the same ground behind the `fingerprint_circle_hide`
  preference; do not run both for the same purpose.
