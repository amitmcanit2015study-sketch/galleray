package com.amitbharat.gallery.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFavorite(FavoriteEntity entity);

    @Delete
    void deleteFavorite(FavoriteEntity entity);

    @Query("DELETE FROM favorites WHERE mediaPath = :path")
    void deleteByPath(String path);

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE mediaPath = :path)")
    boolean isFavorite(String path);

    @Query("SELECT * FROM favorites ORDER BY addedTimestamp DESC")
    LiveData<List<FavoriteEntity>> getAllFavoritesLive();

    @Query("SELECT * FROM favorites")
    List<FavoriteEntity> getAllFavoritesSync();
}
