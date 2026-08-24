package com.amitbharat.gallery.data.repository;

import android.content.Context;
import android.media.MediaScannerConnection;
import androidx.lifecycle.LiveData;
import com.amitbharat.gallery.data.database.AppDatabase;
import com.amitbharat.gallery.data.database.TrashDao;
import com.amitbharat.gallery.data.database.TrashEntity;
import com.amitbharat.gallery.data.models.FileItem;
import com.amitbharat.gallery.data.models.MediaItem;
import com.amitbharat.gallery.utils.FileUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TrashRepository {

    private final Context context;
    private final TrashDao trashDao;
    private final ExecutorService executor;
    private final File trashDir;

    public TrashRepository(Context context) {
        this.context = context.getApplicationContext();
        this.trashDao = AppDatabase.getInstance(context).trashDao();
        this.executor = Executors.newFixedThreadPool(2);
        this.trashDir = new File(context.getFilesDir(), ".trash");
        if (!trashDir.exists()) {
            trashDir.mkdirs();
        }
    }

    public LiveData<List<TrashEntity>> getTrashItemsLive() {
        return trashDao.getAllTrashLive();
    }

    public void moveToTrash(MediaItem mediaItem, Runnable onComplete) {
        executor.execute(() -> {
            try {
                String trashName = UUID.randomUUID().toString() + ".trash";
                File destFile = new File(trashDir, trashName);

                boolean copied = false;
                File srcFile = mediaItem.getPath() != null ? new File(mediaItem.getPath()) : null;

                if (srcFile != null && srcFile.exists() && srcFile.canRead()) {
                    copied = FileUtils.copyFile(srcFile, destFile);
                }
                if (!copied && mediaItem.getUri() != null) {
                    try (InputStream in = context.getContentResolver().openInputStream(mediaItem.getUri());
                         FileOutputStream out = new FileOutputStream(destFile)) {
                        if (in != null) {
                            byte[] buffer = new byte[16384];
                            int len;
                            while ((len = in.read(buffer)) > 0) {
                                out.write(buffer, 0, len);
                            }
                            copied = true;
                        }
                    }
                }

                if (copied && destFile.exists()) {
                    String origPath = srcFile != null ? srcFile.getAbsolutePath() : (mediaItem.getPath() != null ? mediaItem.getPath() : "");
                    String origName = mediaItem.getDisplayName() != null ? mediaItem.getDisplayName() : (srcFile != null ? srcFile.getName() : "deleted_media");

                    TrashEntity trash = new TrashEntity(
                            origPath,
                            destFile.getAbsolutePath(),
                            origName,
                            destFile.length(),
                            System.currentTimeMillis(),
                            false
                    );
                    trashDao.insertTrash(trash);

                    if (srcFile != null && srcFile.exists()) {
                        srcFile.delete();
                    }
                    if (mediaItem.getUri() != null) {
                        try {
                            context.getContentResolver().delete(mediaItem.getUri(), null, null);
                        } catch (Exception ignored) {}
                    }
                    if (srcFile != null) {
                        MediaScannerConnection.scanFile(context, new String[]{srcFile.getAbsolutePath()}, null, null);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (onComplete != null) onComplete.run();
        });
    }

    public void moveFileToTrash(FileItem fileItem, Runnable onComplete) {
        executor.execute(() -> {
            try {
                File srcFile = new File(fileItem.getPath());
                if (srcFile.exists()) {
                    String trashName = UUID.randomUUID().toString() + ".trash";
                    File destFile = new File(trashDir, trashName);

                    if (FileUtils.copyFile(srcFile, destFile)) {
                        TrashEntity trash = new TrashEntity(
                                srcFile.getAbsolutePath(),
                                destFile.getAbsolutePath(),
                                srcFile.getName(),
                                destFile.length(),
                                System.currentTimeMillis(),
                                fileItem.isDirectory()
                        );
                        trashDao.insertTrash(trash);
                        FileUtils.deleteRecursive(srcFile);
                        MediaScannerConnection.scanFile(context, new String[]{srcFile.getAbsolutePath()}, null, null);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (onComplete != null) onComplete.run();
        });
    }

    public void restoreItem(TrashEntity trash, Runnable onComplete) {
        executor.execute(() -> {
            try {
                File trashFile = new File(trash.getTrashPath());
                File restoreTarget = new File(trash.getOriginalPath());
                File parent = restoreTarget.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }

                if (trashFile.exists()) {
                    if (FileUtils.copyFile(trashFile, restoreTarget)) {
                        trashFile.delete();
                        trashDao.deleteTrash(trash);
                        MediaScannerConnection.scanFile(context, new String[]{restoreTarget.getAbsolutePath()}, null, null);
                    }
                } else {
                    trashDao.deleteTrash(trash);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (onComplete != null) onComplete.run();
        });
    }

    public void deletePermanently(TrashEntity trash, Runnable onComplete) {
        executor.execute(() -> {
            try {
                File trashFile = new File(trash.getTrashPath());
                if (trashFile.exists()) {
                    FileUtils.deleteRecursive(trashFile);
                }
                trashDao.deleteTrash(trash);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (onComplete != null) onComplete.run();
        });
    }

    public void emptyTrash(Runnable onComplete) {
        executor.execute(() -> {
            try {
                List<TrashEntity> all = trashDao.getAllTrashSync();
                for (TrashEntity t : all) {
                    File f = new File(t.getTrashPath());
                    if (f.exists()) {
                        FileUtils.deleteRecursive(f);
                    }
                }
                trashDao.clearAllTrash();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (onComplete != null) onComplete.run();
        });
    }
}
