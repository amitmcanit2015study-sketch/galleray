package com.amitbharat.gallery.data.database;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TrashDao_Impl implements TrashDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TrashEntity> __insertionAdapterOfTrashEntity;

  private final EntityDeletionOrUpdateAdapter<TrashEntity> __deletionAdapterOfTrashEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  private final SharedSQLiteStatement __preparedStmtOfClearAllTrash;

  public TrashDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTrashEntity = new EntityInsertionAdapter<TrashEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `recycle_bin` (`id`,`originalPath`,`trashPath`,`fileName`,`size`,`deletedTimestamp`,`isDirectory`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final TrashEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getOriginalPath() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getOriginalPath());
        }
        if (entity.getTrashPath() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getTrashPath());
        }
        if (entity.getFileName() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getFileName());
        }
        statement.bindLong(5, entity.getSize());
        statement.bindLong(6, entity.getDeletedTimestamp());
        final int _tmp = entity.isDirectory() ? 1 : 0;
        statement.bindLong(7, _tmp);
      }
    };
    this.__deletionAdapterOfTrashEntity = new EntityDeletionOrUpdateAdapter<TrashEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `recycle_bin` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final TrashEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM recycle_bin WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearAllTrash = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM recycle_bin";
        return _query;
      }
    };
  }

  @Override
  public long insertTrash(final TrashEntity entity) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfTrashEntity.insertAndReturnId(entity);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteTrash(final TrashEntity entity) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfTrashEntity.handle(entity);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteById(final long id) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, id);
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteById.release(_stmt);
    }
  }

  @Override
  public void clearAllTrash() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfClearAllTrash.acquire();
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfClearAllTrash.release(_stmt);
    }
  }

  @Override
  public LiveData<List<TrashEntity>> getAllTrashLive() {
    final String _sql = "SELECT * FROM recycle_bin ORDER BY deletedTimestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"recycle_bin"}, false, new Callable<List<TrashEntity>>() {
      @Override
      @Nullable
      public List<TrashEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfOriginalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "originalPath");
          final int _cursorIndexOfTrashPath = CursorUtil.getColumnIndexOrThrow(_cursor, "trashPath");
          final int _cursorIndexOfFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "fileName");
          final int _cursorIndexOfSize = CursorUtil.getColumnIndexOrThrow(_cursor, "size");
          final int _cursorIndexOfDeletedTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedTimestamp");
          final int _cursorIndexOfIsDirectory = CursorUtil.getColumnIndexOrThrow(_cursor, "isDirectory");
          final List<TrashEntity> _result = new ArrayList<TrashEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TrashEntity _item;
            final String _tmpOriginalPath;
            if (_cursor.isNull(_cursorIndexOfOriginalPath)) {
              _tmpOriginalPath = null;
            } else {
              _tmpOriginalPath = _cursor.getString(_cursorIndexOfOriginalPath);
            }
            final String _tmpTrashPath;
            if (_cursor.isNull(_cursorIndexOfTrashPath)) {
              _tmpTrashPath = null;
            } else {
              _tmpTrashPath = _cursor.getString(_cursorIndexOfTrashPath);
            }
            final String _tmpFileName;
            if (_cursor.isNull(_cursorIndexOfFileName)) {
              _tmpFileName = null;
            } else {
              _tmpFileName = _cursor.getString(_cursorIndexOfFileName);
            }
            final long _tmpSize;
            _tmpSize = _cursor.getLong(_cursorIndexOfSize);
            final long _tmpDeletedTimestamp;
            _tmpDeletedTimestamp = _cursor.getLong(_cursorIndexOfDeletedTimestamp);
            final boolean _tmpIsDirectory;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDirectory);
            _tmpIsDirectory = _tmp != 0;
            _item = new TrashEntity(_tmpOriginalPath,_tmpTrashPath,_tmpFileName,_tmpSize,_tmpDeletedTimestamp,_tmpIsDirectory);
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            _item.setId(_tmpId);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public List<TrashEntity> getAllTrashSync() {
    final String _sql = "SELECT * FROM recycle_bin";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfOriginalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "originalPath");
      final int _cursorIndexOfTrashPath = CursorUtil.getColumnIndexOrThrow(_cursor, "trashPath");
      final int _cursorIndexOfFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "fileName");
      final int _cursorIndexOfSize = CursorUtil.getColumnIndexOrThrow(_cursor, "size");
      final int _cursorIndexOfDeletedTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedTimestamp");
      final int _cursorIndexOfIsDirectory = CursorUtil.getColumnIndexOrThrow(_cursor, "isDirectory");
      final List<TrashEntity> _result = new ArrayList<TrashEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final TrashEntity _item;
        final String _tmpOriginalPath;
        if (_cursor.isNull(_cursorIndexOfOriginalPath)) {
          _tmpOriginalPath = null;
        } else {
          _tmpOriginalPath = _cursor.getString(_cursorIndexOfOriginalPath);
        }
        final String _tmpTrashPath;
        if (_cursor.isNull(_cursorIndexOfTrashPath)) {
          _tmpTrashPath = null;
        } else {
          _tmpTrashPath = _cursor.getString(_cursorIndexOfTrashPath);
        }
        final String _tmpFileName;
        if (_cursor.isNull(_cursorIndexOfFileName)) {
          _tmpFileName = null;
        } else {
          _tmpFileName = _cursor.getString(_cursorIndexOfFileName);
        }
        final long _tmpSize;
        _tmpSize = _cursor.getLong(_cursorIndexOfSize);
        final long _tmpDeletedTimestamp;
        _tmpDeletedTimestamp = _cursor.getLong(_cursorIndexOfDeletedTimestamp);
        final boolean _tmpIsDirectory;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsDirectory);
        _tmpIsDirectory = _tmp != 0;
        _item = new TrashEntity(_tmpOriginalPath,_tmpTrashPath,_tmpFileName,_tmpSize,_tmpDeletedTimestamp,_tmpIsDirectory);
        final long _tmpId;
        _tmpId = _cursor.getLong(_cursorIndexOfId);
        _item.setId(_tmpId);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
