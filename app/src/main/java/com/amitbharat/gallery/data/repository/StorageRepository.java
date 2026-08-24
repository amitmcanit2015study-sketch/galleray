package com.amitbharat.gallery.data.repository;

import android.content.Context;
import android.os.Environment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.amitbharat.gallery.R;
import com.amitbharat.gallery.data.models.FileItem;
import com.amitbharat.gallery.data.models.StorageCategory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StorageRepository {
    private final Context context;
    private final ExecutorService executor;

    private final MutableLiveData<List<StorageCategory>> categoriesLive = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<FileItem>> largeFilesLive = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<FileItem>> duplicateFilesLive = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<FileItem>> emptyFoldersLive = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isScanningLive = new MutableLiveData<>(false);

    public StorageRepository(Context context) {
        this.context = context.getApplicationContext();
        this.executor = Executors.newFixedThreadPool(4);
    }

    private final MutableLiveData<List<FileItem>> cacheJunkLive = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<FileItem>> apkJunkLive = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<StorageCategory>> getCategoriesLive() { return categoriesLive; }
    public LiveData<List<FileItem>> getLargeFilesLive() { return largeFilesLive; }
    public LiveData<List<FileItem>> getDuplicateFilesLive() { return duplicateFilesLive; }
    public LiveData<List<FileItem>> getEmptyFoldersLive() { return emptyFoldersLive; }
    public LiveData<List<FileItem>> getCacheJunkLive() { return cacheJunkLive; }
    public LiveData<List<FileItem>> getApkJunkLive() { return apkJunkLive; }
    public LiveData<Boolean> getIsScanningLive() { return isScanningLive; }

    public void analyzeStorage() {
        isScanningLive.postValue(true);
        executor.execute(() -> {
            try {
                StorageCategory catImages = new StorageCategory(StorageCategory.CategoryType.IMAGES, "Images", R.drawable.ic_file_image, R.color.cat_images, 0, 0);
                StorageCategory catVideos = new StorageCategory(StorageCategory.CategoryType.VIDEOS, "Videos", R.drawable.ic_file_video, R.color.cat_videos, 0, 0);
                StorageCategory catAudio = new StorageCategory(StorageCategory.CategoryType.AUDIO, "Audio", R.drawable.ic_file_audio, R.color.cat_audio, 0, 0);
                StorageCategory catDocs = new StorageCategory(StorageCategory.CategoryType.DOCUMENTS, "Documents", R.drawable.ic_file_doc, R.color.cat_documents, 0, 0);
                StorageCategory catApks = new StorageCategory(StorageCategory.CategoryType.APKS, "APKs", R.drawable.ic_file_apk, R.color.cat_apks, 0, 0);
                StorageCategory catArchives = new StorageCategory(StorageCategory.CategoryType.ARCHIVES, "Archives", R.drawable.ic_zip, R.color.cat_archives, 0, 0);
                StorageCategory catOthers = new StorageCategory(StorageCategory.CategoryType.OTHER, "Other Files", R.drawable.ic_file_generic, R.color.cat_others, 0, 0);

                List<FileItem> largeFiles = new ArrayList<>();
                List<FileItem> emptyFolders = new ArrayList<>();
                List<FileItem> cacheJunk = new ArrayList<>();
                List<FileItem> apkFiles = new ArrayList<>();
                Map<String, List<FileItem>> sizeAndNameMap = new HashMap<>();

                File root = Environment.getExternalStorageDirectory();
                if (root != null && root.exists()) {
                    scanRecursive(root, catImages, catVideos, catAudio, catDocs, catApks, catArchives, catOthers,
                            largeFiles, emptyFolders, cacheJunk, apkFiles, sizeAndNameMap, 0);
                }

                List<StorageCategory> categories = new ArrayList<>();
                categories.add(catImages);
                categories.add(catVideos);
                categories.add(catAudio);
                categories.add(catDocs);
                categories.add(catApks);
                categories.add(catArchives);
                categories.add(catOthers);

                List<FileItem> duplicates = new ArrayList<>();
                for (List<FileItem> group : sizeAndNameMap.values()) {
                    if (group.size() > 1) {
                        duplicates.addAll(group);
                    }
                }

                categoriesLive.postValue(categories);
                largeFilesLive.postValue(largeFiles);
                duplicateFilesLive.postValue(duplicates);
                emptyFoldersLive.postValue(emptyFolders);
                cacheJunkLive.postValue(cacheJunk);
                apkJunkLive.postValue(apkFiles);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isScanningLive.postValue(false);
            }
        });
    }

    public void cleanWasteFiles(List<FileItem> wasteItems, Runnable onComplete) {
        executor.execute(() -> {
            try {
                if (wasteItems != null) {
                    for (FileItem item : wasteItems) {
                        if (item.getPath() != null) {
                            File f = new File(item.getPath());
                            if (f.exists()) {
                                com.amitbharat.gallery.utils.FileUtils.deleteRecursive(f);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            analyzeStorage();
            if (onComplete != null) onComplete.run();
        });
    }

    private void scanRecursive(File dir,
                               StorageCategory catImages, StorageCategory catVideos, StorageCategory catAudio,
                               StorageCategory catDocs, StorageCategory catApks, StorageCategory catArchives,
                               StorageCategory catOthers, List<FileItem> largeFiles, List<FileItem> emptyFolders,
                               List<FileItem> cacheJunk, List<FileItem> apkFiles,
                               Map<String, List<FileItem>> sizeAndNameMap, int depth) {
        if (depth > 20 || dir == null || !dir.exists() || !dir.canRead()) return;
        
        String dirName = dir.getName().toLowerCase(Locale.ROOT);
        if (dirName.startsWith(".") && !dir.equals(Environment.getExternalStorageDirectory())) {
            if (dirName.contains("thumb") || dirName.contains("cache")) {
                cacheJunk.add(new FileItem(dir));
            }
            return;
        }
        if (dirName.equals("data") || dirName.equals("obb")) {
            File parent = dir.getParentFile();
            if (parent != null && parent.getName().equalsIgnoreCase("Android")) {
                return; // Skip Android/data and Android/obb
            }
        }

        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            if (dir.isDirectory() && !dir.equals(Environment.getExternalStorageDirectory())) {
                emptyFolders.add(new FileItem(dir));
            }
            return;
        }

        for (File f : files) {
            if (f.isDirectory()) {
                String subName = f.getName().toLowerCase(Locale.ROOT);
                if (subName.contains(".thumb") || subName.equals(".cache") || subName.equals("cache") || subName.equals(".exo")) {
                    cacheJunk.add(new FileItem(f));
                }
                scanRecursive(f, catImages, catVideos, catAudio, catDocs, catApks, catArchives, catOthers,
                        largeFiles, emptyFolders, cacheJunk, apkFiles, sizeAndNameMap, depth + 1);
            } else {
                long len = f.length();
                String ext = getExtension(f.getName());

                // Check cache / temp / garbage junk files
                if (ext.equals("tmp") || ext.equals("temp") || ext.equals("cache") || ext.equals("log") ||
                    ext.equals("bak") || ext.equals("dmp") || ext.equals("old") || ext.equals("0") ||
                    f.getName().startsWith("crash_") || f.getName().startsWith(".thumb")) {
                    cacheJunk.add(new FileItem(f));
                }

                // Check APK files
                if ("apk".equals(ext)) {
                    apkFiles.add(new FileItem(f));
                    catApks.setTotalBytes(catApks.getTotalBytes() + len);
                    catApks.setItemCount(catApks.getItemCount() + 1);
                }

                // Check large files (> 50MB)
                if (len > 50 * 1024 * 1024) {
                    largeFiles.add(new FileItem(f));
                }

                // Check duplicate candidates (same size and name)
                if (len > 1024 * 10) { // greater than 10KB
                    String key = f.getName().toLowerCase(Locale.ROOT) + "_" + len;
                    List<FileItem> list = sizeAndNameMap.computeIfAbsent(key, k -> new ArrayList<>());
                    list.add(new FileItem(f));
                }

                if (isImageExt(ext)) {
                    catImages.setTotalBytes(catImages.getTotalBytes() + len);
                    catImages.setItemCount(catImages.getItemCount() + 1);
                } else if (isVideoExt(ext)) {
                    catVideos.setTotalBytes(catVideos.getTotalBytes() + len);
                    catVideos.setItemCount(catVideos.getItemCount() + 1);
                } else if (isAudioExt(ext)) {
                    catAudio.setTotalBytes(catAudio.getTotalBytes() + len);
                    catAudio.setItemCount(catAudio.getItemCount() + 1);
                } else if (isDocExt(ext)) {
                    catDocs.setTotalBytes(catDocs.getTotalBytes() + len);
                    catDocs.setItemCount(catDocs.getItemCount() + 1);
                } else if (isArchiveExt(ext)) {
                    catArchives.setTotalBytes(catArchives.getTotalBytes() + len);
                    catArchives.setItemCount(catArchives.getItemCount() + 1);
                } else if (!"apk".equals(ext)) {
                    catOthers.setTotalBytes(catOthers.getTotalBytes() + len);
                    catOthers.setItemCount(catOthers.getItemCount() + 1);
                }
            }
        }
    }

    private String getExtension(String name) {
        if (name == null || name.lastIndexOf('.') == -1) return "";
        return name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private boolean isImageExt(String ext) {
        return ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png") || ext.equals("webp") || ext.equals("gif") || ext.equals("heic") || ext.equals("bmp");
    }

    private boolean isVideoExt(String ext) {
        return ext.equals("mp4") || ext.equals("mkv") || ext.equals("webm") || ext.equals("mov") || ext.equals("avi") || ext.equals("3gp");
    }

    private boolean isAudioExt(String ext) {
        return ext.equals("mp3") || ext.equals("wav") || ext.equals("m4a") || ext.equals("aac") || ext.equals("ogg") || ext.equals("flac");
    }

    private boolean isDocExt(String ext) {
        return ext.equals("pdf") || ext.equals("doc") || ext.equals("docx") || ext.equals("txt") || ext.equals("xls") || ext.equals("xlsx") || ext.equals("ppt") || ext.equals("pptx");
    }

    private boolean isArchiveExt(String ext) {
        return ext.equals("zip") || ext.equals("rar") || ext.equals("7z") || ext.equals("tar") || ext.equals("gz");
    }
}
