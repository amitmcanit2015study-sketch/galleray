package com.amitbharat.gallery.data.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile FavoriteDao _favoriteDao;

  private volatile TrashDao _trashDao;

  private volatile BookmarkDao _bookmarkDao;

  private volatile VaultDao _vaultDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `favorites` (`mediaPath` TEXT NOT NULL, `addedTimestamp` INTEGER NOT NULL, PRIMARY KEY(`mediaPath`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `recycle_bin` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `originalPath` TEXT, `trashPath` TEXT, `fileName` TEXT, `size` INTEGER NOT NULL, `deletedTimestamp` INTEGER NOT NULL, `isDirectory` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `folder_bookmarks` (`folderPath` TEXT NOT NULL, `folderName` TEXT, `createdTimestamp` INTEGER NOT NULL, PRIMARY KEY(`folderPath`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `vault_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `originalPath` TEXT, `vaultPath` TEXT, `originalName` TEXT, `mimeType` TEXT, `size` INTEGER NOT NULL, `dateHidden` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '896c17727ec9681b8b90f13b96adddd6')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `favorites`");
        db.execSQL("DROP TABLE IF EXISTS `recycle_bin`");
        db.execSQL("DROP TABLE IF EXISTS `folder_bookmarks`");
        db.execSQL("DROP TABLE IF EXISTS `vault_items`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsFavorites = new HashMap<String, TableInfo.Column>(2);
        _columnsFavorites.put("mediaPath", new TableInfo.Column("mediaPath", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFavorites.put("addedTimestamp", new TableInfo.Column("addedTimestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysFavorites = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesFavorites = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoFavorites = new TableInfo("favorites", _columnsFavorites, _foreignKeysFavorites, _indicesFavorites);
        final TableInfo _existingFavorites = TableInfo.read(db, "favorites");
        if (!_infoFavorites.equals(_existingFavorites)) {
          return new RoomOpenHelper.ValidationResult(false, "favorites(com.amitbharat.gallery.data.database.FavoriteEntity).\n"
                  + " Expected:\n" + _infoFavorites + "\n"
                  + " Found:\n" + _existingFavorites);
        }
        final HashMap<String, TableInfo.Column> _columnsRecycleBin = new HashMap<String, TableInfo.Column>(7);
        _columnsRecycleBin.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecycleBin.put("originalPath", new TableInfo.Column("originalPath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecycleBin.put("trashPath", new TableInfo.Column("trashPath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecycleBin.put("fileName", new TableInfo.Column("fileName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecycleBin.put("size", new TableInfo.Column("size", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecycleBin.put("deletedTimestamp", new TableInfo.Column("deletedTimestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecycleBin.put("isDirectory", new TableInfo.Column("isDirectory", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysRecycleBin = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesRecycleBin = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoRecycleBin = new TableInfo("recycle_bin", _columnsRecycleBin, _foreignKeysRecycleBin, _indicesRecycleBin);
        final TableInfo _existingRecycleBin = TableInfo.read(db, "recycle_bin");
        if (!_infoRecycleBin.equals(_existingRecycleBin)) {
          return new RoomOpenHelper.ValidationResult(false, "recycle_bin(com.amitbharat.gallery.data.database.TrashEntity).\n"
                  + " Expected:\n" + _infoRecycleBin + "\n"
                  + " Found:\n" + _existingRecycleBin);
        }
        final HashMap<String, TableInfo.Column> _columnsFolderBookmarks = new HashMap<String, TableInfo.Column>(3);
        _columnsFolderBookmarks.put("folderPath", new TableInfo.Column("folderPath", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFolderBookmarks.put("folderName", new TableInfo.Column("folderName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFolderBookmarks.put("createdTimestamp", new TableInfo.Column("createdTimestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysFolderBookmarks = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesFolderBookmarks = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoFolderBookmarks = new TableInfo("folder_bookmarks", _columnsFolderBookmarks, _foreignKeysFolderBookmarks, _indicesFolderBookmarks);
        final TableInfo _existingFolderBookmarks = TableInfo.read(db, "folder_bookmarks");
        if (!_infoFolderBookmarks.equals(_existingFolderBookmarks)) {
          return new RoomOpenHelper.ValidationResult(false, "folder_bookmarks(com.amitbharat.gallery.data.database.BookmarkEntity).\n"
                  + " Expected:\n" + _infoFolderBookmarks + "\n"
                  + " Found:\n" + _existingFolderBookmarks);
        }
        final HashMap<String, TableInfo.Column> _columnsVaultItems = new HashMap<String, TableInfo.Column>(7);
        _columnsVaultItems.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaultItems.put("originalPath", new TableInfo.Column("originalPath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaultItems.put("vaultPath", new TableInfo.Column("vaultPath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaultItems.put("originalName", new TableInfo.Column("originalName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaultItems.put("mimeType", new TableInfo.Column("mimeType", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaultItems.put("size", new TableInfo.Column("size", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaultItems.put("dateHidden", new TableInfo.Column("dateHidden", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysVaultItems = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesVaultItems = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoVaultItems = new TableInfo("vault_items", _columnsVaultItems, _foreignKeysVaultItems, _indicesVaultItems);
        final TableInfo _existingVaultItems = TableInfo.read(db, "vault_items");
        if (!_infoVaultItems.equals(_existingVaultItems)) {
          return new RoomOpenHelper.ValidationResult(false, "vault_items(com.amitbharat.gallery.data.models.VaultItem).\n"
                  + " Expected:\n" + _infoVaultItems + "\n"
                  + " Found:\n" + _existingVaultItems);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "896c17727ec9681b8b90f13b96adddd6", "76fbf657c9b97bc665487ee8b99be235");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "favorites","recycle_bin","folder_bookmarks","vault_items");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `favorites`");
      _db.execSQL("DELETE FROM `recycle_bin`");
      _db.execSQL("DELETE FROM `folder_bookmarks`");
      _db.execSQL("DELETE FROM `vault_items`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(FavoriteDao.class, FavoriteDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TrashDao.class, TrashDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(BookmarkDao.class, BookmarkDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(VaultDao.class, VaultDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public FavoriteDao favoriteDao() {
    if (_favoriteDao != null) {
      return _favoriteDao;
    } else {
      synchronized(this) {
        if(_favoriteDao == null) {
          _favoriteDao = new FavoriteDao_Impl(this);
        }
        return _favoriteDao;
      }
    }
  }

  @Override
  public TrashDao trashDao() {
    if (_trashDao != null) {
      return _trashDao;
    } else {
      synchronized(this) {
        if(_trashDao == null) {
          _trashDao = new TrashDao_Impl(this);
        }
        return _trashDao;
      }
    }
  }

  @Override
  public BookmarkDao bookmarkDao() {
    if (_bookmarkDao != null) {
      return _bookmarkDao;
    } else {
      synchronized(this) {
        if(_bookmarkDao == null) {
          _bookmarkDao = new BookmarkDao_Impl(this);
        }
        return _bookmarkDao;
      }
    }
  }

  @Override
  public VaultDao vaultDao() {
    if (_vaultDao != null) {
      return _vaultDao;
    } else {
      synchronized(this) {
        if(_vaultDao == null) {
          _vaultDao = new VaultDao_Impl(this);
        }
        return _vaultDao;
      }
    }
  }
}
