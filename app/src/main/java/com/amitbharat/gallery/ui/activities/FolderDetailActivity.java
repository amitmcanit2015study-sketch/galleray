package com.amitbharat.gallery.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.amitbharat.gallery.R;
import com.amitbharat.gallery.data.database.AppDatabase;
import com.amitbharat.gallery.data.database.FavoriteDao;
import com.amitbharat.gallery.data.database.FavoriteEntity;
import com.amitbharat.gallery.data.models.FilterOptions;
import com.amitbharat.gallery.data.models.FolderItem;
import com.amitbharat.gallery.data.models.MediaItem;
import com.amitbharat.gallery.data.repository.MediaRepository;
import com.amitbharat.gallery.data.repository.TrashRepository;
import com.amitbharat.gallery.data.repository.VaultRepository;
import com.amitbharat.gallery.databinding.ActivityFolderDetailBinding;
import com.amitbharat.gallery.ui.adapters.MediaGridAdapter;
import com.amitbharat.gallery.utils.FileUtils;
import com.amitbharat.gallery.utils.MediaUtils;
import com.amitbharat.gallery.utils.PreferencesManager;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class FolderDetailActivity extends AppCompatActivity implements MediaGridAdapter.OnMediaClickListener {

    private ActivityFolderDetailBinding binding;
    private FolderItem folderItem;
    private MediaGridAdapter adapter;
    private final List<MediaItem> mediaItems = new ArrayList<>();
    private final List<MediaItem> selectedItems = new ArrayList<>();
    private boolean isSelectionMode = false;
    private FilterOptions.SortOrder currentSort = FilterOptions.SortOrder.DATE_DESC;
    private MediaRepository mediaRepository;
    private FavoriteDao favoriteDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFolderDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        folderItem = (FolderItem) getIntent().getSerializableExtra("folder_item");
        if (folderItem == null) {
            finish();
            return;
        }

        mediaRepository = new MediaRepository(this);
        favoriteDao = AppDatabase.getInstance(this).favoriteDao();

        setupToolbar();
        setupRecyclerView();
        setupSelectionActionBar();
        setupBackNavigation();
        loadFolderMedia();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFolderMedia();
    }

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isSelectionMode) {
                    clearSelection();
                } else {
                    finish();
                }
            }
        });
    }

    private void setupToolbar() {
        binding.toolbar.setTitle(folderItem.getFolderName());
        binding.toolbar.setNavigationOnClickListener(v -> {
            if (isSelectionMode) {
                clearSelection();
            } else {
                finish();
            }
        });
        updateToggleIcon(binding.toolbar.getMenu().findItem(R.id.action_view_toggle));
        binding.toolbar.setOnMenuItemClickListener(this::onToolbarMenuItemClick);
    }

    private boolean onToolbarMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_view_toggle) {
            boolean currentGrid = PreferencesManager.getInstance(this).isGridView();
            PreferencesManager.getInstance(this).setGridView(!currentGrid);
            updateToggleIcon(item);
            setupLayoutManager();
            return true;
        } else if (id == R.id.action_sort) {
            showSortBottomSheet();
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

    private void setupRecyclerView() {
        adapter = new MediaGridAdapter(this);
        adapter.setListener(this);
        setupLayoutManager();
        binding.recyclerViewMedia.setAdapter(adapter);
        binding.swipeRefresh.setOnRefreshListener(this::loadFolderMedia);
    }

    private void setupLayoutManager() {
        if (binding == null) return;
        boolean isGrid = PreferencesManager.getInstance(this).isGridView();
        int cols = PreferencesManager.getInstance(this).getGridColumns();
        RecyclerView.LayoutManager currentLm = binding.recyclerViewMedia.getLayoutManager();

        boolean needsNewLayout = false;
        if (currentLm == null) {
            needsNewLayout = true;
        } else if (isGrid) {
            if (!(currentLm instanceof GridLayoutManager) || ((GridLayoutManager) currentLm).getSpanCount() != cols) {
                needsNewLayout = true;
            }
        } else {
            if (!(currentLm instanceof LinearLayoutManager) || (currentLm instanceof GridLayoutManager)) {
                needsNewLayout = true;
            }
        }

        if (needsNewLayout) {
            int firstVisiblePos = 0;
            if (currentLm instanceof LinearLayoutManager) {
                firstVisiblePos = ((LinearLayoutManager) currentLm).findFirstVisibleItemPosition();
            }
            if (isGrid) {
                binding.recyclerViewMedia.setLayoutManager(new GridLayoutManager(this, cols));
            } else {
                binding.recyclerViewMedia.setLayoutManager(new LinearLayoutManager(this));
            }
            if (firstVisiblePos > 0) {
                binding.recyclerViewMedia.scrollToPosition(firstVisiblePos);
            }
        }

        if (adapter != null) {
            adapter.setGridView(isGrid);
        }
    }

    private void setupSelectionActionBar() {
        binding.btnCloseSelection.setOnClickListener(v -> clearSelection());

        binding.btnSelectAll.setOnClickListener(v -> {
            boolean selectAll = selectedItems.size() < mediaItems.size();
            selectedItems.clear();
            for (MediaItem m : mediaItems) {
                m.setSelected(selectAll);
                if (selectAll) {
                    selectedItems.add(m);
                }
            }
            isSelectionMode = !selectedItems.isEmpty();
            binding.bottomActionBar.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
            adapter.notifyDataSetChanged();
        });

        binding.btnFavorite.setOnClickListener(v -> {
            if (selectedItems.isEmpty()) return;
            List<MediaItem> toToggle = new ArrayList<>(selectedItems);
            Executors.newSingleThreadExecutor().execute(() -> {
                for (MediaItem m : toToggle) {
                    if (m.getPath() != null) {
                        boolean isFav = favoriteDao.isFavorite(m.getPath());
                        if (isFav) {
                            favoriteDao.deleteByPath(m.getPath());
                            m.setFavorite(false);
                        } else {
                            favoriteDao.insertFavorite(new FavoriteEntity(m.getPath(), System.currentTimeMillis()));
                            m.setFavorite(true);
                        }
                    }
                }
                runOnUiThread(() -> {
                    Toast.makeText(this, "Updated favorites", Toast.LENGTH_SHORT).show();
                    clearSelection();
                });
            });
        });

        binding.btnShare.setOnClickListener(v -> {
            if (selectedItems.isEmpty()) return;
            List<File> files = new ArrayList<>();
            for (MediaItem m : selectedItems) {
                if (m.getPath() != null) files.add(new File(m.getPath()));
            }
            FileUtils.shareFiles(this, files);
        });

        binding.btnCollage.setOnClickListener(v -> {
            if (selectedItems.isEmpty()) return;
            ArrayList<String> imagePaths = new ArrayList<>();
            int videoCount = 0;
            for (MediaItem item : selectedItems) {
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
            clearSelection();
        });

        binding.btnVault.setOnClickListener(v -> {
            if (selectedItems.isEmpty()) return;
            final int total = selectedItems.size();
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Hide " + total + " items in Secure Vault?")
                    .setMessage("These files will be moved to your private encrypted vault and hidden from the public gallery.")
                    .setPositiveButton("Hide in Vault", (d, w) -> {
                        VaultRepository vaultRepo = new VaultRepository(this);
                        final AtomicInteger count = new AtomicInteger(0);
                        List<MediaItem> toHide = new ArrayList<>(selectedItems);
                        for (MediaItem m : toHide) {
                            vaultRepo.hideMedia(m, () -> {
                                if (count.incrementAndGet() >= total) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(this, "Moved " + total + " items to Secure Vault", Toast.LENGTH_SHORT).show();
                                        clearSelection();
                                        loadFolderMedia();
                                    });
                                }
                            });
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        binding.btnDelete.setOnClickListener(v -> {
            if (selectedItems.isEmpty()) return;
            final int total = selectedItems.size();
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Delete " + (total == 1 ? "1 item" : total + " items") + "?")
                    .setMessage("Move " + (total == 1 ? "this item" : "these " + total + " items") + " to the Recycle Bin? You can restore them later.")
                    .setPositiveButton("Yes", (d, which) -> {
                        TrashRepository trashRepo = new TrashRepository(this);
                        final AtomicInteger count = new AtomicInteger(0);
                        List<MediaItem> toDelete = new ArrayList<>(selectedItems);
                        for (MediaItem m : toDelete) {
                            trashRepo.moveToTrash(m, () -> {
                                if (count.incrementAndGet() >= total) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(this, "Moved " + total + " items to Recycle Bin", Toast.LENGTH_SHORT).show();
                                        clearSelection();
                                        loadFolderMedia();
                                    });
                                }
                            });
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void toggleSelection(MediaItem item, int position) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item);
            item.setSelected(false);
        } else {
            selectedItems.add(item);
            item.setSelected(true);
        }
        isSelectionMode = !selectedItems.isEmpty();
        binding.bottomActionBar.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
        adapter.notifyItemChanged(position);
    }

    private void clearSelection() {
        for (MediaItem m : mediaItems) {
            m.setSelected(false);
        }
        selectedItems.clear();
        isSelectionMode = false;
        binding.bottomActionBar.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();
    }

    private void loadFolderMedia() {
        binding.progressBar.setVisibility(View.VISIBLE);
        Executors.newSingleThreadExecutor().execute(() -> {
            List<MediaItem> allMedia = MediaUtils.queryAllMedia(this);
            List<FavoriteEntity> favs = favoriteDao.getAllFavoritesSync();
            Set<String> favPaths = new HashSet<>();
            for (FavoriteEntity f : favs) {
                favPaths.add(f.getMediaPath());
            }

            String targetFolder = folderItem.getFolderPath();
            long targetBucketId = folderItem.getBucketId();
            List<MediaItem> folderMedia = new ArrayList<>();

            for (MediaItem item : allMedia) {
                if (item.getPath() != null && favPaths.contains(item.getPath())) {
                    item.setFavorite(true);
                }

                boolean match = false;
                if (item.getPath() != null && targetFolder != null && !targetFolder.isEmpty()) {
                    File parent = new File(item.getPath()).getParentFile();
                    if (parent != null && parent.getAbsolutePath().equalsIgnoreCase(targetFolder)) {
                        match = true;
                    }
                }
                if (!match && targetBucketId != 0 && item.getBucketId() == targetBucketId) {
                    match = true;
                }
                if (match) {
                    folderMedia.add(item);
                }
            }

            MediaRepository.sortMediaList(folderMedia, currentSort);

            runOnUiThread(() -> {
                mediaItems.clear();
                mediaItems.addAll(folderMedia);
                adapter.submitList(new ArrayList<>(mediaItems));
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                binding.toolbar.setSubtitle(mediaItems.size() + (mediaItems.size() == 1 ? " item" : " items"));
            });
        });
    }

    private void showSortBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_sort, null);
        RadioGroup rg = view.findViewById(R.id.radioGroupSort);

        rg.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbDateDesc) currentSort = FilterOptions.SortOrder.DATE_DESC;
            else if (checkedId == R.id.rbDateAsc) currentSort = FilterOptions.SortOrder.DATE_ASC;
            else if (checkedId == R.id.rbNameAsc) currentSort = FilterOptions.SortOrder.NAME_ASC;
            else if (checkedId == R.id.rbNameDesc) currentSort = FilterOptions.SortOrder.NAME_DESC;
            else if (checkedId == R.id.rbSizeDesc) currentSort = FilterOptions.SortOrder.SIZE_DESC;
            else if (checkedId == R.id.rbSizeAsc) currentSort = FilterOptions.SortOrder.SIZE_ASC;

            MediaRepository.sortMediaList(mediaItems, currentSort);
            adapter.submitList(new ArrayList<>(mediaItems));
            dialog.dismiss();
        });

        dialog.setContentView(view);
        dialog.show();
    }

    @Override
    public void onMediaClick(MediaItem item, int position) {
        if (isSelectionMode) {
            toggleSelection(item, position);
        } else {
            if (item.isVideo()) {
                Intent intent = new Intent(this, VideoPlayerActivity.class);
                intent.putExtra("media_item", item);
                startActivity(intent);
            } else {
                com.amitbharat.gallery.utils.MediaHolder.setCurrentMediaList(mediaItems);
                Intent intent = new Intent(this, ImageViewerActivity.class);
                intent.putExtra("current_position", position);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onMediaLongClick(MediaItem item, int position) {
        toggleSelection(item, position);
    }
}
