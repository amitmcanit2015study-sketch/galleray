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
import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity implements MediaGridAdapter.OnMediaClickListener {

    private ActivitySearchBinding binding;
    private SearchViewModel viewModel;
    private MediaGridAdapter adapter;
    private FilterOptions.TypeFilter currentFilter = FilterOptions.TypeFilter.ALL;

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
            adapter.submitList(results);
            binding.emptyState.setVisibility(results == null || results.isEmpty() ? View.VISIBLE : View.GONE);
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
        } else {
            com.amitbharat.gallery.utils.MediaHolder.setCurrentMediaList(adapter.getMediaList());
            Intent intent = new Intent(this, ImageViewerActivity.class);
            intent.putExtra("current_position", position);
            startActivity(intent);
        }
    }

    @Override
    public void onMediaLongClick(MediaItem item, int position) {}
}
