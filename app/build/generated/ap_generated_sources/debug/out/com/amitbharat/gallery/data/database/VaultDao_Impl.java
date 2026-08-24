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
import com.amitbharat.gallery.data.models.VaultItem;
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
public final class VaultDao_Impl implements VaultDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<VaultItem> __insertionAdapterOfVaultItem;

  private final EntityDeletionOrUpdateAdapter<VaultItem> __deletionAdapterOfVaultItem;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  public VaultDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfVaultItem = new EntityInsertionAdapter<VaultItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `vault_items` (`id`,`originalPath`,`vaultPath`,`originalName`,`mimeType`,`size`,`dateHidden`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final VaultItem entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getOriginalPath() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getOriginalPath());
        }
        if (entity.getVaultPath() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getVaultPath());
        }
        if (entity.getOriginalName() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getOriginalName());
        }
        if (entity.getMimeType() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getMimeType());
        }
        statement.bindLong(6, entity.getSize());
        statement.bindLong(7, entity.getDateHidden());
      }
    };
    this.__deletionAdapterOfVaultItem = new EntityDeletionOrUpdateAdapter<VaultItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `vault_items` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final VaultItem entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM vault_items WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public long insertVaultItem(final VaultItem item) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfVaultItem.insertAndReturnId(item);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteVaultItem(final VaultItem item) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfVaultItem.handle(item);
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
  public LiveData<List<VaultItem>> getAllVaultItemsLive() {
    final String _sql = "SELECT * FROM vault_items ORDER BY dateHidden DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"vault_items"}, false, new Callable<List<VaultItem>>() {
      @Override
      @Nullable
      public List<VaultItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfOriginalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "originalPath");
          final int _cursorIndexOfVaultPath = CursorUtil.getColumnIndexOrThrow(_cursor, "vaultPath");
          final int _cursorIndexOfOriginalName = CursorUtil.getColumnIndexOrThrow(_cursor, "originalName");
          final int _cursorIndexOfMimeType = CursorUtil.getColumnIndexOrThrow(_cursor, "mimeType");
          final int _cursorIndexOfSize = CursorUtil.getColumnIndexOrThrow(_cursor, "size");
          final int _cursorIndexOfDateHidden = CursorUtil.getColumnIndexOrThrow(_cursor, "dateHidden");
          final List<VaultItem> _result = new ArrayList<VaultItem>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final VaultItem _item;
            _item = new VaultItem();
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            _item.setId(_tmpId);
            final String _tmpOriginalPath;
            if (_cursor.isNull(_cursorIndexOfOriginalPath)) {
              _tmpOriginalPath = null;
            } else {
              _tmpOriginalPath = _cursor.getString(_cursorIndexOfOriginalPath);
            }
            _item.setOriginalPath(_tmpOriginalPath);
            final String _tmpVaultPath;
            if (_cursor.isNull(_cursorIndexOfVaultPath)) {
              _tmpVaultPath = null;
            } else {
              _tmpVaultPath = _cursor.getString(_cursorIndexOfVaultPath);
            }
            _item.setVaultPath(_tmpVaultPath);
            final String _tmpOriginalName;
            if (_cursor.isNull(_cursorIndexOfOriginalName)) {
              _tmpOriginalName = null;
            } else {
              _tmpOriginalName = _cursor.getString(_cursorIndexOfOriginalName);
            }
            _item.setOriginalName(_tmpOriginalName);
            final String _tmpMimeType;
            if (_cursor.isNull(_cursorIndexOfMimeType)) {
              _tmpMimeType = null;
            } else {
              _tmpMimeType = _cursor.getString(_cursorIndexOfMimeType);
            }
            _item.setMimeType(_tmpMimeType);
            final long _tmpSize;
            _tmpSize = _cursor.getLong(_cursorIndexOfSize);
            _item.setSize(_tmpSize);
            final long _tmpDateHidden;
            _tmpDateHidden = _cursor.getLong(_cursorIndexOfDateHidden);
            _item.setDateHidden(_tmpDateHidden);
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
  public List<VaultItem> getAllVaultItemsSync() {
    final String _sql = "SELECT * FROM vault_items";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfOriginalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "originalPath");
      final int _cursorIndexOfVaultPath = CursorUtil.getColumnIndexOrThrow(_cursor, "vaultPath");
      final int _cursorIndexOfOriginalName = CursorUtil.getColumnIndexOrThrow(_cursor, "originalName");
      final int _cursorIndexOfMimeType = CursorUtil.getColumnIndexOrThrow(_cursor, "mimeType");
      final int _cursorIndexOfSize = CursorUtil.getColumnIndexOrThrow(_cursor, "size");
      final int _cursorIndexOfDateHidden = CursorUtil.getColumnIndexOrThrow(_cursor, "dateHidden");
      final List<VaultItem> _result = new ArrayList<VaultItem>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final VaultItem _item;
        _item = new VaultItem();
        final long _tmpId;
        _tmpId = _cursor.getLong(_cursorIndexOfId);
        _item.setId(_tmpId);
        final String _tmpOriginalPath;
        if (_cursor.isNull(_cursorIndexOfOriginalPath)) {
          _tmpOriginalPath = null;
        } else {
          _tmpOriginalPath = _cursor.getString(_cursorIndexOfOriginalPath);
        }
        _item.setOriginalPath(_tmpOriginalPath);
        final String _tmpVaultPath;
        if (_cursor.isNull(_cursorIndexOfVaultPath)) {
          _tmpVaultPath = null;
        } else {
          _tmpVaultPath = _cursor.getString(_cursorIndexOfVaultPath);
        }
        _item.setVaultPath(_tmpVaultPath);
        final String _tmpOriginalName;
        if (_cursor.isNull(_cursorIndexOfOriginalName)) {
          _tmpOriginalName = null;
        } else {
          _tmpOriginalName = _cursor.getString(_cursorIndexOfOriginalName);
        }
        _item.setOriginalName(_tmpOriginalName);
        final String _tmpMimeType;
        if (_cursor.isNull(_cursorIndexOfMimeType)) {
          _tmpMimeType = null;
        } else {
          _tmpMimeType = _cursor.getString(_cursorIndexOfMimeType);
        }
        _item.setMimeType(_tmpMimeType);
        final long _tmpSize;
        _tmpSize = _cursor.getLong(_cursorIndexOfSize);
        _item.setSize(_tmpSize);
        final long _tmpDateHidden;
        _tmpDateHidden = _cursor.getLong(_cursorIndexOfDateHidden);
        _item.setDateHidden(_tmpDateHidden);
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
