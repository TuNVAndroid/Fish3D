package com.wave.livewallpaper.data;

import android.util.Log;
import com.google.gson.Gson;
import com.wave.livewallpaper.util.Utility;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

/* loaded from: classes4.dex */
public class LiveWallpaperConfigReader {
    private static final String TAG = "WallpaperConfigReader";

    public static LiveWallpaperConfig read(File file) {
        try {
            return (LiveWallpaperConfig) new Gson().fromJson((Reader) new BufferedReader(new FileReader(file)), LiveWallpaperConfig.class);
        } catch (FileNotFoundException e2) {
            Log.e(TAG, "read", e2);
            return LiveWallpaperConfig.EMPTY;
        }
    }

    public static SceneConfig readSceneConfig(File file) {
        try {
            return (SceneConfig) new Gson().fromJson((Reader) new BufferedReader(new FileReader(file)), SceneConfig.class);
        } catch (FileNotFoundException e2) {
            Log.e(TAG, "readSceneConfig", e2);
            return SceneConfig.EMPTY;
        }
    }

    public static void save(File file, LiveWallpaperConfig liveWallpaperConfig) {
        Utility.writeStringToFile(file.getAbsolutePath(), new Gson().toJson(liveWallpaperConfig));
    }
}
