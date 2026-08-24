package com.amitbharat.gallery.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.amitbharat.gallery.R;
import com.amitbharat.gallery.data.models.StorageInfo;
import com.amitbharat.gallery.utils.FileUtils;
import java.util.ArrayList;
import java.util.List;

public class StorageVolumeAdapter extends RecyclerView.Adapter<StorageVolumeAdapter.VolumeViewHolder> {

    public interface OnVolumeClickListener {
        void onVolumeClick(StorageInfo volume);
    }

    private final List<StorageInfo> volumeList = new ArrayList<>();
    private OnVolumeClickListener listener;

    public StorageVolumeAdapter() {}

    public void setListener(OnVolumeClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<StorageInfo> list) {
        volumeList.clear();
        if (list != null) {
            volumeList.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VolumeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_storage_card, parent, false);
        return new VolumeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VolumeViewHolder holder, int position) {
        holder.bind(volumeList.get(position));
    }

    @Override
    public int getItemCount() {
        return volumeList.size();
    }

    class VolumeViewHolder extends RecyclerView.ViewHolder {
        ImageView imgStorageIcon;
        TextView tvStorageName, tvStorageCapacity, tvFreeSpace;
        ProgressBar pbStorageUsage;

        VolumeViewHolder(@NonNull View itemView) {
            super(itemView);
            imgStorageIcon = itemView.findViewById(R.id.imgStorageIcon);
            tvStorageName = itemView.findViewById(R.id.tvStorageName);
            tvStorageCapacity = itemView.findViewById(R.id.tvStorageCapacity);
            tvFreeSpace = itemView.findViewById(R.id.tvFreeSpace);
            pbStorageUsage = itemView.findViewById(R.id.pbStorageUsage);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onVolumeClick(volumeList.get(pos));
                }
            });
        }

        void bind(StorageInfo info) {
            tvStorageName.setText(info.getName());
            tvStorageCapacity.setText(FileUtils.formatFileSize(info.getUsedBytes()) + " / " + FileUtils.formatFileSize(info.getTotalBytes()));
            pbStorageUsage.setProgress(info.getUsagePercentage());
            tvFreeSpace.setText(FileUtils.formatFileSize(info.getFreeBytes()) + " Free (" + (100 - info.getUsagePercentage()) + "%)");

            if (info.isUsb()) {
                imgStorageIcon.setImageResource(R.drawable.ic_device);
            } else if (info.isRemovable()) {
                imgStorageIcon.setImageResource(R.drawable.ic_device);
            } else {
                imgStorageIcon.setImageResource(R.drawable.ic_device);
            }
        }
    }
}
