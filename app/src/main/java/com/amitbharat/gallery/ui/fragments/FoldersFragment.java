package com.amitbharat.gallery.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import com.amitbharat.gallery.R;
import com.amitbharat.gallery.data.models.FolderItem;
import com.amitbharat.gallery.data.models.MediaItem;
import com.amitbharat.gallery.databinding.FragmentFoldersBinding;
import com.amitbharat.gallery.ui.activities.FolderDetailActivity;
import com.amitbharat.gallery.ui.adapters.FolderGridAdapter;
import com.amitbharat.gallery.utils.MediaUtils;
import com.amitbharat.gallery.viewmodel.FoldersViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FoldersFragment extends Fragment implements FolderGridAdapter.OnFolderClickListener {

    private FragmentFoldersBinding binding;
    private FoldersViewModel viewModel;
    private FolderGridAdapter adapter;
    private List<FolderItem> allFolders = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFoldersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(FoldersViewModel.class);
        adapter = new FolderGridAdapter(requireContext());
        adapter.setListener(this);

        binding.recyclerViewFolders.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.recyclerViewFolders.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadFolders());

        setupFilterChips();

        viewModel.getFoldersList().observe(getViewLifecycleOwner(), folders -> {
            allFolders = folders != null ? new ArrayList<>(folders) : new ArrayList<>();
            filterFoldersByChip(binding.chipGroupSmartAlbums.getCheckedChipId());
            binding.emptyState.setVisibility(allFolders.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.swipeRefresh.setRefreshing(isLoading);
        });

        viewModel.loadFolders();
    }

    private void setupFilterChips() {
        binding.chipGroupSmartAlbums.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                filterFoldersByChip(checkedIds.get(0));
            } else {
                adapter.submitList(allFolders);
            }
        });
    }

    private void filterFoldersByChip(int chipId) {
        if (allFolders == null) return;
        List<FolderItem> filtered = new ArrayList<>();

        if (chipId == R.id.chipCamera) {
            for (FolderItem f : allFolders) {
                if (f.getFolderName().toLowerCase(Locale.ROOT).contains("dcim") ||
                        f.getFolderName().toLowerCase(Locale.ROOT).contains("camera")) {
                    filtered.add(f);
                }
            }
        } else if (chipId == R.id.chipScreenshots) {
            for (FolderItem f : allFolders) {
                if (f.getFolderName().toLowerCase(Locale.ROOT).contains("screenshot")) {
                    filtered.add(f);
                }
            }
        } else if (chipId == R.id.chipDownloads) {
            for (FolderItem f : allFolders) {
                if (f.getFolderName().toLowerCase(Locale.ROOT).contains("download")) {
                    filtered.add(f);
                }
            }
        } else if (chipId == R.id.chipVideos) {
            for (FolderItem f : allFolders) {
                if (f.getFolderName().toLowerCase(Locale.ROOT).contains("movie") ||
                        f.getFolderName().toLowerCase(Locale.ROOT).contains("video")) {
                    filtered.add(f);
                }
            }
        } else if (chipId == R.id.chipFavorites) {
            // Filter media by favorites
            List<MediaItem> allMedia = viewModel.getAllMedia().getValue();
            if (allMedia != null) {
                List<MediaItem> favs = new ArrayList<>();
                for (MediaItem m : allMedia) {
                    if (m.isFavorite()) favs.add(m);
                }
                filtered = MediaUtils.groupMediaByFolders(favs);
            }
        } else {
            filtered = new ArrayList<>(allFolders);
        }

        adapter.submitList(filtered);
    }

    @Override
    public void onFolderClick(FolderItem folder) {
        Intent intent = new Intent(requireContext(), FolderDetailActivity.class);
        intent.putExtra("folder_item", folder);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
