package com.amitbharat.gallery;

import android.app.Application;
import com.amitbharat.gallery.utils.ThemeUtils;

public class GalleryApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Theme from user preferences
        ThemeUtils.applyTheme(this);
    }
}
