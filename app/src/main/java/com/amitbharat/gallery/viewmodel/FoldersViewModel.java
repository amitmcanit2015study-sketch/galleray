package com.amitbharat.gallery.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.amitbharat.gallery.data.models.FilterOptions;
import com.amitbharat.gallery.data.models.FolderItem;
import com.amitbharat.gallery.data.models.MediaItem;
import com.amitbharat.gallery.data.repository.MediaRepository;
import java.util.List;

public class FoldersViewModel extends AndroidViewModel {
    private final MediaRepository mediaRepository;

    public FoldersViewModel(@NonNull Application application) {
        super(application);
        this.mediaRepository = new MediaRepository(application);
    }

    public LiveData<List<FolderItem>> getFoldersList() {
        return mediaRepository.getFoldersLive();
    }

    public LiveData<List<MediaItem>> getAllMedia() {
        return mediaRepository.getAllMediaLive();
    }

    public LiveData<Boolean> getIsLoading() {
        return mediaRepository.getIsLoadingLive();
    }

    public void loadFolders() {
        mediaRepository.loadMedia(FilterOptions.SortOrder.DATE_DESC);
    }
}
