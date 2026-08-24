package com.amitbharat.gallery.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.amitbharat.gallery.R;
import com.amitbharat.gallery.data.models.FolderItem;
import com.amitbharat.gallery.utils.FileUtils;
import java.util.ArrayList;
import java.util.List;

public class FolderGridAdapter extends RecyclerView.Adapter<FolderGridAdapter.FolderViewHolder> {

    public interface OnFolderClickListener {
        void onFolderClick(FolderItem folder);
    }

    private final Context context;
    private final List<FolderItem> folderList = new ArrayList<>();
    private OnFolderClickListener listener;

    public FolderGridAdapter(Context context) {
        this.context = context;
        setStateRestorationPolicy(StateRestorationPolicy.PREVENT_WHEN_EMPTY);
    }

    public void setListener(OnFolderClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<FolderItem> newList) {
        folderList.clear();
        if (newList != null) {
            folderList.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_folder_grid, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        holder.bind(folderList.get(position));
    }

    @Override
    public int getItemCount() {
        return folderList.size();
    }

    class FolderViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCover, imgFolderIcon;
        TextView tvCountBadge, tvFolderName, tvFolderDetails;

        FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.imgCover);
            imgFolderIcon = itemView.findViewById(R.id.imgFolderIcon);
            tvCountBadge = itemView.findViewById(R.id.tvCountBadge);
            tvFolderName = itemView.findViewById(R.id.tvFolderName);
            tvFolderDetails = itemView.findViewById(R.id.tvFolderDetails);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onFolderClick(folderList.get(pos));
                }
            });
        }

        void bind(FolderItem folder) {
            tvFolderName.setText(folder.getFolderName());
            tvCountBadge.setText(String.valueOf(folder.getFileCount()));
            tvFolderDetails.setText(folder.getFileCount() + " items • " + FileUtils.formatFileSize(folder.getTotalSize()));

            if (folder.getCoverUri() != null || folder.getCoverPath() != null) {
                imgFolderIcon.setVisibility(View.GONE);
                Glide.with(context)
                        .load(folder.getCoverUri() != null ? folder.getCoverUri() : folder.getCoverPath())
                        .centerCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imgCover);
            } else {
                imgCover.setImageDrawable(null);
                imgFolderIcon.setVisibility(View.VISIBLE);
            }
        }
    }
}
