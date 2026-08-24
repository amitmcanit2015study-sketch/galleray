package com.amitbharat.gallery.ui.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.amitbharat.gallery.data.models.FileItem;
import com.amitbharat.gallery.databinding.ActivityStorageAnalyzerBinding;
import com.amitbharat.gallery.utils.FileUtils;
import com.amitbharat.gallery.viewmodel.StorageAnalyzerViewModel;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StorageAnalyzerActivity extends AppCompatActivity {

    private ActivityStorageAnalyzerBinding binding;
    private StorageAnalyzerViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStorageAnalyzerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(StorageAnalyzerViewModel.class);

        setupToolbar();
        setupClickListeners();
        observeViewModel();

        viewModel.analyze();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupClickListeners() {
        binding.btnCleanAllJunk.setOnClickListener(v -> cleanAllJunk());
        binding.btnCleanApks.setOnClickListener(v -> cleanApks());
        binding.btnCleanCache.setOnClickListener(v -> cleanCacheFiles());
        binding.btnCleanEmptyFolders.setOnClickListener(v -> cleanEmptyFolders());
        binding.btnCleanDuplicates.setOnClickListener(v -> cleanDuplicates());
        binding.btnReviewLargeFiles.setOnClickListener(v -> showLargeFilesDialog());
    }

    private void observeViewModel() {
        viewModel.getIsScanningLive().observe(this, isScanning -> {
            if (isScanning) {
                binding.tvTotalJunkSize.setText("Scanning Junk...");
                binding.tvJunkSummary.setText("Detecting waste and garbage files in memory...");
                binding.btnCleanAllJunk.setEnabled(false);
            } else {
                updateJunkOverview();
                binding.btnCleanAllJunk.setEnabled(true);
            }
        });

        viewModel.getApkJunkLive().observe(this, apks -> {
            long totalBytes = 0;
            for (FileItem f : apks) totalBytes += f.getSize();
            binding.tvApkCount.setText(apks.size() + " APK packages • " + FileUtils.formatFileSize(totalBytes));
            updateJunkOverview();
        });

        viewModel.getCacheJunkLive().observe(this, cache -> {
            long totalBytes = 0;
            for (FileItem f : cache) totalBytes += f.getSize();
            binding.tvCacheCount.setText(cache.size() + " cache & temp items • " + FileUtils.formatFileSize(totalBytes));
            updateJunkOverview();
        });

        viewModel.getEmptyFoldersLive().observe(this, emptyFolders -> {
            binding.tvEmptyFoldersCount.setText(emptyFolders.size() + " empty directory structures");
            updateJunkOverview();
        });

        viewModel.getDuplicateFilesLive().observe(this, duplicates -> {
            long totalBytes = 0;
            for (FileItem f : duplicates) totalBytes += f.getSize();
            binding.tvDuplicatesCount.setText(duplicates.size() + " duplicate copies • " + FileUtils.formatFileSize(totalBytes));
            updateJunkOverview();
        });

        viewModel.getLargeFilesLive().observe(this, largeFiles -> {
            long totalBytes = 0;
            for (FileItem f : largeFiles) totalBytes += f.getSize();
            binding.tvLargeFilesCount.setText(largeFiles.size() + " files detected • " + FileUtils.formatFileSize(totalBytes));
        });
    }

    private void updateJunkOverview() {
        List<FileItem> apks = viewModel.getApkJunkLive().getValue();
        List<FileItem> cache = viewModel.getCacheJunkLive().getValue();
        List<FileItem> empty = viewModel.getEmptyFoldersLive().getValue();

        long totalJunkBytes = 0;
        int count = 0;
        if (apks != null) {
            for (FileItem f : apks) totalJunkBytes += f.getSize();
            count += apks.size();
        }
        if (cache != null) {
            for (FileItem f : cache) totalJunkBytes += f.getSize();
            count += cache.size();
        }
        if (empty != null) {
            count += empty.size();
        }

        binding.tvTotalJunkSize.setText(FileUtils.formatFileSize(totalJunkBytes));
        binding.tvJunkSummary.setText(count + " garbage & waste items ready to clean");
        binding.btnCleanAllJunk.setText("🧹 CLEAN " + FileUtils.formatFileSize(totalJunkBytes) + " NOW");
    }

    private void cleanAllJunk() {
        List<FileItem> allJunk = new ArrayList<>();
        if (viewModel.getCacheJunkLive().getValue() != null) allJunk.addAll(viewModel.getCacheJunkLive().getValue());
        if (viewModel.getEmptyFoldersLive().getValue() != null) allJunk.addAll(viewModel.getEmptyFoldersLive().getValue());
        if (viewModel.getApkJunkLive().getValue() != null) allJunk.addAll(viewModel.getApkJunkLive().getValue());

        if (allJunk.isEmpty()) {
            Toast.makeText(this, "Your device memory is already clean!", Toast.LENGTH_SHORT).show();
            return;
        }

        long totalBytes = 0;
        for (FileItem f : allJunk) totalBytes += f.getSize();

        new MaterialAlertDialogBuilder(this)
                .setTitle("Clean All Waste & Garbage?")
                .setMessage("This will delete " + allJunk.size() + " junk files & empty folders to free up " + FileUtils.formatFileSize(totalBytes) + " of memory.")
                .setPositiveButton("Yes", (d, w) -> {
                    viewModel.cleanWasteFiles(allJunk, () -> {
                        runOnUiThread(() -> Toast.makeText(this, "✨ Cleaned memory successfully!", Toast.LENGTH_SHORT).show());
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void cleanApks() {
        List<FileItem> apks = viewModel.getApkJunkLive().getValue();
        if (apks == null || apks.isEmpty()) {
            Toast.makeText(this, "No APK packages found to clean", Toast.LENGTH_SHORT).show();
            return;
        }
        long totalBytes = 0;
        for (FileItem f : apks) totalBytes += f.getSize();

        new MaterialAlertDialogBuilder(this)
                .setTitle("Clean Unused APKs?")
                .setMessage("Delete " + apks.size() + " APK installer files (" + FileUtils.formatFileSize(totalBytes) + ")?")
                .setPositiveButton("Yes", (d, w) -> {
                    viewModel.cleanWasteFiles(apks, () -> {
                        runOnUiThread(() -> Toast.makeText(this, "Cleaned APK files", Toast.LENGTH_SHORT).show());
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void cleanCacheFiles() {
        List<FileItem> cache = viewModel.getCacheJunkLive().getValue();
        if (cache == null || cache.isEmpty()) {
            Toast.makeText(this, "No cache or temporary files found", Toast.LENGTH_SHORT).show();
            return;
        }
        viewModel.cleanWasteFiles(cache, () -> {
            runOnUiThread(() -> Toast.makeText(this, "Cleaned cache & temp files", Toast.LENGTH_SHORT).show());
        });
    }

    private void cleanEmptyFolders() {
        List<FileItem> emptyFolders = viewModel.getEmptyFoldersLive().getValue();
        if (emptyFolders == null || emptyFolders.isEmpty()) {
            Toast.makeText(this, "No empty folders to clean!", Toast.LENGTH_SHORT).show();
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Clean Empty Folders?")
                .setMessage("Delete " + emptyFolders.size() + " empty folders from memory?")
                .setPositiveButton("Yes", (d, w) -> {
                    viewModel.cleanWasteFiles(emptyFolders, () -> {
                        runOnUiThread(() -> Toast.makeText(this, "Cleaned " + emptyFolders.size() + " empty folders!", Toast.LENGTH_SHORT).show());
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void cleanDuplicates() {
        List<FileItem> duplicates = viewModel.getDuplicateFilesLive().getValue();
        if (duplicates == null || duplicates.isEmpty()) {
            Toast.makeText(this, "No duplicate files found", Toast.LENGTH_SHORT).show();
            return;
        }
        showDuplicatesDialog();
    }

    private void showDuplicatesDialog() {
        List<FileItem> items = viewModel.getDuplicateFilesLive().getValue();
        if (items == null || items.isEmpty()) return;

        String[] names = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
            names[i] = items.get(i).getName() + " • " + FileUtils.formatFileSize(items.get(i).getSize());
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Duplicate Files (" + items.size() + ")")
                .setItems(names, (dialog, which) -> {
                    FileItem selected = items.get(which);
                    FileUtils.openFileWithIntent(this, new File(selected.getPath()));
                })
                .setPositiveButton("Close", null)
                .show();
    }

    private void showLargeFilesDialog() {
        List<FileItem> items = viewModel.getLargeFilesLive().getValue();
        if (items == null || items.isEmpty()) {
            Toast.makeText(this, "No large files found (>50 MB)", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
            names[i] = items.get(i).getName() + " (" + FileUtils.formatFileSize(items.get(i).getSize()) + ")";
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Large Files (>50 MB)")
                .setItems(names, (dialog, which) -> {
                    FileItem selected = items.get(which);
                    FileUtils.openFileWithIntent(this, new File(selected.getPath()));
                })
                .setPositiveButton("Close", null)
                .show();
    }
}
