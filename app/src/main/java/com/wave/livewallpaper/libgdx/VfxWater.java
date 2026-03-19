package com.wave.livewallpaper.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.wave.livewallpaper.vfx.VfxConfigFile;
import com.wave.livewallpaper.vfx.VfxLibrary;

public class VfxWater implements LibgdxLifecycle {

    private Water water;
    private FrameBuffer frameBuffer;
    private String wallpaperDiskPath;
    private boolean hasPendingResize;
    private int pendingWidth;
    private int pendingHeight;
    private boolean enabled;

    public VfxWater(String wallpaperDiskPath) {
        this.wallpaperDiskPath = wallpaperDiskPath;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void doInitWater() {
        if (this.water == null) {
            Water newWater = new Water();
            this.water = newWater;
            if (this.hasPendingResize) {
                newWater.resize(this.pendingWidth, this.pendingHeight);
            }
        }
        this.enabled = true;
    }

    private void loadConfig() {
        VfxConfigFile configFile = VfxConfigFile.forPath(this.wallpaperDiskPath);
        if (configFile.hasTouch() && VfxLibrary.WATER_VFX.name.equals(configFile.touchVfxName)) {
            initWater();
        }
    }

    public void initWater() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public final void run() {
                VfxWater.this.doInitWater();
            }
        });
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void create() {
        loadConfig();
    }

    @Override
    public void dispose() {
        Water w = this.water;
        if (w != null) {
            w.dispose();
        }
    }

    public void addRipple(float x, float y) {
        Water w = this.water;
        if (w != null) {
            w.addRippleScaled(x, y);
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setFrameBuffer(FrameBuffer frameBuffer) {
        this.frameBuffer = frameBuffer;
    }

    @Override
    public void pause() {
    }

    @Override
    public void render() {
        Water w = this.water;
        if (w == null || this.frameBuffer == null || !this.enabled) {
            return;
        }
        w.updateSimulation(Gdx.graphics.getDeltaTime());
        this.water.renderWater(this.frameBuffer);
    }

    @Override
    public void resize(int width, int height) {
        this.pendingWidth = width;
        this.pendingHeight = height;
        Water w = this.water;
        if (w == null) {
            this.hasPendingResize = true;
        } else {
            w.resize(width, height);
        }
    }

    @Override
    public void resume() {
    }
}
