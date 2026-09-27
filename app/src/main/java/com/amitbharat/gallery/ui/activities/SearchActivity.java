package com.amitbharat.gallery.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import com.amitbharat.gallery.R;
import com.amitbharat.gallery.data.models.FilterOptions;
import com.amitbharat.gallery.data.models.MediaItem;
import com.amitbharat.gallery.databinding.ActivitySearchBinding;
import com.amitbharat.gallery.ui.adapters.MediaGridAdapter;
import com.amitbharat.gallery.utils.PreferencesManager;
import com.amitbharat.gallery.viewmodel.SearchViewModel;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements MediaGridAdapter.OnMediaClickListener {

    private ActivitySearchBinding binding;
    private SearchViewModel viewModel;
    private MediaGridAdapter adapter;
    private FilterOptions.TypeFilter currentFilter = FilterOptions.TypeFilter.ALL;
    private final List<MediaItem> allSearchResults = new ArrayList<>();
    private static final int INITIAL_PAGE_SIZE = 60;
    private static final int LOAD_MORE_PAGE_SIZE = 40;
    private int currentLoadedCount = 0;
    private boolean isLoadingMore = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupFilterChips();
        setupSearchInput();
        observeViewModel();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new MediaGridAdapter(this);
        adapter.setListener(this);
        int cols = PreferencesManager.getInstance(this).getGridColumns();
        binding.recyclerViewSearch.setLayoutManager(new GridLayoutManager(this, cols));
        binding.recyclerViewSearch.setAdapter(adapter);

        binding.recyclerViewSearch.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0) return;

                RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
                if (lm instanceof LinearLayoutManager) {
                    int totalCount = lm.getItemCount();
                    int lastVisible = ((LinearLayoutManager) lm).findLastVisibleItemPosition();
                    if (!isLoadingMore && currentLoadedCount < allSearchResults.size() && totalCount <= lastVisible + 20) {
                        loadNextChunk();
                    }
                }
            }
        });
    }

    private void loadNextChunk() {
        if (isLoadingMore || currentLoadedCount >= allSearchResults.size()) return;
        isLoadingMore = true;
        int nextCount = Math.min(currentLoadedCount + LOAD_MORE_PAGE_SIZE, allSearchResults.size());
        List<MediaItem> more = new ArrayList<>(allSearchResults.subList(currentLoadedCount, nextCount));
        adapter.appendItems(more);
        currentLoadedCount = nextCount;
        isLoadingMore = false;
    }

    private void setupSearchInput() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.performSearch(s.toString(), currentFilter);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilterChips() {
        binding.chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipFilterAll) {
                currentFilter = FilterOptions.TypeFilter.ALL;
            } else if (id == R.id.chipFilterImages) {
                currentFilter = FilterOptions.TypeFilter.IMAGES;
            } else if (id == R.id.chipFilterVideos) {
                currentFilter = FilterOptions.TypeFilter.VIDEOS;
            } else if (id == R.id.chipFilterAudio) {
                currentFilter = FilterOptions.TypeFilter.AUDIO;
            } else if (id == R.id.chipFilterGif) {
                currentFilter = FilterOptions.TypeFilter.GIF;
            } else if (id == R.id.chipFilterLarge) {
                currentFilter = FilterOptions.TypeFilter.LARGE;
            }
            viewModel.performSearch(binding.etSearch.getText().toString(), currentFilter);
        });
    }

    private void observeViewModel() {
        viewModel.getSearchResultsLive().observe(this, results -> {
            allSearchResults.clear();
            if (results != null) {
                allSearchResults.addAll(results);
            }
            int initialCount = Math.min(INITIAL_PAGE_SIZE, allSearchResults.size());
            currentLoadedCount = initialCount;
            adapter.submitList(new ArrayList<>(allSearchResults.subList(0, initialCount)));
            binding.emptyState.setVisibility(allSearchResults.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getIsSearchingLive().observe(this, isSearching -> {
            binding.progressBar.setVisibility(isSearching ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onMediaClick(MediaItem item, int position) {
        if (item.isVideo()) {
            Intent intent = new Intent(this, VideoPlayerActivity.class);
            intent.putExtra("media_item", item);
            startActivity(intent);
        } else if (item.isAudio()) {
            List<MediaItem> fullList = allSearchResults.isEmpty() ? adapter.getMediaList() : allSearchResults;
            com.amitbharat.gallery.utils.MediaHolder.setCurrentMediaList(fullList);
            int actualPos = fullList.indexOf(item);
            Intent intent = new Intent(this, AudioPlayerActivity.class);
            intent.putExtra("current_position", actualPos >= 0 ? actualPos : position);
            startActivity(intent);
        } else {
            List<MediaItem> fullList = allSearchResults.isEmpty() ? adapter.getMediaList() : allSearchResults;
            com.amitbharat.gallery.utils.MediaHolder.setCurrentMediaList(fullList);
            int actualPos = fullList.indexOf(item);
            Intent intent = new Intent(this, ImageViewerActivity.class);
            intent.putExtra("current_position", actualPos >= 0 ? actualPos : position);
            startActivity(intent);
        }
    }

    @Override
    public void onMediaLongClick(MediaItem item, int position) {}
}
