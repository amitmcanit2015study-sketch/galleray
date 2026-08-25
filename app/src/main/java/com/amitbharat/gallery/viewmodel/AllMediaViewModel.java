package com.amitbharat.gallery.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.amitbharat.gallery.data.models.FilterOptions;
import com.amitbharat.gallery.data.models.MediaItem;
import com.amitbharat.gallery.data.repository.MediaRepository;
import java.util.ArrayList;
import java.util.List;

public class AllMediaViewModel extends AndroidViewModel {
    private final MediaRepository mediaRepository;
    private FilterOptions.SortOrder currentSort = FilterOptions.SortOrder.DATE_DESC;
    private final MutableLiveData<List<MediaItem>> selectedItems = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isSelectionMode = new MutableLiveData<>(false);

    public AllMediaViewModel(@NonNull Application application) {
        super(application);
        this.mediaRepository = new MediaRepository(application);
    }

    public LiveData<List<MediaItem>> getMediaList() {
        return mediaRepository.getAllMediaLive();
    }

    public LiveData<Boolean> getIsLoading() {
        return mediaRepository.getIsLoadingLive();
    }

    public LiveData<List<MediaItem>> getSelectedItems() {
        return selectedItems;
    }

    public LiveData<Boolean> getIsSelectionMode() {
        return isSelectionMode;
    }

    public void loadMedia() {
        mediaRepository.loadMedia(currentSort);
    }

    public void setSortOrder(FilterOptions.SortOrder sortOrder) {
        this.currentSort = sortOrder;
        mediaRepository.loadMedia(currentSort);
    }

    public void toggleFavorite(MediaItem item) {
        mediaRepository.toggleFavorite(item);
    }

    public void toggleSelection(MediaItem item) {
        List<MediaItem> current = selectedItems.getValue();
        if (current == null) current = new ArrayList<>();
        if (current.contains(item)) {
            current.remove(item);
            item.setSelected(false);
        } else {
            current.add(item);
            item.setSelected(true);
        }
        selectedItems.setValue(new ArrayList<>(current));
        isSelectionMode.setValue(!current.isEmpty());
    }

    public void selectAll(List<MediaItem> allItems) {
        if (allItems == null) return;
        List<MediaItem> selected = new ArrayList<>();
        for (MediaItem item : allItems) {
            item.setSelected(true);
            selected.add(item);
        }
        selectedItems.setValue(selected);
        isSelectionMode.setValue(!selected.isEmpty());
    }

    public void clearSelection() {
        List<MediaItem> all = mediaRepository.getAllMediaLive().getValue();
        if (all != null) {
            for (MediaItem m : all) {
                m.setSelected(false);
            }
        }
        List<MediaItem> current = selectedItems.getValue();
        if (current != null) {
            for (MediaItem item : current) {
                item.setSelected(false);
            }
        }
        selectedItems.setValue(new ArrayList<>());
        isSelectionMode.setValue(false);
    }
}
