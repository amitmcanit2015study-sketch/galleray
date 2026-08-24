package com.amitbharat.gallery.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.amitbharat.gallery.R;
import com.amitbharat.gallery.data.models.StorageCategory;
import com.amitbharat.gallery.utils.FileUtils;
import java.util.ArrayList;
import java.util.List;

public class StorageCategoryAdapter extends RecyclerView.Adapter<StorageCategoryAdapter.CategoryViewHolder> {

    private final Context context;
    private final List<StorageCategory> categories = new ArrayList<>();

    public StorageCategoryAdapter(Context context) {
        this.context = context;
    }

    public void submitList(List<StorageCategory> list) {
        categories.clear();
        if (list != null) {
            categories.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_storage_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        holder.bind(categories.get(position));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCategoryIcon;
        TextView tvCategoryName, tvCategoryItemCount, tvCategorySize;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCategoryIcon = itemView.findViewById(R.id.imgCategoryIcon);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvCategoryItemCount = itemView.findViewById(R.id.tvCategoryItemCount);
            tvCategorySize = itemView.findViewById(R.id.tvCategorySize);
        }

        void bind(StorageCategory cat) {
            imgCategoryIcon.setImageResource(cat.getIconRes());
            imgCategoryIcon.setColorFilter(ContextCompat.getColor(context, cat.getColorRes()));
            tvCategoryName.setText(cat.getName());
            tvCategoryItemCount.setText(cat.getItemCount() + " items");
            tvCategorySize.setText(FileUtils.formatFileSize(cat.getTotalBytes()));
        }
    }
}
