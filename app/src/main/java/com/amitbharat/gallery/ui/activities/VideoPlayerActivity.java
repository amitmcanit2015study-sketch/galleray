package com.amitbharat.gallery.ui.activities;

import android.app.PictureInPictureParams;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Rational;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.amitbharat.gallery.R;
import com.amitbharat.gallery.data.models.ExifInfo;
import com.amitbharat.gallery.databinding.ActivityVideoPlayerBinding;
import com.amitbharat.gallery.ui.custom.VideoGestureOverlay;
import com.amitbharat.gallery.utils.DateUtils;
import com.amitbharat.gallery.utils.ExifHelper;
import com.amitbharat.gallery.utils.FileUtils;
import com.amitbharat.gallery.utils.MediaUtils;
import java.io.File;

public class VideoPlayerActivity extends AppCompatActivity implements VideoGestureOverlay.VideoGestureListener {

    private ActivityVideoPlayerBinding binding;
    private ExoPlayer player;
    private com.amitbharat.gallery.data.models.MediaItem mediaItem;
    private boolean areControlsVisible = true;
    private int currentAspectMode = AspectRatioFrameLayout.RESIZE_MODE_FIT;
    private float currentSpeed = 1.0f;
    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private Runnable progressRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVideoPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mediaItem = (com.amitbharat.gallery.data.models.MediaItem) getIntent().getSerializableExtra("media_item");
        if (mediaItem == null && getIntent().getData() != null) {
            mediaItem = FileUtils.getMediaItemFromUri(this, getIntent().getData());
        }
        if (mediaItem == null) {
            finish();
            return;
        }

        setupGestureOverlay();
        setupControls();
        initializePlayer();
    }

    private void setupGestureOverlay() {
        binding.gestureOverlay.setIndicatorViews(
                binding.gestureIndicator,
                binding.indicatorIcon,
                binding.indicatorProgress,
                binding.indicatorText
        );
        binding.gestureOverlay.setVideoGestureListener(this);
    }

    private void setupControls() {
        binding.tvVideoTitle.setText(mediaItem.getDisplayName());
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnPlayPause.setOnClickListener(v -> {
            if (player == null) return;
            if (player.isPlaying()) {
                player.pause();
                binding.btnPlayPause.setImageResource(R.drawable.ic_play);
            } else {
                player.play();
                binding.btnPlayPause.setImageResource(R.drawable.ic_pause);
            }
        });

        binding.btnSpeed.setOnClickListener(v -> showSpeedDialog());
        binding.btnAspectRatio.setOnClickListener(v -> toggleAspectRatio());
        binding.btnTrim.setOnClickListener(v -> {
            if (mediaItem != null && mediaItem.getPath() != null) {
                if (player != null) player.pause();
                Intent trimIntent = new Intent(this, VideoTrimActivity.class);
                trimIntent.putExtra("video_path", mediaItem.getPath());
                startActivity(trimIntent);
            } else {
                Toast.makeText(this, "Cannot trim: local path not found", Toast.LENGTH_SHORT).show();
            }
        });
        binding.btnInfo.setOnClickListener(v -> showVideoInfoBottomSheet());
        binding.btnPip.setOnClickListener(v -> enterPipMode());

        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && player != null) {
                    long duration = player.getDuration();
                    long targetPos = (duration * progress) / 100;
                    player.seekTo(targetPos);
                    binding.tvCurrentTime.setText(MediaUtils.formatDuration(targetPos));
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void initializePlayer() {
        player = new ExoPlayer.Builder(this).build();
        binding.playerView.setPlayer(player);

        Uri uri = mediaItem.getUri();
        if (uri == null && mediaItem.getPath() != null) {
            uri = Uri.fromFile(new File(mediaItem.getPath()));
        }
        if (uri == null) {
            Toast.makeText(this, "Cannot play video: file not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        MediaItem exoItem = MediaItem.fromUri(uri);
        player.setMediaItem(exoItem);
        player.prepare();
        player.play();

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    binding.tvTotalDuration.setText(MediaUtils.formatDuration(player.getDuration()));
                    startProgressTracker();
                } else if (playbackState == Player.STATE_ENDED) {
                    binding.btnPlayPause.setImageResource(R.drawable.ic_play);
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                binding.btnPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
            }
        });
    }

    private void startProgressTracker() {
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (player != null && player.isPlaying()) {
                    long pos = player.getCurrentPosition();
                    long dur = player.getDuration();
                    if (dur > 0) {
                        int progress = (int) ((pos * 100) / dur);
                        binding.seekBar.setProgress(progress);
                        binding.tvCurrentTime.setText(MediaUtils.formatDuration(pos));
                    }
                }
                progressHandler.postDelayed(this, 500);
            }
        };
        progressHandler.post(progressRunnable);
    }

    private void showSpeedDialog() {
        String[] speeds = new String[]{"0.5x", "0.75x", "1.0x (Normal)", "1.25x", "1.5x", "2.0x"};
        float[] speedValues = new float[]{0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f};

        new MaterialAlertDialogBuilder(this)
                .setTitle("Playback Speed")
                .setItems(speeds, (dialog, which) -> {
                    currentSpeed = speedValues[which];
                    if (player != null) {
                        player.setPlaybackParameters(new PlaybackParameters(currentSpeed));
                    }
                })
                .show();
    }

    private void toggleAspectRatio() {
        if (currentAspectMode == AspectRatioFrameLayout.RESIZE_MODE_FIT) {
            currentAspectMode = AspectRatioFrameLayout.RESIZE_MODE_FILL;
        } else if (currentAspectMode == AspectRatioFrameLayout.RESIZE_MODE_FILL) {
            currentAspectMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM;
        } else {
            currentAspectMode = AspectRatioFrameLayout.RESIZE_MODE_FIT;
        }
        binding.playerView.setResizeMode(currentAspectMode);
    }

    private void enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PictureInPictureParams.Builder builder = new PictureInPictureParams.Builder();
            builder.setAspectRatio(new Rational(16, 9));
            enterPictureInPictureMode(builder.build());
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, @NonNull Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        binding.topBar.setVisibility(isInPictureInPictureMode ? View.GONE : View.VISIBLE);
        binding.bottomControls.setVisibility(isInPictureInPictureMode ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onSingleTap() {
        areControlsVisible = !areControlsVisible;
        binding.topBar.setVisibility(areControlsVisible ? View.VISIBLE : View.GONE);
        binding.bottomControls.setVisibility(areControlsVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDoubleTap() {
        if (player == null) return;
        if (player.isPlaying()) {
            player.pause();
        } else {
            player.play();
        }
    }

    @Override
    public void onSeek(long deltaMillis) {
        if (player == null) return;
        long target = Math.max(0, Math.min(player.getDuration(), player.getCurrentPosition() + deltaMillis));
        player.seekTo(target);
    }

    @Override
    public long getCurrentPosition() {
        return player != null ? player.getCurrentPosition() : 0;
    }

    @Override
    public long getDuration() {
        return player != null ? player.getDuration() : 0;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null && player.isPlaying()) {
            player.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressRunnable != null) {
            progressHandler.removeCallbacks(progressRunnable);
        }
        if (player != null) {
            player.release();
            player = null;
        }
    }

    private void showVideoInfoBottomSheet() {
        if (mediaItem == null) return;
        ExifInfo info = ExifHelper.extractExif(this, mediaItem);

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_file_properties, null);

        android.widget.TextView tvName = view.findViewById(R.id.tvPropFileName);
        android.widget.TextView tvPath = view.findViewById(R.id.tvPropPath);
        android.widget.TextView tvSize = view.findViewById(R.id.tvPropSize);
        android.widget.TextView tvResolution = view.findViewById(R.id.tvPropResolution);
        android.widget.TextView tvMime = view.findViewById(R.id.tvPropMime);
        android.widget.TextView tvDate = view.findViewById(R.id.tvPropDate);

        View headerVideo = view.findViewById(R.id.tvHeaderVideo);
        View tableVideo = view.findViewById(R.id.tableVideoExif);
        View headerCamera = view.findViewById(R.id.tvHeaderCamera);
        View tableCamera = view.findViewById(R.id.tableCameraExif);

        tvName.setText(info.getFileName());
        tvPath.setText(info.getFilePath());
        tvSize.setText(FileUtils.formatFileSize(info.getFileSize()) + " (" + info.getFileSize() + " bytes)");
        tvResolution.setText(info.getResolution());
        tvMime.setText(info.getMimeType());
        tvDate.setText(info.getDateModified());

        if (headerVideo != null) headerVideo.setVisibility(View.VISIBLE);
        if (tableVideo != null) tableVideo.setVisibility(View.VISIBLE);
        if (headerCamera != null) headerCamera.setVisibility(View.GONE);
        if (tableCamera != null) tableCamera.setVisibility(View.GONE);

        android.widget.TextView tvVideoDuration = view.findViewById(R.id.tvVideoDuration);
        android.widget.TextView tvVideoBitrate = view.findViewById(R.id.tvVideoBitrate);
        android.widget.TextView tvVideoCodec = view.findViewById(R.id.tvVideoCodec);
        android.widget.TextView tvVideoAudio = view.findViewById(R.id.tvVideoAudio);
        android.widget.TextView tvVideoOrientation = view.findViewById(R.id.tvVideoOrientation);
        android.widget.TextView tvVideoDate = view.findViewById(R.id.tvVideoDate);

        tvVideoDuration.setText(info.getDuration());
        tvVideoBitrate.setText(info.getBitrate());
        tvVideoCodec.setText(info.getVideoCodec());
        tvVideoAudio.setText(info.getAudioCodec());
        tvVideoOrientation.setText(info.getOrientation());
        tvVideoDate.setText(info.getDateTaken());

        dialog.setContentView(view);
        dialog.show();
    }
}
