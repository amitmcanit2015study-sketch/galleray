package com.amitbharat.gallery.utils;

import android.app.WallpaperManager;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import com.amitbharat.gallery.data.models.FolderItem;
import com.amitbharat.gallery.data.models.MediaItem;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MediaUtils {

    public static List<MediaItem> queryAllMedia(Context context) {
        List<MediaItem> mediaList = new ArrayList<>();
        Uri collectionUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collectionUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collectionUri = MediaStore.Files.getContentUri("external");
        }

        String[] projection = new String[]{
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.WIDTH,
                MediaStore.Files.FileColumns.HEIGHT,
                MediaStore.Video.VideoColumns.DURATION,
                MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        };

        String selection = "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO
                + " OR (" + MediaStore.Files.FileColumns.MIME_TYPE + " LIKE 'image/%' AND " + MediaStore.Files.FileColumns.SIZE + " > 0)"
                + " OR (" + MediaStore.Files.FileColumns.MIME_TYPE + " LIKE 'video/%' AND " + MediaStore.Files.FileColumns.SIZE + " > 0)"
                + " OR (" + MediaStore.Files.FileColumns.MIME_TYPE + " LIKE 'audio/%' AND " + MediaStore.Files.FileColumns.SIZE + " > 0)"
                + ")";

        String sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";

        try (Cursor cursor = context.getContentResolver().query(
                collectionUri,
                projection,
                selection,
                null,
                sortOrder
        )) {
            if (cursor != null) {
                int idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
                int dataCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
                int nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);
                int sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);
                int mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE);
                int dateAddCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED);
                int dateModCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED);
                int mediaTypeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE);
                int widthCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.WIDTH);
                int heightCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.HEIGHT);
                int durCol = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION);
                int bucketIdCol = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
                int bucketNameCol = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idCol);
                    String path = cursor.getString(dataCol);
                    String name = cursor.getString(nameCol);
                    long size = cursor.getLong(sizeCol);
                    String mime = cursor.getString(mimeCol);
                    long dateAdded = cursor.getLong(dateAddCol) * 1000;
                    long dateModified = cursor.getLong(dateModCol) * 1000;
                    int mediaType = cursor.getInt(mediaTypeCol);
                    boolean isVideo = mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                            || (mime != null && mime.startsWith("video/"))
                            || (name != null && isVideoExtension(name));
                    boolean isAudio = !isVideo && (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO
                            || (mime != null && mime.startsWith("audio/"))
                            || (name != null && isAudioExtension(name)));
                    int width = cursor.getInt(widthCol);
                    int height = cursor.getInt(heightCol);
                    long duration = ((isVideo || isAudio) && durCol != -1) ? cursor.getLong(durCol) : 0;
                    long bucketId = bucketIdCol != -1 ? cursor.getLong(bucketIdCol) : 0;
                    String bucketName = bucketNameCol != -1 ? cursor.getString(bucketNameCol) : "";

                    Uri contentUri;
                    if (isVideo) {
                        contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                    } else if (isAudio) {
                        contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                    } else {
                        contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                    }

                    if (path != null && new File(path).exists()) {
                        MediaItem item = new MediaItem(id, contentUri, path, name, size, mime,
                                dateAdded, dateModified, duration, width, height, isVideo, bucketId, bucketName);
                        item.setAudio(isAudio);
                        mediaList.add(item);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Also scan Telegram download directories directly on disk
        scanTelegramDirectories(context, mediaList);

        return mediaList;
    }

    public static void scanTelegramDirectories(Context context, List<MediaItem> mediaList) {
        Set<String> existingPaths = new HashSet<>();
        for (MediaItem item : mediaList) {
            if (item.getPath() != null) {
                existingPaths.add(item.getPath().toLowerCase(Locale.ROOT));
            }
        }

        List<File> telegramDirs = new ArrayList<>();
        File ext = Environment.getExternalStorageDirectory();
        File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File movies = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);

        if (downloads != null) telegramDirs.add(new File(downloads, "Telegram"));
        if (pictures != null) telegramDirs.add(new File(pictures, "Telegram"));
        if (movies != null) telegramDirs.add(new File(movies, "Telegram"));
        if (ext != null) {
            telegramDirs.add(new File(ext, "Telegram"));
            telegramDirs.add(new File(ext, "Telegram/Telegram Images"));
            telegramDirs.add(new File(ext, "Telegram/Telegram Video"));
            telegramDirs.add(new File(ext, "Telegram/Telegram Documents"));
            telegramDirs.add(new File(ext, "Android/media/org.telegram.messenger/Telegram/Telegram Images"));
            telegramDirs.add(new File(ext, "Android/media/org.telegram.messenger/Telegram/Telegram Video"));
            telegramDirs.add(new File(ext, "Android/media/org.telegram.messenger.web/Telegram/Telegram Images"));
            telegramDirs.add(new File(ext, "Android/media/org.telegram.messenger.web/Telegram/Telegram Video"));
            telegramDirs.add(new File(ext, "Android/data/org.telegram.messenger/files/Telegram/Telegram Images"));
            telegramDirs.add(new File(ext, "Android/data/org.telegram.messenger/files/Telegram/Telegram Video"));
            telegramDirs.add(new File(ext, "Android/data/org.telegram.messenger.web/files/Telegram/Telegram Images"));
            telegramDirs.add(new File(ext, "Android/data/org.telegram.messenger.web/files/Telegram/Telegram Video"));
        }

        List<String> pathsToScan = new ArrayList<>();

        for (File dir : telegramDirs) {
            if (dir != null && dir.exists() && dir.isDirectory()) {
                scanTelegramDirRecursive(dir, mediaList, existingPaths, pathsToScan, 0);
            }
        }

        if (!pathsToScan.isEmpty()) {
            try {
                MediaScannerConnection.scanFile(
                        context,
                        pathsToScan.toArray(new String[0]),
                        null,
                        null
                );
            } catch (Exception ignored) {}
        }
    }

    private static void scanTelegramDirRecursive(File dir, List<MediaItem> mediaList,
                                                 Set<String> existingPaths, List<String> pathsToScan, int depth) {
        if (depth > 5 || dir == null || !dir.exists() || !dir.canRead()) return;
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.isDirectory()) {
                if (!f.getName().startsWith(".")) {
                    scanTelegramDirRecursive(f, mediaList, existingPaths, pathsToScan, depth + 1);
                }
            } else if (f.isFile() && f.length() > 0) {
                String pathLower = f.getAbsolutePath().toLowerCase(Locale.ROOT);
                if (existingPaths.contains(pathLower)) continue;

                boolean isImage = isImageExtension(f.getName());
                boolean isVideo = isVideoExtension(f.getName());

                if (isImage || isVideo) {
                    existingPaths.add(pathLower);
                    pathsToScan.add(f.getAbsolutePath());

                    long dateMod = f.lastModified();
                    long id = f.getAbsolutePath().hashCode();
                    Uri uri = Uri.fromFile(f);
                    String mime = isVideo ? "video/mp4" : "image/jpeg";
                    String bucketName = dir.getName();
                    long bucketId = dir.getAbsolutePath().toLowerCase(Locale.ROOT).hashCode();

                    MediaItem item = new MediaItem(
                            id, uri, f.getAbsolutePath(), f.getName(), f.length(), mime,
                            dateMod, dateMod, 0, 0, 0, isVideo, bucketId, bucketName
                    );
                    mediaList.add(item);
                }
            }
        }
    }

    public static boolean isImageExtension(String name) {
        if (name == null) return false;
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")
                || lower.endsWith(".webp") || lower.endsWith(".gif") || lower.endsWith(".bmp")
                || lower.endsWith(".heic") || lower.endsWith(".heif");
    }

    public static boolean isVideoExtension(String name) {
        if (name == null) return false;
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".mov")
                || lower.endsWith(".webm") || lower.endsWith(".3gp") || lower.endsWith(".avi")
                || lower.endsWith(".flv");
    }

    public static boolean isAudioExtension(String name) {
        if (name == null) return false;
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".mp3") || lower.endsWith(".wav") || lower.endsWith(".m4a")
                || lower.endsWith(".flac") || lower.endsWith(".aac") || lower.endsWith(".ogg")
                || lower.endsWith(".wma") || lower.endsWith(".opus") || lower.endsWith(".m4p");
    }

    public static List<FolderItem> groupMediaByFolders(List<MediaItem> mediaItems) {
        Map<String, FolderItem> folderMap = new HashMap<>();
        Map<String, Long> latestImageDates = new HashMap<>();
        Map<String, Long> latestMediaDates = new HashMap<>();

        for (MediaItem item : mediaItems) {
            String folderPath = "";
            String folderName = item.getBucketDisplayName();
            if (item.getPath() != null) {
                File file = new File(item.getPath());
                File parent = file.getParentFile();
                if (parent != null) {
                    folderPath = parent.getAbsolutePath();
                    if (folderName == null || folderName.isEmpty()) {
                        folderName = parent.getName();
                    }
                }
            }
            if (folderPath.isEmpty()) {
                folderPath = String.valueOf(item.getBucketId());
            }

            long itemDate = Math.max(item.getDateModified(), item.getDateAdded());
            FolderItem folder = folderMap.get(folderPath);

            if (folder == null) {
                folder = new FolderItem(
                        item.getBucketId(),
                        folderName,
                        folderPath,
                        item.getUri(),
                        item.getPath(),
                        1,
                        item.getSize(),
                        itemDate
                );
                folderMap.put(folderPath, folder);

                if (!item.isVideo()) {
                    latestImageDates.put(folderPath, itemDate);
                }
                latestMediaDates.put(folderPath, itemDate);
            } else {
                folder.setFileCount(folder.getFileCount() + 1);
                folder.setTotalSize(folder.getTotalSize() + item.getSize());

                if (itemDate > folder.getLastModified()) {
                    folder.setLastModified(itemDate);
                }

                Long latestImgDate = latestImageDates.get(folderPath);
                Long latestMedDate = latestMediaDates.get(folderPath);

                if (!item.isVideo()) {
                    // Update cover to latest image
                    if (latestImgDate == null || itemDate > latestImgDate) {
                        folder.setCoverUri(item.getUri());
                        folder.setCoverPath(item.getPath());
                        latestImageDates.put(folderPath, itemDate);
                    }
                } else {
                    // Video only used as cover if no image has been found yet
                    if (latestImgDate == null && (latestMedDate == null || itemDate > latestMedDate)) {
                        folder.setCoverUri(item.getUri());
                        folder.setCoverPath(item.getPath());
                    }
                }

                if (latestMedDate == null || itemDate > latestMedDate) {
                    latestMediaDates.put(folderPath, itemDate);
                }
            }
        }

        return new ArrayList<>(folderMap.values());
    }

    public static String formatDuration(long millis) {
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        long hours = (millis / (1000 * 60 * 60));

        if (hours > 0) {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
    }

    public static boolean setWallpaper(Context context, Uri uri) {
        try {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
            InputStream is = context.getContentResolver().openInputStream(uri);
            if (is != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                wallpaperManager.setBitmap(bitmap);
                is.close();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
