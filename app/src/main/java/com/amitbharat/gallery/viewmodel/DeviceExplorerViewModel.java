package com.amitbharat.gallery.viewmodel;

import android.app.Application;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.amitbharat.gallery.data.models.FileItem;
import com.amitbharat.gallery.data.models.FilterOptions;
import com.amitbharat.gallery.data.models.StorageInfo;
import com.amitbharat.gallery.data.repository.FileRepository;
import com.amitbharat.gallery.utils.StorageUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class DeviceExplorerViewModel extends AndroidViewModel {
    private final FileRepository fileRepository;
    private final MutableLiveData<List<StorageInfo>> storageVolumesLive = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> currentPathLive = new MutableLiveData<>("");
    private final MutableLiveData<List<String>> breadcrumbsLive = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<FileItem>> selectedFiles = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isSelectionMode = new MutableLiveData<>(false);
    private FilterOptions.SortOrder currentSort = FilterOptions.SortOrder.NAME_ASC;
    private final Stack<String> pathHistory = new Stack<>();

    public DeviceExplorerViewModel(@NonNull Application application) {
        super(application);
        this.fileRepository = new FileRepository(application);
    }

    public LiveData<List<StorageInfo>> getStorageVolumesLive() { return storageVolumesLive; }
    public LiveData<List<FileItem>> getCurrentFilesLive() { return fileRepository.getCurrentFilesLive(); }
    public LiveData<Boolean> getIsLoadingLive() { return fileRepository.getIsLoadingLive(); }
    public LiveData<String> getCurrentPathLive() { return currentPathLive; }
    public LiveData<List<String>> getBreadcrumbsLive() { return breadcrumbsLive; }
    public LiveData<List<FileItem>> getSelectedFiles() { return selectedFiles; }
    public LiveData<Boolean> getIsSelectionMode() { return isSelectionMode; }

    public void loadStorageVolumes() {
        List<StorageInfo> volumes = StorageUtils.getStorageVolumes(getApplication());
        storageVolumesLive.setValue(volumes);
    }

    public void navigateTo(String path) {
        if (path == null || path.isEmpty()) return;
        if (currentPathLive.getValue() != null && !currentPathLive.getValue().isEmpty()) {
            pathHistory.push(currentPathLive.getValue());
        }
        openPath(path);
    }

    public void openPath(String path) {
        currentPathLive.setValue(path);
        updateBreadcrumbs(path);
        fileRepository.loadDirectory(path, currentSort);
        clearSelection();
    }

    private final List<String> breadcrumbPaths = new ArrayList<>();

    public void openCategory(String categoryKey, String displayName) {
        if (currentPathLive.getValue() != null && !currentPathLive.getValue().isEmpty()) {
            pathHistory.push(currentPathLive.getValue());
        }
        currentPathLive.setValue("category:" + categoryKey);
        List<String> crumbs = new ArrayList<>();
        crumbs.add("Storage");
        crumbs.add(displayName);
        breadcrumbsLive.setValue(crumbs);
        breadcrumbPaths.clear();
        File root = Environment.getExternalStorageDirectory();
        if (root != null) {
            breadcrumbPaths.add(root.getAbsolutePath());
        } else {
            breadcrumbPaths.add("");
        }
        breadcrumbPaths.add("category:" + categoryKey);
        fileRepository.loadCategoryFiles(categoryKey, currentSort);
        clearSelection();
    }

    public void toggleHiddenFiles() {
        com.amitbharat.gallery.utils.PreferencesManager prefs = com.amitbharat.gallery.utils.PreferencesManager.getInstance(getApplication());
        boolean current = prefs.isShowHiddenFiles();
        prefs.setShowHiddenFiles(!current);
        refresh();
    }

    public boolean navigateToBreadcrumb(int position) {
        if (position >= 0 && position < breadcrumbPaths.size()) {
            String targetPath = breadcrumbPaths.get(position);
            String current = currentPathLive.getValue();
            if (targetPath != null && !targetPath.equals(current)) {
                if (current != null && !current.isEmpty()) {
                    pathHistory.push(current);
                }
                if (targetPath.startsWith("category:")) {
                    String cat = targetPath.replace("category:", "");
                    openCategory(cat, cat);
                } else {
                    openPath(targetPath);
                }
                return true;
            }
        }
        return false;
    }

    public boolean navigateBack() {
        if (!pathHistory.isEmpty()) {
            String prev = pathHistory.pop();
            openPath(prev);
            return true;
        }
        String current = currentPathLive.getValue();
        File root = Environment.getExternalStorageDirectory();
        if (current != null && !current.isEmpty() && !current.startsWith("category:")) {
            File curFile = new File(current);
            if (root != null && !curFile.getAbsolutePath().equals(root.getAbsolutePath())) {
                File parent = curFile.getParentFile();
                if (parent != null) {
                    openPath(parent.getAbsolutePath());
                    return true;
                }
            }
        } else if (current != null && current.startsWith("category:")) {
            if (root != null) {
                openPath(root.getAbsolutePath());
                return true;
            }
        }
        return false;
    }

    public void refresh() {
        String path = currentPathLive.getValue();
        if (path != null && !path.isEmpty()) {
            if (path.startsWith("category:")) {
                String cat = path.replace("category:", "");
                fileRepository.loadCategoryFiles(cat, currentSort);
            } else {
                fileRepository.loadDirectory(path, currentSort);
            }
        }
        loadStorageVolumes();
    }

    public void setSortOrder(FilterOptions.SortOrder sortOrder) {
        this.currentSort = sortOrder;
        refresh();
    }

    private void updateBreadcrumbs(String path) {
        List<String> crumbs = new ArrayList<>();
        breadcrumbPaths.clear();
        File file = new File(path);
        File root = Environment.getExternalStorageDirectory();
        List<File> fileList = new ArrayList<>();
        while (file != null) {
            fileList.add(0, file);
            if (root != null && file.getAbsolutePath().equals(root.getAbsolutePath())) {
                break;
            }
            file = file.getParentFile();
        }
        for (File f : fileList) {
            crumbs.add(f.getName().isEmpty() ? "Storage" : f.getName());
            breadcrumbPaths.add(f.getAbsolutePath());
        }
        breadcrumbsLive.setValue(crumbs);
    }

    public void toggleSelection(FileItem item) {
        List<FileItem> current = selectedFiles.getValue();
        if (current == null) current = new ArrayList<>();
        if (current.contains(item)) {
            current.remove(item);
            item.setSelected(false);
        } else {
            current.add(item);
            item.setSelected(true);
        }
        selectedFiles.setValue(new ArrayList<>(current));
        isSelectionMode.setValue(!current.isEmpty());
    }

    public void selectAll(List<FileItem> allFiles) {
        if (allFiles == null) return;
        List<FileItem> selected = new ArrayList<>();
        for (FileItem item : allFiles) {
            item.setSelected(true);
            selected.add(item);
        }
        selectedFiles.setValue(selected);
        isSelectionMode.setValue(!selected.isEmpty());
    }

    public void clearSelection() {
        List<FileItem> current = selectedFiles.getValue();
        if (current != null) {
            for (FileItem item : current) {
                item.setSelected(false);
            }
        }
        selectedFiles.setValue(new ArrayList<>());
        isSelectionMode.setValue(false);
    }

    public void createFolder(String folderName) {
        String current = currentPathLive.getValue();
        if (current != null) {
            fileRepository.createNewFolder(new File(current), folderName, this::refresh);
        }
    }

    public void renameFile(FileItem item, String newName) {
        if (item.getPath() != null) {
            fileRepository.renameFile(new File(item.getPath()), newName, this::refresh);
        }
    }

    public void deleteSelected(Runnable onDone) {
        List<FileItem> current = selectedFiles.getValue();
        if (current != null && !current.isEmpty()) {
            List<File> files = new ArrayList<>();
            for (FileItem item : current) {
                files.add(new File(item.getPath()));
            }
            fileRepository.deleteFiles(files, () -> {
                refresh();
                if (onDone != null) onDone.run();
            });
        }
    }
}
