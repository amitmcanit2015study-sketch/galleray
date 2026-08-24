package com.amitbharat.gallery.data.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorites")
public class FavoriteEntity {
    @PrimaryKey
    @NonNull
    private String mediaPath;
    private long addedTimestamp;

    public FavoriteEntity(@NonNull String mediaPath, long addedTimestamp) {
        this.mediaPath = mediaPath;
        this.addedTimestamp = addedTimestamp;
    }

    @NonNull
    public String getMediaPath() { return mediaPath; }
    public void setMediaPath(@NonNull String mediaPath) { this.mediaPath = mediaPath; }

    public long getAddedTimestamp() { return addedTimestamp; }
    public void setAddedTimestamp(long addedTimestamp) { this.addedTimestamp = addedTimestamp; }
}
