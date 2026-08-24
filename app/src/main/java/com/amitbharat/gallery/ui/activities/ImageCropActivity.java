package com.amitbharat.gallery.ui.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.amitbharat.gallery.R;
import com.amitbharat.gallery.databinding.ActivityImageCropBinding;
import com.amitbharat.gallery.ui.custom.CropImageView;
import com.amitbharat.gallery.utils.FileUtils;
import com.amitbharat.gallery.utils.ImageEnhancer;
import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.Executors;

public class ImageCropActivity extends AppCompatActivity {

    private ActivityImageCropBinding binding;
    private String imagePath;
    private Bitmap originalBitmap;

    private float currentBrightness = 0f;
    private float currentContrast = 0f;
    private float currentSaturation = 0f;
    private float currentWarmth = 0f;
    private ImageEnhancer.FilterType currentFilter = ImageEnhancer.FilterType.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImageCropBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        imagePath = getIntent().getStringExtra("image_path");
        if (imagePath == null || !new File(imagePath).exists()) {
            finish();
            return;
        }

        setupToolbar();
        loadImage();
        setupControls();
        setupEnhancements();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnSaveConfig.setOnClickListener(v -> showAdvancedSaveOptionsDialog());
        binding.btnSave.setOnClickListener(v -> showQuickSaveDialog());
    }

    private void loadImage() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                originalBitmap = BitmapFactory.decodeFile(imagePath);
                runOnUiThread(() -> {
                    if (originalBitmap != null) {
                        binding.cropImageView.setImageBitmap(originalBitmap);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void setupControls() {
        // Tab switching: Crop (0), Adjust (1), Filters (2)
        binding.tabEditorMode.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                binding.sectionCrop.setVisibility(pos == 0 ? View.VISIBLE : View.GONE);
                binding.sectionAdjust.setVisibility(pos == 1 ? View.VISIBLE : View.GONE);
                binding.sectionFilters.setVisibility(pos == 2 ? View.VISIBLE : View.GONE);
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        binding.chipGroupRatios.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipFree) {
                binding.cropImageView.setAspectRatio(CropImageView.AspectRatio.FREE);
            } else if (id == R.id.chip1_1) {
                binding.cropImageView.setAspectRatio(CropImageView.AspectRatio.SQUARE_1_1);
            } else if (id == R.id.chip4_3) {
                binding.cropImageView.setAspectRatio(CropImageView.AspectRatio.RATIO_4_3);
            } else if (id == R.id.chip16_9) {
                binding.cropImageView.setAspectRatio(CropImageView.AspectRatio.RATIO_16_9);
            }
        });

        binding.btnRotate.setOnClickListener(v -> binding.cropImageView.rotate90());
        binding.btnFlipH.setOnClickListener(v -> binding.cropImageView.flipHorizontal());
        binding.btnFlipV.setOnClickListener(v -> binding.cropImageView.flipVertical());
    }

    private void setupEnhancements() {
        // Brightness Slider
        binding.sliderBrightness.addOnChangeListener((slider, value, fromUser) -> {
            currentBrightness = value;
            binding.tvValBrightness.setText(String.valueOf(Math.round(value)));
            updateLivePreviewFilter();
        });

        // Contrast Slider
        binding.sliderContrast.addOnChangeListener((slider, value, fromUser) -> {
            currentContrast = value;
            binding.tvValContrast.setText(String.valueOf(Math.round(value)));
            updateLivePreviewFilter();
        });

        // Saturation Slider
        binding.sliderSaturation.addOnChangeListener((slider, value, fromUser) -> {
            currentSaturation = value;
            binding.tvValSaturation.setText(String.valueOf(Math.round(value)));
            updateLivePreviewFilter();
        });

        // Warmth Slider
        binding.sliderWarmth.addOnChangeListener((slider, value, fromUser) -> {
            currentWarmth = value;
            binding.tvValWarmth.setText(String.valueOf(Math.round(value)));
            updateLivePreviewFilter();
        });

        // Reset Adjustments
        binding.btnResetAdjustments.setOnClickListener(v -> {
            binding.sliderBrightness.setValue(0f);
            binding.sliderContrast.setValue(0f);
            binding.sliderSaturation.setValue(0f);
            binding.sliderWarmth.setValue(0f);
            currentBrightness = 0f;
            currentContrast = 0f;
            currentSaturation = 0f;
            currentWarmth = 0f;
            updateLivePreviewFilter();
        });

        // Direct Filter Chip Listeners for instant response
        binding.chipFilterOriginal.setOnClickListener(v -> selectFilter(ImageEnhancer.FilterType.NONE, R.id.chipFilterOriginal));
        binding.chipFilterAuto.setOnClickListener(v -> selectFilter(ImageEnhancer.FilterType.AUTO_ENHANCE, R.id.chipFilterAuto));
        binding.chipFilterVivid.setOnClickListener(v -> selectFilter(ImageEnhancer.FilterType.VIVID, R.id.chipFilterVivid));
        binding.chipFilterBW.setOnClickListener(v -> selectFilter(ImageEnhancer.FilterType.BW, R.id.chipFilterBW));
        binding.chipFilterWarm.setOnClickListener(v -> selectFilter(ImageEnhancer.FilterType.WARM, R.id.chipFilterWarm));
        binding.chipFilterCool.setOnClickListener(v -> selectFilter(ImageEnhancer.FilterType.COOL, R.id.chipFilterCool));
        binding.chipFilterVintage.setOnClickListener(v -> selectFilter(ImageEnhancer.FilterType.VINTAGE, R.id.chipFilterVintage));
        binding.chipFilterDramatic.setOnClickListener(v -> selectFilter(ImageEnhancer.FilterType.DRAMATIC, R.id.chipFilterDramatic));
    }

    private void selectFilter(ImageEnhancer.FilterType filter, int chipId) {
        currentFilter = filter;
        binding.chipGroupFilters.check(chipId);
        updateLivePreviewFilter();
    }

    private void updateLivePreviewFilter() {
        ColorMatrix matrix = ImageEnhancer.createAdjustmentMatrix(currentBrightness, currentContrast, currentSaturation, currentWarmth);
        if (currentFilter != ImageEnhancer.FilterType.NONE) {
            ColorMatrix filterMat = ImageEnhancer.getFilterMatrix(currentFilter);
            matrix.postConcat(filterMat);
        }
        binding.cropImageView.setColorMatrix(matrix);
    }

    /**
     * Default quick save flow: Fast, non-intrusive without prompting for height/width/size
     */
    private void showQuickSaveDialog() {
        if (originalBitmap == null) return;

        String[] options = new String[]{
                "Save as New Copy (Default)",
                "Overwrite Original",
                "⚙️ Custom Dimensions & Target Size..."
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle("Save Image")
                .setItems(options, (d, which) -> {
                    if (which == 0) { // Save as Copy
                        processAndSaveImage(-1, -1, 95, false);
                    } else if (which == 1) { // Overwrite
                        processAndSaveImage(-1, -1, 95, true);
                    } else if (which == 2) { // Advanced config
                        showAdvancedSaveOptionsDialog();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAdvancedSaveOptionsDialog() {
        if (originalBitmap == null) return;

        Bitmap croppedPreview = binding.cropImageView.cropBitmap(originalBitmap);
        int origW = croppedPreview.getWidth();
        int origH = croppedPreview.getHeight();
        final double aspectRatio = (double) origW / (double) Math.max(1, origH);

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_crop_save_options, null);

        TextInputEditText etWidth = view.findViewById(R.id.etCustomWidth);
        TextInputEditText etHeight = view.findViewById(R.id.etCustomHeight);
        MaterialCheckBox cbLockAspect = view.findViewById(R.id.cbLockAspect);
        SeekBar seekBarQuality = view.findViewById(R.id.seekBarQuality);
        TextView tvQualityValue = view.findViewById(R.id.tvQualityValue);
        TextView tvEstimatedSize = view.findViewById(R.id.tvEstimatedSize);

        ChipGroup chipGroupRes = view.findViewById(R.id.chipGroupResolutionPresets);
        ChipGroup chipGroupSize = view.findViewById(R.id.chipGroupSizePresets);

        etWidth.setText(String.valueOf(origW));
        etHeight.setText(String.valueOf(origH));

        final boolean[] isUpdatingText = {false};

        Runnable updateSizeEstimate = () -> {
            try {
                int w = Integer.parseInt(etWidth.getText().toString());
                int h = Integer.parseInt(etHeight.getText().toString());
                int q = seekBarQuality.getProgress();
                long estBytes = (long) ((w * h * 3L * (q / 100.0)) / 12);
                tvEstimatedSize.setText("Est: ~" + FileUtils.formatFileSize(Math.max(1024, estBytes)));
            } catch (Exception ignored) {}
        };

        etWidth.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdatingText[0]) return;
                if (cbLockAspect.isChecked() && s.length() > 0) {
                    try {
                        int w = Integer.parseInt(s.toString());
                        int h = (int) Math.round(w / aspectRatio);
                        isUpdatingText[0] = true;
                        etHeight.setText(String.valueOf(Math.max(1, h)));
                        isUpdatingText[0] = false;
                    } catch (Exception ignored) {}
                }
                updateSizeEstimate.run();
            }
        });

        etHeight.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdatingText[0]) return;
                if (cbLockAspect.isChecked() && s.length() > 0) {
                    try {
                        int h = Integer.parseInt(s.toString());
                        int w = (int) Math.round(h * aspectRatio);
                        isUpdatingText[0] = true;
                        etWidth.setText(String.valueOf(Math.max(1, w)));
                        isUpdatingText[0] = false;
                    } catch (Exception ignored) {}
                }
                updateSizeEstimate.run();
            }
        });

        chipGroupRes.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            isUpdatingText[0] = true;
            if (id == R.id.chipPresetOriginal) {
                etWidth.setText(String.valueOf(origW));
                etHeight.setText(String.valueOf(origH));
            } else if (id == R.id.chipPreset1080p) {
                int targetW = 1920;
                int targetH = (int) Math.round(targetW / aspectRatio);
                etWidth.setText(String.valueOf(targetW));
                etHeight.setText(String.valueOf(targetH));
            } else if (id == R.id.chipPreset720p) {
                int targetW = 1280;
                int targetH = (int) Math.round(targetW / aspectRatio);
                etWidth.setText(String.valueOf(targetW));
                etHeight.setText(String.valueOf(targetH));
            } else if (id == R.id.chipPresetAvatar) {
                etWidth.setText("500");
                etHeight.setText("500");
            }
            isUpdatingText[0] = false;
            updateSizeEstimate.run();
        });

        seekBarQuality.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvQualityValue.setText(progress + "%");
                updateSizeEstimate.run();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        chipGroupSize.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipSizeMax) {
                seekBarQuality.setProgress(95);
            } else if (id == R.id.chipSize2MB) {
                seekBarQuality.setProgress(85);
            } else if (id == R.id.chipSize1MB) {
                seekBarQuality.setProgress(70);
            } else if (id == R.id.chipSize500KB) {
                seekBarQuality.setProgress(50);
            }
        });

        updateSizeEstimate.run();

        view.findViewById(R.id.btnSaveNewCopy).setOnClickListener(v -> {
            int targetW = parseIntSafe(etWidth.getText().toString(), origW);
            int targetH = parseIntSafe(etHeight.getText().toString(), origH);
            int quality = seekBarQuality.getProgress();
            dialog.dismiss();
            processAndSaveImage(targetW, targetH, quality, false);
        });

        view.findViewById(R.id.btnOverwrite).setOnClickListener(v -> {
            int targetW = parseIntSafe(etWidth.getText().toString(), origW);
            int targetH = parseIntSafe(etHeight.getText().toString(), origH);
            int quality = seekBarQuality.getProgress();
            dialog.dismiss();
            processAndSaveImage(targetW, targetH, quality, true);
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private int parseIntSafe(String val, int fallback) {
        try {
            int parsed = Integer.parseInt(val.trim());
            return parsed > 0 ? parsed : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    private void processAndSaveImage(int targetWidth, int targetHeight, int quality, boolean overwrite) {
        if (originalBitmap == null) return;
        Toast.makeText(this, "Saving enhanced image...", Toast.LENGTH_SHORT).show();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // 1. Apply geometric crop, rotation & flips
                Bitmap cropped = binding.cropImageView.cropBitmap(originalBitmap);

                // 2. Apply enhancements (brightness, contrast, saturation, warmth, filter)
                Bitmap enhanced = ImageEnhancer.applyEnhancements(
                        cropped,
                        currentBrightness,
                        currentContrast,
                        currentSaturation,
                        currentWarmth,
                        currentFilter
                );

                Bitmap finalBitmap = enhanced != null ? enhanced : cropped;

                // 3. Scale dimensions if requested
                if (targetWidth > 0 && targetHeight > 0 && (targetWidth != finalBitmap.getWidth() || targetHeight != finalBitmap.getHeight())) {
                    finalBitmap = Bitmap.createScaledBitmap(finalBitmap, targetWidth, targetHeight, true);
                }

                File targetFile;
                if (overwrite) {
                    targetFile = new File(imagePath);
                } else {
                    File original = new File(imagePath);
                    String name = original.getName();
                    int dot = name.lastIndexOf('.');
                    String baseName = (dot > 0) ? name.substring(0, dot) : name;
                    String ext = (dot > 0) ? name.substring(dot) : ".jpg";
                    targetFile = new File(original.getParentFile(), baseName + "_edited_" + System.currentTimeMillis() + ext);
                }

                FileOutputStream fos = new FileOutputStream(targetFile);
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, Math.min(100, Math.max(10, quality)), fos);
                fos.flush();
                fos.close();

                MediaScannerConnection.scanFile(this, new String[]{targetFile.getAbsolutePath()}, null, null);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Saved: " + targetFile.getName(), Toast.LENGTH_LONG).show();
                    finish();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Failed to save image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (originalBitmap != null && !originalBitmap.isRecycled()) {
            originalBitmap.recycle();
        }
    }
}
