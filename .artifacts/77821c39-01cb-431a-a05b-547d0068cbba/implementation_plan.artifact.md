# Fix YouTube Crash and Refine Media Pausing

The previous attempt to force YouTube to pause was too aggressive and is causing YouTube to crash in the background. This plan reverts the aggressive changes and implements a safer, more standard approach to pausing media.

## User Review Required

> [!WARNING]
> I am removing the manual "Media Pause" key injection as it is likely the cause of the YouTube crashes. We will rely on a robust Audio Focus request instead.

## Proposed Changes

### Accessibility Service

#### [MODIFY] [FrictionizerAccessibilityService.kt](file:///Users/horse/AndroidStudioProjects/frictionizer/app/src/main/java/com/frictionizer/app/FrictionizerAccessibilityService.kt)
- **Revert Audio Focus Level**: Change `AUDIOFOCUS_GAIN` back to `AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE`. This is a strong transient focus that tells other apps they must pause and cannot "duck" (lower volume).
- **Remove Key Injection**: Delete the `dispatchMediaKeyEvent` logic that was attempting to force a pause via system keys.
- **Update Audio Attributes**: Use `USAGE_ASSISTANCE_SONIFICATION` which is appropriate for system-level overlays and highly likely to trigger pauses in media players.

## Verification Plan

### Manual Verification
1. Play a video or "Short" in the YouTube app.
2. Trigger the Frictionizer overlay by opening a monitored app.
3. Verify that YouTube pauses immediately.
4. Verify that YouTube **does not crash** and can be resumed normally after the overlay is dismissed.
5. Test with other media apps (e.g., Spotify, Instagram) to ensure consistent behavior.
