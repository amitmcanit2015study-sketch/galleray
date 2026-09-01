package com.amitbharat.gallery.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayoutMediator;
import com.amitbharat.gallery.R;
import com.amitbharat.gallery.data.models.MediaItem;
import com.amitbharat.gallery.databinding.ActivityMainBinding;
import com.amitbharat.gallery.ui.fragments.AllMediaFragment;
import com.amitbharat.gallery.ui.fragments.DeviceExplorerFragment;
import com.amitbharat.gallery.ui.fragments.FoldersFragment;
import com.amitbharat.gallery.ui.fragments.VaultFragment;
import com.amitbharat.gallery.utils.FileUtils;
import com.amitbharat.gallery.utils.PermissionHelper;
import com.amitbharat.gallery.utils.PreferencesManager;
import com.amitbharat.gallery.viewmodel.AllMediaViewModel;
import java.io.File;
import androidx.activity.OnBackPressedCallback;
import android.widget.Toast;
import com.amitbharat.gallery.viewmodel.DeviceExplorerViewModel;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AllMediaViewModel mediaViewModel;
    private DeviceExplorerViewModel deviceExplorerViewModel;
    private AllMediaFragment allMediaFragment;
    private FoldersFragment foldersFragment;
    private DeviceExplorerFragment deviceExplorerFragment;
    private long lastBackPressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mediaViewModel = new ViewModelProvider(this).get(AllMediaViewModel.class);
        deviceExplorerViewModel = new ViewModelProvider(this).get(DeviceExplorerViewModel.class);

        checkPermissions();
        setupToolbar();
        setupTabsAndViewPager();
        observeSelectionMode();
        setupBackNavigation();
    }

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 1. If selection mode is active, clear it first
                if (Boolean.TRUE.equals(mediaViewModel.getIsSelectionMode().getValue())) {
                    mediaViewModel.clearSelection();
                    return;
                }
                if (Boolean.TRUE.equals(deviceExplorerViewModel.getIsSelectionMode().getValue())) {
                    deviceExplorerViewModel.clearSelection();
                    return;
                }

                // 2. If on Device Explorer tab and there is directory history, navigate back
                if (binding.viewPager.getCurrentItem() == 2 && deviceExplorerViewModel != null) {
                    if (deviceExplorerViewModel.navigateBack()) {
                        return;
                    }
                }

                // 3. If on Folders tab (1), Device tab (2), or Vault tab (3), go back to All Media tab (0)
                if (binding.viewPager.getCurrentItem() != 0) {
                    binding.viewPager.setCurrentItem(0, true);
                    return;
                }

                // 4. Double back to exit from root (tab 0)
                if (System.currentTimeMillis() - lastBackPressedTime < 2000) {
                    finish();
                } else {
                    lastBackPressedTime = System.currentTimeMillis();
                    Toast.makeText(MainActivity.this, "Press back again to exit", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (!android.os.Environment.isExternalStorageManager()) {
                PermissionHelper.requestManageStoragePermission(this);
            }
        }
        if (!PermissionHelper.hasStoragePermission(this)) {
            PermissionHelper.requestStoragePermission(this);
        }
    }

    private void setupToolbar() {
        updateToggleIcon(binding.topAppBar.getMenu().findItem(R.id.action_view_toggle));

        binding.topAppBar.setOnMenuItemClickListener(this::onToolbarMenuItemClick);
    }

    private boolean onToolbarMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            startActivity(new Intent(this, SearchActivity.class));
            return true;
        } else if (id == R.id.action_view_toggle) {
            boolean currentGrid = PreferencesManager.getInstance(this).isGridView();
            PreferencesManager.getInstance(this).setGridView(!currentGrid);
            updateToggleIcon(item);
            if (allMediaFragment != null) {
                allMediaFragment.updateViewMode();
            }
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        return false;
    }

    private void updateToggleIcon(MenuItem item) {
        if (item == null) return;
        boolean isGrid = PreferencesManager.getInstance(this).isGridView();
        if (isGrid) {
            item.setIcon(R.drawable.ic_list);
            item.setTitle(R.string.action_view_list);
        } else {
            item.setIcon(R.drawable.ic_grid);
            item.setTitle(R.string.action_view_grid);
        }
    }

    private void setupTabsAndViewPager() {
        allMediaFragment = new AllMediaFragment();
        foldersFragment = new FoldersFragment();
        deviceExplorerFragment = new DeviceExplorerFragment();

        binding.viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0: return allMediaFragment;
                    case 1: return foldersFragment;
                    case 2: return deviceExplorerFragment;
                    default: return allMediaFragment;
                }
            }

            @Override
            public int getItemCount() {
                return 3;
            }
        });

        binding.viewPager.registerOnPageChangeCallback(new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 2) {
                    if (Boolean.TRUE.equals(mediaViewModel.getIsSelectionMode().getValue())) {
                        mediaViewModel.clearSelection();
                    }
                } else {
                    if (Boolean.TRUE.equals(deviceExplorerViewModel.getIsSelectionMode().getValue())) {
                        deviceExplorerViewModel.clearSelection();
                    }
                }
                updateBottomActionBar();
            }
        });

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.tab_all);
                    break;
                case 1:
                    tab.setText(R.string.tab_folders);
                    break;
                case 2:
                    tab.setText(R.string.tab_device);
                    break;
            }
        }).attach();
    }

    private void updateBottomActionBar() {
        int currentTab = binding.viewPager.getCurrentItem();
        if (currentTab == 2) {
            boolean isDeviceSelection = Boolean.TRUE.equals(deviceExplorerViewModel.getIsSelectionMode().getValue());
            binding.bottomActionBar.setVisibility(isDeviceSelection ? View.VISIBLE : View.GONE);
            if (isDeviceSelection) {
                binding.btnFavorite.setVisibility(View.GONE);
                binding.btnCollage.setVisibility(View.GONE);
                binding.btnVault.setVisibility(View.VISIBLE);
                binding.btnShare.setVisibility(View.VISIBLE);
                binding.btnDelete.setVisibility(View.VISIBLE);
                binding.btnSelectAll.setVisibility(View.VISIBLE);
                binding.btnCloseSelection.setVisibility(View.VISIBLE);
            }
        } else {
            boolean isMediaSelection = Boolean.TRUE.equals(mediaViewModel.getIsSelectionMode().getValue());
            binding.bottomActionBar.setVisibility(isMediaSelection ? View.VISIBLE : View.GONE);
            if (isMediaSelection) {
                binding.btnFavorite.setVisibility(View.VISIBLE);
                binding.btnCollage.setVisibility(View.VISIBLE);
                binding.btnVault.setVisibility(View.VISIBLE);
                binding.btnShare.setVisibility(View.VISIBLE);
                binding.btnDelete.setVisibility(View.VISIBLE);
                binding.btnSelectAll.setVisibility(View.VISIBLE);
                binding.btnCloseSelection.setVisibility(View.VISIBLE);
            }
        }
    }

    private void observeSelectionMode() {
        mediaViewModel.getIsSelectionMode().observe(this, isSelected -> updateBottomActionBar());
        deviceExplorerViewModel.getIsSelectionMode().observe(this, isSelected -> updateBottomActionBar());

        binding.btnCloseSelection.setOnClickListener(v -> {
            if (binding.viewPager.getCurrentItem() == 2) {
                deviceExplorerViewModel.clearSelection();
            } else {
                mediaViewModel.clearSelection();
            }
        });

        binding.btnSelectAll.setOnClickListener(v -> {
            if (binding.viewPager.getCurrentItem() == 2) {
                List<com.amitbharat.gallery.data.models.FileItem> all = deviceExplorerViewModel.getCurrentFilesLive().getValue();
                if (all != null) {
                    deviceExplorerViewModel.selectAll(all);
                    if (deviceExplorerFragment != null && deviceExplorerFragment.isAdded()) {
                        deviceExplorerFragment.refreshAdapter();
                    }
                }
            } else {
                List<MediaItem> all = mediaViewModel.getMediaList().getValue();
                if (all != null) {
                    mediaViewModel.selectAll(all);
                }
            }
        });

        binding.btnShare.setOnClickListener(v -> {
            if (binding.viewPager.getCurrentItem() == 2) {
                List<com.amitbharat.gallery.data.models.FileItem> selected = deviceExplorerViewModel.getSelectedFiles().getValue();
                if (selected != null && !selected.isEmpty()) {
                    List<File> files = new ArrayList<>();
                    for (com.amitbharat.gallery.data.models.FileItem f : selected) {
                        if (f.getPath() != null) files.add(new File(f.getPath()));
                    }
                    FileUtils.shareFiles(this, files);
                }
            } else {
                List<MediaItem> selected = mediaViewModel.getSelectedItems().getValue();
                if (selected != null && !selected.isEmpty()) {
                    List<File> files = new ArrayList<>();
                    for (MediaItem m : selected) {
                        if (m.getPath() != null) files.add(new File(m.getPath()));
                    }
                    FileUtils.shareFiles(this, files);
                }
            }
        });

        binding.btnFavorite.setOnClickListener(v -> {
            List<MediaItem> selected = mediaViewModel.getSelectedItems().getValue();
            if (selected != null) {
                for (MediaItem m : selected) {
                    mediaViewModel.toggleFavorite(m);
                }
                mediaViewModel.clearSelection();
            }
        });

        binding.btnCollage.setOnClickListener(v -> {
            List<MediaItem> selected = mediaViewModel.getSelectedItems().getValue();
            if (selected == null || selected.isEmpty()) return;

            ArrayList<String> imagePaths = new ArrayList<>();
            int videoCount = 0;
            for (MediaItem item : selected) {
                if (!item.isVideo() && item.getPath() != null) {
                    imagePaths.add(item.getPath());
                } else if (item.isVideo()) {
                    videoCount++;
                }
            }

            if (imagePaths.size() < 2) {
                if (videoCount > 0) {
                    Toast.makeText(this, R.string.collage_video_excluded_msg, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.collage_min_images_msg, Toast.LENGTH_SHORT).show();
                }
                return;
            }

            if (videoCount > 0) {
                Toast.makeText(this, R.string.collage_video_excluded_msg, Toast.LENGTH_SHORT).show();
            }

            Intent intent = new Intent(this, CollageMakerActivity.class);
            intent.putStringArrayListExtra("image_paths", imagePaths);
            startActivity(intent);
            mediaViewModel.clearSelection();
        });

        binding.btnVault.setOnClickListener(v -> {
            if (binding.viewPager.getCurrentItem() == 2) {
                List<com.amitbharat.gallery.data.models.FileItem> selected = deviceExplorerViewModel.getSelectedFiles().getValue();
                if (selected != null && !selected.isEmpty()) {
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("Hide " + selected.size() + " items in Secure Vault?")
                            .setMessage("These files will be moved to your private encrypted vault.")
                            .setPositiveButton("Hide in Vault", (d, w) -> {
                                com.amitbharat.gallery.data.repository.VaultRepository vaultRepo = new com.amitbharat.gallery.data.repository.VaultRepository(this);
                                final int total = selected.size();
                                final java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger(0);
                                for (com.amitbharat.gallery.data.models.FileItem f : selected) {
                                    vaultRepo.hideFile(f, () -> {
                                        if (count.incrementAndGet() >= total) {
                                            runOnUiThread(() -> {
                                                android.widget.Toast.makeText(this, "Moved " + total + " items to Secure Vault", android.widget.Toast.LENGTH_SHORT).show();
                                                deviceExplorerViewModel.clearSelection();
                                                deviceExplorerViewModel.refresh();
                                            });
                                        }
                                    });
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            } else {
                List<MediaItem> selected = mediaViewModel.getSelectedItems().getValue();
                if (selected != null && !selected.isEmpty()) {
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("Hide " + selected.size() + " items in Secure Vault?")
                            .setMessage("These files will be moved to your private encrypted vault and hidden from the public gallery.")
                            .setPositiveButton("Hide in Vault", (d, w) -> {
                                com.amitbharat.gallery.data.repository.VaultRepository vaultRepo = new com.amitbharat.gallery.data.repository.VaultRepository(this);
                                final int total = selected.size();
                                final java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger(0);
                                for (MediaItem m : selected) {
                                    vaultRepo.hideMedia(m, () -> {
                                        if (count.incrementAndGet() >= total) {
                                            runOnUiThread(() -> {
                                                android.widget.Toast.makeText(this, "Moved " + total + " items to Secure Vault", android.widget.Toast.LENGTH_SHORT).show();
                                                mediaViewModel.clearSelection();
                                                mediaViewModel.loadMedia();
                                            });
                                        }
                                    });
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            }
        });

        binding.btnDelete.setOnClickListener(v -> {
            if (binding.viewPager.getCurrentItem() == 2) {
                List<com.amitbharat.gallery.data.models.FileItem> selected = deviceExplorerViewModel.getSelectedFiles().getValue();
                if (selected != null && !selected.isEmpty()) {
                    final int total = selected.size();
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("Delete " + (total == 1 ? "1 item" : total + " items") + "?")
                            .setMessage("Move " + (total == 1 ? "this item" : "these " + total + " items") + " to the Recycle Bin? You can restore them later.")
                            .setPositiveButton("Yes", (d, which) -> {
                                com.amitbharat.gallery.data.repository.TrashRepository trashRepo = new com.amitbharat.gallery.data.repository.TrashRepository(this);
                                final java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger(0);
                                for (com.amitbharat.gallery.data.models.FileItem f : selected) {
                                    trashRepo.moveFileToTrash(f, () -> {
                                        if (count.incrementAndGet() >= total) {
                                            runOnUiThread(() -> {
                                                android.widget.Toast.makeText(this, "Moved " + total + " items to Recycle Bin", android.widget.Toast.LENGTH_SHORT).show();
                                                deviceExplorerViewModel.clearSelection();
                                                deviceExplorerViewModel.refresh();
                                            });
                                        }
                                    });
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
            } else {
                List<MediaItem> selected = mediaViewModel.getSelectedItems().getValue();
                if (selected != null && !selected.isEmpty()) {
                    final int total = selected.size();
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("Delete " + (total == 1 ? "1 item" : total + " items") + "?")
                            .setMessage("Move " + (total == 1 ? "this item" : "these " + total + " items") + " to the Recycle Bin? You can restore them later.")
                            .setPositiveButton("Yes", (d, which) -> {
                                com.amitbharat.gallery.data.repository.TrashRepository trashRepo = new com.amitbharat.gallery.data.repository.TrashRepository(this);
                                final java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger(0);
                                for (MediaItem m : selected) {
                                    trashRepo.moveToTrash(m, () -> {
                                        if (count.incrementAndGet() >= total) {
                                            runOnUiThread(() -> {
                                                android.widget.Toast.makeText(this, "Moved " + total + " items to Recycle Bin", android.widget.Toast.LENGTH_SHORT).show();
                                                mediaViewModel.clearSelection();
                                                mediaViewModel.loadMedia();
                                            });
                                        }
                                    });
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (mediaViewModel != null) {
            mediaViewModel.loadMedia();
        }
    }
}
