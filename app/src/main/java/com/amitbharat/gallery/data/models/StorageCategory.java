package com.amitbharat.gallery.data.models;

import java.io.Serializable;

public class StorageCategory implements Serializable {
    public enum CategoryType {
        IMAGES, VIDEOS, AUDIO, DOCUMENTS, APKS, ARCHIVES, OTHER, LARGE_FILES, DUPLICATES, EMPTY_FOLDERS
    }

    private CategoryType type;
    private String name;
    private int iconRes;
    private int colorRes;
    private long totalBytes;
    private int itemCount;

    public StorageCategory(CategoryType type, String name, int iconRes, int colorRes, long totalBytes, int itemCount) {
        this.type = type;
        this.name = name;
        this.iconRes = iconRes;
        this.colorRes = colorRes;
        this.totalBytes = totalBytes;
        this.itemCount = itemCount;
    }

    public CategoryType getType() { return type; }
    public String getName() { return name; }
    public int getIconRes() { return iconRes; }
    public int getColorRes() { return colorRes; }
    public long getTotalBytes() { return totalBytes; }
    public void setTotalBytes(long totalBytes) { this.totalBytes = totalBytes; }
    public int getItemCount() { return itemCount; }
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }
}
