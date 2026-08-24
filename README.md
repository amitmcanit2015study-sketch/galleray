# Gallery (Gallery + File Manager)

A modern, high-performance Android application combining a **Media Gallery** and a **File Explorer** into one seamless experience.

**Company:** Rooys Soft Tech  
**Developer:** Amit Bharat  
**Email Support:** rooyssofttech2020@gmail.com  
**Package Name:** `com.amitbharat.gallery`  
**Language:** 100% Java  
**Architecture:** MVVM (Model-View-ViewModel) + Room Database + ViewBinding  
**UI Framework:** Material Design 3 (M3)  
**Minimum SDK:** API 33 (Android 13) | **Target SDK:** API 34 (Android 14) / Android 15 Ready  
**Permissions:** Modern Granular Media Permissions (`READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_MEDIA_AUDIO`, `READ_MEDIA_VISUAL_USER_SELECTED`), Scoped Storage & Predictive Back support.  
**Offline First:** 100% Offline • Zero Trackers • No Ads • No External Analytics  

---

## 🌟 Key Features

### 1. Home Screen (3 Primary Tabs)
* **Tab 1: All Media (`AllMediaFragment`)**
  * Instant MediaStore background scanner for both internal and mounted external storages.
  * Sort by Created Date (Newest first), Name, Size.
  * Fast RecyclerView with lazy loading and image caching via Glide.
  * Instant Grid / List toggle without restarting activity.
  * Video duration badges, multi-selection action mode (Share, Favorite, Delete, Copy, Move, Compress).
* **Tab 2: Folders & Smart Albums (`FoldersFragment`)**
  * Automatic album aggregation (Camera, DCIM, Screenshots, Download, WhatsApp, Movies, Instagram, Pictures, Custom folders).
  * Filter chips: All, Camera, Screenshots, Downloads, Videos, Favorites.
  * Displays folder cover thumbnail, item count, total folder size, and modified date.
  * Opens nested folder media viewer.
* **Tab 3: Device Explorer & Storage Analyzer (`DeviceExplorerFragment`)**
  * Storage Overview cards (Internal Storage, SD Card, USB OTG) with colorful capacity progress bar.
  * Storage Analyzer shortcuts (Storage Breakdown, Cleaner, Vault).
  * Breadcrumb navigation bar with backward/forward path traversal and unlimited depth.
  * File Operations: Open, Rename, Copy, Move, Delete, Share, Create Folder, Zip Compression, ZIP Extraction, Properties / EXIF Inspector.

### 2. Media Viewers & Tools
* **Pinch-to-Zoom Image Viewer (`ImageViewerActivity`)**
  * ViewPager2 swiping with touch-friendly `ZoomableImageView` (pinch-to-zoom, double-tap zoom, smooth panning).
  * Immersive fullscreen mode (tap to toggle controls).
  * Slideshow auto-play mode (3-second intervals).
  * EXIF Inspector bottom sheet (Aperture, ISO, Exposure, Focal Length, Camera Model, GPS Location, Resolution, Size).
  * Set as Wallpaper directly via WallpaperManager.
  * Favorite toggle persisted in Room database.
* **Image Crop & Transform Tool (`ImageCropActivity`)**
  * Aspect ratios: Free, 1:1 Square, 4:3, 16:9.
  * 90° Rotation, Horizontal Flip, Vertical Flip.
  * Export options: "Save as Copy" or "Overwrite Original".
* **Built-in Video Player (`VideoPlayerActivity`)**
  * Powered by Media3 / ExoPlayer.
  * Gesture Controls: Left-half swipe for brightness, right-half swipe for volume, horizontal swipe for seek.
  * Aspect ratio modes: Fit, Fill, Zoom.
  * Playback Speed selector: 0.5x, 0.75x, 1.0x, 1.25x, 1.5x, 2.0x.
  * Picture-in-Picture (PiP) support.
* **Storage Analyzer & Cleaner (`StorageAnalyzerActivity`)**
  * Segmented colorful storage bar chart.
  * Category breakdown: Images, Videos, Audio, Documents, APKs, Archives, Other.
  * Large file scanner (>50 MB).
  * Duplicate file candidate detector (by size & hash/name).
  * Empty folder cleaner.
* **Secure Private Vault (`SecureVaultActivity`)**
  * 4-Digit Security PIN protection with SHA-256 hashing.
  * Encrypted/hidden storage for confidential photos and files.
  * One-tap restore to original file path.
* **Global Search & Filter (`SearchActivity`)**
  * Real-time search across all photos, videos, and folders.
  * Filter chips: All, Images, Videos, GIF, Large Files.
* **Settings & Customization (`SettingsActivity`)**
  * Theme switcher: System Default, Light, Dark.
  * Grid columns customizer: 2, 3, 4, or 5 columns.
  * Show/Hide hidden files and folders.
  * One-click cache cleaner for thumbnail and disk storage.

---

## 🛠️ Project Structure

```
d:/_test/_-_gallery/
├── build.gradle
├── settings.gradle
├── gradle.properties
├── gradle/wrapper/gradle-wrapper.properties
└── app/
    ├── build.gradle
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/amitbharat/gallery/
        │   ├── GalleryApplication.java
        │   ├── data/
        │   │   ├── database/ (AppDatabase, DAOs for Favorites, Trash, Bookmarks, Vault)
        │   │   ├── models/ (MediaItem, FolderItem, FileItem, StorageInfo, StorageCategory, ExifInfo, FilterOptions, VaultItem)
        │   │   └── repository/ (MediaRepository, FileRepository, StorageRepository, VaultRepository)
        │   ├── viewmodel/ (AllMediaViewModel, FoldersViewModel, DeviceExplorerViewModel, SearchViewModel, StorageAnalyzerViewModel, VaultViewModel)
        │   ├── ui/
        │   │   ├── activities/ (SplashActivity, MainActivity, FolderDetailActivity, ImageViewerActivity, ImageCropActivity, VideoPlayerActivity, StorageAnalyzerActivity, SecureVaultActivity, SearchActivity, SettingsActivity, AboutActivity)
        │   │   ├── fragments/ (AllMediaFragment, FoldersFragment, DeviceExplorerFragment)
        │   │   ├── adapters/ (MediaGridAdapter, FolderGridAdapter, FileListAdapter, StorageVolumeAdapter, StorageCategoryAdapter, BreadcrumbAdapter, VaultAdapter, ViewPagerMediaAdapter)
        │   │   └── custom/ (ZoomableImageView, StorageProgressView, VideoGestureOverlay, CropImageView)
        │   └── utils/ (PermissionHelper, StorageUtils, FileUtils, MediaUtils, DateUtils, ExifHelper, ZipHelper, ThemeUtils, PreferencesManager, SecurityUtils)
        └── res/
            ├── drawable/ (Vector icons, adaptive icons, gradients, cards, badges)
            ├── mipmap-anydpi-v26/ (ic_launcher.xml, ic_launcher_round.xml)
            ├── layout/ (All XML layouts for activities, fragments, items, dialogs)
            ├── menu/ (main_menu.xml, media_selection_menu.xml, folder_detail_menu.xml)
            ├── values/ (colors.xml, strings.xml, themes.xml, styles.xml)
            ├── values-night/ (themes.xml)
            └── xml/ (file_paths.xml)
```

---

## 🚀 How to Build & Run

1. Open **Android Studio** (Hedgehog, Iguana, Jellyfish, or newer).
2. Select **Open** and choose the directory: `d:\_test\_-_gallery`.
3. Allow Gradle to sync dependencies.
4. Connect an Android device or launch an Emulator running Android 7.0 (API 24) to Android 14+ (API 34).
5. Click **Run (`Shift + F10`)**.
