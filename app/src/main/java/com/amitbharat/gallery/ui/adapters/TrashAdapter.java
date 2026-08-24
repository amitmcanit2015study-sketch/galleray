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
import com.amitbharat.gallery.data.database.TrashEntity;
import com.amitbharat.gallery.utils.DateUtils;
import com.amitbharat.gallery.utils.FileUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TrashAdapter extends RecyclerView.Adapter<TrashAdapter.TrashViewHolder> {

    public interface OnTrashActionListener {
        void onRestore(TrashEntity item);
        void onDeletePermanent(TrashEntity item);
    }

    private final Context context;
    private final List<TrashEntity> items = new ArrayList<>();
    private OnTrashActionListener listener;

    public TrashAdapter(Context context) {
        this.context = context;
        setStateRestorationPolicy(StateRestorationPolicy.PREVENT_WHEN_EMPTY);
    }

    public void setListener(OnTrashActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<TrashEntity> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TrashViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trash_grid, parent, false);
        return new TrashViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrashViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class TrashViewHolder extends RecyclerView.ViewHolder {
        ImageView imgTrashThumbnail;
        TextView tvTrashName, tvTrashInfo;
        Button btnRestoreTrash, btnDeletePermanent;

        TrashViewHolder(@NonNull View itemView) {
            super(itemView);
            imgTrashThumbnail = itemView.findViewById(R.id.imgTrashThumbnail);
            tvTrashName = itemView.findViewById(R.id.tvTrashName);
            tvTrashInfo = itemView.findViewById(R.id.tvTrashInfo);
            btnRestoreTrash = itemView.findViewById(R.id.btnRestoreTrash);
            btnDeletePermanent = itemView.findViewById(R.id.btnDeletePermanent);

            btnRestoreTrash.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onRestore(items.get(pos));
                }
            });

            btnDeletePermanent.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDeletePermanent(items.get(pos));
                }
            });
        }

        void bind(TrashEntity item) {
            tvTrashName.setText(item.getFileName());
            tvTrashInfo.setText(FileUtils.formatFileSize(item.getSize()) + " • " + DateUtils.formatDateTime(item.getDeletedTimestamp()));

            if (item.isDirectory()) {
                imgTrashThumbnail.setImageResource(R.drawable.ic_folder);
            } else if (item.getTrashPath() != null) {
                File file = new File(item.getTrashPath());
                try {
                    Glide.with(context)
                            .asBitmap()
                            .load(file)
                            .placeholder(R.drawable.ic_delete)
                            .error(R.drawable.ic_delete)
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(imgTrashThumbnail);
                } catch (Exception e) {
                    imgTrashThumbnail.setImageResource(R.drawable.ic_delete);
                }
            } else {
                imgTrashThumbnail.setImageResource(R.drawable.ic_delete);
            }
        }
    }
}
