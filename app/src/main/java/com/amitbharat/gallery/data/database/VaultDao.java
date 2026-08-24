package com.amitbharat.gallery.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.amitbharat.gallery.data.models.VaultItem;
import java.util.List;

@Dao
public interface VaultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertVaultItem(VaultItem item);

    @Delete
    void deleteVaultItem(VaultItem item);

    @Query("DELETE FROM vault_items WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT * FROM vault_items ORDER BY dateHidden DESC")
    LiveData<List<VaultItem>> getAllVaultItemsLive();

    @Query("SELECT * FROM vault_items")
    List<VaultItem> getAllVaultItemsSync();
}
