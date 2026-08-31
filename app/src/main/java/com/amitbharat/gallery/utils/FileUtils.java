package com.amitbharat.gallery.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import androidx.core.content.FileProvider;
import com.amitbharat.gallery.R;
import com.amitbharat.gallery.data.models.FileItem;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class FileUtils {

    public static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        digitGroups = Math.min(digitGroups, units.length - 1);
        return new DecimalFormat("#,##0.#").format(bytes / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static String getMimeType(String path) {
        if (path == null) return "*/*";
        int lastDot = path.lastIndexOf('.');
        if (lastDot >= 0 && lastDot < path.length() - 1) {
            String extension = path.substring(lastDot + 1).toLowerCase(Locale.ROOT).trim();
            String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mime != null) {
                return mime;
            }
            switch (extension) {
                case "apk": return "application/vnd.android.package-archive";
                case "pdf": return "application/pdf";
                case "zip": return "application/zip";
                case "rar": return "application/x-rar-compressed";
                case "7z": return "application/x-7z-compressed";
                case "tar": return "application/x-tar";
                case "gz": return "application/gzip";
                case "doc": return "application/msword";
                case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                case "xls": return "application/vnd.ms-excel";
                case "xlsx": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                case "ppt": return "application/vnd.ms-powerpoint";
                case "pptx": return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
                case "txt": return "text/plain";
                case "epub": return "application/epub+zip";
                case "mp3": return "audio/mpeg";
                case "wav": return "audio/wav";
                case "m4a": return "audio/mp4";
                case "flac": return "audio/flac";
                case "aac": return "audio/aac";
                case "ogg": return "audio/ogg";
                case "mp4": return "video/mp4";
                case "mkv": return "video/x-matroska";
                case "webm": return "video/webm";
                case "avi": return "video/x-msvideo";
                case "mov": return "video/quicktime";
                case "3gp": return "video/3gpp";
            }
        }
        return "*/*";
    }

    public static int getFileIconRes(FileItem item) {
        if (item.isDirectory()) {
            return R.drawable.ic_folder;
        }
        String ext = item.getExtension().toLowerCase(Locale.ROOT);
        switch (ext) {
            case "jpg":
            case "jpeg":
            case "png":
            case "webp":
            case "gif":
            case "bmp":
            case "heic":
                return R.drawable.ic_file_image;
            case "mp4":
            case "mkv":
            case "webm":
            case "avi":
            case "mov":
            case "3gp":
                return R.drawable.ic_file_video;
            case "mp3":
            case "wav":
            case "m4a":
            case "flac":
            case "ogg":
            case "aac":
                return R.drawable.ic_file_audio;
            case "pdf":
                return R.drawable.ic_file_pdf;
            case "apk":
                return R.drawable.ic_file_apk;
            case "doc":
            case "docx":
            case "txt":
            case "xls":
            case "xlsx":
            case "ppt":
            case "pptx":
                return R.drawable.ic_file_doc;
            case "zip":
            case "rar":
            case "7z":
            case "tar":
            case "gz":
                return R.drawable.ic_zip;
            default:
                return R.drawable.ic_file_generic;
        }
    }

    public static boolean copyFile(File source, File destination) {
        try {
            if (source.isDirectory()) {
                if (!destination.exists() && !destination.mkdirs()) {
                    return false;
                }
                String[] children = source.list();
                if (children != null) {
                    for (String child : children) {
                        copyFile(new File(source, child), new File(destination, child));
                    }
                }
                return true;
            } else {
                File parent = destination.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
                try (InputStream in = new FileInputStream(source);
                     OutputStream out = new FileOutputStream(destination)) {
                    byte[] buffer = new byte[8192];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean moveFile(File source, File destination) {
        if (source.renameTo(destination)) {
            return true;
        }
        if (copyFile(source, destination)) {
            return deleteRecursive(source);
        }
        return false;
    }

    public static boolean deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] children = fileOrDirectory.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        return fileOrDirectory.delete();
    }

    public static long calculateDirectorySize(File dir) {
        if (dir == null || !dir.exists()) return 0;
        if (!dir.isDirectory()) return dir.length();
        long size = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    size += calculateDirectorySize(file);
                } else {
                    size += file.length();
                }
            }
        }
        return size;
    }

    public static void openFileWithIntent(Context context, File file) {
        if (context == null || file == null || !file.exists()) return;
        try {
            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
            String mime = getMimeType(file.getAbsolutePath());
            String fileName = file.getName().toLowerCase(Locale.ROOT);

            if (fileName.endsWith(".apk") || "application/vnd.android.package-archive".equals(mime)) {
                Intent installIntent = new Intent(Intent.ACTION_VIEW);
                installIntent.setDataAndType(uri, "application/vnd.android.package-archive");
                installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(installIntent);
                return;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mime);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Intent chooser = Intent.createChooser(intent, "Open with");
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(chooser);
        } catch (android.content.ActivityNotFoundException e) {
            android.widget.Toast.makeText(context, "No app found to open this file", android.widget.Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            android.widget.Toast.makeText(context, "Error opening file: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    public static void shareFiles(Context context, List<File> files) {
        if (files == null || files.isEmpty()) return;
        try {
            if (files.size() == 1) {
                File file = files.get(0);
                Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType(getMimeType(file.getAbsolutePath()));
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(Intent.createChooser(intent, "Share via"));
            } else {
                ArrayList<Uri> uris = new ArrayList<>();
                for (File f : files) {
                    uris.add(FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", f));
                }
                Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                intent.setType("*/*");
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(Intent.createChooser(intent, "Share via"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
