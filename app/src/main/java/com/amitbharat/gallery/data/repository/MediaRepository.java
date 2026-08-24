package com.amitbharat.gallery.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.amitbharat.gallery.data.database.AppDatabase;
import com.amitbharat.gallery.data.database.FavoriteDao;
import com.amitbharat.gallery.data.database.FavoriteEntity;
import com.amitbharat.gallery.data.models.FilterOptions;
import com.amitbharat.gallery.data.models.FolderItem;
import com.amitbharat.gallery.data.models.MediaItem;
import com.amitbharat.gallery.utils.MediaUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MediaRepository {
    private final Context context;
    private final FavoriteDao favoriteDao;
    private final ExecutorService executor;

    private final MutableLiveData<List<MediaItem>> allMediaLive = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<FolderItem>> foldersLive = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoadingLive = new MutableLiveData<>(false);

    public MediaRepository(Context context) {
        this.context = context.getApplicationContext();
        this.favoriteDao = AppDatabase.getInstance(context).favoriteDao();
        this.executor = Executors.newFixedThreadPool(4);
    }

    public LiveData<List<MediaItem>> getAllMediaLive() { return allMediaLive; }
    public LiveData<List<FolderItem>> getFoldersLive() { return foldersLive; }
    public LiveData<Boolean> getIsLoadingLive() { return isLoadingLive; }

    public void loadMedia(final FilterOptions.SortOrder sortOrder) {
        isLoadingLive.postValue(true);
        executor.execute(() -> {
            try {
                List<MediaItem> items = MediaUtils.queryAllMedia(context);
                List<FavoriteEntity> favs = favoriteDao.getAllFavoritesSync();
                Set<String> favPaths = new HashSet<>();
                for (FavoriteEntity f : favs) {
                    favPaths.add(f.getMediaPath());
                }

                for (MediaItem item : items) {
                    if (item.getPath() != null && favPaths.contains(item.getPath())) {
                        item.setFavorite(true);
                    }
                }

                sortMediaList(items, sortOrder);

                allMediaLive.postValue(items);

                List<FolderItem> folders = MediaUtils.groupMediaByFolders(items);
                foldersLive.postValue(folders);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isLoadingLive.postValue(false);
            }
        });
    }

    public void toggleFavorite(MediaItem item) {
        if (item.getPath() == null) return;
        executor.execute(() -> {
            boolean isFav = favoriteDao.isFavorite(item.getPath());
            if (isFav) {
                favoriteDao.deleteByPath(item.getPath());
                item.setFavorite(false);
            } else {
                favoriteDao.insertFavorite(new FavoriteEntity(item.getPath(), System.currentTimeMillis()));
                item.setFavorite(true);
            }
            List<MediaItem> current = allMediaLive.getValue();
            if (current != null) {
                allMediaLive.postValue(new ArrayList<>(current));
            }
        });
    }

    public static void sortMediaList(List<MediaItem> list, FilterOptions.SortOrder sortOrder) {
        if (list == null) return;
        Collections.sort(list, (o1, o2) -> {
            switch (sortOrder) {
                case DATE_ASC:
                    return Long.compare(o1.getDateAdded(), o2.getDateAdded());
                case NAME_ASC:
                    return o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName());
                case NAME_DESC:
                    return o2.getDisplayName().compareToIgnoreCase(o1.getDisplayName());
                case SIZE_DESC:
                    return Long.compare(o2.getSize(), o1.getSize());
                case SIZE_ASC:
                    return Long.compare(o1.getSize(), o2.getSize());
                case DATE_DESC:
                default:
                    return Long.compare(o2.getDateAdded(), o1.getDateAdded());
            }
        });
    }
}
