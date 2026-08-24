package com.amitbharat.gallery.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.amitbharat.gallery.data.database.TrashEntity;
import com.amitbharat.gallery.data.repository.TrashRepository;
import com.amitbharat.gallery.databinding.ActivityRecycleBinBinding;
import com.amitbharat.gallery.ui.adapters.TrashAdapter;

import android.view.MenuItem;
import com.amitbharat.gallery.R;

public class RecycleBinActivity extends AppCompatActivity implements TrashAdapter.OnTrashActionListener {

    private ActivityRecycleBinBinding binding;
    private TrashRepository trashRepo;
    private TrashAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecycleBinBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        trashRepo = new TrashRepository(this);

        setupToolbar();
        setupRecyclerView();
        observeTrash();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.toolbar.inflateMenu(R.menu.menu_recycle_bin);
        binding.toolbar.setOnMenuItemClickListener(this::onMenuItemClick);
    }

    private boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.action_empty_bin) {
            showEmptyTrashDialog();
            return true;
        }
        return false;
    }

    private void showEmptyTrashDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Empty Recycle Bin?")
                .setMessage("Are you sure you want to permanently delete all items in the Recycle Bin? This action cannot be undone.")
                .setPositiveButton("Yes", (d, w) -> {
                    trashRepo.emptyTrash(() -> {
                        runOnUiThread(() -> Toast.makeText(this, "Recycle bin emptied", Toast.LENGTH_SHORT).show());
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void setupRecyclerView() {
        adapter = new TrashAdapter(this);
        adapter.setListener(this);
        binding.rvTrashItems.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvTrashItems.setAdapter(adapter);
    }

    private void observeTrash() {
        trashRepo.getTrashItemsLive().observe(this, items -> {
            adapter.submitList(items);
            boolean isEmpty = (items == null || items.isEmpty());
            binding.emptyTrashState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            MenuItem emptyItem = binding.toolbar.getMenu().findItem(R.id.action_empty_bin);
            if (emptyItem != null) {
                emptyItem.setVisible(!isEmpty);
            }
        });
    }

    @Override
    public void onRestore(TrashEntity item) {
        trashRepo.restoreItem(item, () -> {
            runOnUiThread(() -> Toast.makeText(this, "Restored: " + item.getFileName(), Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public void onDeletePermanent(TrashEntity item) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Permanently?")
                .setMessage("Are you sure you want to permanently delete \"" + item.getFileName() + "\"? This action cannot be undone.")
                .setPositiveButton("Yes", (d, w) -> {
                    trashRepo.deletePermanently(item, () -> {
                        runOnUiThread(() -> Toast.makeText(this, "Deleted permanently", Toast.LENGTH_SHORT).show());
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }
}
