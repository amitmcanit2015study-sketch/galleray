package com.amitbharat.gallery.data.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "folder_bookmarks")
public class BookmarkEntity {
    @PrimaryKey
    @NonNull
    private String folderPath;
    private String folderName;
    private long createdTimestamp;

    public BookmarkEntity(@NonNull String folderPath, String folderName, long createdTimestamp) {
        this.folderPath = folderPath;
        this.folderName = folderName;
        this.createdTimestamp = createdTimestamp;
    }

    @NonNull
    public String getFolderPath() { return folderPath; }
    public void setFolderPath(@NonNull String folderPath) { this.folderPath = folderPath; }

    public String getFolderName() { return folderName; }
    public void setFolderName(String folderName) { this.folderName = folderName; }

    public long getCreatedTimestamp() { return createdTimestamp; }
    public void setCreatedTimestamp(long createdTimestamp) { this.createdTimestamp = createdTimestamp; }
}
