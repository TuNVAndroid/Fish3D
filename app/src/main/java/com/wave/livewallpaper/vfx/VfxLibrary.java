package com.wave.livewallpaper.vfx;

import java.util.List;

public class VfxLibrary {

    public static final VfxParticle STUB_A = VfxParticle.EMPTY;
    public static final VfxParticle STUB_B = VfxParticle.EMPTY;
    public static final VfxParticle STUB_C = VfxParticle.EMPTY;
    public static final VfxParticle STUB_D = VfxParticle.EMPTY;
    public static final VfxParticle STUB_E = VfxParticle.EMPTY;
    public static final VfxParticle STUB_F = VfxParticle.EMPTY;
    public static final VfxParticle STUB_G = VfxParticle.EMPTY;
    public static final VfxParticle STUB_H = VfxParticle.EMPTY;
    public static final VfxParticle STUB_I = VfxParticle.EMPTY;
    public static final VfxParticle STUB_J = VfxParticle.EMPTY;
    public static final VfxParticle STUB_K = VfxParticle.EMPTY;
    public static final VfxParticle STUB_L = VfxParticle.EMPTY;
    public static final VfxParticle STUB_M = VfxParticle.EMPTY;
    public static final VfxParticle STUB_N = VfxParticle.EMPTY;
    public static final VfxParticle STUB_O = VfxParticle.EMPTY;
    public static final VfxParticle STUB_P = VfxParticle.EMPTY;
    public static final VfxParticle STUB_Q = VfxParticle.EMPTY;
    public static final VfxParticle STUB_R = VfxParticle.EMPTY;
    public static final VfxParticle STUB_S = VfxParticle.EMPTY;
    public static final VfxParticle STUB_T = VfxParticle.EMPTY;
    public static final VfxParticle STUB_U = VfxParticle.EMPTY;
    public static final VfxParticle WATER_VFX = VfxParticle.builder().name("water").isWaterVfx(true).build();
    public static final VfxParticle SNOW_VFX = VfxParticle.builder().name("snow").particlePath("touchvfx/snow/particle.p").build();
    public static final VfxParticle USA_VFX = VfxParticle.builder().name("usa").particlePath("touchvfx/usa/particle.p").build();
    
    public static final VfxParticle LOVE_VFX = VfxParticle.builder().name("love").particlePath("overlayvfx/love/particle.p").build();
    public static final VfxParticle WINDOWRAIN_VFX = VfxParticle.builder().name("windowrain").particlePath("overlayvfx/windowrain/particle.p").build();

    public static final VfxParticle[] OVERLAY_PRESETS = new VfxParticle[]{VfxParticle.EMPTY, LOVE_VFX, WINDOWRAIN_VFX};
    public static final VfxParticle[] TOUCH_PRESETS = new VfxParticle[]{WATER_VFX, SNOW_VFX, USA_VFX};

    public static VfxParticle getByName(String name) {
        if ("water".equals(name)) return WATER_VFX;
        if ("snow".equals(name)) return SNOW_VFX;
        if ("usa".equals(name)) return USA_VFX;
        
        if ("love".equals(name)) return LOVE_VFX;
        if ("windowrain".equals(name)) return WINDOWRAIN_VFX;
        
        return VfxParticle.EMPTY;
    }

    public static int indexOf(List list, String name) {
        return 0;
    }
}
