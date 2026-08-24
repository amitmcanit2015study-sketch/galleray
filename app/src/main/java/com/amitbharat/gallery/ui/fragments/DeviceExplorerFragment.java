package com.amitbharat.gallery.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.amitbharat.gallery.R;
import com.amitbharat.gallery.data.models.FileItem;
import com.amitbharat.gallery.data.models.MediaItem;
import com.amitbharat.gallery.data.models.ExifInfo;
import com.amitbharat.gallery.databinding.FragmentDeviceExplorerBinding;
import com.amitbharat.gallery.ui.activities.RecycleBinActivity;
import com.amitbharat.gallery.ui.activities.SecureVaultActivity;
import com.amitbharat.gallery.ui.activities.StorageAnalyzerActivity;
import com.amitbharat.gallery.ui.adapters.BreadcrumbAdapter;
import com.amitbharat.gallery.ui.adapters.FileListAdapter;
import com.amitbharat.gallery.ui.adapters.StorageVolumeAdapter;
import com.amitbharat.gallery.utils.DateUtils;
import com.amitbharat.gallery.utils.ExifHelper;
import com.amitbharat.gallery.utils.FileUtils;
import com.amitbharat.gallery.utils.ZipHelper;
import com.amitbharat.gallery.viewmodel.DeviceExplorerViewModel;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeviceExplorerFragment extends Fragment implements
        FileListAdapter.OnFileClickListener,
        StorageVolumeAdapter.OnVolumeClickListener,
        BreadcrumbAdapter.OnCrumbClickListener {

    private FragmentDeviceExplorerBinding binding;
    private DeviceExplorerViewModel viewModel;
    private FileListAdapter fileAdapter;
    private StorageVolumeAdapter volumeAdapter;
    private BreadcrumbAdapter breadcrumbAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDeviceExplorerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(DeviceExplorerViewModel.class);

        volumeAdapter = new StorageVolumeAdapter();
        volumeAdapter.setListener(this);
        binding.rvStorageVolumes.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvStorageVolumes.setAdapter(volumeAdapter);

        breadcrumbAdapter = new BreadcrumbAdapter();
        breadcrumbAdapter.setListener(this);
        binding.rvBreadcrumbs.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvBreadcrumbs.setAdapter(breadcrumbAdapter);

        fileAdapter = new FileListAdapter(requireContext());
        fileAdapter.setListener(this);
        binding.recyclerViewFiles.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewFiles.setAdapter(fileAdapter);

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());

        setupClickListeners();
        observeViewModel();

        viewModel.loadStorageVolumes();
        File root = Environment.getExternalStorageDirectory();
        if (root != null) {
            viewModel.openPath(root.getAbsolutePath());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refresh();
        }
    }

    private void setupClickListeners() {
        binding.btnBreadcrumbBack.setOnClickListener(v -> viewModel.navigateBack());

        binding.btnCreateFolder.setOnClickListener(v -> showCreateFolderDialog());

        binding.btnToggleHidden.setOnClickListener(v -> {
            viewModel.toggleHiddenFiles();
            boolean shown = com.amitbharat.gallery.utils.PreferencesManager.getInstance(requireContext()).isShowHiddenFiles();
            android.widget.Toast.makeText(requireContext(), shown ? "Hidden files visible" : "Hidden files hidden", android.widget.Toast.LENGTH_SHORT).show();
        });

        binding.btnQuickCleaner.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), StorageAnalyzerActivity.class);
            startActivity(intent);
        });

        binding.btnQuickVault.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SecureVaultActivity.class);
            startActivity(intent);
        });

        binding.btnQuickRecycleBin.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), RecycleBinActivity.class);
            startActivity(intent);
        });

        binding.chipCategoryApks.setOnClickListener(v -> viewModel.openCategory("apks", "📦 APK Files"));
        binding.chipCategoryDocs.setOnClickListener(v -> viewModel.openCategory("documents", "📄 Documents"));
        binding.chipCategoryAudio.setOnClickListener(v -> viewModel.openCategory("audio", "🎵 Audio"));
        binding.chipCategoryArchives.setOnClickListener(v -> viewModel.openCategory("archives", "🗜️ Archives"));
    }

    private void observeViewModel() {
        viewModel.getStorageVolumesLive().observe(getViewLifecycleOwner(), volumes -> {
            volumeAdapter.submitList(volumes);
        });

        viewModel.getCurrentFilesLive().observe(getViewLifecycleOwner(), files -> {
            fileAdapter.submitList(files);
        });

        viewModel.getBreadcrumbsLive().observe(getViewLifecycleOwner(), crumbs -> {
            breadcrumbAdapter.submitList(crumbs);
        });

        viewModel.getIsLoadingLive().observe(getViewLifecycleOwner(), isLoading -> {
            binding.swipeRefresh.setRefreshing(isLoading);
        });
    }

    @Override
    public void onFileClick(FileItem file, int position) {
        if (Boolean.TRUE.equals(viewModel.getIsSelectionMode().getValue())) {
            viewModel.toggleSelection(file);
            fileAdapter.notifyItemChanged(position);
        } else {
            if (file.isDirectory()) {
                viewModel.navigateTo(file.getPath());
            } else if (isVideoFile(file)) {
                MediaItem mediaItem = fileItemToMediaItem(file);
                Intent intent = new Intent(requireContext(), com.amitbharat.gallery.ui.activities.VideoPlayerActivity.class);
                intent.putExtra("media_item", mediaItem);
                startActivity(intent);
            } else if (isImageFile(file)) {
                List<FileItem> allFiles = viewModel.getCurrentFilesLive().getValue();
                List<MediaItem> imageList = new ArrayList<>();
                int clickedIndex = 0;
                if (allFiles != null) {
                    for (FileItem f : allFiles) {
                        if (isImageFile(f)) {
                            if (f.getPath().equals(file.getPath())) {
                                clickedIndex = imageList.size();
                            }
                            imageList.add(fileItemToMediaItem(f));
                        }
                    }
                }
                if (imageList.isEmpty()) {
                    imageList.add(fileItemToMediaItem(file));
                    clickedIndex = 0;
                }
                com.amitbharat.gallery.utils.MediaHolder.setCurrentMediaList(imageList);
                Intent intent = new Intent(requireContext(), com.amitbharat.gallery.ui.activities.ImageViewerActivity.class);
                intent.putExtra("current_position", clickedIndex);
                startActivity(intent);
            } else {
                FileUtils.openFileWithIntent(requireContext(), new File(file.getPath()));
            }
        }
    }

    private boolean isImageFile(FileItem file) {
        if (file == null || file.isDirectory() || file.getPath() == null) return false;
        String ext = file.getExtension().toLowerCase(java.util.Locale.ROOT);
        return ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png") ||
                ext.equals("webp") || ext.equals("gif") || ext.equals("bmp") ||
                ext.equals("heic") || ext.equals("heif") || ext.equals("svg") ||
                ext.equals("ico") || ext.equals("tiff") || ext.equals("tif");
    }

    private boolean isVideoFile(FileItem file) {
        if (file == null || file.isDirectory() || file.getPath() == null) return false;
        String ext = file.getExtension().toLowerCase(java.util.Locale.ROOT);
        return ext.equals("mp4") || ext.equals("mkv") || ext.equals("webm") ||
                ext.equals("avi") || ext.equals("mov") || ext.equals("3gp") ||
                ext.equals("ts") || ext.equals("flv") || ext.equals("wmv") ||
                ext.equals("m4v") || ext.equals("mpg") || ext.equals("mpeg");
    }

    private MediaItem fileItemToMediaItem(FileItem file) {
        MediaItem item = new MediaItem();
        item.setPath(file.getPath());
        item.setDisplayName(file.getName());
        item.setSize(file.getSize());
        item.setDateModified(file.getLastModified());
        item.setDateAdded(file.getLastModified());
        item.setUri(android.net.Uri.fromFile(new File(file.getPath())));
        item.setMimeType(FileUtils.getMimeType(file.getPath()));
        item.setVideo(isVideoFile(file));
        return item;
    }

    @Override
    public void onFileLongClick(FileItem file, int position) {
        viewModel.toggleSelection(file);
        fileAdapter.notifyItemChanged(position);
    }

    @Override
    public void onFileMoreClick(FileItem file, View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add("Share");
        popup.getMenu().add("Rename");
        popup.getMenu().add("Hide in Secure Vault");
        popup.getMenu().add("Compress (ZIP)");
        if (file.getExtension().equalsIgnoreCase("zip")) {
            popup.getMenu().add("Extract ZIP");
        }
        popup.getMenu().add("Properties");
        popup.getMenu().add("Delete");

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            File targetFile = new File(file.getPath());
            if ("Share".equals(title)) {
                FileUtils.shareFiles(requireContext(), Collections.singletonList(targetFile));
            } else if ("Rename".equals(title)) {
                showRenameDialog(file);
            } else if ("Hide in Secure Vault".equals(title)) {
                new com.amitbharat.gallery.data.repository.VaultRepository(requireContext()).hideFile(file, () -> {
                    requireActivity().runOnUiThread(() -> {
                        android.widget.Toast.makeText(requireContext(), "Moved to Secure Vault", android.widget.Toast.LENGTH_SHORT).show();
                        viewModel.refresh();
                    });
                });
            } else if ("Compress (ZIP)".equals(title)) {
                File zipTarget = new File(targetFile.getParentFile(), targetFile.getName() + ".zip");
                ZipHelper.zipFiles(Collections.singletonList(targetFile), zipTarget);
                viewModel.refresh();
            } else if ("Extract ZIP".equals(title)) {
                File extractDir = new File(targetFile.getParentFile(), targetFile.getName().replace(".zip", ""));
                ZipHelper.unzip(targetFile, extractDir);
                viewModel.refresh();
            } else if ("Properties".equals(title)) {
                showPropertiesDialog(file);
            } else if ("Delete".equals(title)) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Delete " + file.getName() + "?")
                        .setMessage("Move \"" + file.getName() + "\" to the Recycle Bin? You can restore it later.")
                        .setPositiveButton("Yes", (d, which) -> {
                            new com.amitbharat.gallery.data.repository.TrashRepository(requireContext()).moveFileToTrash(file, () -> {
                                requireActivity().runOnUiThread(() -> {
                                    android.widget.Toast.makeText(requireContext(), "Moved to Recycle Bin", android.widget.Toast.LENGTH_SHORT).show();
                                    viewModel.refresh();
                                });
                            });
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
            return true;
        });
        popup.show();
    }

    private void showCreateFolderDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_folder, null);
        EditText etName = dialogView.findViewById(R.id.etFolderName);

        new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Create", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    if (!name.isEmpty()) {
                        viewModel.createFolder(name);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showRenameDialog(FileItem file) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_rename, null);
        EditText etName = dialogView.findViewById(R.id.etNewName);
        etName.setText(file.getName());

        new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Rename", (d, w) -> {
                    String newName = etName.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        viewModel.renameFile(file, newName);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showPropertiesDialog(FileItem file) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_file_properties, null);
        android.widget.TextView tvName = dialogView.findViewById(R.id.tvPropFileName);
        android.widget.TextView tvPath = dialogView.findViewById(R.id.tvPropPath);
        android.widget.TextView tvSize = dialogView.findViewById(R.id.tvPropSize);
        android.widget.TextView tvResolution = dialogView.findViewById(R.id.tvPropResolution);
        android.widget.TextView tvMime = dialogView.findViewById(R.id.tvPropMime);
        android.widget.TextView tvDate = dialogView.findViewById(R.id.tvPropDate);
        View cameraHeader = dialogView.findViewById(R.id.tvHeaderCamera);
        View cameraTable = dialogView.findViewById(R.id.tableCameraExif);

        tvName.setText(file.getName());
        tvPath.setText(file.getPath());
        tvSize.setText(FileUtils.formatFileSize(file.getSize()) + " (" + file.getSize() + " bytes)");
        tvMime.setText(FileUtils.getMimeType(file.getPath()));
        tvDate.setText(DateUtils.formatDateTime(file.getLastModified()));

        String mime = FileUtils.getMimeType(file.getPath());
        if (mime != null && mime.startsWith("image/")) {
            MediaItem tempMedia = new MediaItem();
            tempMedia.setPath(file.getPath());
            tempMedia.setDisplayName(file.getName());
            tempMedia.setSize(file.getSize());
            ExifInfo info = ExifHelper.extractExif(requireContext(), tempMedia);

            tvResolution.setText(info.getResolution() + (!info.getMegapixels().isEmpty() ? " • " + info.getMegapixels() : ""));
            android.widget.TextView tvExifCamera = dialogView.findViewById(R.id.tvExifCamera);
            android.widget.TextView tvExifDateTaken = dialogView.findViewById(R.id.tvExifDateTaken);
            android.widget.TextView tvExifAperture = dialogView.findViewById(R.id.tvExifAperture);
            android.widget.TextView tvExifShutter = dialogView.findViewById(R.id.tvExifShutter);
            android.widget.TextView tvExifIso = dialogView.findViewById(R.id.tvExifIso);
            android.widget.TextView tvExifFocal = dialogView.findViewById(R.id.tvExifFocal);
            android.widget.TextView tvExifFlash = dialogView.findViewById(R.id.tvExifFlash);
            android.widget.TextView tvExifWb = dialogView.findViewById(R.id.tvExifWb);
            android.widget.TextView tvExifLocation = dialogView.findViewById(R.id.tvExifLocation);

            tvExifCamera.setText(info.getCameraModel());
            tvExifDateTaken.setText(info.getDateTaken());
            tvExifAperture.setText(info.getAperture());
            tvExifShutter.setText(info.getExposureTime());
            tvExifIso.setText(info.getIso());
            tvExifFocal.setText(info.getFocalLength() + (!info.getFocalLength35mm().isEmpty() ? " (" + info.getFocalLength35mm() + ")" : ""));
            tvExifFlash.setText(info.getFlash());
            tvExifWb.setText(info.getWhiteBalance());
            tvExifLocation.setText(info.getLocationText());
        } else {
            tvResolution.setText("--");
            if (cameraHeader != null) cameraHeader.setVisibility(View.GONE);
            if (cameraTable != null) cameraTable.setVisibility(View.GONE);
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onVolumeClick(com.amitbharat.gallery.data.models.StorageInfo volume) {
        viewModel.navigateTo(volume.getPath());
    }

    @Override
    public void onCrumbClick(int position) {
        viewModel.navigateToBreadcrumb(position);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
