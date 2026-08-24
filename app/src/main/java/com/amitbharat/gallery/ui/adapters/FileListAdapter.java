package com.amitbharat.gallery.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.amitbharat.gallery.R;
import com.amitbharat.gallery.data.models.FileItem;
import com.amitbharat.gallery.utils.DateUtils;
import com.amitbharat.gallery.utils.FileUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.FileViewHolder> {

    public interface OnFileClickListener {
        void onFileClick(FileItem file, int position);
        void onFileLongClick(FileItem file, int position);
        void onFileMoreClick(FileItem file, View anchor);
    }

    private final Context context;
    private final List<FileItem> fileList = new ArrayList<>();
    private OnFileClickListener listener;

    public FileListAdapter(Context context) {
        this.context = context;
        setStateRestorationPolicy(StateRestorationPolicy.PREVENT_WHEN_EMPTY);
    }

    public void setListener(OnFileClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<FileItem> newList) {
        fileList.clear();
        if (newList != null) {
            fileList.addAll(newList);
        }
        notifyDataSetChanged();
    }

    public List<FileItem> getFileList() {
        return fileList;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_list, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        holder.bind(fileList.get(position));
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    class FileViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFileIcon, imgCheck;
        TextView tvFileName, tvFileDetails;
        ImageButton btnMore;

        FileViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFileIcon = itemView.findViewById(R.id.imgFileIcon);
            imgCheck = itemView.findViewById(R.id.imgCheck);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            tvFileDetails = itemView.findViewById(R.id.tvFileDetails);
            btnMore = itemView.findViewById(R.id.btnMore);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onFileClick(fileList.get(pos), pos);
                }
            });

            itemView.setOnLongClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onFileLongClick(fileList.get(pos), pos);
                }
                return true;
            });

            btnMore.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onFileMoreClick(fileList.get(pos), v);
                }
            });
        }

        void bind(FileItem item) {
            tvFileName.setText(item.getName());

            if (item.isDirectory()) {
                imgFileIcon.setImageResource(R.drawable.ic_folder);
                tvFileDetails.setText(item.getChildCount() + " items • " + DateUtils.formatDate(item.getLastModified()));
            } else {
                String ext = item.getExtension().toLowerCase();
                if (ext.equals("jpg") || ext.equals("png") || ext.equals("jpeg") || ext.equals("webp") || ext.equals("mp4")) {
                    Glide.with(context)
                            .load(new File(item.getPath()))
                            .centerCrop()
                            .placeholder(FileUtils.getFileIconRes(item))
                            .into(imgFileIcon);
                } else {
                    imgFileIcon.setImageResource(FileUtils.getFileIconRes(item));
                }
                tvFileDetails.setText(FileUtils.formatFileSize(item.getSize()) + " • " + DateUtils.formatDate(item.getLastModified()));
            }

            imgCheck.setVisibility(item.isSelected() ? View.VISIBLE : View.GONE);
        }
    }
}
