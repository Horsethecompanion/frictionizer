package com.frictionizer.app.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SessionDao_Impl implements SessionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Session> __insertionAdapterOfSession;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public SessionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSession = new EntityInsertionAdapter<Session>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `sessions` (`id`,`packageName`,`appLabel`,`activityName`,`startTime`,`durationMs`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Session entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getPackageName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getPackageName());
        }
        if (entity.getAppLabel() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getAppLabel());
        }
        if (entity.getActivityName() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getActivityName());
        }
        statement.bindLong(5, entity.getStartTime());
        statement.bindLong(6, entity.getDurationMs());
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM sessions";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final Session session, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSession.insert(session);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllSessions(final Continuation<? super List<Session>> $completion) {
    final String _sql = "SELECT * FROM sessions ORDER BY startTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Session>>() {
      @Override
      @NonNull
      public List<Session> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfAppLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "appLabel");
          final int _cursorIndexOfActivityName = CursorUtil.getColumnIndexOrThrow(_cursor, "activityName");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMs");
          final List<Session> _result = new ArrayList<Session>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Session _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpPackageName;
            if (_cursor.isNull(_cursorIndexOfPackageName)) {
              _tmpPackageName = null;
            } else {
              _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            }
            final String _tmpAppLabel;
            if (_cursor.isNull(_cursorIndexOfAppLabel)) {
              _tmpAppLabel = null;
            } else {
              _tmpAppLabel = _cursor.getString(_cursorIndexOfAppLabel);
            }
            final String _tmpActivityName;
            if (_cursor.isNull(_cursorIndexOfActivityName)) {
              _tmpActivityName = null;
            } else {
              _tmpActivityName = _cursor.getString(_cursorIndexOfActivityName);
            }
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final long _tmpDurationMs;
            _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            _item = new Session(_tmpId,_tmpPackageName,_tmpAppLabel,_tmpActivityName,_tmpStartTime,_tmpDurationMs);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getTotalByApp(final Continuation<? super List<AppTotal>> $completion) {
    final String _sql = "SELECT packageName, appLabel, SUM(durationMs) as totalMs FROM sessions GROUP BY packageName ORDER BY totalMs DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AppTotal>>() {
      @Override
      @NonNull
      public List<AppTotal> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPackageName = 0;
          final int _cursorIndexOfAppLabel = 1;
          final int _cursorIndexOfTotalMs = 2;
          final List<AppTotal> _result = new ArrayList<AppTotal>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AppTotal _item;
            final String _tmpPackageName;
            if (_cursor.isNull(_cursorIndexOfPackageName)) {
              _tmpPackageName = null;
            } else {
              _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            }
            final String _tmpAppLabel;
            if (_cursor.isNull(_cursorIndexOfAppLabel)) {
              _tmpAppLabel = null;
            } else {
              _tmpAppLabel = _cursor.getString(_cursorIndexOfAppLabel);
            }
            final long _tmpTotalMs;
            _tmpTotalMs = _cursor.getLong(_cursorIndexOfTotalMs);
            _item = new AppTotal(_tmpPackageName,_tmpAppLabel,_tmpTotalMs);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getTotalByActivity(final Continuation<? super List<ActivityTotal>> $completion) {
    final String _sql = "SELECT activityName, SUM(durationMs) as totalMs FROM sessions GROUP BY activityName ORDER BY totalMs DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ActivityTotal>>() {
      @Override
      @NonNull
      public List<ActivityTotal> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfActivityName = 0;
          final int _cursorIndexOfTotalMs = 1;
          final List<ActivityTotal> _result = new ArrayList<ActivityTotal>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ActivityTotal _item;
            final String _tmpActivityName;
            if (_cursor.isNull(_cursorIndexOfActivityName)) {
              _tmpActivityName = null;
            } else {
              _tmpActivityName = _cursor.getString(_cursorIndexOfActivityName);
            }
            final long _tmpTotalMs;
            _tmpTotalMs = _cursor.getLong(_cursorIndexOfTotalMs);
            _item = new ActivityTotal(_tmpActivityName,_tmpTotalMs);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getDetailedBreakdown(
      final Continuation<? super List<AppActivityTotal>> $completion) {
    final String _sql = "SELECT packageName, appLabel, activityName, SUM(durationMs) as totalMs FROM sessions GROUP BY packageName, activityName ORDER BY packageName, totalMs DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AppActivityTotal>>() {
      @Override
      @NonNull
      public List<AppActivityTotal> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPackageName = 0;
          final int _cursorIndexOfAppLabel = 1;
          final int _cursorIndexOfActivityName = 2;
          final int _cursorIndexOfTotalMs = 3;
          final List<AppActivityTotal> _result = new ArrayList<AppActivityTotal>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AppActivityTotal _item;
            final String _tmpPackageName;
            if (_cursor.isNull(_cursorIndexOfPackageName)) {
              _tmpPackageName = null;
            } else {
              _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            }
            final String _tmpAppLabel;
            if (_cursor.isNull(_cursorIndexOfAppLabel)) {
              _tmpAppLabel = null;
            } else {
              _tmpAppLabel = _cursor.getString(_cursorIndexOfAppLabel);
            }
            final String _tmpActivityName;
            if (_cursor.isNull(_cursorIndexOfActivityName)) {
              _tmpActivityName = null;
            } else {
              _tmpActivityName = _cursor.getString(_cursorIndexOfActivityName);
            }
            final long _tmpTotalMs;
            _tmpTotalMs = _cursor.getLong(_cursorIndexOfTotalMs);
            _item = new AppActivityTotal(_tmpPackageName,_tmpAppLabel,_tmpActivityName,_tmpTotalMs);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
