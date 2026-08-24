package com.amitbharat.gallery.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.amitbharat.gallery.R;
import com.amitbharat.gallery.data.models.VaultItem;
import com.amitbharat.gallery.utils.FileUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VaultAdapter extends RecyclerView.Adapter<VaultAdapter.VaultViewHolder> {

    public interface OnVaultItemClickListener {
        void onRestoreClick(VaultItem item);
    }

    private final Context context;
    private final List<VaultItem> items = new ArrayList<>();
    private OnVaultItemClickListener listener;

    public VaultAdapter(Context context) {
        this.context = context;
        setStateRestorationPolicy(StateRestorationPolicy.PREVENT_WHEN_EMPTY);
    }

    public void setListener(OnVaultItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<VaultItem> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VaultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vault_grid, parent, false);
        return new VaultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VaultViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class VaultViewHolder extends RecyclerView.ViewHolder {
        ImageView imgVaultThumbnail;
        TextView tvVaultName, tvVaultSize;
        Button btnRestore;

        VaultViewHolder(@NonNull View itemView) {
            super(itemView);
            imgVaultThumbnail = itemView.findViewById(R.id.imgVaultThumbnail);
            tvVaultName = itemView.findViewById(R.id.tvVaultName);
            tvVaultSize = itemView.findViewById(R.id.tvVaultSize);
            btnRestore = itemView.findViewById(R.id.btnRestore);

            btnRestore.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onRestoreClick(items.get(pos));
                }
            });
        }

        void bind(VaultItem item) {
            tvVaultName.setText(item.getOriginalName());
            tvVaultSize.setText(FileUtils.formatFileSize(item.getSize()));

            if (item.getVaultPath() != null) {
                File file = new File(item.getVaultPath());
                Glide.with(context)
                        .load(file)
                        .placeholder(R.drawable.ic_vault)
                        .error(R.drawable.ic_vault)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .into(imgVaultThumbnail);
            }
        }
    }
}
