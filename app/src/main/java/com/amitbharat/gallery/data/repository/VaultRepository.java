package com.amitbharat.gallery.data.repository;

import android.content.Context;
import android.media.MediaScannerConnection;
import androidx.lifecycle.LiveData;
import com.amitbharat.gallery.data.database.AppDatabase;
import com.amitbharat.gallery.data.database.VaultDao;
import com.amitbharat.gallery.data.models.FileItem;
import com.amitbharat.gallery.data.models.MediaItem;
import com.amitbharat.gallery.data.models.VaultItem;
import com.amitbharat.gallery.utils.FileUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VaultRepository {
    private final Context context;
    private final VaultDao vaultDao;
    private final ExecutorService executor;
    private final File vaultDir;

    public VaultRepository(Context context) {
        this.context = context.getApplicationContext();
        this.vaultDao = AppDatabase.getInstance(context).vaultDao();
        this.executor = Executors.newFixedThreadPool(2);
        this.vaultDir = new File(context.getFilesDir(), ".vault");
        if (!vaultDir.exists()) {
            vaultDir.mkdirs();
        }
    }

    public LiveData<List<VaultItem>> getAllVaultItems() {
        return vaultDao.getAllVaultItemsLive();
    }

    public void hideMedia(MediaItem mediaItem, Runnable onComplete) {
        if (mediaItem.getPath() == null && mediaItem.getUri() == null) {
            if (onComplete != null) onComplete.run();
            return;
        }
        executor.execute(() -> {
            try {
                String hiddenName = UUID.randomUUID().toString() + ".dat";
                File destFile = new File(vaultDir, hiddenName);

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

                if (copied && destFile.exists() && destFile.length() > 0) {
                    String origPath = srcFile != null ? srcFile.getAbsolutePath() : (mediaItem.getPath() != null ? mediaItem.getPath() : "");
                    String origName = mediaItem.getDisplayName() != null ? mediaItem.getDisplayName() : (srcFile != null ? srcFile.getName() : "media_" + System.currentTimeMillis());

                    VaultItem vaultItem = new VaultItem(
                            origPath,
                            destFile.getAbsolutePath(),
                            origName,
                            mediaItem.getMimeType(),
                            destFile.length(),
                            System.currentTimeMillis()
                    );
                    vaultDao.insertVaultItem(vaultItem);

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

    public void hideFile(FileItem fileItem, Runnable onComplete) {
        if (fileItem.getPath() == null) {
            if (onComplete != null) onComplete.run();
            return;
        }
        executor.execute(() -> {
            try {
                File srcFile = new File(fileItem.getPath());
                if (srcFile.exists()) {
                    String hiddenName = UUID.randomUUID().toString() + ".dat";
                    File destFile = new File(vaultDir, hiddenName);
                    if (FileUtils.copyFile(srcFile, destFile)) {
                        VaultItem vaultItem = new VaultItem(
                                srcFile.getAbsolutePath(),
                                destFile.getAbsolutePath(),
                                srcFile.getName(),
                                FileUtils.getMimeType(srcFile.getAbsolutePath()),
                                destFile.length(),
                                System.currentTimeMillis()
                        );
                        vaultDao.insertVaultItem(vaultItem);
                        srcFile.delete();
                        MediaScannerConnection.scanFile(context, new String[]{srcFile.getAbsolutePath()}, null, null);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (onComplete != null) onComplete.run();
        });
    }

    public void restoreItem(VaultItem vaultItem, Runnable onComplete) {
        executor.execute(() -> {
            try {
                File hiddenFile = new File(vaultItem.getVaultPath());
                File restoreTarget = new File(vaultItem.getOriginalPath());
                File parent = restoreTarget.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
                if (hiddenFile.exists()) {
                    if (FileUtils.copyFile(hiddenFile, restoreTarget)) {
                        hiddenFile.delete();
                        vaultDao.deleteVaultItem(vaultItem);
                        MediaScannerConnection.scanFile(context, new String[]{restoreTarget.getAbsolutePath()}, null, null);
                    }
                } else {
                    vaultDao.deleteVaultItem(vaultItem);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (onComplete != null) onComplete.run();
        });
    }
}
