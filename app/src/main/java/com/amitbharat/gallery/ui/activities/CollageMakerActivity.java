package com.amitbharat.gallery.ui.activities;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.amitbharat.gallery.R;
import com.amitbharat.gallery.databinding.ActivityCollageMakerBinding;
import com.amitbharat.gallery.ui.custom.CollageView;
import com.amitbharat.gallery.utils.ImageEnhancer;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class CollageMakerActivity extends AppCompatActivity {

    private ActivityCollageMakerBinding binding;
    private final List<Bitmap> loadedBitmaps = new ArrayList<>();
    private ArrayList<String> imagePaths;

    private static class ColorOption {
        final String name;
        final int color;

        ColorOption(String name, int color) {
            this.name = name;
            this.color = color;
        }
    }

    private final ColorOption[] solidColors = new ColorOption[]{
            new ColorOption("White", Color.WHITE),
            new ColorOption("Black", Color.BLACK),
            new ColorOption("Dark Charcoal", 0xFF212121),
            new ColorOption("Light Gray", 0xFFE0E0E0),
            new ColorOption("Cream", 0xFFFFFDD0),
            new ColorOption("Pastel Blue", 0xFFBBDEFB),
            new ColorOption("Pastel Pink", 0xFFF8BBD0),
            new ColorOption("Mint Green", 0xFFC8E6C9),
            new ColorOption("Soft Lavender", 0xFFE1BEE7),
            new ColorOption("Warm Amber", 0xFFFFE082)
    };

    private final CollageView.GradientPreset[] gradientPresets = new CollageView.GradientPreset[]{
            new CollageView.GradientPreset("Sunset Glow", new int[]{0xFFFF512F, 0xFFDD2476}),
            new CollageView.GradientPreset("Neon Night", new int[]{0xFF8A2387, 0xFFE94057, 0xFFF27121}),
            new CollageView.GradientPreset("Ocean Breeze", new int[]{0xFF2E3192, 0xFF1BFFFF}),
            new CollageView.GradientPreset("Emerald Forest", new int[]{0xFF0BA360, 0xFF3CBA92}),
            new CollageView.GradientPreset("Rose Gold", new int[]{0xFFFAD0C4, 0xFFFFD1FF}),
            new CollageView.GradientPreset("Cyberpunk", new int[]{0xFFFF007F, 0xFF7928CA}),
            new CollageView.GradientPreset("Midnight Aura", new int[]{0xFF0F2027, 0xFF203A43, 0xFF2C5364})
    };

    private final String[] stickerEmojis = new String[]{
            "❤️", "⭐", "🔥", "🎉", "✨", "🌸", "😎", "📸", "👑", "🥳", "🌈", "💯", "🕶️", "💫", "💖", "🍕", "🏖️", "🚀", "💎", "🎨"
    };

    private final int[] doodleColors = new int[]{
            0xFFFF1744, // Red
            0xFFFFEA00, // Yellow
            0xFF00E676, // Green
            0xFF00E5FF, // Cyan
            0xFFD500F9, // Purple
            0xFFFF9100, // Orange
            0xFFFFFFFF, // White
            0xFF000000  // Black
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCollageMakerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        imagePaths = getIntent().getStringArrayListExtra("image_paths");
        if (imagePaths == null || imagePaths.size() < 2) {
            Toast.makeText(this, R.string.collage_min_images_msg, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        setupTabs();
        setupTileActions();
        setupRatios();
        setupBorders();
        setupBackgrounds();
        setupFilters();
        setupTextAndStickers();
        setupDoodle();
        loadBitmaps();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnSaveCollage.setOnClickListener(v -> saveCollage());
    }

    private void setupTabs() {
        binding.tabCollageOptions.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                binding.sectionLayouts.setVisibility(pos == 0 ? View.VISIBLE : View.GONE);
                binding.sectionRatios.setVisibility(pos == 1 ? View.VISIBLE : View.GONE);
                binding.sectionBorders.setVisibility(pos == 2 ? View.VISIBLE : View.GONE);
                binding.sectionColors.setVisibility(pos == 3 ? View.VISIBLE : View.GONE);
                binding.sectionFilters.setVisibility(pos == 4 ? View.VISIBLE : View.GONE);
                binding.sectionTextStickers.setVisibility(pos == 5 ? View.VISIBLE : View.GONE);
                binding.sectionDoodle.setVisibility(pos == 6 ? View.VISIBLE : View.GONE);

                if (pos == 6) {
                    binding.collageView.setInteractiveMode(CollageView.InteractiveMode.DOODLE);
                    binding.tvSwapHint.setText("✏️ Draw freely with your finger on the canvas");
                } else {
                    binding.collageView.setInteractiveMode(CollageView.InteractiveMode.SELECT_TILE);
                    binding.tvSwapHint.setText("Tap image to edit/rotate/filter or tap two to swap");
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupTileActions() {
        binding.collageView.setOnCollageEventListener(new CollageView.OnCollageEventListener() {
            @Override
            public void onTileSelected(int index, CollageView.TileState tile) {
                binding.layoutTileActions.setVisibility(View.VISIBLE);
                binding.tvSelectedTileLabel.setText("Image " + (index + 1) + " Selected");
                binding.tvSwapHint.setText("Tap another image to swap positions, or use tools above");
            }

            @Override
            public void onTileDeselected() {
                binding.layoutTileActions.setVisibility(View.GONE);
                binding.tvSwapHint.setText("Tap image to edit/rotate/filter or tap two to swap");
            }

            @Override
            public void onTilesSwapped(int index1, int index2) {
                Toast.makeText(CollageMakerActivity.this, "Swapped image " + (index1 + 1) + " and " + (index2 + 1), Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnRotateTile.setOnClickListener(v -> binding.collageView.rotateSelectedTile());
        binding.btnFlipHTile.setOnClickListener(v -> binding.collageView.flipSelectedTileH());
        binding.btnFlipVTile.setOnClickListener(v -> binding.collageView.flipSelectedTileV());
        binding.btnZoomInTile.setOnClickListener(v -> {
            CollageView.TileState tile = binding.collageView.getSelectedTile();
            if (tile != null) {
                if (tile.zoom >= 2.5f) {
                    binding.collageView.resetSelectedTileZoom();
                    Toast.makeText(this, "Zoom reset (1x)", Toast.LENGTH_SHORT).show();
                } else {
                    binding.collageView.zoomSelectedTile(1.3f);
                    Toast.makeText(this, "Zoom: " + String.format(Locale.getDefault(), "%.1fx", tile.zoom), Toast.LENGTH_SHORT).show();
                }
            }
        });
        binding.btnCloseTileAction.setOnClickListener(v -> binding.collageView.deselectTile());
    }

    private void loadBitmaps() {
        binding.progressBar.setVisibility(View.VISIBLE);
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Bitmap> bitmaps = new ArrayList<>();
            for (String path : imagePaths) {
                try {
                    Bitmap bmp = decodeSampledBitmapFromFile(path, 1200, 1200);
                    if (bmp != null) {
                        bitmaps.add(bmp);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            runOnUiThread(() -> {
                binding.progressBar.setVisibility(View.GONE);
                if (bitmaps.size() < 2) {
                    Toast.makeText(this, "Failed to load enough images for collage", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                loadedBitmaps.clear();
                loadedBitmaps.addAll(bitmaps);
                binding.collageView.setBitmaps(loadedBitmaps);
                setupLayoutOptions();
            });
        });
    }

    private void setupLayoutOptions() {
        binding.chipGroupLayouts.removeAllViews();
        List<CollageView.Template> templates = CollageView.getAvailableTemplates(loadedBitmaps.size());

        for (int i = 0; i < templates.size(); i++) {
            CollageView.Template t = templates.get(i);
            Chip chip = new Chip(this, null, com.google.android.material.R.style.Widget_Material3_Chip_Filter);
            chip.setText(t.name);
            chip.setTextColor(Color.WHITE);
            chip.setCheckable(true);
            final int index = i;
            if (i == 0) chip.setChecked(true);

            chip.setOnClickListener(v -> {
                binding.chipGroupLayouts.check(chip.getId());
                binding.collageView.setTemplateIndex(index);
            });
            binding.chipGroupLayouts.addView(chip);
        }
    }

    private void setupRatios() {
        binding.chipGroupRatios.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipRatio1_1) {
                binding.collageView.setAspectRatio(CollageView.AspectRatio.RATIO_1_1);
            } else if (id == R.id.chipRatio4_5) {
                binding.collageView.setAspectRatio(CollageView.AspectRatio.RATIO_4_5);
            } else if (id == R.id.chipRatio3_4) {
                binding.collageView.setAspectRatio(CollageView.AspectRatio.RATIO_3_4);
            } else if (id == R.id.chipRatio16_9) {
                binding.collageView.setAspectRatio(CollageView.AspectRatio.RATIO_16_9);
            } else if (id == R.id.chipRatio9_16) {
                binding.collageView.setAspectRatio(CollageView.AspectRatio.RATIO_9_16);
            }
        });
    }

    private void setupBorders() {
        binding.sliderInnerPadding.addOnChangeListener((slider, value, fromUser) -> {
            binding.collageView.setInnerPadding(value);
        });

        binding.sliderOuterMargin.addOnChangeListener((slider, value, fromUser) -> {
            binding.collageView.setOuterMargin(value);
        });

        binding.sliderCornerRadius.addOnChangeListener((slider, value, fromUser) -> {
            binding.collageView.setCornerRadius(value);
        });
    }

    private void setupBackgrounds() {
        // Solid colors
        binding.chipGroupColors.removeAllViews();
        for (int i = 0; i < solidColors.length; i++) {
            ColorOption opt = solidColors[i];
            Chip chip = new Chip(this, null, com.google.android.material.R.style.Widget_Material3_Chip_Filter);
            chip.setText(opt.name);
            chip.setTextColor(Color.WHITE);
            chip.setChipIconTint(ColorStateList.valueOf(opt.color));
            chip.setCheckable(true);
            if (i == 0) chip.setChecked(true);

            chip.setOnClickListener(v -> {
                binding.chipGroupGradients.clearCheck();
                binding.chipGroupColors.check(chip.getId());
                binding.collageView.setSolidBackgroundColor(opt.color);
            });
            binding.chipGroupColors.addView(chip);
        }

        // Gradients
        binding.chipGroupGradients.removeAllViews();
        for (int i = 0; i < gradientPresets.length; i++) {
            CollageView.GradientPreset preset = gradientPresets[i];
            Chip chip = new Chip(this, null, com.google.android.material.R.style.Widget_Material3_Chip_Filter);
            chip.setText(preset.name);
            chip.setTextColor(Color.WHITE);
            chip.setChipIconTint(ColorStateList.valueOf(preset.colors[0]));
            chip.setCheckable(true);

            chip.setOnClickListener(v -> {
                binding.chipGroupColors.clearCheck();
                binding.chipGroupGradients.check(chip.getId());
                binding.collageView.setGradientBackground(preset);
            });
            binding.chipGroupGradients.addView(chip);
        }
    }

    private void setupFilters() {
        binding.chipFilterOriginal.setOnClickListener(v -> applyFilter(ImageEnhancer.FilterType.NONE, R.id.chipFilterOriginal));
        binding.chipFilterAuto.setOnClickListener(v -> applyFilter(ImageEnhancer.FilterType.AUTO_ENHANCE, R.id.chipFilterAuto));
        binding.chipFilterVivid.setOnClickListener(v -> applyFilter(ImageEnhancer.FilterType.VIVID, R.id.chipFilterVivid));
        binding.chipFilterBW.setOnClickListener(v -> applyFilter(ImageEnhancer.FilterType.BW, R.id.chipFilterBW));
        binding.chipFilterWarm.setOnClickListener(v -> applyFilter(ImageEnhancer.FilterType.WARM, R.id.chipFilterWarm));
        binding.chipFilterCool.setOnClickListener(v -> applyFilter(ImageEnhancer.FilterType.COOL, R.id.chipFilterCool));
        binding.chipFilterVintage.setOnClickListener(v -> applyFilter(ImageEnhancer.FilterType.VINTAGE, R.id.chipFilterVintage));
        binding.chipFilterDramatic.setOnClickListener(v -> applyFilter(ImageEnhancer.FilterType.DRAMATIC, R.id.chipFilterDramatic));
    }

    private void applyFilter(ImageEnhancer.FilterType filter, int chipId) {
        binding.chipGroupFilters.check(chipId);
        if (binding.collageView.getSelectedTile() != null) {
            binding.collageView.setFilterForSelectedTile(filter);
            Toast.makeText(this, "Filter applied to selected image", Toast.LENGTH_SHORT).show();
        } else {
            // Apply to all tiles
            for (CollageView.TileState tile : binding.collageView.getTiles()) {
                tile.filter = filter;
            }
            binding.collageView.invalidate();
            Toast.makeText(this, "Filter applied to all images", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupTextAndStickers() {
        binding.btnAddText.setOnClickListener(v -> showAddTextDialog());

        binding.chipGroupStickers.removeAllViews();
        for (String emoji : stickerEmojis) {
            Chip chip = new Chip(this, null, com.google.android.material.R.style.Widget_Material3_Chip_Filter);
            chip.setText(emoji);
            chip.setTextSize(20f);
            chip.setTextColor(Color.WHITE);
            chip.setOnClickListener(v -> {
                binding.collageView.addStickerOverlay(new CollageView.StickerOverlay(emoji, 0.5f, 0.5f));
                Toast.makeText(this, "Sticker added! Drag to place", Toast.LENGTH_SHORT).show();
            });
            binding.chipGroupStickers.addView(chip);
        }
    }

    private void showAddTextDialog() {
        final EditText et = new EditText(this);
        et.setHint("Enter caption / text...");
        et.setPadding(40, 40, 40, 40);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Add Text Overlay")
                .setView(et)
                .setPositiveButton("Add", (d, w) -> {
                    String txt = et.getText().toString().trim();
                    if (!txt.isEmpty()) {
                        binding.collageView.addTextOverlay(new CollageView.TextOverlay(txt, Color.WHITE, 0xAA000000));
                        Toast.makeText(this, "Text added! Drag on canvas to place", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupDoodle() {
        binding.btnUndoDoodle.setOnClickListener(v -> binding.collageView.undoLastDoodle());
        binding.btnClearDoodle.setOnClickListener(v -> binding.collageView.clearDoodles());

        binding.chipGroupDoodleColors.removeAllViews();
        for (int i = 0; i < doodleColors.length; i++) {
            final int c = doodleColors[i];
            Chip chip = new Chip(this, null, com.google.android.material.R.style.Widget_Material3_Chip_Filter);
            chip.setText("● Color");
            chip.setChipIconTint(ColorStateList.valueOf(c));
            chip.setTextColor(Color.WHITE);
            chip.setCheckable(true);
            if (i == 0) chip.setChecked(true);

            chip.setOnClickListener(v -> {
                binding.chipGroupDoodleColors.check(chip.getId());
                binding.collageView.setDoodleColor(c);
            });
            binding.chipGroupDoodleColors.addView(chip);
        }
    }

    private void saveCollage() {
        if (loadedBitmaps.isEmpty()) return;

        binding.btnSaveCollage.setEnabled(false);
        Toast.makeText(this, R.string.collage_saving, Toast.LENGTH_SHORT).show();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Bitmap result = binding.collageView.renderBitmap(2048);
                if (result == null) {
                    throw new Exception("Failed to render collage canvas");
                }

                File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File collageDir = new File(picturesDir, "Collage");
                if (!collageDir.exists()) {
                    collageDir.mkdirs();
                }

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String fileName = "Collage_" + timeStamp + ".jpg";
                File destFile = new File(collageDir, fileName);

                FileOutputStream out = new FileOutputStream(destFile);
                result.compress(Bitmap.CompressFormat.JPEG, 95, out);
                out.flush();
                out.close();

                MediaScannerConnection.scanFile(this, new String[]{destFile.getAbsolutePath()}, new String[]{"image/jpeg"}, null);

                runOnUiThread(() -> {
                    Toast.makeText(this, getString(R.string.collage_saved) + ": " + fileName, Toast.LENGTH_LONG).show();
                    finish();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    binding.btnSaveCollage.setEnabled(true);
                    Toast.makeText(this, getString(R.string.collage_save_error) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (Bitmap b : loadedBitmaps) {
            if (b != null && !b.isRecycled()) {
                b.recycle();
            }
        }
        loadedBitmaps.clear();
    }
}
