package com.amitbharat.gallery.data.models;

import android.net.Uri;
import java.io.File;
import java.io.Serializable;
import java.util.Objects;

public class FolderItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private long bucketId;
    private String folderName;
    private String folderPath;
    private transient Uri coverUri;
    private String coverUriString;
    private String coverPath;
    private int fileCount;
    private long totalSize;
    private long lastModified;
    private boolean isVideoFolder;

    public FolderItem() {
    }

    public FolderItem(long bucketId, String folderName, String folderPath, Uri coverUri,
                      String coverPath, int fileCount, long totalSize, long lastModified) {
        this.bucketId = bucketId;
        this.folderName = folderName;
        this.folderPath = folderPath;
        this.coverUri = coverUri;
        this.coverUriString = coverUri != null ? coverUri.toString() : null;
        this.coverPath = coverPath;
        this.fileCount = fileCount;
        this.totalSize = totalSize;
        this.lastModified = lastModified;
    }

    public long getBucketId() { return bucketId; }
    public void setBucketId(long bucketId) { this.bucketId = bucketId; }

    public String getFolderName() { return folderName != null ? folderName : ""; }
    public void setFolderName(String folderName) { this.folderName = folderName; }

    public String getFolderPath() { return folderPath != null ? folderPath : ""; }
    public void setFolderPath(String folderPath) { this.folderPath = folderPath; }

    public Uri getCoverUri() {
        if (coverUri != null) return coverUri;
        if (coverUriString != null && !coverUriString.isEmpty()) {
            coverUri = Uri.parse(coverUriString);
            return coverUri;
        }
        if (coverPath != null && !coverPath.isEmpty()) {
            coverUri = Uri.fromFile(new File(coverPath));
            return coverUri;
        }
        return null;
    }

    public void setCoverUri(Uri coverUri) {
        this.coverUri = coverUri;
        this.coverUriString = coverUri != null ? coverUri.toString() : null;
    }

    public String getCoverUriString() { return coverUriString; }
    public void setCoverUriString(String coverUriString) {
        this.coverUriString = coverUriString;
        this.coverUri = coverUriString != null ? Uri.parse(coverUriString) : null;
    }

    public String getCoverPath() { return coverPath; }
    public void setCoverPath(String coverPath) { this.coverPath = coverPath; }

    public int getFileCount() { return fileCount; }
    public void setFileCount(int fileCount) { this.fileCount = fileCount; }

    public long getTotalSize() { return totalSize; }
    public void setTotalSize(long totalSize) { this.totalSize = totalSize; }

    public long getLastModified() { return lastModified; }
    public void setLastModified(long lastModified) { this.lastModified = lastModified; }

    public boolean isVideoFolder() { return isVideoFolder; }
    public void setVideoFolder(boolean videoFolder) { isVideoFolder = videoFolder; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FolderItem)) return false;
        FolderItem that = (FolderItem) o;
        return bucketId == that.bucketId && Objects.equals(folderPath, that.folderPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bucketId, folderPath);
    }
}
