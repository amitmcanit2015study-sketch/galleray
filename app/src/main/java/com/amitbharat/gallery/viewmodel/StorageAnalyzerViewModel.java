package com.amitbharat.gallery.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.amitbharat.gallery.data.models.FileItem;
import com.amitbharat.gallery.data.models.StorageCategory;
import com.amitbharat.gallery.data.repository.StorageRepository;
import java.util.List;

public class StorageAnalyzerViewModel extends AndroidViewModel {
    private final StorageRepository storageRepository;

    public StorageAnalyzerViewModel(@NonNull Application application) {
        super(application);
        this.storageRepository = new StorageRepository(application);
    }

    public LiveData<List<StorageCategory>> getCategoriesLive() { return storageRepository.getCategoriesLive(); }
    public LiveData<List<FileItem>> getLargeFilesLive() { return storageRepository.getLargeFilesLive(); }
    public LiveData<List<FileItem>> getDuplicateFilesLive() { return storageRepository.getDuplicateFilesLive(); }
    public LiveData<List<FileItem>> getEmptyFoldersLive() { return storageRepository.getEmptyFoldersLive(); }
    public LiveData<List<FileItem>> getCacheJunkLive() { return storageRepository.getCacheJunkLive(); }
    public LiveData<List<FileItem>> getApkJunkLive() { return storageRepository.getApkJunkLive(); }
    public LiveData<Boolean> getIsScanningLive() { return storageRepository.getIsScanningLive(); }

    public void analyze() {
        storageRepository.analyzeStorage();
    }

    public void cleanWasteFiles(List<FileItem> wasteItems, Runnable onComplete) {
        storageRepository.cleanWasteFiles(wasteItems, onComplete);
    }
}
