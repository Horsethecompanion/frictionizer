# UI Refinement and Release Preparation

This plan addresses UI layout issues on the main screen and branding updates, followed by building a shareable APK.

## User Review Required

> [!NOTE]
> I will be replacing the plain text "Frictionizer" title in the main screen's toolbar with the official wordmark logo, matching the branding seen in the countdown overlay.

> [!IMPORTANT]
> Since no signing key is configured in the project, the "release" APK will be unsigned. For testing purposes, I will also provide the debug APK which is ready to install immediately.

## Proposed Changes

### Main Screen (MainActivity)

#### [MODIFY] [MainActivity.kt](file:///Users/horse/AndroidStudioProjects/frictionizer/app/src/main/java/com/frictionizer/app/MainActivity.kt)
- Update the `WindowInsetsListener` to apply the top system bar inset to the root view instead of just the ScrollView. This prevents the Toolbar from being obscured by the status bar.

#### [MODIFY] [activity_main.xml](file:///Users/horse/AndroidStudioProjects/frictionizer/app/src/main/res/layout/activity_main.xml)
- Add `android:id="@+id/main_root"` to the top-level `LinearLayout`.
- Remove `app:title="Frictionizer"` from the `Toolbar`.
- Add a custom `ImageView` inside the `Toolbar` to display `@drawable/frictionizer_wordmark`.

### Other Screens

#### [MODIFY] [SettingsActivity.kt](file:///Users/horse/AndroidStudioProjects/frictionizer/app/src/main/java/com/frictionizer/app/SettingsActivity.kt)
- Ensure top insets are handled consistently (already appears to be done, but will verify).

## Verification Plan

### Automated Tests
- Run `gradlew assembleDebug` to ensure the project compiles with the new layout changes.

### Manual Verification
- Deploy to the device and verify:
    - The Toolbar on the main screen is fully visible below the status bar.
    - The Frictionizer logo appears in the Toolbar instead of plain text.
    - The app still functions correctly after these layout changes.

### Build and Package
- Run `gradlew assembleRelease` to generate the release APK.
- Run `gradlew assembleDebug` to generate the debug APK.
- Commit all changes to git.
