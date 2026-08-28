package com.frictionizer.app.utils;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0002\b\b\n\u0002\u0010!\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\"\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\n\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0014\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00050\u000e2\u0006\u0010\u000f\u001a\u00020\u0010J\u000e\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u000f\u001a\u00020\u0010J\u000e\u0010\u0013\u001a\u00020\u00122\u0006\u0010\u000f\u001a\u00020\u0010J\u000e\u0010\u0014\u001a\u00020\u00122\u0006\u0010\u000f\u001a\u00020\u0010J\u000e\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u000f\u001a\u00020\u0010J\u000e\u0010\u0017\u001a\u00020\u00122\u0006\u0010\u000f\u001a\u00020\u0010J\u0014\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00050\u00192\u0006\u0010\u000f\u001a\u00020\u0010J\u000e\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u000f\u001a\u00020\u0010J\u000e\u0010\u001c\u001a\u00020\u001b2\u0006\u0010\u000f\u001a\u00020\u0010J\u000e\u0010\u001d\u001a\u00020\u001e2\u0006\u0010\u000f\u001a\u00020\u0010J\u001c\u0010\u001f\u001a\u00020\u001e2\u0006\u0010\u000f\u001a\u00020\u00102\f\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00050\u0004J\u001e\u0010!\u001a\u00020\u001e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\"\u001a\u00020\u00122\u0006\u0010#\u001a\u00020\u0012J\u0016\u0010$\u001a\u00020\u001e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010%\u001a\u00020\u001bJ\u001c\u0010&\u001a\u00020\u001e2\u0006\u0010\u000f\u001a\u00020\u00102\f\u0010\'\u001a\b\u0012\u0004\u0012\u00020\u00050\u0019R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006("}, d2 = {"Lcom/frictionizer/app/utils/PrefsHelper;", "", "()V", "DEFAULT_ACTIVITIES", "", "", "KEY_ACTIVITIES", "KEY_BEDTIME_ENABLED", "KEY_BEDTIME_HOUR", "KEY_BEDTIME_MINUTE", "KEY_COUNTDOWN_START", "KEY_MONITORED_APPS", "PREFS", "getActivities", "", "context", "Landroid/content/Context;", "getBedtimeHour", "", "getBedtimeMinute", "getCountdownSeconds", "getCountdownStartDate", "", "getEffectiveCountdownSeconds", "getMonitoredApps", "", "isBedtimeEnabled", "", "isInBedtimeWindow", "resetCountdown", "", "setActivities", "activities", "setBedtime", "hour", "minute", "setBedtimeEnabled", "enabled", "setMonitoredApps", "apps", "app_debug"})
public final class PrefsHelper {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS = "frictionizer_prefs";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_MONITORED_APPS = "monitored_apps";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_ACTIVITIES = "activities";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_COUNTDOWN_START = "countdown_start_ms";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_BEDTIME_ENABLED = "bedtime_enabled";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_BEDTIME_HOUR = "bedtime_hour";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_BEDTIME_MINUTE = "bedtime_minute";
    @org.jetbrains.annotations.NotNull()
    private static final java.util.List<java.lang.String> DEFAULT_ACTIVITIES = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.frictionizer.app.utils.PrefsHelper INSTANCE = null;
    
    private PrefsHelper() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Set<java.lang.String> getMonitoredApps(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    public final void setMonitoredApps(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.util.Set<java.lang.String> apps) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> getActivities(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    public final void setActivities(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> activities) {
    }
    
    /**
     * Returns or lazily creates the countdown start date (ms since epoch).
     */
    public final long getCountdownStartDate(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return 0L;
    }
    
    /**
     * Day 1 = 1 s, Day 2 = 2 s … capped at 60 s (day 60+).
     */
    public final int getCountdownSeconds(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return 0;
    }
    
    /**
     * Reset: start counting from today again.
     */
    public final void resetCountdown(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    public final boolean isBedtimeEnabled(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    public final void setBedtimeEnabled(@org.jetbrains.annotations.NotNull()
    android.content.Context context, boolean enabled) {
    }
    
    public final int getBedtimeHour(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return 0;
    }
    
    public final int getBedtimeMinute(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return 0;
    }
    
    public final void setBedtime(@org.jetbrains.annotations.NotNull()
    android.content.Context context, int hour, int minute) {
    }
    
    /**
     * Returns true if current time is within 2 hours before the set bedtime.
     * e.g. bedtime = 22:30 → window is 20:30–22:30
     */
    public final boolean isInBedtimeWindow(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    /**
     * Returns the effective countdown seconds, applying bedtime minimum.
     */
    public final int getEffectiveCountdownSeconds(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return 0;
    }
}