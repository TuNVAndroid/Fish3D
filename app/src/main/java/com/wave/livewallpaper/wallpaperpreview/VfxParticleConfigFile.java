package com.wave.livewallpaper.wallpaperpreview;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.wave.livewallpaper.vfx.VfxCooldown;
import com.wave.livewallpaper.vfx.VfxParticle;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class VfxParticleConfigFile {
    private static final String TAG = "VfxParticleConfigFile";
    private static final Gson gson = new Gson();

    private static class JsonConfig {
        @SerializedName("fileName")
        public String fileName;
        @SerializedName("emitInSequence")
        public boolean emitInSequence;
        @SerializedName("randomizeEmitters")
        public boolean randomizeEmitters;
        @SerializedName("cooldown")
        public JsonCooldown cooldown;
        @SerializedName("isPostProcessing")
        public boolean isPostProcessing;
    }

    private static class JsonCooldown {
        @SerializedName("minDistanceBetweenTouches")
        public float minDistanceBetweenTouches;
        @SerializedName("minTimeBetweenTouches")
        public float minTimeBetweenTouches;
    }

    public static VfxParticle readVfxParticleConfig(Context context, String configName, String type) {
        if (configName == null || configName.isEmpty()) return null;
        
        String folder = (type.equals("overlay") ? "overlayvfx" : "touchvfx") + "/" + configName;
        String configPath = folder + "/vfxconfig.json";
        
        try (InputStream is = context.getAssets().open(configPath);
             Reader reader = new InputStreamReader(is)) {
            
            JsonConfig config = gson.fromJson(reader, JsonConfig.class);
            if (config == null) return null;

            VfxCooldown cooldown = VfxCooldown.DEFAULT;
            if (config.cooldown != null) {
                cooldown = VfxCooldown.builder()
                        .duration(config.cooldown.minTimeBetweenTouches)
                        .minDistance(config.cooldown.minDistanceBetweenTouches)
                        .build();
            }

            String particlePath = folder + "/" + (config.fileName != null ? config.fileName : "particle.p");
            
            return VfxParticle.builder()
                    .name(configName)
                    .particlePath(particlePath)
                    .cooldown(cooldown)
                    .randomize(config.randomizeEmitters)
                    .sequential(config.emitInSequence)
                    .build();
            
        } catch (Exception e) {
            Log.e(TAG, "Error reading vfx config from " + configPath, e);
        }
        return null;
    }
}
