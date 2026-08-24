package com.amitbharat.gallery.utils;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class VideoTrimHelper {

    public interface TrimCallback {
        void onProgress(int percent);
        void onSuccess(File savedFile);
        void onError(Exception e);
    }

    public static void trimVideo(File srcFile, File destFile, long startMs, long endMs, TrimCallback callback) {
        new Thread(() -> {
            MediaExtractor extractor = new MediaExtractor();
            MediaMuxer muxer = null;
            try {
                extractor.setDataSource(srcFile.getAbsolutePath());
                int trackCount = extractor.getTrackCount();

                muxer = new MediaMuxer(destFile.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                HashMap<Integer, Integer> indexMap = new HashMap<>();

                // Set orientation hint from metadata
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(srcFile.getAbsolutePath());
                String rotationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
                if (rotationStr != null) {
                    try {
                        int rotation = Integer.parseInt(rotationStr);
                        muxer.setOrientationHint(rotation);
                    } catch (Exception ignored) {}
                }
                retriever.release();

                int maxBufferSize = 1024 * 1024; // 1 MB default
                for (int i = 0; i < trackCount; i++) {
                    MediaFormat format = extractor.getTrackFormat(i);
                    if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                        int newSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
                        if (newSize > maxBufferSize) {
                            maxBufferSize = newSize;
                        }
                    }
                    int dstIndex = muxer.addTrack(format);
                    indexMap.put(i, dstIndex);
                }

                muxer.start();

                long startUs = startMs * 1000L;
                long endUs = endMs * 1000L;
                long totalTrimUs = Math.max(1, endUs - startUs);

                ByteBuffer buffer = ByteBuffer.allocateDirect(maxBufferSize);
                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

                for (int i = 0; i < trackCount; i++) {
                    extractor.selectTrack(i);
                    extractor.seekTo(startUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);

                    while (true) {
                        bufferInfo.offset = 0;
                        bufferInfo.size = extractor.readSampleData(buffer, 0);

                        if (bufferInfo.size < 0) {
                            break;
                        }

                        long sampleTimeUs = extractor.getSampleTime();
                        if (sampleTimeUs >= endUs) {
                            break;
                        }

                        if (sampleTimeUs >= startUs) {
                            bufferInfo.presentationTimeUs = sampleTimeUs - startUs;
                            bufferInfo.flags = extractor.getSampleFlags();
                            int trackIndex = indexMap.get(i);
                            muxer.writeSampleData(trackIndex, buffer, bufferInfo);

                            if (callback != null && i == 0) {
                                int progress = (int) Math.min(100, Math.max(0, ((sampleTimeUs - startUs) * 100) / totalTrimUs));
                                callback.onProgress(progress);
                            }
                        }

                        extractor.advance();
                    }
                    extractor.unselectTrack(i);
                }

                muxer.stop();
                muxer.release();
                extractor.release();

                if (callback != null) {
                    callback.onSuccess(destFile);
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (muxer != null) {
                    try { muxer.release(); } catch (Exception ignored) {}
                }
                try { extractor.release(); } catch (Exception ignored) {}
                if (destFile.exists()) {
                    destFile.delete();
                }
                if (callback != null) {
                    callback.onError(e);
                }
            }
        }).start();
    }
}
