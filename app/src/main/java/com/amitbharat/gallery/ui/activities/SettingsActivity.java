package com.amitbharat.gallery.ui.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.amitbharat.gallery.R;
import com.amitbharat.gallery.databinding.ActivitySettingsBinding;
import com.amitbharat.gallery.utils.PreferencesManager;
import com.amitbharat.gallery.utils.ThemeUtils;
import java.util.concurrent.Executors;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private PreferencesManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = PreferencesManager.getInstance(this);

        setupToolbar();
        setupPreferencesViews();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupPreferencesViews() {
        updateThemeSummary();
        updateGridColumnsSummary();

        binding.switchShowHidden.setChecked(prefs.isShowHiddenFiles());
        binding.switchShowHidden.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.setShowHiddenFiles(isChecked);
        });

        binding.rowTheme.setOnClickListener(v -> showThemeDialog());
        binding.rowGridColumns.setOnClickListener(v -> showGridColumnsDialog());
        binding.rowClearCache.setOnClickListener(v -> clearCache());
    }

    private void updateThemeSummary() {
        int mode = prefs.getThemeMode();
        if (mode == 1) {
            binding.tvCurrentTheme.setText(R.string.pref_theme_light);
        } else if (mode == 2) {
            binding.tvCurrentTheme.setText(R.string.pref_theme_dark);
        } else {
            binding.tvCurrentTheme.setText(R.string.pref_theme_system);
        }
    }

    private void updateGridColumnsSummary() {
        int cols = prefs.getGridColumns();
        binding.tvGridColumns.setText(cols + " Columns");
    }

    private void showThemeDialog() {
        String[] options = new String[]{"System Default", "Light", "Dark"};
        int current = prefs.getThemeMode();

        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Theme")
                .setSingleChoiceItems(options, current, (dialog, which) -> {
                    prefs.setThemeMode(which);
                    ThemeUtils.applyTheme(this);
                    updateThemeSummary();
                    dialog.dismiss();
                })
                .show();
    }

    private void showGridColumnsDialog() {
        String[] options = new String[]{"2 Columns", "3 Columns", "4 Columns", "5 Columns"};
        int[] values = new int[]{2, 3, 4, 5};
        int currentCols = prefs.getGridColumns();
        int selectedIndex = 1;
        for (int i = 0; i < values.length; i++) {
            if (values[i] == currentCols) {
                selectedIndex = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Grid Columns")
                .setSingleChoiceItems(options, selectedIndex, (dialog, which) -> {
                    prefs.setGridColumns(values[which]);
                    updateGridColumnsSummary();
                    dialog.dismiss();
                })
                .show();
    }

    private void clearCache() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Glide.get(this).clearDiskCache();
            runOnUiThread(() -> {
                Glide.get(this).clearMemory();
                Toast.makeText(this, R.string.cache_cleared, Toast.LENGTH_SHORT).show();
            });
        });
    }
}
