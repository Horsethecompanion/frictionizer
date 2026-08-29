# Frictionizer Tweaks Task List

- `[x]` Update App Selection Screen UI & Logic
    - `[x]` Fix bottom padding/insets in `activity_app_selection.xml`
    - `[x]` Update `AppAdapter` for global search and dynamic list updates
    - `[x]` Update `AppSelectionActivity` to load all apps for searching
- `[x]` Implement Overlay Fade-out Animation
    - `[x]` Modify `FrictionizerAccessibilityService#dismissOverlay` to use `ViewPropertyAnimator`
- `[x]` Fix YouTube Shorts / Media Pausing
    - `[x]` Update `grabAudioFocus` and add media pause keyevent in `FrictionizerAccessibilityService`
- `[x]` Adjust App Icon Whitespace
    - `[x]` Scale logo in `ic_launcher_foreground.xml` to 80%
- `[ ]` Verification
    - `[ ]` Build and manual check of each fix
