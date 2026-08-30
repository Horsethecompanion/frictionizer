# Final Stability and Navigation Restore

This plan addresses the remaining navigation issues and the accidental dismissal of the overlay during system events (volume/charging). We are reverting to a more standard "non-modal" overlay pattern that was more reliable in previous iterations.

## User Review Required

> [!IMPORTANT]
> I will be using `FLAG_NOT_FOCUSABLE` and `FLAG_NOT_TOUCH_MODAL` on a `MATCH_PARENT` window. To ensure navigation and notifications work, the root of the overlay will be set to **not clickable**. This allows Android to pass touches through the empty space of our window to the system UI and the app underneath.

> [!NOTE]
> I am replacing the hardcoded system package list with a "Real App" check. The overlay will only dismiss if the user switches to another app that can actually be launched. System overlays, volume bars, and charging alerts will be ignored, keeping the friction stable.

## Proposed Changes

### Accessibility Service (Stability & Touch)

#### [MODIFY] [FrictionizerAccessibilityService.kt](file:///Users/horse/AndroidStudioProjects/frictionizer/app/src/main/java/com/frictionizer/app/FrictionizerAccessibilityService.kt)
- **Robust App Switching**: Implement `isRealApp(pkg)` check using `packageManager.getLaunchIntentForPackage`. This prevents accidental dismissal from system events.
- **Full Screen Passthrough**:
    - Set window size back to `MATCH_PARENT` for both width and height.
    - Set flags: `FLAG_NOT_FOCUSABLE`, `FLAG_NOT_TOUCH_MODAL`, `FLAG_LAYOUT_IN_SCREEN`, `FLAG_LAYOUT_NO_LIMITS`.
    - Use `FLAG_DIM_BEHIND` with a moderate `dimAmount` (0.7f).
- **Event Filtering**: Ensure `TYPE_WINDOW_STATE_CHANGED` logic correctly handles switches back and forth between system overlays and the monitored app.

#### [MODIFY] [overlay_friction.xml](file:///Users/horse/AndroidStudioProjects/frictionizer/app/src/main/res/layout/overlay_friction.xml)
- **Non-Clickable Root**: Ensure the root `FrameLayout` is `match_parent` but `android:clickable="false"`.
- **Card Sizing**: Re-center the `CardView` within the full-screen container.

## Verification Plan

### Manual Verification
1. **Notifications**: While the overlay is visible, swipe down from the very top. Verify the notification shade opens perfectly.
2. **Navigation Bar**: Tap the Back, Home, and Recents buttons. Verify they respond immediately.
3. **Stability**: Change volume and plug in the charger. Verify the overlay **does not disappear**.
4. **App Switch**: Switch to another app (e.g., Settings or Calculator). Verify the overlay dismisses.
5. **Return**: Return to the monitored app and verify the overlay re-appears (unless unlocked).

### Build and Package
- Deploy to the connected Pixel device.
- Build the final Release APK.
- Commit all changes to Git.
