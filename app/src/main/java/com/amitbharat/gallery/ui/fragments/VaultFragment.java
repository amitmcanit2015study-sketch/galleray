package com.amitbharat.gallery.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import com.amitbharat.gallery.data.models.VaultItem;
import com.amitbharat.gallery.databinding.FragmentVaultBinding;
import com.amitbharat.gallery.ui.adapters.VaultAdapter;
import com.amitbharat.gallery.utils.PreferencesManager;
import com.amitbharat.gallery.utils.SecurityUtils;
import com.amitbharat.gallery.viewmodel.AllMediaViewModel;
import com.amitbharat.gallery.viewmodel.VaultViewModel;

public class VaultFragment extends Fragment implements VaultAdapter.OnVaultItemClickListener {

    private FragmentVaultBinding binding;
    private VaultViewModel viewModel;
    private AllMediaViewModel allMediaViewModel;
    private VaultAdapter adapter;
    private boolean isUnlocked = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentVaultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(VaultViewModel.class);
        allMediaViewModel = new ViewModelProvider(requireActivity()).get(AllMediaViewModel.class);

        setupPinLock();
    }

    private void setupPinLock() {
        PreferencesManager prefs = PreferencesManager.getInstance(requireContext());
        boolean isConfigured = prefs.isVaultConfigured();

        if (!isConfigured) {
            binding.tvPinPrompt.setText("Create 4-Digit Security PIN");
            binding.btnUnlock.setText("Set PIN & Unlock");
        } else {
            binding.tvPinPrompt.setText("Enter 4-Digit Security PIN");
            binding.btnUnlock.setText("Unlock Vault");
        }

        binding.btnUnlock.setOnClickListener(v -> {
            String enteredPin = binding.etPin.getText() != null ? binding.etPin.getText().toString().trim() : "";
            if (enteredPin.length() < 4) {
                Toast.makeText(requireContext(), "PIN must be 4 digits", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isConfigured) {
                prefs.setVaultPin(SecurityUtils.hashPin(enteredPin));
                Toast.makeText(requireContext(), "PIN configured successfully!", Toast.LENGTH_SHORT).show();
                unlockVault();
            } else {
                if (SecurityUtils.verifyPin(enteredPin, prefs.getVaultPin())) {
                    unlockVault();
                } else {
                    Toast.makeText(requireContext(), "Incorrect PIN. Try again.", Toast.LENGTH_SHORT).show();
                    binding.etPin.setText("");
                }
            }
        });

        binding.swipeRefreshVault.setOnRefreshListener(() -> {
            binding.swipeRefreshVault.setRefreshing(false);
        });
    }

    private void unlockVault() {
        isUnlocked = true;
        binding.layoutPinLock.setVisibility(View.GONE);
        binding.swipeRefreshVault.setVisibility(View.VISIBLE);

        adapter = new VaultAdapter(requireContext());
        adapter.setListener(this);
        binding.rvVaultItems.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.rvVaultItems.setAdapter(adapter);

        viewModel.getVaultItems().observe(getViewLifecycleOwner(), items -> {
            adapter.submitList(items);
            binding.emptyVaultState.setVisibility(items == null || items.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onRestoreClick(VaultItem item) {
        viewModel.restoreItem(item, () -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Restored: " + item.getOriginalName(), Toast.LENGTH_SHORT).show();
                    if (allMediaViewModel != null) {
                        allMediaViewModel.loadMedia();
                    }
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
