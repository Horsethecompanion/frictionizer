# Fix Overlay Navigation and Persistence Issues

This plan addresses the regressions where the overlay blocks system navigation and dismisses incorrectly during system events like volume changes or charging.

## User Review Required

> [!IMPORTANT]
> I will be reverting the logic that dismisses the overlay on *any* non-monitored app switch. Instead, the overlay will only be dismissed if the user explicitly switches to a different "real" app (e.g., not the System UI). This will fix the issue where the pop-up disappears when plugging in a charger or adjusting volume.

> [!NOTE]
> To fully unblock the navigation bar and notification shade, I will use `FLAG_NOT_TOUCH_MODAL`. This tells Android to pass any touch events that occur *outside* our pop-up card through to the apps and system bars underneath.

## Proposed Changes

### Accessibility Service (Stability & Navigation)

#### [MODIFY] [FrictionizerAccessibilityService.kt](file:///Users/horse/AndroidStudioProjects/frictionizer/app/src/main/java/com/frictionizer/app/FrictionizerAccessibilityService.kt)
- **Safe Package Switching**: Update `onAccessibilityEvent` to ignore "System UI" and other background system packages. This ensures that transient system overlays (volume, battery alerts) don't accidentally dismiss the Frictionizer overlay.
- **Window Size Optimization**: Change the `WindowManager` width to `WRAP_CONTENT` in addition to height. This makes the "hit box" of the window as small as possible.
- **Improved Window Flags**:
    - Add `FLAG_NOT_TOUCH_MODAL` to allow touches outside the card to pass through.
    - Keep `FLAG_NOT_FOCUSABLE` to allow the notification shade to expand.
    - Remove `FLAG_LAYOUT_IN_SCREEN` to ensure the window doesn't compete with the status/navigation bar areas.

#### [MODIFY] [overlay_friction.xml](file:///Users/horse/AndroidStudioProjects/frictionizer/app/src/main/res/layout/overlay_friction.xml)
- **Remove Clickable Root**: Remove `android:clickable="true"` and `android:focusable="true"` from the root `FrameLayout`. This prevents the overlay container from "eating" touches that should pass through to the background.
- **Sizing**: Set the root container width to `wrap_content`.

## Verification Plan

### Manual Verification
1. **System Events**: Open a monitored app, wait for the overlay, then plug/unplug the charger and adjust the volume. Verify the overlay **stays visible**.
2. **Navigation Bar**: While the overlay is visible, use the Home, Back, and Recents buttons. Verify they work perfectly.
3. **Notification Shade**: Swipe down from the top while the overlay is visible. Verify the notifications/settings panel opens correctly.
4. **App Switching**: Navigate to a non-monitored app (e.g., Calculator). Verify the overlay disappears. Return to the monitored app and verify it re-appears.

### Build and Package
- Deploy to the connected Pixel device for testing.
- Build the final Release APK.
- Commit all changes to Git.
