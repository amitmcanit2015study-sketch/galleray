package com.amitbharat.gallery.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "vault_items")
public class VaultItem implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String originalPath;
    private String vaultPath;
    private String originalName;
    private String mimeType;
    private long size;
    private long dateHidden;

    public VaultItem() {}

    @androidx.room.Ignore
    public VaultItem(String originalPath, String vaultPath, String originalName, String mimeType, long size, long dateHidden) {
        this.originalPath = originalPath;
        this.vaultPath = vaultPath;
        this.originalName = originalName;
        this.mimeType = mimeType;
        this.size = size;
        this.dateHidden = dateHidden;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getOriginalPath() { return originalPath; }
    public void setOriginalPath(String originalPath) { this.originalPath = originalPath; }

    public String getVaultPath() { return vaultPath; }
    public void setVaultPath(String vaultPath) { this.vaultPath = vaultPath; }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public long getDateHidden() { return dateHidden; }
    public void setDateHidden(long dateHidden) { this.dateHidden = dateHidden; }
}
