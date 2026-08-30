# Final Navigation and Stability Fix (Take 3)

This plan addresses the persistent navigation issues and the "lock screen" bug. We will use a more robust window management strategy and add a "safety valve" to the UI to ensure the user can always exit the friction loop.

## User Review Required

> [!IMPORTANT]
> I am adding a "Close & Exit" button to the pop-up card. This button will use the Accessibility Service to perform a "Home" action, guaranteed to take the user back to their launcher regardless of system gestures.

> [!NOTE]
> The overlay window will be shrunken to `WRAP_CONTENT` for both dimensions and shifted slightly up from the bottom. This physically leaves the navigation and status bar areas untouched by our window, which should resolve the gesture blocking on your Pixel.

## Proposed Changes

### Accessibility Service (Logic & Window)

#### [MODIFY] [FrictionizerAccessibilityService.kt](file:///Users/horse/AndroidStudioProjects/frictionizer/app/src/main/java/com/frictionizer/app/FrictionizerAccessibilityService.kt)
- **Screen Off Detection**: Register a `BroadcastReceiver` for `ACTION_SCREEN_OFF` to instantly dismiss the overlay when the phone is locked.
- **Physical Window Sizing**:
    - Set window `width` and `height` to `WRAP_CONTENT`.
    - Use `FLAG_NOT_FOCUSABLE` and `FLAG_NOT_TOUCH_MODAL`.
    - Set `gravity = Gravity.CENTER` but add a `y` offset if needed (will try pure center first).
- **Refined Dismissal**:
    - Keep the "Real App" check but ensure it doesn't block the launcher.
    - If a switch to `com.android.systemui` is detected, we will now check if the screen is locked/off and dismiss accordingly.

### Overlay UI (Safety Valve)

#### [MODIFY] [overlay_friction.xml](file:///Users/horse/AndroidStudioProjects/frictionizer/app/src/main/res/layout/overlay_friction.xml)
- **Add Exit Button**: Add a subtle "Nevermind, go home" text button at the bottom of the card. This gives users a high-visibility way to exit without needing to wait for the countdown or fight with gestures.
- **Root Sizing**: Set root container to `wrap_content`.

## Verification Plan

### Manual Verification
1. **Navigation**:
    - Verify swiping from the edges for "Back" works.
    - Verify swiping from the bottom for "Home" works.
2. **Notifications**: Verify swiping down from the top works.
3. **Locking**:
    - Trigger the overlay.
    - Press the power button to lock.
    - Unlock the phone.
    - Verify the overlay is **gone** (it should re-trigger if you open the app again).
4. **Safety Valve**: Tap the "Exit" button on the card and verify you are taken to the Home screen immediately.

### Build and Package
- Deploy to the connected Pixel device.
- Build the final Release APK.
- Commit all changes to Git.
