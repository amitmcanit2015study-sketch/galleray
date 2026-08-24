package com.amitbharat.gallery.utils;

import android.content.Context;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeUtils {

    public static void applyTheme(Context context) {
        int themeMode = PreferencesManager.getInstance(context).getThemeMode();
        switch (themeMode) {
            case 1:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case 2:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case 0:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
}
