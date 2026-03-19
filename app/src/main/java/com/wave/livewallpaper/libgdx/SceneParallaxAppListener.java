package com.wave.livewallpaper.libgdx;

import android.content.Context;
import com.wave.livewallpaper.data.LiveWallpaperConfig;
import org.json.JSONObject;

public class SceneParallaxAppListener extends BaseAppListener {
    public SceneParallaxAppListener(String str, LiveWallpaperConfig config, JSONObject json, Context context) {
        super(str, config, context);
    }
}
