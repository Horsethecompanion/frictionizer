# Frictionizer Tweaks Implementation Plan

This plan addresses several UI/UX improvements, a fix for YouTube Shorts pausing, and an adjustment to the app icon.

## User Review Required

> [!IMPORTANT]
> The search behavior in the "Select monitored apps" screen will be changed:
> - When not searching, the list shows "Common time-wasters" and any other apps you've already selected.
> - When searching, it will search through **all** installed apps on the device, allowing you to find and add any app directly from the main screen.

## Proposed Changes

### App Selection Screen

#### [MODIFY] [AppSelectionActivity.kt](file:///Users/horse/AndroidStudioProjects/frictionizer/app/src/main/java/com/frictionizer/app/AppSelectionActivity.kt)
- Update `AppAdapter` to handle a full list of installed apps.
- Implement a dual-mode filtering logic: curated/selected apps when query is empty, and global search when query is present.
- Fix `enableEdgeToEdge` implementation to handle bottom navigation bar insets.
- Ensure the main list refreshes correctly when apps are added via the "Add other app" dialog.

#### [MODIFY] [activity_app_selection.xml](file:///Users/horse/AndroidStudioProjects/frictionizer/app/src/main/res/layout/activity_app_selection.xml)
- Add additional bottom padding to ensure the "Add another app" button isn't too close to the system navigation bar.

### Overlay Animation

#### [MODIFY] [FrictionizerAccessibilityService.kt](file:///Users/horse/AndroidStudioProjects/frictionizer/app/src/main/java/com/frictionizer/app/FrictionizerAccessibilityService.kt)
- Update `dismissOverlay` to animate the alpha of the overlay to 0 over 1000ms before removing it from the WindowManager.

### YouTube Shorts Fix

#### [MODIFY] [FrictionizerAccessibilityService.kt](file:///Users/horse/AndroidStudioProjects/frictionizer/app/src/main/java/com/frictionizer/app/FrictionizerAccessibilityService.kt)
- Change `grabAudioFocus` to use `AUDIOFOCUS_GAIN` for a stronger "pause" signal.
- Explicitly dispatch a `KEYCODE_MEDIA_PAUSE` event when the overlay is shown to force media players (like YouTube Shorts) to stop.

### App Icon

#### [MODIFY] [ic_launcher_foreground.xml](file:///Users/horse/AndroidStudioProjects/frictionizer/app/src/main/res/drawable/ic_launcher_foreground.xml)
- Wrap the vector paths in a group with 80% scaling to provide more whitespace and prevent cropping in circular icons.

## Verification Plan

### Automated Tests
- Build the project to ensure no syntax errors in the new logic.
- Run `gradlew lint` to check for UI/layout issues.

### Manual Verification
- **App Selection:**
    - Open "Select monitored apps".
    - Verify that curated apps and already selected apps are shown.
    - Search for a non-curated app (e.g. "Calculator") and verify it appears.
    - Add a non-curated app via the dialog and verify it now appears in the main list.
    - Check the padding at the bottom of the screen.
- **Overlay:**
    - Open a monitored app.
    - Wait for countdown and select an activity.
    - Verify the overlay fades out smoothly over 1 second.
- **YouTube Shorts:**
    - Play a YouTube Short.
    - Switch to another monitored app (or re-enter YouTube if it's monitored).
    - Verify that the Short pauses immediately when the overlay appears.
- **Icon:**
    - Check the app icon in the launcher (especially if the device uses circular icons) to ensure the "F" logo fits well.
