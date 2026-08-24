package com.amitbharat.gallery.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBookmark(BookmarkEntity entity);

    @Delete
    void deleteBookmark(BookmarkEntity entity);

    @Query("DELETE FROM folder_bookmarks WHERE folderPath = :path")
    void deleteByPath(String path);

    @Query("SELECT EXISTS(SELECT 1 FROM folder_bookmarks WHERE folderPath = :path)")
    boolean isBookmarked(String path);

    @Query("SELECT * FROM folder_bookmarks ORDER BY folderName ASC")
    LiveData<List<BookmarkEntity>> getAllBookmarksLive();

    @Query("SELECT * FROM folder_bookmarks")
    List<BookmarkEntity> getAllBookmarksSync();
}
