package com.amitbharat.gallery.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.amitbharat.gallery.R;
import java.util.ArrayList;
import java.util.List;

public class BreadcrumbAdapter extends RecyclerView.Adapter<BreadcrumbAdapter.BreadcrumbViewHolder> {

    public interface OnCrumbClickListener {
        void onCrumbClick(int position);
    }

    private final List<String> crumbs = new ArrayList<>();
    private OnCrumbClickListener listener;

    public void setListener(OnCrumbClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<String> list) {
        crumbs.clear();
        if (list != null) {
            crumbs.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BreadcrumbViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_breadcrumb, parent, false);
        return new BreadcrumbViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BreadcrumbViewHolder holder, int position) {
        holder.tvCrumbName.setText(crumbs.get(position));
    }

    @Override
    public int getItemCount() {
        return crumbs.size();
    }

    class BreadcrumbViewHolder extends RecyclerView.ViewHolder {
        TextView tvCrumbName;

        BreadcrumbViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCrumbName = itemView.findViewById(R.id.tvCrumbName);

            tvCrumbName.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCrumbClick(pos);
                }
            });
        }
    }
}
