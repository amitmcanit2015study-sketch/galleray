package com.amitbharat.gallery.ui.activities;

import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.amitbharat.gallery.databinding.ActivityVideoTrimBinding;
import com.amitbharat.gallery.utils.MediaUtils;
import com.amitbharat.gallery.utils.VideoTrimHelper;
import java.io.File;

@OptIn(markerClass = UnstableApi.class)
public class VideoTrimActivity extends AppCompatActivity {

    private ActivityVideoTrimBinding binding;
    private String videoPath;
    private ExoPlayer player;

    private long totalDurationMs = 0;
    private long startTrimMs = 0;
    private long endTrimMs = 0;

    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private boolean isUserSeeking = false;
    private boolean isPlayingRangeOnly = false;

    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (player != null && !isUserSeeking) {
                long pos = player.getCurrentPosition();
                binding.tvCurrentPlayhead.setText("Play: " + MediaUtils.formatDuration(pos));

                if (totalDurationMs > 0) {
                    int progress = (int) ((pos * 1000) / totalDurationMs);
                    binding.seekBarVideo.setProgress(progress);
                }

                if (isPlayingRangeOnly && pos >= endTrimMs) {
                    player.pause();
                    isPlayingRangeOnly = false;
                    binding.btnPlayPausePreview.setVisibility(View.VISIBLE);
                }
            }
            progressHandler.postDelayed(this, 100);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVideoTrimBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        videoPath = getIntent().getStringExtra("video_path");
        if (videoPath == null || !new File(videoPath).exists()) {
            Toast.makeText(this, "Video file not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        setupPlayer();
        setupControls();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnExportTrim.setOnClickListener(v -> showExportDialog());
    }

    private void setupPlayer() {
        player = new ExoPlayer.Builder(this).build();
        binding.playerView.setPlayer(player);

        MediaItem mediaItem = MediaItem.fromUri(Uri.fromFile(new File(videoPath)));
        player.setMediaItem(mediaItem);
        player.prepare();

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY) {
                    totalDurationMs = player.getDuration();
                    if (endTrimMs == 0) {
                        endTrimMs = totalDurationMs;
                        updateTrimLabels();
                    }
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                binding.btnPlayPausePreview.setVisibility(isPlaying ? View.GONE : View.VISIBLE);
            }
        });

        binding.playerContainer.setOnClickListener(v -> {
            if (player.isPlaying()) {
                player.pause();
            } else {
                player.play();
            }
        });

        binding.btnPlayPausePreview.setOnClickListener(v -> player.play());
    }

    private void setupControls() {
        binding.seekBarVideo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && totalDurationMs > 0) {
                    long seekMs = (progress * totalDurationMs) / 1000;
                    binding.tvCurrentPlayhead.setText("Play: " + MediaUtils.formatDuration(seekMs));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUserSeeking = false;
                if (totalDurationMs > 0) {
                    long seekMs = (seekBar.getProgress() * totalDurationMs) / 1000;
                    player.seekTo(seekMs);
                }
            }
        });

        // Start controls
        binding.btnStartMinus.setOnClickListener(v -> {
            startTrimMs = Math.max(0, startTrimMs - 1000);
            updateTrimLabels();
            player.seekTo(startTrimMs);
        });
        binding.btnStartPlus.setOnClickListener(v -> {
            startTrimMs = Math.min(endTrimMs - 500, startTrimMs + 1000);
            updateTrimLabels();
            player.seekTo(startTrimMs);
        });
        binding.btnSetStart.setOnClickListener(v -> {
            long cur = player.getCurrentPosition();
            if (cur < endTrimMs) {
                startTrimMs = cur;
                updateTrimLabels();
            } else {
                Toast.makeText(this, "Start time must be before End time", Toast.LENGTH_SHORT).show();
            }
        });

        // End controls
        binding.btnEndMinus.setOnClickListener(v -> {
            endTrimMs = Math.max(startTrimMs + 500, endTrimMs - 1000);
            updateTrimLabels();
            player.seekTo(endTrimMs);
        });
        binding.btnEndPlus.setOnClickListener(v -> {
            endTrimMs = Math.min(totalDurationMs, endTrimMs + 1000);
            updateTrimLabels();
            player.seekTo(endTrimMs);
        });
        binding.btnSetEnd.setOnClickListener(v -> {
            long cur = player.getCurrentPosition();
            if (cur > startTrimMs) {
                endTrimMs = cur;
                updateTrimLabels();
            } else {
                Toast.makeText(this, "End time must be after Start time", Toast.LENGTH_SHORT).show();
            }
        });

        // Preview selected range
        binding.btnPreviewTrimRange.setOnClickListener(v -> {
            isPlayingRangeOnly = true;
            player.seekTo(startTrimMs);
            player.play();
        });
    }

    private void updateTrimLabels() {
        binding.tvStartTime.setText(MediaUtils.formatDuration(startTrimMs));
        binding.tvEndTime.setText(MediaUtils.formatDuration(endTrimMs));

        long trimDurationMs = Math.max(0, endTrimMs - startTrimMs);
        long durSec = trimDurationMs / 1000;
        binding.tvTrimDuration.setText("Cut: " + MediaUtils.formatDuration(startTrimMs) + " - " +
                MediaUtils.formatDuration(endTrimMs) + " (" + durSec + "s)");
    }

    private void showExportDialog() {
        if (endTrimMs <= startTrimMs) {
            Toast.makeText(this, "Invalid trim range", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] options = new String[]{"Save as New Video", "Overwrite Original Video"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Export Trimmed Video")
                .setItems(options, (dialog, which) -> {
                    boolean overwrite = (which == 1);
                    executeVideoTrim(overwrite);
                })
                .show();
    }

    private void executeVideoTrim(boolean overwrite) {
        player.pause();
        binding.layoutExportProgress.setVisibility(View.VISIBLE);
        binding.btnExportTrim.setEnabled(false);

        File srcFile = new File(videoPath);
        File destFile;
        if (overwrite) {
            destFile = new File(srcFile.getParentFile(), "temp_trim_" + System.currentTimeMillis() + ".mp4");
        } else {
            String name = srcFile.getName();
            int dot = name.lastIndexOf('.');
            String base = (dot > 0) ? name.substring(0, dot) : name;
            destFile = new File(srcFile.getParentFile(), base + "_trimmed_" + System.currentTimeMillis() + ".mp4");
        }

        VideoTrimHelper.trimVideo(srcFile, destFile, startTrimMs, endTrimMs, new VideoTrimHelper.TrimCallback() {
            @Override
            public void onProgress(int percent) {
                runOnUiThread(() -> {
                    binding.progressBarExport.setProgress(percent);
                    binding.tvExportProgress.setText("Trimming video... " + percent + "%");
                });
            }

            @Override
            public void onSuccess(File savedFile) {
                runOnUiThread(() -> {
                    if (overwrite) {
                        srcFile.delete();
                        savedFile.renameTo(srcFile);
                        MediaScannerConnection.scanFile(VideoTrimActivity.this, new String[]{srcFile.getAbsolutePath()}, null, null);
                        Toast.makeText(VideoTrimActivity.this, "Original video trimmed successfully!", Toast.LENGTH_LONG).show();
                    } else {
                        MediaScannerConnection.scanFile(VideoTrimActivity.this, new String[]{savedFile.getAbsolutePath()}, null, null);
                        Toast.makeText(VideoTrimActivity.this, "Saved: " + savedFile.getName(), Toast.LENGTH_LONG).show();
                    }
                    finish();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    binding.layoutExportProgress.setVisibility(View.GONE);
                    binding.btnExportTrim.setEnabled(true);
                    Toast.makeText(VideoTrimActivity.this, "Trim failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressHandler.post(progressRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        progressHandler.removeCallbacks(progressRunnable);
        if (player != null) {
            player.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progressHandler.removeCallbacks(progressRunnable);
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
