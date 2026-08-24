package com.amitbharat.gallery.utils;

import com.amitbharat.gallery.data.models.MediaItem;
import java.util.ArrayList;
import java.util.List;

public class MediaHolder {
    private static List<MediaItem> currentMediaList = new ArrayList<>();

    public static synchronized void setCurrentMediaList(List<MediaItem> list) {
        currentMediaList = (list != null) ? new ArrayList<>(list) : new ArrayList<>();
    }

    public static synchronized List<MediaItem> getCurrentMediaList() {
        return new ArrayList<>(currentMediaList);
    }

    public static synchronized void clear() {
        currentMediaList.clear();
    }
}
