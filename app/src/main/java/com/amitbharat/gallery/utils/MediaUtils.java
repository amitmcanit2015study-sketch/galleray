package com.amitbharat.gallery.utils;

import android.app.WallpaperManager;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import com.amitbharat.gallery.data.models.FolderItem;
import com.amitbharat.gallery.data.models.MediaItem;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

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
                    boolean isVideo = mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
                    int width = cursor.getInt(widthCol);
                    int height = cursor.getInt(heightCol);
                    long duration = (isVideo && durCol != -1) ? cursor.getLong(durCol) : 0;
                    long bucketId = bucketIdCol != -1 ? cursor.getLong(bucketIdCol) : 0;
                    String bucketName = bucketNameCol != -1 ? cursor.getString(bucketNameCol) : "";

                    Uri contentUri;
                    if (isVideo) {
                        contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                    } else {
                        contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                    }

                    if (path != null && new File(path).exists()) {
                        MediaItem item = new MediaItem(id, contentUri, path, name, size, mime,
                                dateAdded, dateModified, duration, width, height, isVideo, bucketId, bucketName);
                        mediaList.add(item);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mediaList;
    }

    public static List<FolderItem> groupMediaByFolders(List<MediaItem> mediaItems) {
        Map<String, FolderItem> folderMap = new HashMap<>();

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
                        item.getDateModified()
                );
                folderMap.put(folderPath, folder);
            } else {
                folder.setFileCount(folder.getFileCount() + 1);
                folder.setTotalSize(folder.getTotalSize() + item.getSize());
                if (item.getDateModified() > folder.getLastModified()) {
                    folder.setLastModified(item.getDateModified());
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
