package com.wave.livewallpaper;

import android.app.ActivityManager;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import com.mbridge.msdk.newreward.player.view.hybrid.util.MRAIDCommunicatorUtil;
import com.wave.keyboard.theme.supercolor.settings.ThemeSettings;
import com.wave.livewallpaper.data.WallpaperDatabaseHelper;

/* loaded from: classes4.dex */
public class WallpaperPlaybackManager {
    public static String getDbValue(Context context, String key) {
        WallpaperDatabaseHelper wallpaperDatabaseHelper = new WallpaperDatabaseHelper(context);
        String value = wallpaperDatabaseHelper.getValue(key);
        wallpaperDatabaseHelper.closeDbConnection();
        return value;
    }

    public static boolean isUnityWallpaperForTheme(Context context, String shortname) {
        return isUnityWallpaperActive(context) && shortname.equals(ThemeSettings.getShortname(context));
    }

    public static boolean isDefaultWallpaperActive(Context context, String path) {
        String wallpaperPath = getDbValue(context, "wallpaper_disk_path");
        boolean isDefault = getDbValue(context, "wallpaper_set").equals(MRAIDCommunicatorUtil.STATES_DEFAULT);
        boolean isRunning = isLibGdxWallpaperRunning(context);
        Log.d("LWPlaybackManager", "isActiveAndSelected " + wallpaperPath + " lastSetWallpaper " + isDefault + " isActive " + isRunning);
        return wallpaperPath.contains(path) && isRunning && isDefault;
    }

    public static boolean isAlternateWallpaperActive(Context context, String path) {
        String wallpaperPath = getDbValue(context, "wallpaper_disk_path_alternate");
        boolean isAlternate = getDbValue(context, "wallpaper_set").equals("alternate");
        boolean isRunning = isLibGdxAlternateRunning(context);
        Log.d("LWPlaybackManager", "isActiveAndSelected " + wallpaperPath + " lastSetWallpaper " + isAlternate + " isActive " + isRunning);
        return wallpaperPath.contains(path) && isRunning && isAlternate;
    }

    public static boolean isUnityWallpaperActive(Context context) {
        if (Build.VERSION.SDK_INT >= 33) {
            return isServiceRunning(context, "com.wave.livewallpaper.unity.UnityWallpaperService");
        }
        WallpaperInfo wallpaperInfo = WallpaperManager.getInstance(context).getWallpaperInfo();
        if (wallpaperInfo == null) {
            return false;
        }
        return wallpaperInfo.getComponent().getClassName().contains("UnityWallpaperService");
    }

    public static boolean isLibGdxWallpaperRunning(Context context) {
        if (Build.VERSION.SDK_INT >= 33) {
            if (isServiceRunning(context, "com.wave.livewallpaper.libgdx.LibGdxLiveWallpaper")) {
                Log.d("LWPlaybackManager", "LiveWallpaper service running");
                return true;
            }
            Log.d("LWPlaybackManager", "LiveWallpaper service not running");
            return false;
        }
        WallpaperInfo wallpaperInfo = WallpaperManager.getInstance(context).getWallpaperInfo();
        if (wallpaperInfo == null || wallpaperInfo.getComponent().getClassName().contains("Alternate")) {
            Log.d("LWPlaybackManager", "We're not running, this should be a preview");
            return false;
        }
        Log.d("LWPlaybackManager", "We're already running");
        return true;
    }

    public static boolean isLibGdxAlternateRunning(Context context) {
        if (Build.VERSION.SDK_INT >= 33) {
            if (isServiceRunning(context, "com.wave.livewallpaper.libgdx.LibGdxLiveWallpaperAlternate")) {
                Log.d("LWPlaybackManager", "LibGdxLiveWallpaperAlt service running");
                return true;
            }
            Log.d("LWPlaybackManager", "LibGdxLiveWallpaperAlt service not running");
            return false;
        }
        WallpaperInfo wallpaperInfo = WallpaperManager.getInstance(context).getWallpaperInfo();
        if (wallpaperInfo == null || !wallpaperInfo.getComponent().getClassName().contains("LibGdxLiveWallpaperAlternate")) {
            Log.d("LWPlaybackManager", "We're not running, this should be a preview");
            return false;
        }
        Log.d("LWPlaybackManager", "We're already running");
        return true;
    }

    public static boolean isServiceRunning(Context context, String className) {
        for (ActivityManager.RunningServiceInfo runningServiceInfo : ((ActivityManager) context.getSystemService("activity")).getRunningServices(Integer.MAX_VALUE)) {
            Log.d("LWPlaybackManager", String.format("Service:%s", runningServiceInfo.service.getClassName()));
            if (runningServiceInfo.service.getClassName().equals(className)) {
                return true;
            }
        }
        return false;
    }

    public static void setDbValue(Context context, String key, String value) {
        WallpaperDatabaseHelper wallpaperDatabaseHelper = new WallpaperDatabaseHelper(context);
        wallpaperDatabaseHelper.insertValue(key, value);
        wallpaperDatabaseHelper.closeDbConnection();
    }
}
