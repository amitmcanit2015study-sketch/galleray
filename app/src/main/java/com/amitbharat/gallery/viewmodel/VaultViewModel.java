package com.amitbharat.gallery.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.amitbharat.gallery.data.models.FileItem;
import com.amitbharat.gallery.data.models.MediaItem;
import com.amitbharat.gallery.data.models.VaultItem;
import com.amitbharat.gallery.data.repository.VaultRepository;
import java.util.List;

public class VaultViewModel extends AndroidViewModel {
    private final VaultRepository vaultRepository;

    public VaultViewModel(@NonNull Application application) {
        super(application);
        this.vaultRepository = new VaultRepository(application);
    }

    public LiveData<List<VaultItem>> getVaultItems() {
        return vaultRepository.getAllVaultItems();
    }

    public void hideMedia(MediaItem item, Runnable onDone) {
        vaultRepository.hideMedia(item, onDone);
    }

    public void hideFile(FileItem item, Runnable onDone) {
        vaultRepository.hideFile(item, onDone);
    }

    public void restoreItem(VaultItem item, Runnable onDone) {
        vaultRepository.restoreItem(item, onDone);
    }
}
