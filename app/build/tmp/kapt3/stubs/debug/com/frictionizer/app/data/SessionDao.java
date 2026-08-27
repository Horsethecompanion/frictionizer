package com.frictionizer.app.data;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\bg\u0018\u00002\u00020\u0001J\u000e\u0010\u0002\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\r0\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u000e\u001a\u00020\u00032\u0006\u0010\u000f\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\u0010\u00a8\u0006\u0011"}, d2 = {"Lcom/frictionizer/app/data/SessionDao;", "", "deleteAll", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllSessions", "", "Lcom/frictionizer/app/data/Session;", "getDetailedBreakdown", "Lcom/frictionizer/app/data/AppActivityTotal;", "getTotalByActivity", "Lcom/frictionizer/app/data/ActivityTotal;", "getTotalByApp", "Lcom/frictionizer/app/data/AppTotal;", "insert", "session", "(Lcom/frictionizer/app/data/Session;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
@androidx.room.Dao()
public abstract interface SessionDao {
    
    @androidx.room.Insert()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insert(@org.jetbrains.annotations.NotNull()
    com.frictionizer.app.data.Session session, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM sessions ORDER BY startTime DESC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getAllSessions(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.frictionizer.app.data.Session>> $completion);
    
    @androidx.room.Query(value = "SELECT packageName, appLabel, SUM(durationMs) as totalMs FROM sessions GROUP BY packageName ORDER BY totalMs DESC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getTotalByApp(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.frictionizer.app.data.AppTotal>> $completion);
    
    @androidx.room.Query(value = "SELECT activityName, SUM(durationMs) as totalMs FROM sessions GROUP BY activityName ORDER BY totalMs DESC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getTotalByActivity(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.frictionizer.app.data.ActivityTotal>> $completion);
    
    @androidx.room.Query(value = "SELECT packageName, appLabel, activityName, SUM(durationMs) as totalMs FROM sessions GROUP BY packageName, activityName ORDER BY packageName, totalMs DESC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getDetailedBreakdown(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.frictionizer.app.data.AppActivityTotal>> $completion);
    
    @androidx.room.Query(value = "DELETE FROM sessions")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteAll(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}