package com.amitbharat.gallery.data.models;

import android.net.Uri;
import java.io.File;
import java.io.Serializable;
import java.util.Objects;

public class MediaItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private transient Uri uri;
    private String uriString;
    private String path;
    private String displayName;
    private long size;
    private String mimeType;
    private long dateAdded;
    private long dateModified;
    private long duration; // In milliseconds for videos
    private int width;
    private int height;
    private boolean isVideo;
    private boolean isFavorite;
    private boolean isSelected;
    private long bucketId;
    private String bucketDisplayName;

    public MediaItem() {
    }

    public MediaItem(long id, Uri uri, String path, String displayName, long size, String mimeType,
                     long dateAdded, long dateModified, long duration, int width, int height,
                     boolean isVideo, long bucketId, String bucketDisplayName) {
        this.id = id;
        this.uri = uri;
        this.uriString = uri != null ? uri.toString() : null;
        this.path = path;
        this.displayName = displayName;
        this.size = size;
        this.mimeType = mimeType;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
        this.duration = duration;
        this.width = width;
        this.height = height;
        this.isVideo = isVideo;
        this.bucketId = bucketId;
        this.bucketDisplayName = bucketDisplayName;
        this.isFavorite = false;
        this.isSelected = false;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public Uri getUri() {
        if (uri != null) return uri;
        if (uriString != null && !uriString.isEmpty()) {
            uri = Uri.parse(uriString);
            return uri;
        }
        if (path != null && !path.isEmpty()) {
            uri = Uri.fromFile(new File(path));
            return uri;
        }
        return null;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
        this.uriString = uri != null ? uri.toString() : null;
    }

    public String getUriString() { return uriString; }
    public void setUriString(String uriString) {
        this.uriString = uriString;
        this.uri = uriString != null ? Uri.parse(uriString) : null;
    }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getDisplayName() { return displayName != null ? displayName : ""; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public String getMimeType() { return mimeType != null ? mimeType : ""; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public long getDateAdded() { return dateAdded; }
    public void setDateAdded(long dateAdded) { this.dateAdded = dateAdded; }

    public long getDateModified() { return dateModified; }
    public void setDateModified(long dateModified) { this.dateModified = dateModified; }

    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public boolean isVideo() { return isVideo; }
    public void setVideo(boolean video) { isVideo = video; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }

    public long getBucketId() { return bucketId; }
    public void setBucketId(long bucketId) { this.bucketId = bucketId; }

    public String getBucketDisplayName() { return bucketDisplayName != null ? bucketDisplayName : ""; }
    public void setBucketDisplayName(String bucketDisplayName) { this.bucketDisplayName = bucketDisplayName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MediaItem)) return false;
        MediaItem mediaItem = (MediaItem) o;
        return id == mediaItem.id && Objects.equals(path, mediaItem.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, path);
    }
}
