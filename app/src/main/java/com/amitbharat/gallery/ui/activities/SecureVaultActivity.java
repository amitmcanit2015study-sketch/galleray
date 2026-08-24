package com.amitbharat.gallery.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import com.amitbharat.gallery.data.models.VaultItem;
import com.amitbharat.gallery.databinding.ActivitySecureVaultBinding;
import com.amitbharat.gallery.ui.adapters.VaultAdapter;
import com.amitbharat.gallery.utils.PreferencesManager;
import com.amitbharat.gallery.utils.SecurityUtils;
import com.amitbharat.gallery.viewmodel.VaultViewModel;

public class SecureVaultActivity extends AppCompatActivity implements VaultAdapter.OnVaultItemClickListener {

    private ActivitySecureVaultBinding binding;
    private VaultViewModel viewModel;
    private VaultAdapter adapter;
    private boolean isUnlocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySecureVaultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(VaultViewModel.class);

        setupToolbar();
        setupPinLock();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupPinLock() {
        PreferencesManager prefs = PreferencesManager.getInstance(this);
        boolean isConfigured = prefs.isVaultConfigured();

        if (!isConfigured) {
            binding.tvPinPrompt.setText("Create 4-Digit Security PIN");
            binding.btnUnlock.setText("Set PIN & Unlock");
        } else {
            binding.tvPinPrompt.setText("Enter 4-Digit Security PIN");
            binding.btnUnlock.setText("Unlock Vault");
        }

        binding.btnUnlock.setOnClickListener(v -> {
            String enteredPin = binding.etPin.getText().toString().trim();
            if (enteredPin.length() < 4) {
                Toast.makeText(this, "PIN must be 4 digits", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isConfigured) {
                prefs.setVaultPin(SecurityUtils.hashPin(enteredPin));
                Toast.makeText(this, "PIN configured successfully!", Toast.LENGTH_SHORT).show();
                unlockVault();
            } else {
                if (SecurityUtils.verifyPin(enteredPin, prefs.getVaultPin())) {
                    unlockVault();
                } else {
                    Toast.makeText(this, "Incorrect PIN. Try again.", Toast.LENGTH_SHORT).show();
                    binding.etPin.setText("");
                }
            }
        });
    }

    private void unlockVault() {
        isUnlocked = true;
        binding.layoutPinLock.setVisibility(View.GONE);
        binding.layoutVaultContent.setVisibility(View.VISIBLE);

        adapter = new VaultAdapter(this);
        adapter.setListener(this);
        binding.rvVaultItems.setLayoutManager(new GridLayoutManager(this, 3));
        binding.rvVaultItems.setAdapter(adapter);

        viewModel.getVaultItems().observe(this, items -> {
            adapter.submitList(items);
            binding.emptyVaultState.setVisibility(items == null || items.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onRestoreClick(VaultItem item) {
        viewModel.restoreItem(item, () -> {
            runOnUiThread(() -> Toast.makeText(this, "Restored: " + item.getOriginalName(), Toast.LENGTH_SHORT).show());
        });
    }
}
