# Final Navigation and Stability Restore Task List

- `[x]` Refine Overlay Logic & Window Management
    - `[x]` Update `FrictionizerAccessibilityService.kt`: Implement `isRealApp` check
    - `[x]` Update `FrictionizerAccessibilityService.kt`: Set window to `MATCH_PARENT` with `FLAG_NOT_TOUCH_MODAL`
    - `[x]` Update `overlay_friction.xml`: Restore full-screen container with non-clickable root
- `[ ]` Verification & Final Build
    - `[ ]` Deploy to Pixel device and test:
        - `[ ]` Bottom navigation bar (Home/Back/Recents)
        - `[ ]` Notification shade (swipe down)
        - `[ ]` System events (Volume/Charging)
        - `[ ]` App switching dismissal
    - `[ ]` Build final Release APK
    - `[ ]` Commit all changes to Git
