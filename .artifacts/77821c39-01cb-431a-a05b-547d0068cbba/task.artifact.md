# Stability and Navigation Fix Task List

- `[x]` Fix Overlay Dismissal & Stability
    - `[x]` Update `onAccessibilityEvent` to ignore system UI packages
    - `[x]` Refine `dismissOverlay` to ensure it only fires for "real" app switches
- `[x]` Fix Touch Passthrough & Navigation
    - `[x]` Update `WindowManager.LayoutParams`: Use `WRAP_CONTENT` for Width & Height
    - `[x]` Update `WindowManager.LayoutParams`: Add `FLAG_NOT_TOUCH_MODAL`, Remove `FLAG_LAYOUT_IN_SCREEN`
    - `[x]` Update `overlay_friction.xml`: Remove clickable/focusable root
- `[/]` Final Verification & Build
    - `[ ]` Deploy and test system events (volume/charging)
    - `[ ]` Test navigation buttons and notification shade
    - `[ ]` Build final Release APK
    - `[ ]` Commit changes to Git
