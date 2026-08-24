package com.amitbharat.gallery.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.amitbharat.gallery.R;
import com.amitbharat.gallery.data.database.AppDatabase;
import com.amitbharat.gallery.data.database.FavoriteDao;
import com.amitbharat.gallery.data.database.FavoriteEntity;
import com.amitbharat.gallery.data.models.ExifInfo;
import com.amitbharat.gallery.data.models.MediaItem;
import com.amitbharat.gallery.databinding.ActivityImageViewerBinding;
import com.amitbharat.gallery.ui.adapters.ViewPagerMediaAdapter;
import com.amitbharat.gallery.utils.DateUtils;
import com.amitbharat.gallery.utils.ExifHelper;
import com.amitbharat.gallery.utils.FileUtils;
import com.amitbharat.gallery.utils.MediaUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

public class ImageViewerActivity extends AppCompatActivity implements ViewPagerMediaAdapter.OnMediaViewerListener {

    private ActivityImageViewerBinding binding;
    private ViewPagerMediaAdapter adapter;
    private final List<MediaItem> mediaList = new ArrayList<>();
    private int currentPosition = 0;
    private boolean isOverlayVisible = true;
    private boolean isSlideshowRunning = false;
    private final Handler slideshowHandler = new Handler(Looper.getMainLooper());
    private Runnable slideshowRunnable;
    private FavoriteDao favoriteDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImageViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        favoriteDao = AppDatabase.getInstance(this).favoriteDao();

        currentPosition = getIntent().getIntExtra("current_position", 0);
        List<MediaItem> holderList = com.amitbharat.gallery.utils.MediaHolder.getCurrentMediaList();
        if (holderList != null && !holderList.isEmpty()) {
            mediaList.addAll(holderList);
        } else {
            List<MediaItem> list = (List<MediaItem>) getIntent().getSerializableExtra("media_list");
            if (list != null) {
                mediaList.addAll(list);
            }
        }

        if (mediaList.isEmpty()) {
            finish();
            return;
        }

        setupViewPager();
        setupControls();
        updateOverlay(currentPosition);
    }

    private void setupViewPager() {
        adapter = new ViewPagerMediaAdapter(this);
        adapter.setListener(this);
        adapter.submitList(mediaList);

        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setCurrentItem(currentPosition, false);

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPosition = position;
                updateOverlay(position);
            }
        });
    }

    private void updateOverlay(int position) {
        if (position < 0 || position >= mediaList.size()) return;
        MediaItem item = mediaList.get(position);
        binding.tvTitle.setText(item.getDisplayName());
        binding.tvSubTitle.setText((position + 1) + " of " + mediaList.size() + " • " + DateUtils.formatDate(item.getDateAdded()));

        Executors.newSingleThreadExecutor().execute(() -> {
            boolean isFav = item.getPath() != null && favoriteDao.isFavorite(item.getPath());
            runOnUiThread(() -> {
                binding.btnFavorite.setImageResource(isFav ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite);
            });
        });
    }

    private void setupControls() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnInfo.setOnClickListener(v -> showExifInfoBottomSheet());

        binding.btnVault.setOnClickListener(v -> {
            if (currentPosition < mediaList.size()) {
                MediaItem item = mediaList.get(currentPosition);
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Hide in Secure Vault?")
                        .setMessage("This item will be moved to your private encrypted vault and hidden from the public gallery.")
                        .setPositiveButton("Hide in Vault", (d, w) -> {
                            new com.amitbharat.gallery.data.repository.VaultRepository(this).hideMedia(item, () -> {
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "Moved to Secure Vault", Toast.LENGTH_SHORT).show();
                                    mediaList.remove(currentPosition);
                                    if (mediaList.isEmpty()) {
                                        finish();
                                    } else {
                                        adapter.submitList(new ArrayList<>(mediaList));
                                        updateOverlay(Math.min(currentPosition, mediaList.size() - 1));
                                    }
                                });
                            });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        binding.btnSlideshow.setOnClickListener(v -> toggleSlideshow());

        binding.btnShare.setOnClickListener(v -> {
            if (currentPosition < mediaList.size()) {
                MediaItem item = mediaList.get(currentPosition);
                if (item.getPath() != null) {
                    FileUtils.shareFiles(this, Collections.singletonList(new File(item.getPath())));
                }
            }
        });

        binding.btnFavorite.setOnClickListener(v -> {
            if (currentPosition < mediaList.size()) {
                MediaItem item = mediaList.get(currentPosition);
                if (item.getPath() == null) return;
                Executors.newSingleThreadExecutor().execute(() -> {
                    boolean isFav = favoriteDao.isFavorite(item.getPath());
                    if (isFav) {
                        favoriteDao.deleteByPath(item.getPath());
                        item.setFavorite(false);
                    } else {
                        favoriteDao.insertFavorite(new FavoriteEntity(item.getPath(), System.currentTimeMillis()));
                        item.setFavorite(true);
                    }
                    runOnUiThread(() -> {
                        binding.btnFavorite.setImageResource(item.isFavorite() ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite);
                    });
                });
            }
        });

        binding.btnCrop.setOnClickListener(v -> {
            if (currentPosition < mediaList.size()) {
                MediaItem item = mediaList.get(currentPosition);
                Intent intent = new Intent(this, ImageCropActivity.class);
                intent.putExtra("image_path", item.getPath());
                startActivity(intent);
            }
        });

        binding.btnWallpaper.setOnClickListener(v -> {
            if (currentPosition < mediaList.size()) {
                MediaItem item = mediaList.get(currentPosition);
                boolean success = MediaUtils.setWallpaper(this, item.getUri());
                Toast.makeText(this, success ? "Wallpaper set successfully!" : "Failed to set wallpaper", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnDelete.setOnClickListener(v -> {
            if (currentPosition < mediaList.size()) {
                MediaItem item = mediaList.get(currentPosition);
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Delete Media?")
                        .setMessage("Move \"" + item.getDisplayName() + "\" to the Recycle Bin? You can restore it later.")
                        .setPositiveButton("Yes", (d, which) -> {
                            new com.amitbharat.gallery.data.repository.TrashRepository(this).moveToTrash(item, () -> {
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "Moved to Recycle Bin", Toast.LENGTH_SHORT).show();
                                    mediaList.remove(currentPosition);
                                    if (mediaList.isEmpty()) {
                                        finish();
                                    } else {
                                        adapter.submitList(new ArrayList<>(mediaList));
                                        updateOverlay(Math.min(currentPosition, mediaList.size() - 1));
                                    }
                                });
                            });
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
    }

    private void showExifInfoBottomSheet() {
        if (currentPosition >= mediaList.size()) return;
        MediaItem item = mediaList.get(currentPosition);
        ExifInfo info = ExifHelper.extractExif(this, item);

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_file_properties, null);

        TextView tvName = view.findViewById(R.id.tvPropFileName);
        TextView tvPath = view.findViewById(R.id.tvPropPath);
        TextView tvSize = view.findViewById(R.id.tvPropSize);
        TextView tvResolution = view.findViewById(R.id.tvPropResolution);
        TextView tvMime = view.findViewById(R.id.tvPropMime);
        TextView tvDate = view.findViewById(R.id.tvPropDate);

        View headerVideo = view.findViewById(R.id.tvHeaderVideo);
        View tableVideo = view.findViewById(R.id.tableVideoExif);
        View headerCamera = view.findViewById(R.id.tvHeaderCamera);
        View tableCamera = view.findViewById(R.id.tableCameraExif);

        tvName.setText(info.getFileName());
        tvPath.setText(info.getFilePath());
        tvSize.setText(FileUtils.formatFileSize(info.getFileSize()) + " (" + info.getFileSize() + " bytes)");
        tvResolution.setText(info.getResolution() + (!info.getMegapixels().isEmpty() ? " • " + info.getMegapixels() : ""));
        tvMime.setText(info.getMimeType());
        tvDate.setText(info.getDateModified());

        if (info.isVideo()) {
            if (headerVideo != null) headerVideo.setVisibility(View.VISIBLE);
            if (tableVideo != null) tableVideo.setVisibility(View.VISIBLE);
            if (headerCamera != null) headerCamera.setVisibility(View.GONE);
            if (tableCamera != null) tableCamera.setVisibility(View.GONE);

            TextView tvVideoDuration = view.findViewById(R.id.tvVideoDuration);
            TextView tvVideoBitrate = view.findViewById(R.id.tvVideoBitrate);
            TextView tvVideoCodec = view.findViewById(R.id.tvVideoCodec);
            TextView tvVideoAudio = view.findViewById(R.id.tvVideoAudio);
            TextView tvVideoOrientation = view.findViewById(R.id.tvVideoOrientation);
            TextView tvVideoDate = view.findViewById(R.id.tvVideoDate);

            tvVideoDuration.setText(info.getDuration());
            tvVideoBitrate.setText(info.getBitrate());
            tvVideoCodec.setText(info.getVideoCodec());
            tvVideoAudio.setText(info.getAudioCodec());
            tvVideoOrientation.setText(info.getOrientation());
            tvVideoDate.setText(info.getDateTaken());
        } else {
            if (headerVideo != null) headerVideo.setVisibility(View.GONE);
            if (tableVideo != null) tableVideo.setVisibility(View.GONE);
            if (headerCamera != null) headerCamera.setVisibility(View.VISIBLE);
            if (tableCamera != null) tableCamera.setVisibility(View.VISIBLE);

            TextView tvExifCamera = view.findViewById(R.id.tvExifCamera);
            TextView tvExifDateTaken = view.findViewById(R.id.tvExifDateTaken);
            TextView tvExifAperture = view.findViewById(R.id.tvExifAperture);
            TextView tvExifShutter = view.findViewById(R.id.tvExifShutter);
            TextView tvExifIso = view.findViewById(R.id.tvExifIso);
            TextView tvExifFocal = view.findViewById(R.id.tvExifFocal);
            TextView tvExifFlash = view.findViewById(R.id.tvExifFlash);
            TextView tvExifWb = view.findViewById(R.id.tvExifWb);
            TextView tvExifLocation = view.findViewById(R.id.tvExifLocation);

            tvExifCamera.setText(info.getCameraModel());
            tvExifDateTaken.setText(info.getDateTaken());
            tvExifAperture.setText(info.getAperture());
            tvExifShutter.setText(info.getExposureTime());
            tvExifIso.setText(info.getIso());
            tvExifFocal.setText(info.getFocalLength() + (!info.getFocalLength35mm().isEmpty() ? " (" + info.getFocalLength35mm() + ")" : ""));
            tvExifFlash.setText(info.getFlash());
            tvExifWb.setText(info.getWhiteBalance());
            tvExifLocation.setText(info.getLocationText() + (!info.getGpsAltitude().isEmpty() ? " • " + info.getGpsAltitude() : ""));
        }

        dialog.setContentView(view);
        dialog.show();
    }

    private void toggleSlideshow() {
        isSlideshowRunning = !isSlideshowRunning;
        if (isSlideshowRunning) {
            Toast.makeText(this, "Slideshow started", Toast.LENGTH_SHORT).show();
            slideshowRunnable = new Runnable() {
                @Override
                public void run() {
                    if (currentPosition < mediaList.size() - 1) {
                        binding.viewPager.setCurrentItem(currentPosition + 1, true);
                        slideshowHandler.postDelayed(this, 3000);
                    } else {
                        isSlideshowRunning = false;
                        Toast.makeText(ImageViewerActivity.this, "Slideshow finished", Toast.LENGTH_SHORT).show();
                    }
                }
            };
            slideshowHandler.postDelayed(slideshowRunnable, 3000);
        } else {
            slideshowHandler.removeCallbacks(slideshowRunnable);
            Toast.makeText(this, "Slideshow stopped", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSingleTap() {
        isOverlayVisible = !isOverlayVisible;
        binding.topBar.setVisibility(isOverlayVisible ? View.VISIBLE : View.GONE);
        binding.bottomBar.setVisibility(isOverlayVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onPlayVideoClick(MediaItem item) {
        Intent intent = new Intent(this, VideoPlayerActivity.class);
        intent.putExtra("media_item", item);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (slideshowRunnable != null) {
            slideshowHandler.removeCallbacks(slideshowRunnable);
        }
    }
}
