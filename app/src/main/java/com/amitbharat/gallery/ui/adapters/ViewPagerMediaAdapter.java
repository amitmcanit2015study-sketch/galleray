package com.amitbharat.gallery.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.amitbharat.gallery.R;
import com.amitbharat.gallery.data.models.MediaItem;
import com.amitbharat.gallery.ui.custom.ZoomableImageView;
import java.util.ArrayList;
import java.util.List;

public class ViewPagerMediaAdapter extends RecyclerView.Adapter<ViewPagerMediaAdapter.MediaViewHolder> {

    public interface OnMediaViewerListener {
        void onSingleTap();
        void onPlayVideoClick(MediaItem item);
    }

    private final Context context;
    private final List<MediaItem> items = new ArrayList<>();
    private OnMediaViewerListener listener;

    public ViewPagerMediaAdapter(Context context) {
        this.context = context;
    }

    public void setListener(OnMediaViewerListener listener) {
        this.listener = listener;
    }

    public void submitList(List<MediaItem> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        notifyDataSetChanged();
    }

    public MediaItem getItem(int position) {
        if (position >= 0 && position < items.size()) {
            return items.get(position);
        }
        return null;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_viewer_page, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class MediaViewHolder extends RecyclerView.ViewHolder {
        ZoomableImageView zoomableImageView;
        ImageView btnPlayVideoOverlay;

        MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            zoomableImageView = itemView.findViewById(R.id.zoomableImageView);
            btnPlayVideoOverlay = itemView.findViewById(R.id.btnPlayVideoOverlay);

            zoomableImageView.setOnSingleClickListener(v -> {
                if (listener != null) {
                    listener.onSingleTap();
                }
            });

            btnPlayVideoOverlay.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPlayVideoClick(items.get(pos));
                }
            });
        }

        void bind(MediaItem item) {
            Glide.with(context)
                    .load(item.getUri() != null ? item.getUri() : item.getPath())
                    .into(zoomableImageView);

            btnPlayVideoOverlay.setVisibility(item.isVideo() ? View.VISIBLE : View.GONE);
        }
    }
}
