package com.wave.livewallpaper.vfx;

public class VfxConfigFile {

    private static final VfxConfigFile EMPTY = new VfxConfigFile();

    public String overlayVfxName = "";

    public String touchVfxName = "";

    public static VfxConfigFile forPath(String path) {
        return EMPTY;
    }

    public boolean hasOverlay() {
        return false;
    }

    public boolean hasTouch() {
        return false;
    }

    public void setPath(String path) {
    }
}
