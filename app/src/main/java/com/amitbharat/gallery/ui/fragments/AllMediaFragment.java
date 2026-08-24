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
import androidx.recyclerview.widget.LinearLayoutManager;
import com.amitbharat.gallery.data.models.MediaItem;
import com.amitbharat.gallery.databinding.FragmentAllMediaBinding;
import com.amitbharat.gallery.ui.activities.ImageViewerActivity;
import com.amitbharat.gallery.ui.activities.MainActivity;
import com.amitbharat.gallery.ui.activities.VideoPlayerActivity;
import com.amitbharat.gallery.ui.adapters.MediaGridAdapter;
import com.amitbharat.gallery.utils.PreferencesManager;
import com.amitbharat.gallery.viewmodel.AllMediaViewModel;
import java.util.ArrayList;

public class AllMediaFragment extends Fragment implements MediaGridAdapter.OnMediaClickListener {

    private FragmentAllMediaBinding binding;
    private AllMediaViewModel viewModel;
    private MediaGridAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAllMediaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(AllMediaViewModel.class);
        adapter = new MediaGridAdapter(requireContext());
        adapter.setListener(this);

        setupLayoutManager();
        binding.recyclerViewMedia.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadMedia());

        viewModel.getMediaList().observe(getViewLifecycleOwner(), items -> {
            adapter.submitList(items);
            binding.emptyState.setVisibility(items == null || items.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.swipeRefresh.setRefreshing(isLoading);
        });

        if (viewModel.getMediaList().getValue() == null || viewModel.getMediaList().getValue().isEmpty()) {
            viewModel.loadMedia();
        }
    }

    public void updateViewMode() {
        setupLayoutManager();
    }

    private void setupLayoutManager() {
        if (binding == null || getContext() == null) return;
        boolean isGrid = PreferencesManager.getInstance(requireContext()).isGridView();
        int cols = PreferencesManager.getInstance(requireContext()).getGridColumns();
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
                binding.recyclerViewMedia.setLayoutManager(new GridLayoutManager(requireContext(), cols));
            } else {
                binding.recyclerViewMedia.setLayoutManager(new LinearLayoutManager(requireContext()));
            }
            if (firstVisiblePos > 0) {
                binding.recyclerViewMedia.scrollToPosition(firstVisiblePos);
            }
        }

        if (adapter != null) {
            adapter.setGridView(isGrid);
        }
    }

    @Override
    public void onMediaClick(MediaItem item, int position) {
        if (Boolean.TRUE.equals(viewModel.getIsSelectionMode().getValue())) {
            viewModel.toggleSelection(item);
            adapter.notifyItemChanged(position);
        } else {
            if (item.isVideo()) {
                Intent intent = new Intent(requireContext(), VideoPlayerActivity.class);
                intent.putExtra("media_item", item);
                startActivity(intent);
            } else {
                com.amitbharat.gallery.utils.MediaHolder.setCurrentMediaList(adapter.getMediaList());
                Intent intent = new Intent(requireContext(), ImageViewerActivity.class);
                intent.putExtra("current_position", position);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onMediaLongClick(MediaItem item, int position) {
        viewModel.toggleSelection(item);
        adapter.notifyItemChanged(position);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateViewMode();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
