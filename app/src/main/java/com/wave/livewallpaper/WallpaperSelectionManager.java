package com.wave.livewallpaper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.io.File;

/**
 * Manages wallpaper selection and paths for the live wallpaper service
 */
public class WallpaperSelectionManager {
    
    private static final String TAG = "WallpaperSelectionManager";
    private static final String PREFS_NAME = "wallpaper_selection";
    private static final String KEY_SELECTED_WALLPAPER_ID = "selected_wallpaper_id";
    private static final String KEY_WALLPAPER_PATH_PREFIX = "wallpaper_path_";
    
    // Wallpaper IDs
    public static final String WALLPAPER_CLOWNFISH = "clownfish";
    public static final String WALLPAPER_GOLDFISH = "goldfish";
    public static final String WALLPAPER_TEST_FISH = "test_fish";
    public static final String WALLPAPER_RED_DEVIL = "reddevil";
    
    // Asset paths
    private static final String ASSETS_CLOWNFISH = "com.wave.livewallpaper.clownfishes";
    private static final String ASSETS_GOLDFISH = "com.wave.livewallpaper.livefisheslivewallpaper";
    private static final String ASSETS_TEST_FISH = "test_fish";
    private static final String ASSETS_RED_DEVIL = "reddevil";
    
    private final Context context;
    private final SharedPreferences prefs;
    
    public WallpaperSelectionManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Set the currently selected wallpaper
     */
    public void setSelectedWallpaper(String wallpaperId, String wallpaperPath) {
        Log.d(TAG, "Setting selected wallpaper: " + wallpaperId + " -> " + wallpaperPath);
        
        prefs.edit()
            .putString(KEY_SELECTED_WALLPAPER_ID, wallpaperId)
            .putString(KEY_WALLPAPER_PATH_PREFIX + wallpaperId, wallpaperPath)
            .apply();
            
        // Set the appropriate database key based on wallpaper type
        if (WALLPAPER_GOLDFISH.equals(wallpaperId)) {
            WallpaperPlaybackManager.setDbValue(context, "wallpaper_disk_path_alternate", wallpaperPath);
        } else {
            WallpaperPlaybackManager.setDbValue(context, "wallpaper_disk_path", wallpaperPath);
        }
        WallpaperPlaybackManager.setDbValue(context, "selected_wallpaper_type", wallpaperId);
    }
    
    /**
     * Get the currently selected wallpaper ID
     */
    public String getSelectedWallpaperId() {
        String wallpaperId = prefs.getString(KEY_SELECTED_WALLPAPER_ID, WALLPAPER_CLOWNFISH);
        Log.d(TAG, "getSelectedWallpaperId: " + wallpaperId);
        return wallpaperId;
    }
    
    /**
     * Get the path for the currently selected wallpaper
     */
    public String getSelectedWallpaperPath() {
        String wallpaperId = getSelectedWallpaperId();
        String path = prefs.getString(KEY_WALLPAPER_PATH_PREFIX + wallpaperId, "");
        
        if (path.isEmpty()) {
            // Fallback: try to find the wallpaper in files directory
            path = findWallpaperPath(wallpaperId);
            if (!path.isEmpty()) {
                // Cache the found path
                prefs.edit().putString(KEY_WALLPAPER_PATH_PREFIX + wallpaperId, path).apply();
            }
        }
        
        Log.d(TAG, "Selected wallpaper path: " + wallpaperId + " -> " + path);
        return path;
    }
    
    /**
     * Get path for a specific wallpaper ID
     */
    public String getWallpaperPath(String wallpaperId) {
        String path = prefs.getString(KEY_WALLPAPER_PATH_PREFIX + wallpaperId, "");
        
        if (path.isEmpty()) {
            path = findWallpaperPath(wallpaperId);
        }
        
        return path;
    }
    
    /**
     * Find wallpaper path in files directory
     */
    private String findWallpaperPath(String wallpaperId) {
        String assetsPath = getAssetsPathForWallpaper(wallpaperId);
        if (assetsPath == null) {
            return "";
        }
        
        File wallpaperDir = new File(context.getFilesDir(), assetsPath);
        if (wallpaperDir.exists() && wallpaperDir.isDirectory()) {
            return wallpaperDir.getAbsolutePath();
        }
        
        return "";
    }
    
    /**
     * Get assets path for wallpaper ID
     */
    private String getAssetsPathForWallpaper(String wallpaperId) {
        switch (wallpaperId) {
            case WALLPAPER_CLOWNFISH:
                return ASSETS_CLOWNFISH;
            case WALLPAPER_GOLDFISH:
                return ASSETS_GOLDFISH;
            case WALLPAPER_TEST_FISH:
                return ASSETS_TEST_FISH;
            case WALLPAPER_RED_DEVIL:
                return ASSETS_RED_DEVIL;
            default:
                return null;
        }
    }
    
    /**
     * Get wallpaper ID from assets path
     */
    public static String getWallpaperIdFromPath(String path) {
        if (path.contains(ASSETS_CLOWNFISH) || path.contains("clownfishes")) {
            return WALLPAPER_CLOWNFISH;
        } else if (path.contains(ASSETS_GOLDFISH) || path.contains("livefisheslivewallpaper")) {
            return WALLPAPER_GOLDFISH;
        } else if (path.contains(ASSETS_TEST_FISH) || path.contains("test_fish")) {
            return WALLPAPER_TEST_FISH;
        } else if (path.contains(ASSETS_RED_DEVIL) || path.contains("reddevil")) {
            return WALLPAPER_RED_DEVIL;
        }
        // Log warning for unknown path
        Log.w("WallpaperSelectionManager", "Unknown wallpaper path: " + path + ", defaulting to clownfish");
        return WALLPAPER_CLOWNFISH; // default
    }
    
    /**
     * Check if wallpaper assets exist
     */
    public boolean isWallpaperAvailable(String wallpaperId) {
        String path = getWallpaperPath(wallpaperId);
        if (path.isEmpty()) {
            return false;
        }
        
        File configFile = new File(path, "config.json");
        return configFile.exists();
    }
    
    /**
     * Get all available wallpaper IDs
     */
    public String[] getAvailableWallpapers() {
        return new String[] { WALLPAPER_CLOWNFISH, WALLPAPER_GOLDFISH, WALLPAPER_TEST_FISH, WALLPAPER_RED_DEVIL };
    }
}