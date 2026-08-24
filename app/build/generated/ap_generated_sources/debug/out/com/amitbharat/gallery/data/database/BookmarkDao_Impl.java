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
public final class BookmarkDao_Impl implements BookmarkDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<BookmarkEntity> __insertionAdapterOfBookmarkEntity;

  private final EntityDeletionOrUpdateAdapter<BookmarkEntity> __deletionAdapterOfBookmarkEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteByPath;

  public BookmarkDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfBookmarkEntity = new EntityInsertionAdapter<BookmarkEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `folder_bookmarks` (`folderPath`,`folderName`,`createdTimestamp`) VALUES (?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final BookmarkEntity entity) {
        if (entity.getFolderPath() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getFolderPath());
        }
        if (entity.getFolderName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getFolderName());
        }
        statement.bindLong(3, entity.getCreatedTimestamp());
      }
    };
    this.__deletionAdapterOfBookmarkEntity = new EntityDeletionOrUpdateAdapter<BookmarkEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `folder_bookmarks` WHERE `folderPath` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final BookmarkEntity entity) {
        if (entity.getFolderPath() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getFolderPath());
        }
      }
    };
    this.__preparedStmtOfDeleteByPath = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM folder_bookmarks WHERE folderPath = ?";
        return _query;
      }
    };
  }

  @Override
  public void insertBookmark(final BookmarkEntity entity) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfBookmarkEntity.insert(entity);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteBookmark(final BookmarkEntity entity) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfBookmarkEntity.handle(entity);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteByPath(final String path) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteByPath.acquire();
    int _argIndex = 1;
    if (path == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, path);
    }
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteByPath.release(_stmt);
    }
  }

  @Override
  public boolean isBookmarked(final String path) {
    final String _sql = "SELECT EXISTS(SELECT 1 FROM folder_bookmarks WHERE folderPath = ?)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (path == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, path);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final boolean _result;
      if (_cursor.moveToFirst()) {
        final int _tmp;
        _tmp = _cursor.getInt(0);
        _result = _tmp != 0;
      } else {
        _result = false;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public LiveData<List<BookmarkEntity>> getAllBookmarksLive() {
    final String _sql = "SELECT * FROM folder_bookmarks ORDER BY folderName ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"folder_bookmarks"}, false, new Callable<List<BookmarkEntity>>() {
      @Override
      @Nullable
      public List<BookmarkEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfFolderPath = CursorUtil.getColumnIndexOrThrow(_cursor, "folderPath");
          final int _cursorIndexOfFolderName = CursorUtil.getColumnIndexOrThrow(_cursor, "folderName");
          final int _cursorIndexOfCreatedTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "createdTimestamp");
          final List<BookmarkEntity> _result = new ArrayList<BookmarkEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BookmarkEntity _item;
            final String _tmpFolderPath;
            if (_cursor.isNull(_cursorIndexOfFolderPath)) {
              _tmpFolderPath = null;
            } else {
              _tmpFolderPath = _cursor.getString(_cursorIndexOfFolderPath);
            }
            final String _tmpFolderName;
            if (_cursor.isNull(_cursorIndexOfFolderName)) {
              _tmpFolderName = null;
            } else {
              _tmpFolderName = _cursor.getString(_cursorIndexOfFolderName);
            }
            final long _tmpCreatedTimestamp;
            _tmpCreatedTimestamp = _cursor.getLong(_cursorIndexOfCreatedTimestamp);
            _item = new BookmarkEntity(_tmpFolderPath,_tmpFolderName,_tmpCreatedTimestamp);
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
  public List<BookmarkEntity> getAllBookmarksSync() {
    final String _sql = "SELECT * FROM folder_bookmarks";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfFolderPath = CursorUtil.getColumnIndexOrThrow(_cursor, "folderPath");
      final int _cursorIndexOfFolderName = CursorUtil.getColumnIndexOrThrow(_cursor, "folderName");
      final int _cursorIndexOfCreatedTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "createdTimestamp");
      final List<BookmarkEntity> _result = new ArrayList<BookmarkEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final BookmarkEntity _item;
        final String _tmpFolderPath;
        if (_cursor.isNull(_cursorIndexOfFolderPath)) {
          _tmpFolderPath = null;
        } else {
          _tmpFolderPath = _cursor.getString(_cursorIndexOfFolderPath);
        }
        final String _tmpFolderName;
        if (_cursor.isNull(_cursorIndexOfFolderName)) {
          _tmpFolderName = null;
        } else {
          _tmpFolderName = _cursor.getString(_cursorIndexOfFolderName);
        }
        final long _tmpCreatedTimestamp;
        _tmpCreatedTimestamp = _cursor.getLong(_cursorIndexOfCreatedTimestamp);
        _item = new BookmarkEntity(_tmpFolderPath,_tmpFolderName,_tmpCreatedTimestamp);
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
