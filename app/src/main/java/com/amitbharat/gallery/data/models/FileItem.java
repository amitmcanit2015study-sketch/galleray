package com.amitbharat.gallery.data.models;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;

public class FileItem implements Serializable {
    private String name;
    private String path;
    private boolean isDirectory;
    private long size;
    private long lastModified;
    private String extension;
    private boolean isHidden;
    private int childCount;
    private boolean isSelected;

    public FileItem() {}

    public FileItem(File file) {
        if (file != null) {
            this.name = file.getName();
            this.path = file.getAbsolutePath();
            this.isDirectory = file.isDirectory();
            this.size = file.isDirectory() ? 0 : file.length();
            this.lastModified = file.lastModified();
            this.isHidden = file.isHidden() || (this.name != null && this.name.startsWith("."));
            this.extension = extractExtension(this.name);
            if (this.isDirectory) {
                String[] list = file.list();
                this.childCount = list != null ? list.length : 0;
            } else {
                this.childCount = 0;
            }
        }
    }

    public FileItem(String name, String path, boolean isDirectory, long size, long lastModified, String extension, boolean isHidden, int childCount) {
        this.name = name;
        this.path = path;
        this.isDirectory = isDirectory;
        this.size = size;
        this.lastModified = lastModified;
        this.extension = extension;
        this.isHidden = isHidden;
        this.childCount = childCount;
        this.isSelected = false;
    }

    private String extractExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    public String getName() { return name != null ? name : ""; }
    public void setName(String name) { this.name = name; }

    public String getPath() { return path != null ? path : ""; }
    public void setPath(String path) { this.path = path; }

    public boolean isDirectory() { return isDirectory; }
    public void setDirectory(boolean directory) { isDirectory = directory; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public long getLastModified() { return lastModified; }
    public void setLastModified(long lastModified) { this.lastModified = lastModified; }

    public String getExtension() { return extension != null ? extension : ""; }
    public void setExtension(String extension) { this.extension = extension; }

    public boolean isHidden() { return isHidden; }
    public void setHidden(boolean hidden) { isHidden = hidden; }

    public int getChildCount() { return childCount; }
    public void setChildCount(int childCount) { this.childCount = childCount; }

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileItem)) return false;
        FileItem fileItem = (FileItem) o;
        return Objects.equals(path, fileItem.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}
