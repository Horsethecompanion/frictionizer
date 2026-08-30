# Fix System Navigation and UI Polish

This plan addresses the issue where the overlay blocks the bottom navigation buttons and further polishes the main dashboard UI.

## User Review Required

> [!IMPORTANT]
> The overlay window will be changed from `MATCH_PARENT` height to `WRAP_CONTENT`. This ensures it physically doesn't cover the bottom navigation bar area. I will also use the system `FLAG_DIM_BEHIND` instead of a manual background color to allow touches to pass through outside the pop-up.

## Proposed Changes

### Accessibility Service (Navigation Fix)

#### [MODIFY] [FrictionizerAccessibilityService.kt](file:///Users/horse/AndroidStudioProjects/frictionizer/app/src/main/java/com/frictionizer/app/FrictionizerAccessibilityService.kt)
- **Window Layout**: Change `LayoutParams` height to `WRAP_CONTENT`.
- **System Dimming**: Add `FLAG_DIM_BEHIND` and set `dimAmount` to `0.8f` to replace the manual scrim.
- **Touch Passthrough**: Ensure the window doesn't intercept touches intended for system buttons.

#### [MODIFY] [overlay_friction.xml](file:///Users/horse/AndroidStudioProjects/frictionizer/app/src/main/res/layout/overlay_friction.xml)
- **Remove Scrim**: Remove `android:background="@color/scrim"` from the root `FrameLayout` as the system will now handle dimming.
- **Root Sizing**: Set root `layout_height` to `wrap_content`.

### Main Dashboard (UI Polish)

#### [MODIFY] [MainActivity.kt](file:///Users/horse/AndroidStudioProjects/frictionizer/app/src/main/java/com/frictionizer/app/MainActivity.kt)
- **Breathing Room**: Increase the top padding offset from `+24` to `+48` to ensure the branding is perfectly clear of all status bar elements.

## Verification Plan

### Manual Verification
1. **Navigation:**
    - Open a monitored app.
    - Verify that the bottom "Back", "Home", and "Recents" buttons are visible and functional while the pop-up is showing.
2. **Branding:**
    - Open the Frictionizer app and verify the logo is well-spaced from the top edge.

### Build and Package
- Deploy to the device for testing.
- Build the final Release APK.
- Commit all changes.
