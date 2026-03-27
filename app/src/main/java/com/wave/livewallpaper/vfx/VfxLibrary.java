package com.wave.livewallpaper.vfx;

import android.content.Context;
import com.wave.livewallpaper.wallpaperpreview.VfxParticleConfigFile;
import java.util.List;

public class VfxLibrary {

    public static final VfxParticle WATER_VFX = VfxParticle.builder().name("water").isWaterVfx(true).build();
    public static final VfxParticle SNOW_VFX = VfxParticle.builder().name("snow").particlePath("touchvfx/snow/particle.p").build();
    public static final VfxParticle USA_VFX = VfxParticle.builder().name("usa").particlePath("touchvfx/usa/particle.p").build();
    
    public static final VfxParticle LOVE_VFX = VfxParticle.builder().name("love").particlePath("overlayvfx/love/particle.p").build();
    public static final VfxParticle WINDOWRAIN_VFX = VfxParticle.builder().name("windowrain").particlePath("overlayvfx/windowrain/particle.p").build();

    public static VfxParticle getByName(String name) {
        if ("water".equals(name)) return WATER_VFX;
        if ("snow".equals(name)) return SNOW_VFX;
        if ("usa".equals(name)) return USA_VFX;
        if ("love".equals(name)) return LOVE_VFX;
        if ("windowrain".equals(name)) return WINDOWRAIN_VFX;
        return VfxParticle.EMPTY;
    }

    public static VfxParticle getVfx(Context context, String name, String type) {
        if (name == null || name.isEmpty() || "none".equals(name)) return VfxParticle.EMPTY;
        if ("water".equals(name)) return WATER_VFX;
        
        VfxParticle particle = VfxParticleConfigFile.readVfxParticleConfig(context, name, type);
        if (particle == null || particle.isEmpty()) {
            return getByName(name);
        }
        return particle;
    }

    public static int indexOf(List list, String name) {
        return 0;
    }
}
