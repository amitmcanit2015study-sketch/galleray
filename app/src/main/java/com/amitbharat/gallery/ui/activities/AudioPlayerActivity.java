package com.amitbharat.gallery.ui.activities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.SeekBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.amitbharat.gallery.R;
import com.amitbharat.gallery.databinding.ActivityAudioPlayerBinding;
import com.amitbharat.gallery.utils.DateUtils;
import com.amitbharat.gallery.utils.FileUtils;
import com.amitbharat.gallery.utils.MediaHolder;
import com.amitbharat.gallery.utils.MediaUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class AudioPlayerActivity extends AppCompatActivity {

    private ActivityAudioPlayerBinding binding;
    private ExoPlayer player;
    private List<com.amitbharat.gallery.data.models.MediaItem> playlist = new ArrayList<>();
    private int currentTrackIndex = 0;
    private com.amitbharat.gallery.data.models.MediaItem currentItem;

    private ObjectAnimator discAnimator;
    private float currentSpeed = 1.0f;
    private int repeatMode = Player.REPEAT_MODE_OFF;
    private boolean isShuffleEnabled = false;
    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private Runnable progressRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAudioPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        parseIncomingData();
        if (currentItem == null) {
            Toast.makeText(this, "Audio file not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        setupDiscAnimation();
        setupControls();
        initializePlayer();
        loadTrack(currentItem);
    }

    private void parseIncomingData() {
        Intent intent = getIntent();
        if (intent == null) return;

        int pos = intent.getIntExtra("current_position", -1);
        List<com.amitbharat.gallery.data.models.MediaItem> sharedList = MediaHolder.getCurrentMediaList();

        if (sharedList != null && !sharedList.isEmpty()) {
            playlist = new ArrayList<>(sharedList);
            if (pos >= 0 && pos < playlist.size()) {
                currentTrackIndex = pos;
                currentItem = playlist.get(currentTrackIndex);
            }
        }

        if (currentItem == null) {
            com.amitbharat.gallery.data.models.MediaItem item =
                    (com.amitbharat.gallery.data.models.MediaItem) intent.getSerializableExtra("media_item");
            if (item != null) {
                currentItem = item;
                playlist.add(item);
                currentTrackIndex = 0;
            }
        }

        if (currentItem == null && intent.getStringExtra("audio_path") != null) {
            String path = intent.getStringExtra("audio_path");
            File file = new File(path);
            if (file.exists()) {
                currentItem = new com.amitbharat.gallery.data.models.MediaItem();
                currentItem.setPath(path);
                currentItem.setDisplayName(file.getName());
                currentItem.setSize(file.length());
                currentItem.setDateModified(file.lastModified());
                currentItem.setAudio(true);
                currentItem.setUri(Uri.fromFile(file));
                playlist.add(currentItem);
                currentTrackIndex = 0;
            }
        }

        if (currentItem == null && intent.getData() != null) {
            Uri uri = intent.getData();
            currentItem = FileUtils.getMediaItemFromUri(this, uri);
            if (currentItem != null) {
                currentItem.setAudio(true);
                playlist.add(currentItem);
                currentTrackIndex = 0;
            }
        }
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupDiscAnimation() {
        discAnimator = ObjectAnimator.ofFloat(binding.imgVinylDisc, "rotation", 0f, 360f);
        discAnimator.setDuration(8000);
        discAnimator.setRepeatCount(ValueAnimator.INFINITE);
        discAnimator.setInterpolator(new LinearInterpolator());
    }

    private void setupControls() {
        binding.tvAudioTitle.setSelected(true); // Enable marquee scrolling

        binding.btnPlayPause.setOnClickListener(v -> {
            if (player == null) return;
            if (player.isPlaying()) {
                player.pause();
            } else {
                player.play();
            }
        });

        binding.btnPrevious.setOnClickListener(v -> playPreviousTrack());
        binding.btnNext.setOnClickListener(v -> playNextTrack());

        binding.btnRewind10.setOnClickListener(v -> {
            if (player != null) {
                long target = Math.max(0, player.getCurrentPosition() - 10000);
                player.seekTo(target);
            }
        });

        binding.btnForward10.setOnClickListener(v -> {
            if (player != null) {
                long target = Math.min(player.getDuration(), player.getCurrentPosition() + 10000);
                player.seekTo(target);
            }
        });

        binding.btnSpeed.setOnClickListener(v -> showSpeedDialog());
        binding.btnRepeat.setOnClickListener(v -> toggleRepeatMode());
        binding.btnShuffle.setOnClickListener(v -> toggleShuffleMode());
        binding.btnShare.setOnClickListener(v -> shareCurrentAudio());
        binding.btnInfo.setOnClickListener(v -> showAudioPropertiesDialog());

        binding.seekBarAudio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && player != null) {
                    long dur = player.getDuration();
                    if (dur > 0) {
                        long target = (dur * progress) / 1000;
                        player.seekTo(target);
                        binding.tvCurrentTime.setText(MediaUtils.formatDuration(target));
                    }
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void initializePlayer() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build();

        player = new ExoPlayer.Builder(this)
                .setAudioAttributes(audioAttributes, true)
                .build();

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    binding.tvTotalDuration.setText(MediaUtils.formatDuration(player.getDuration()));
                    startProgressTracker();
                } else if (playbackState == Player.STATE_ENDED) {
                    binding.btnPlayPause.setImageResource(R.drawable.ic_play);
                    discAnimator.pause();
                    if (repeatMode != Player.REPEAT_MODE_ONE) {
                        playNextTrack();
                    }
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                binding.btnPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
                if (isPlaying) {
                    if (discAnimator.isPaused()) {
                        discAnimator.resume();
                    } else if (!discAnimator.isStarted()) {
                        discAnimator.start();
                    }
                } else {
                    discAnimator.pause();
                }
            }
        });
    }

    private void loadTrack(com.amitbharat.gallery.data.models.MediaItem item) {
        if (item == null || player == null) return;
        currentItem = item;

        Uri uri = item.getUri();
        if (uri == null && item.getPath() != null) {
            uri = Uri.fromFile(new File(item.getPath()));
        }
        if (uri == null) {
            Toast.makeText(this, "Cannot play: Audio URI is invalid", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update track index if multiple items in playlist
        if (playlist.size() > 1) {
            binding.tvTrackIndex.setVisibility(View.VISIBLE);
            binding.tvTrackIndex.setText(String.format(Locale.getDefault(), "Track %d of %d", currentTrackIndex + 1, playlist.size()));
            binding.btnPrevious.setEnabled(true);
            binding.btnNext.setEnabled(true);
        } else {
            binding.tvTrackIndex.setVisibility(View.GONE);
        }

        // Update UI details
        String title = item.getDisplayName();
        String artist = "Audio Track";
        String formatBadge = "AUDIO";

        // Try extracting metadata
        try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
            if (item.getPath() != null && new File(item.getPath()).exists()) {
                retriever.setDataSource(item.getPath());
            } else {
                retriever.setDataSource(this, uri);
            }

            String metaTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            if (metaTitle != null && !metaTitle.trim().isEmpty()) {
                title = metaTitle.trim();
            }
            String metaArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String metaAlbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            if (metaArtist != null && !metaArtist.trim().isEmpty()) {
                artist = metaArtist.trim();
                if (metaAlbum != null && !metaAlbum.trim().isEmpty()) {
                    artist += " • " + metaAlbum.trim();
                }
            } else if (metaAlbum != null && !metaAlbum.trim().isEmpty()) {
                artist = metaAlbum.trim();
            }

            String bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
            String ext = "";
            if (item.getDisplayName() != null && item.getDisplayName().contains(".")) {
                ext = item.getDisplayName().substring(item.getDisplayName().lastIndexOf('.') + 1).toUpperCase(Locale.ROOT);
            }
            if (!ext.isEmpty()) {
                if (bitrate != null) {
                    try {
                        int kbps = Integer.parseInt(bitrate) / 1000;
                        formatBadge = ext + " • " + kbps + " KBPS";
                    } catch (Exception ignored) {
                        formatBadge = ext + " AUDIO";
                    }
                } else {
                    formatBadge = ext + " AUDIO";
                }
            }

            // Check embedded picture
            byte[] artBytes = retriever.getEmbeddedPicture();
            if (artBytes != null && artBytes.length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length);
                if (bitmap != null) {
                    binding.imgVinylDisc.setImageBitmap(bitmap);
                } else {
                    binding.imgVinylDisc.setImageResource(R.drawable.ic_audio_disc);
                }
            } else {
                binding.imgVinylDisc.setImageResource(R.drawable.ic_audio_disc);
            }
        } catch (Exception e) {
            e.printStackTrace();
            binding.imgVinylDisc.setImageResource(R.drawable.ic_audio_disc);
        }

        binding.tvAudioTitle.setText(title);
        binding.tvAudioArtist.setText(artist);
        binding.tvAudioFormatBadge.setText(formatBadge);
        binding.toolbar.setSubtitle(title);

        MediaItem exoItem = MediaItem.fromUri(uri);
        player.setMediaItem(exoItem);
        player.setPlaybackParameters(new PlaybackParameters(currentSpeed));
        player.prepare();
        player.play();
    }

    private void playNextTrack() {
        if (playlist.isEmpty()) return;
        currentTrackIndex = (currentTrackIndex + 1) % playlist.size();
        loadTrack(playlist.get(currentTrackIndex));
    }

    private void playPreviousTrack() {
        if (playlist.isEmpty()) return;
        if (player != null && player.getCurrentPosition() > 3000) {
            player.seekTo(0);
            return;
        }
        currentTrackIndex = (currentTrackIndex - 1 + playlist.size()) % playlist.size();
        loadTrack(playlist.get(currentTrackIndex));
    }

    private void startProgressTracker() {
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (player != null && player.isPlaying()) {
                    long pos = player.getCurrentPosition();
                    long dur = player.getDuration();
                    if (dur > 0) {
                        int progress = (int) ((pos * 1000) / dur);
                        binding.seekBarAudio.setProgress(progress);
                        binding.tvCurrentTime.setText(MediaUtils.formatDuration(pos));
                    }
                }
                progressHandler.postDelayed(this, 250);
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
                    binding.btnSpeed.setText(speeds[which].replace(" (Normal)", ""));
                    if (player != null) {
                        player.setPlaybackParameters(new PlaybackParameters(currentSpeed));
                    }
                })
                .show();
    }

    private void toggleRepeatMode() {
        if (repeatMode == Player.REPEAT_MODE_OFF) {
            repeatMode = Player.REPEAT_MODE_ALL;
            binding.btnRepeat.setImageResource(R.drawable.ic_repeat);
            binding.btnRepeat.setColorFilter(ContextCompat.getColor(this, R.color.md_theme_light_primary));
            Toast.makeText(this, "Repeat: All", Toast.LENGTH_SHORT).show();
        } else if (repeatMode == Player.REPEAT_MODE_ALL) {
            repeatMode = Player.REPEAT_MODE_ONE;
            binding.btnRepeat.setImageResource(R.drawable.ic_repeat_one);
            binding.btnRepeat.setColorFilter(ContextCompat.getColor(this, R.color.md_theme_light_primary));
            Toast.makeText(this, "Repeat: One", Toast.LENGTH_SHORT).show();
        } else {
            repeatMode = Player.REPEAT_MODE_OFF;
            binding.btnRepeat.setImageResource(R.drawable.ic_repeat);
            binding.btnRepeat.setColorFilter(ContextCompat.getColor(this, R.color.cat_others));
            Toast.makeText(this, "Repeat: Off", Toast.LENGTH_SHORT).show();
        }
        if (player != null) {
            player.setRepeatMode(repeatMode);
        }
    }

    private void toggleShuffleMode() {
        isShuffleEnabled = !isShuffleEnabled;
        if (isShuffleEnabled) {
            binding.btnShuffle.setColorFilter(ContextCompat.getColor(this, R.color.md_theme_light_primary));
            Collections.shuffle(playlist);
            currentTrackIndex = playlist.indexOf(currentItem);
            Toast.makeText(this, "Shuffle: On", Toast.LENGTH_SHORT).show();
        } else {
            binding.btnShuffle.setColorFilter(ContextCompat.getColor(this, R.color.cat_others));
            Toast.makeText(this, "Shuffle: Off", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareCurrentAudio() {
        if (currentItem == null) return;
        if (currentItem.getPath() != null) {
            FileUtils.shareFiles(this, Collections.singletonList(new File(currentItem.getPath())));
        } else if (currentItem.getUri() != null) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("audio/*");
            intent.putExtra(Intent.EXTRA_STREAM, currentItem.getUri());
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share Audio via"));
        }
    }

    private void showAudioPropertiesDialog() {
        if (currentItem == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("File Name: ").append(currentItem.getDisplayName()).append("\n\n");
        if (currentItem.getPath() != null) {
            sb.append("Location: ").append(currentItem.getPath()).append("\n\n");
        }
        sb.append("Size: ").append(FileUtils.formatFileSize(currentItem.getSize())).append("\n\n");
        if (player != null && player.getDuration() > 0) {
            sb.append("Duration: ").append(MediaUtils.formatDuration(player.getDuration())).append("\n\n");
        }
        if (currentItem.getDateModified() > 0) {
            sb.append("Modified: ").append(DateUtils.formatDateTime(currentItem.getDateModified())).append("\n\n");
        }
        sb.append("MIME Type: ").append(currentItem.getMimeType() != null ? currentItem.getMimeType() : "audio/*");

        new MaterialAlertDialogBuilder(this)
                .setTitle("Audio File Properties")
                .setMessage(sb.toString())
                .setPositiveButton("Close", null)
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing() && player != null) {
            player.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressRunnable != null) {
            progressHandler.removeCallbacks(progressRunnable);
        }
        if (discAnimator != null) {
            discAnimator.cancel();
        }
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
