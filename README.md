# Frictionizer

An Android app that adds a brief moment of intentionality before you open time-wasting apps.

## How it works

When you open a monitored app (e.g. YouTube, Instagram), a full-screen overlay appears asking
**"What are you doing?"** You must pick a reason before you can tap **Go!**.

### Grace Period
To avoid being repetitive, Frictionizer includes a **3-minute grace period**. If you switch away from an "unlocked" app and return to it within 3 minutes, you won't be prompted again. This period is tracked independently for every app you monitor.

Optionally, time spent per app and per activity is recorded so you can review your habits.

---

## Building

### Requirements
- Android Studio Iguana (2023.2.1) or newer
- Gradle 8.10.2
- JDK 17
- Android SDK 34

### Steps
1. Open the `Frictionizer/` folder in Android Studio.
2. Let Gradle sync.
3. Run on a physical device or emulator. 
   - *Note: On physical devices, you may need to disable battery optimization for the service to remain reliable.*
4. On first launch, tap **Enable in Accessibility Settings** and enable **Frictionizer**.

---

## Permissions explained

| Permission | Why |
|---|---|
| `QUERY_ALL_PACKAGES` | To list your installed apps so you can pick which to monitor |
| Accessibility Service | To detect when a monitored app comes to the foreground and show the friction overlay |

**No internet access, no advertising IDs, no data leaves your device.**

---

## Architecture

```
FrictionizerAccessibilityService   ← detects foreground app changes
  └─ TYPE_ACCESSIBILITY_OVERLAY    ← draws popup over any app
  └─ Room database                 ← records sessions locally

MainActivity                       ← hub / settings
AppSelectionActivity               ← choose monitored apps (includes system apps)
ActivitiesActivity                 ← manage the activity list
StatsActivity                      ← view time by app / activity
```

---

## Customisation & Logic

- **Forced Intentionality**: The **Go!** button is disabled until an activity is selected.
- **Skip Delay**: The **Skip** button only appears after a 2-second delay to ensure you don't bypass friction mindlessly.
- **Session Tracking**: Time spent in the background (during the grace period) is subtracted from session stats for accuracy.
- **Manage Activities**: Add/remove activities in the settings screen.

---

## Known limitations

- On some manufacturer ROMs (Xiaomi/MIUI, Samsung One UI), background services may be killed. Use the "Battery Optimization" shortcut in the app to exempt Frictionizer.
- The overlay is dismissed if you rotate the screen mid-popup (reopening the app shows it again).
