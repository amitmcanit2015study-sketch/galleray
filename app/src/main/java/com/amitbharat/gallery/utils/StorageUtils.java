package com.amitbharat.gallery.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import androidx.core.content.ContextCompat;
import com.amitbharat.gallery.data.models.StorageInfo;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StorageUtils {

    public static List<StorageInfo> getStorageVolumes(Context context) {
        List<StorageInfo> storageList = new ArrayList<>();

        // 1. Primary Internal Storage
        File internalPath = Environment.getExternalStorageDirectory();
        if (internalPath != null && internalPath.exists()) {
            try {
                StatFs stat = new StatFs(internalPath.getPath());
                long totalBytes = stat.getTotalBytes();
                long freeBytes = stat.getAvailableBytes();
                storageList.add(new StorageInfo("Internal Storage", internalPath.getAbsolutePath(), totalBytes, freeBytes, true, false, false));
            } catch (Exception ignored) {}
        }

        // 2. Secondary Volumes (SD Card, USB OTG)
        try {
            File[] externalDirs = ContextCompat.getExternalFilesDirs(context, null);
            for (File file : externalDirs) {
                if (file != null) {
                    String path = file.getAbsolutePath();
                    int androidIdx = path.indexOf("/Android");
                    if (androidIdx > 0) {
                        String rootPath = path.substring(0, androidIdx);
                        if (internalPath != null && !rootPath.equalsIgnoreCase(internalPath.getAbsolutePath())) {
                            File rootFile = new File(rootPath);
                            if (rootFile.exists() && rootFile.canRead()) {
                                StatFs stat = new StatFs(rootFile.getPath());
                                long totalBytes = stat.getTotalBytes();
                                long freeBytes = stat.getAvailableBytes();
                                boolean isUsb = rootPath.toLowerCase().contains("usb") || rootPath.toLowerCase().contains("otg");
                                String name = isUsb ? "USB Drive" : "SD Card";
                                storageList.add(new StorageInfo(name, rootPath, totalBytes, freeBytes, false, !isUsb, isUsb));
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        return storageList;
    }
}
