package com.amitbharat.gallery.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.amitbharat.gallery.R;
import com.amitbharat.gallery.data.models.FilterOptions;
import com.amitbharat.gallery.data.models.FolderItem;
import com.amitbharat.gallery.data.models.MediaItem;
import com.amitbharat.gallery.data.repository.MediaRepository;
import com.amitbharat.gallery.databinding.ActivityFolderDetailBinding;
import com.amitbharat.gallery.ui.adapters.MediaGridAdapter;
import com.amitbharat.gallery.utils.MediaUtils;
import com.amitbharat.gallery.utils.PreferencesManager;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class FolderDetailActivity extends AppCompatActivity implements MediaGridAdapter.OnMediaClickListener {

    private ActivityFolderDetailBinding binding;
    private FolderItem folderItem;
    private MediaGridAdapter adapter;
    private final List<MediaItem> mediaItems = new ArrayList<>();
    private FilterOptions.SortOrder currentSort = FilterOptions.SortOrder.DATE_DESC;

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

        setupToolbar();
        setupRecyclerView();
        loadFolderMedia();
    }

    private void setupToolbar() {
        binding.toolbar.setTitle(folderItem.getFolderName());
        binding.toolbar.setNavigationOnClickListener(v -> finish());
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
        androidx.recyclerview.widget.RecyclerView.LayoutManager currentLm = binding.recyclerViewMedia.getLayoutManager();

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

    private void loadFolderMedia() {
        binding.progressBar.setVisibility(View.VISIBLE);
        Executors.newSingleThreadExecutor().execute(() -> {
            List<MediaItem> allMedia = MediaUtils.queryAllMedia(this);
            List<MediaItem> folderMedia = new ArrayList<>();

            for (MediaItem item : allMedia) {
                if (item.getBucketId() == folderItem.getBucketId() ||
                        (item.getPath() != null && folderItem.getFolderPath() != null &&
                                item.getPath().startsWith(folderItem.getFolderPath()))) {
                    folderMedia.add(item);
                }
            }

            MediaRepository.sortMediaList(folderMedia, currentSort);

            runOnUiThread(() -> {
                mediaItems.clear();
                mediaItems.addAll(folderMedia);
                adapter.submitList(mediaItems);
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
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

    @Override
    public void onMediaLongClick(MediaItem item, int position) {}
}
