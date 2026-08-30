# Navigation Fix and UI Polish

This plan addresses the issue where the overlay blocks system navigation, improves the dismissal logic when switching apps, and polishes the main dashboard UI.

## User Review Required

> [!IMPORTANT]
> The overlay will now be non-focusable by default. This allows the system navigation (Back/Home/Recents) to function even when the countdown is active. If you hit "Back", the underlying app will receive the event and likely close, which will then trigger the overlay to dismiss.

## Proposed Changes

### Accessibility Service (Navigation Fix)

#### [MODIFY] [FrictionizerAccessibilityService.kt](file:///Users/horse/AndroidStudioProjects/frictionizer/app/src/main/java/com/frictionizer/app/FrictionizerAccessibilityService.kt)
- **Non-Focusable Overlay**: Add `FLAG_NOT_FOCUSABLE` to the overlay's WindowManager parameters. This ensures system buttons and gestures work.
- **Dismiss on App Switch**: Update `onAccessibilityEvent` to dismiss the overlay if the user switches to a non-monitored app (like the Home screen).
- **Restart Friction on Return**: Ensure that switching back to a monitored app re-triggers the friction (unless within the grace period).
- **Refined Dismissal**: Create a helper to dismiss the overlay without adding the current app to the "recently dismissed" list if the user simply navigated away.

### Main Dashboard (UI Polish)

#### [MODIFY] [MainActivity.kt](file:///Users/horse/AndroidStudioProjects/frictionizer/app/src/main/java/com/frictionizer/app/MainActivity.kt)
- **Hide Default Title**: Explicitly disable the ActionBar title display to ensure only the branding wordmark logo is visible in the toolbar.
- **Additional Top Spacing**: Add an extra 8dp of padding on top of the system status bar inset to give the header more breathing room.

#### [MODIFY] [activity_main.xml](file:///Users/horse/AndroidStudioProjects/frictionizer/app/src/main/res/layout/activity_main.xml)
- **Logo Alignment**: Ensure the logo in the toolbar is vertically centered and has proper margins.

## Verification Plan

### Manual Verification
1. **Navigation:**
    - Open a monitored app (e.g., YouTube).
    - While the countdown is active, hit the **Home** button or use the **Back** gesture.
    - Verify that the app closes/minimizes and the overlay disappears smoothly.
2. **UI Polish:**
    - Open the Frictionizer app.
    - Verify that the "Frictionizer" logo in the toolbar is not overlapping with the status bar clock/icons.
    - Verify that there is no plain text "Frictionizer" title visible (only the logo).

### Build and Package
- Deploy to the connected Pixel device for a final check.
- Run `gradlew assembleRelease` to generate the final release APK.
- Commit all changes to git.
