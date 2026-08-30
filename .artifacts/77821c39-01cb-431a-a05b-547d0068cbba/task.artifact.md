# Final Navigation and Stability Restore Task List (Take 3)

- `[x]` Refine Overlay Logic & Window Management
    - `[x]` Update `FrictionizerAccessibilityService.kt`: Implement `isRealApp` check (previously added)
    - `[x]` Update `FrictionizerAccessibilityService.kt`: Set window to `WRAP_CONTENT` to physically clear system bars
    - `[x]` Update `FrictionizerAccessibilityService.kt`: Add `ACTION_SCREEN_OFF` listener to dismiss overlay on lock
    - `[x]` Update `overlay_friction.xml`: Add "Nevermind, go home" safety valve button
- `[x]` Verification & Final Build
    - `[x]` Deploy to Pixel device and test:
        - `[x]` Bottom navigation bar (Home/Back/Recents)
        - `[x]` Notification shade (swipe down)
        - `[x]` System events (Volume/Charging)
        - `[x]` Lock screen dismissal
        - `[x]` Safety Valve button
    - `[x]` Build final Release APK
    - `[x]` Commit all changes to Git
