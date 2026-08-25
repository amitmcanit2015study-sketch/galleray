package com.amitbharat.gallery.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.amitbharat.gallery.R;
import com.amitbharat.gallery.data.models.MediaItem;
import com.amitbharat.gallery.utils.DateUtils;
import com.amitbharat.gallery.utils.FileUtils;
import com.amitbharat.gallery.utils.MediaUtils;
import java.util.ArrayList;
import java.util.List;

public class MediaGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnMediaClickListener {
        void onMediaClick(MediaItem item, int position);
        void onMediaLongClick(MediaItem item, int position);
    }

    private static final int TYPE_GRID = 0;
    private static final int TYPE_LIST = 1;

    private final Context context;
    private final List<MediaItem> mediaList = new ArrayList<>();
    private boolean isGridView = true;
    private OnMediaClickListener listener;

    public MediaGridAdapter(Context context) {
        this.context = context;
        setStateRestorationPolicy(StateRestorationPolicy.PREVENT_WHEN_EMPTY);
    }

    public void setListener(OnMediaClickListener listener) {
        this.listener = listener;
    }

    public void setGridView(boolean isGrid) {
        if (this.isGridView != isGrid) {
            this.isGridView = isGrid;
            notifyDataSetChanged();
        }
    }

    public boolean isGridView() {
        return isGridView;
    }

    public void submitList(List<MediaItem> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() { return mediaList.size(); }
            @Override
            public int getNewListSize() { return newList != null ? newList.size() : 0; }
            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return mediaList.get(oldItemPosition).equals(newList.get(newItemPosition));
            }
            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                MediaItem oldItem = mediaList.get(oldItemPosition);
                MediaItem newItem = newList.get(newItemPosition);
                return oldItem.isSelected() == newItem.isSelected() &&
                        oldItem.isFavorite() == newItem.isFavorite() &&
                        oldItem.getSize() == newItem.getSize();
            }
        });

        mediaList.clear();
        if (newList != null) {
            mediaList.addAll(newList);
        }
        diffResult.dispatchUpdatesTo(this);
    }

    public List<MediaItem> getMediaList() {
        return mediaList;
    }

    public void clearAllSelections() {
        for (MediaItem item : mediaList) {
            item.setSelected(false);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return isGridView ? TYPE_GRID : TYPE_LIST;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_GRID) {
            View view = inflater.inflate(R.layout.item_media_grid, parent, false);
            return new GridViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_media_list, parent, false);
            return new ListViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MediaItem item = mediaList.get(position);
        if (holder instanceof GridViewHolder) {
            ((GridViewHolder) holder).bind(item);
        } else if (holder instanceof ListViewHolder) {
            ((ListViewHolder) holder).bind(item);
        }
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    class GridViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumbnail, imgCheck, imgFavorite;
        TextView tvDuration;

        GridViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.imgThumbnail);
            imgCheck = itemView.findViewById(R.id.imgCheck);
            imgFavorite = itemView.findViewById(R.id.imgFavorite);
            tvDuration = itemView.findViewById(R.id.tvDuration);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onMediaClick(mediaList.get(pos), pos);
                }
            });

            itemView.setOnLongClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onMediaLongClick(mediaList.get(pos), pos);
                }
                return true;
            });
        }

        void bind(MediaItem item) {
            Glide.with(context)
                    .load(item.getUri() != null ? item.getUri() : item.getPath())
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imgThumbnail);

            if (item.isVideo() && item.getDuration() > 0) {
                tvDuration.setVisibility(View.VISIBLE);
                tvDuration.setText(MediaUtils.formatDuration(item.getDuration()));
            } else {
                tvDuration.setVisibility(View.GONE);
            }

            imgCheck.setVisibility(item.isSelected() ? View.VISIBLE : View.GONE);
            imgFavorite.setVisibility(item.isFavorite() ? View.VISIBLE : View.GONE);
        }
    }

    class ListViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumbnail, imgCheck, imgFavorite;
        TextView tvTitle, tvDetails, tvDuration;

        ListViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.imgThumbnail);
            imgCheck = itemView.findViewById(R.id.imgCheck);
            imgFavorite = itemView.findViewById(R.id.imgFavorite);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            tvDuration = itemView.findViewById(R.id.tvDuration);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onMediaClick(mediaList.get(pos), pos);
                }
            });

            itemView.setOnLongClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onMediaLongClick(mediaList.get(pos), pos);
                }
                return true;
            });
        }

        void bind(MediaItem item) {
            Glide.with(context)
                    .load(item.getUri() != null ? item.getUri() : item.getPath())
                    .centerCrop()
                    .into(imgThumbnail);

            tvTitle.setText(item.getDisplayName());
            String details = FileUtils.formatFileSize(item.getSize()) + " • " + DateUtils.formatDate(item.getDateAdded());
            tvDetails.setText(details);

            if (item.isVideo() && item.getDuration() > 0) {
                tvDuration.setVisibility(View.VISIBLE);
                tvDuration.setText(MediaUtils.formatDuration(item.getDuration()));
            } else {
                tvDuration.setVisibility(View.GONE);
            }

            imgCheck.setVisibility(item.isSelected() ? View.VISIBLE : View.GONE);
            imgFavorite.setVisibility(item.isFavorite() ? View.VISIBLE : View.GONE);
        }
    }
}
