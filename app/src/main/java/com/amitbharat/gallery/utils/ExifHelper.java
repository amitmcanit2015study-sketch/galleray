package com.amitbharat.gallery.utils;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import androidx.exifinterface.media.ExifInterface;
import com.amitbharat.gallery.data.models.ExifInfo;
import com.amitbharat.gallery.data.models.MediaItem;
import java.io.File;
import java.io.InputStream;
import java.util.Locale;

public class ExifHelper {

    public static ExifInfo extractExif(Context context, MediaItem item) {
        ExifInfo info = new ExifInfo();
        info.setFileName(item.getDisplayName());
        info.setFilePath(item.getPath());
        info.setFileSize(item.getSize());
        info.setMimeType(item.getMimeType());
        info.setDateModified(DateUtils.formatDateTime(item.getDateModified()));

        boolean isVideo = item.isVideo() || (item.getMimeType() != null && item.getMimeType().startsWith("video/"));
        info.setVideo(isVideo);

        if (item.getPath() != null) {
            File f = new File(item.getPath());
            if (f.getParentFile() != null) {
                info.setFileDirectory(f.getParentFile().getAbsolutePath());
            }
        }

        int width = item.getWidth();
        int height = item.getHeight();
        if (width > 0 && height > 0) {
            info.setResolution(width + " × " + height);
            double mp = (width * height) / 1_000_000.0;
            info.setMegapixels(String.format(Locale.US, "%.1f MP", mp));
        }

        if (isVideo) {
            extractVideoMetadata(context, item, info);
            return info;
        }

        // Image EXIF Extraction
        if (item.getPath() == null && item.getUri() == null) return info;

        try {
            ExifInterface exif = null;
            if (item.getPath() != null) {
                File file = new File(item.getPath());
                if (file.exists() && file.canRead()) {
                    exif = new ExifInterface(item.getPath());
                }
            }
            if (exif == null && item.getUri() != null) {
                try (InputStream is = context.getContentResolver().openInputStream(item.getUri())) {
                    if (is != null) {
                        exif = new ExifInterface(is);
                    }
                }
            }

            if (exif != null) {
                int exifW = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0);
                int exifH = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0);
                if ((width <= 0 || height <= 0) && exifW > 0 && exifH > 0) {
                    info.setResolution(exifW + " × " + exifH);
                    double mp = (exifW * exifH) / 1_000_000.0;
                    info.setMegapixels(String.format(Locale.US, "%.1f MP", mp));
                }

                String make = exif.getAttribute(ExifInterface.TAG_MAKE);
                String model = exif.getAttribute(ExifInterface.TAG_MODEL);
                info.setCameraMake(make != null ? make.trim() : "");
                if (model != null) {
                    info.setCameraModel(model.trim());
                } else if (make != null) {
                    info.setCameraModel(make.trim());
                }

                String lens = exif.getAttribute(ExifInterface.TAG_LENS_MODEL);
                if (lens != null) info.setLensModel(lens.trim());

                String software = exif.getAttribute(ExifInterface.TAG_SOFTWARE);
                if (software != null) info.setSoftware(software.trim());

                String dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL);
                if (dateTime == null) dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME);
                if (dateTime != null) {
                    info.setDateTaken(dateTime);
                } else {
                    info.setDateTaken(DateUtils.formatDateTime(item.getDateAdded()));
                }

                String fNumber = exif.getAttribute(ExifInterface.TAG_F_NUMBER);
                if (fNumber != null && !fNumber.isEmpty()) {
                    info.setAperture("f/" + fNumber);
                }

                String expTime = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
                if (expTime != null) {
                    try {
                        double exp = Double.parseDouble(expTime);
                        if (exp > 0 && exp < 1.0) {
                            info.setExposureTime(String.format(Locale.US, "1/%d s", Math.round(1.0 / exp)));
                        } else {
                            info.setExposureTime(expTime + " s");
                        }
                    } catch (Exception e) {
                        info.setExposureTime(expTime + " s");
                    }
                }

                String iso = exif.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY);
                if (iso == null) iso = exif.getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS);
                if (iso != null) {
                    info.setIso("ISO " + iso);
                }

                String focal = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
                if (focal != null) {
                    try {
                        double val = Double.parseDouble(focal);
                        info.setFocalLength(String.format(Locale.US, "%.1f mm", val));
                    } catch (Exception e) {
                        info.setFocalLength(focal + " mm");
                    }
                }

                String focal35 = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM);
                if (focal35 != null) {
                    info.setFocalLength35mm(focal35 + " mm (35mm equivalent)");
                }

                int flashVal = exif.getAttributeInt(ExifInterface.TAG_FLASH, -1);
                if (flashVal != -1) {
                    info.setFlash((flashVal & 1) != 0 ? "Flash Fired" : "No Flash (Off)");
                }

                int wb = exif.getAttributeInt(ExifInterface.TAG_WHITE_BALANCE, -1);
                if (wb == ExifInterface.WHITE_BALANCE_AUTO) {
                    info.setWhiteBalance("Auto");
                } else if (wb == ExifInterface.WHITE_BALANCE_MANUAL) {
                    info.setWhiteBalance("Manual");
                }

                String expBias = exif.getAttribute(ExifInterface.TAG_EXPOSURE_BIAS_VALUE);
                if (expBias != null) {
                    info.setExposureBias(expBias + " EV");
                }

                int meter = exif.getAttributeInt(ExifInterface.TAG_METERING_MODE, -1);
                switch (meter) {
                    case ExifInterface.METERING_MODE_AVERAGE: info.setMeteringMode("Average"); break;
                    case ExifInterface.METERING_MODE_CENTER_WEIGHT_AVERAGE: info.setMeteringMode("Center-weighted"); break;
                    case ExifInterface.METERING_MODE_SPOT: info.setMeteringMode("Spot"); break;
                    case ExifInterface.METERING_MODE_MULTI_SPOT: info.setMeteringMode("Multi-spot"); break;
                    case ExifInterface.METERING_MODE_PATTERN: info.setMeteringMode("Pattern / Matrix"); break;
                }

                int orient = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                switch (orient) {
                    case ExifInterface.ORIENTATION_ROTATE_90: info.setOrientation("90° CW"); break;
                    case ExifInterface.ORIENTATION_ROTATE_180: info.setOrientation("180°"); break;
                    case ExifInterface.ORIENTATION_ROTATE_270: info.setOrientation("270° CW"); break;
                    default: info.setOrientation("Normal (0°)"); break;
                }

                double[] latLong = exif.getLatLong();
                if (latLong != null && latLong.length == 2) {
                    info.setGpsLatitude(String.format(Locale.US, "%.6f", latLong[0]));
                    info.setGpsLongitude(String.format(Locale.US, "%.6f", latLong[1]));
                    info.setLocationText(String.format(Locale.US, "%.5f, %.5f", latLong[0], latLong[1]));
                }

                double altitude = exif.getAltitude(-999999);
                if (altitude != -999999) {
                    info.setGpsAltitude(String.format(Locale.US, "%.1f m above sea level", altitude));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return info;
    }

    private static void extractVideoMetadata(Context context, MediaItem item, ExifInfo info) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            if (item.getPath() != null && new File(item.getPath()).exists()) {
                retriever.setDataSource(item.getPath());
            } else if (item.getUri() != null) {
                retriever.setDataSource(context, item.getUri());
            } else {
                return;
            }

            String durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (durStr != null) {
                long durMs = Long.parseLong(durStr);
                info.setDuration(MediaUtils.formatDuration(durMs) + " (" + (durMs / 1000) + "s)");
            }

            String w = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            String h = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            if (w != null && h != null) {
                info.setResolution(w + " × " + h);
            }

            String bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
            if (bitrate != null) {
                try {
                    long br = Long.parseLong(bitrate);
                    info.setBitrate(String.format(Locale.US, "%.2f Mbps", br / 1_000_000.0));
                } catch (Exception ignored) {
                    info.setBitrate(bitrate + " bps");
                }
            }

            String rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
            if (rotation != null) {
                info.setOrientation(rotation + "°");
            }

            String date = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE);
            if (date != null) {
                info.setDateTaken(date);
            } else {
                info.setDateTaken(DateUtils.formatDateTime(item.getDateAdded()));
            }

            String location = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_LOCATION);
            if (location != null && !location.isEmpty()) {
                info.setLocationText(location);
            }

            String mime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            if (mime != null) {
                info.setVideoCodec(mime);
            }

            String hasAudio = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO);
            if ("yes".equalsIgnoreCase(hasAudio)) {
                info.setAudioCodec("Audio Track Available (Stereo)");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { retriever.release(); } catch (Exception ignored) {}
        }
    }
}
