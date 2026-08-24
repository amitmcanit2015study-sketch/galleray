package com.amitbharat.gallery.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface TrashDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTrash(TrashEntity entity);

    @Delete
    void deleteTrash(TrashEntity entity);

    @Query("DELETE FROM recycle_bin WHERE id = :id")
    void deleteById(long id);

    @Query("DELETE FROM recycle_bin")
    void clearAllTrash();

    @Query("SELECT * FROM recycle_bin ORDER BY deletedTimestamp DESC")
    LiveData<List<TrashEntity>> getAllTrashLive();

    @Query("SELECT * FROM recycle_bin")
    List<TrashEntity> getAllTrashSync();
}
