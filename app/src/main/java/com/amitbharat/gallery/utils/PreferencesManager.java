package com.amitbharat.gallery.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {
    private static final String PREF_NAME = "gallery_prefs";
    private static final String KEY_VIEW_MODE_GRID = "key_view_mode_grid";
    private static final String KEY_GRID_COLUMNS = "key_grid_columns";
    private static final String KEY_SHOW_HIDDEN = "key_show_hidden";
    private static final String KEY_SORT_ORDER = "key_sort_order";
    private static final String KEY_THEME_MODE = "key_theme_mode"; // 0: System, 1: Light, 2: Dark
    private static final String KEY_VAULT_PIN = "key_vault_pin";

    private final SharedPreferences prefs;
    private static PreferencesManager instance;

    private PreferencesManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized PreferencesManager getInstance(Context context) {
        if (instance == null) {
            instance = new PreferencesManager(context);
        }
        return instance;
    }

    public boolean isGridView() {
        return prefs.getBoolean(KEY_VIEW_MODE_GRID, true);
    }

    public void setGridView(boolean isGrid) {
        prefs.edit().putBoolean(KEY_VIEW_MODE_GRID, isGrid).apply();
    }

    public int getGridColumns() {
        return prefs.getInt(KEY_GRID_COLUMNS, 3);
    }

    public void setGridColumns(int columns) {
        prefs.edit().putInt(KEY_GRID_COLUMNS, columns).apply();
    }

    public boolean isShowHiddenFiles() {
        return prefs.getBoolean(KEY_SHOW_HIDDEN, false);
    }

    public void setShowHiddenFiles(boolean show) {
        prefs.edit().putBoolean(KEY_SHOW_HIDDEN, show).apply();
    }

    public String getSortOrder() {
        return prefs.getString(KEY_SORT_ORDER, "DATE_DESC");
    }

    public void setSortOrder(String sortOrder) {
        prefs.edit().putString(KEY_SORT_ORDER, sortOrder).apply();
    }

    public int getThemeMode() {
        return prefs.getInt(KEY_THEME_MODE, 0); // 0 = System default
    }

    public void setThemeMode(int mode) {
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply();
    }

    public String getVaultPin() {
        return prefs.getString(KEY_VAULT_PIN, null);
    }

    public void setVaultPin(String hashedPin) {
        prefs.edit().putString(KEY_VAULT_PIN, hashedPin).apply();
    }

    public boolean isVaultConfigured() {
        return getVaultPin() != null;
    }
}
