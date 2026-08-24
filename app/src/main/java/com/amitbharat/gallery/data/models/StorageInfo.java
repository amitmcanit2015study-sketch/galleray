package com.amitbharat.gallery.data.models;

import java.io.Serializable;

public class StorageInfo implements Serializable {
    private String name;
    private String path;
    private long totalBytes;
    private long usedBytes;
    private long freeBytes;
    private boolean isPrimary;
    private boolean isRemovable;
    private boolean isUsb;

    public StorageInfo(String name, String path, long totalBytes, long freeBytes, boolean isPrimary, boolean isRemovable, boolean isUsb) {
        this.name = name;
        this.path = path;
        this.totalBytes = totalBytes;
        this.freeBytes = freeBytes;
        this.usedBytes = Math.max(0, totalBytes - freeBytes);
        this.isPrimary = isPrimary;
        this.isRemovable = isRemovable;
        this.isUsb = isUsb;
    }

    public String getName() { return name; }
    public String getPath() { return path; }
    public long getTotalBytes() { return totalBytes; }
    public long getUsedBytes() { return usedBytes; }
    public long getFreeBytes() { return freeBytes; }
    public boolean isPrimary() { return isPrimary; }
    public boolean isRemovable() { return isRemovable; }
    public boolean isUsb() { return isUsb; }

    public int getUsagePercentage() {
        if (totalBytes <= 0) return 0;
        return (int) ((usedBytes * 100) / totalBytes);
    }
}
