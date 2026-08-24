package com.amitbharat.gallery.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipHelper {

    public interface ZipProgressListener {
        void onProgress(int current, int total);
    }

    public static boolean zipFiles(List<File> srcFiles, File zipFile) {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            byte[] buffer = new byte[8192];
            for (File file : srcFiles) {
                if (file.exists()) {
                    addFileToZip(file, file.getName(), zos, buffer);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void addFileToZip(File file, String entryPath, ZipOutputStream zos, byte[] buffer) throws Exception {
        if (file.isDirectory()) {
            String dirPath = entryPath.endsWith("/") ? entryPath : entryPath + "/";
            zos.putNextEntry(new ZipEntry(dirPath));
            zos.closeEntry();
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    addFileToZip(child, dirPath + child.getName(), zos, buffer);
                }
            }
        } else {
            zos.putNextEntry(new ZipEntry(entryPath));
            try (FileInputStream fis = new FileInputStream(file)) {
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
            }
            zos.closeEntry();
        }
    }

    public static boolean unzip(File zipFile, File targetDir) {
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        byte[] buffer = new byte[8192];
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File newFile = new File(targetDir, entry.getName());
                // Prevent Zip Slip vulnerability
                String canonicalDest = targetDir.getCanonicalPath();
                String canonicalEntry = newFile.getCanonicalPath();
                if (!canonicalEntry.startsWith(canonicalDest + File.separator) && !canonicalEntry.equals(canonicalDest)) {
                    throw new SecurityException("Zip entry is outside of target directory: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    File parent = newFile.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
