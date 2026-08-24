package com.amitbharat.gallery.data.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "recycle_bin")
public class TrashEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String originalPath;
    private String trashPath;
    private String fileName;
    private long size;
    private long deletedTimestamp;
    private boolean isDirectory;

    public TrashEntity(String originalPath, String trashPath, String fileName, long size, long deletedTimestamp, boolean isDirectory) {
        this.originalPath = originalPath;
        this.trashPath = trashPath;
        this.fileName = fileName;
        this.size = size;
        this.deletedTimestamp = deletedTimestamp;
        this.isDirectory = isDirectory;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getOriginalPath() { return originalPath; }
    public void setOriginalPath(String originalPath) { this.originalPath = originalPath; }

    public String getTrashPath() { return trashPath; }
    public void setTrashPath(String trashPath) { this.trashPath = trashPath; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public long getDeletedTimestamp() { return deletedTimestamp; }
    public void setDeletedTimestamp(long deletedTimestamp) { this.deletedTimestamp = deletedTimestamp; }

    public boolean isDirectory() { return isDirectory; }
    public void setDirectory(boolean directory) { isDirectory = directory; }
}
