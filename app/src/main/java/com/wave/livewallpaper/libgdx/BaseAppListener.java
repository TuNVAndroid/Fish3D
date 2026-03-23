package com.wave.livewallpaper.libgdx;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.AssetManager;
import net.mgsx.gltf.loaders.glb.GLBAssetLoader;
import net.mgsx.gltf.loaders.gltf.GLTFAssetLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.backends.android.AndroidWallpaperListener;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.wave.keyboard.theme.utils.FirebaseHelper;
import com.wave.livewallpaper.data.LiveWallpaperConfig;
import com.wave.livewallpaper.libgdx.reward.RewardPopup;
import com.wave.livewallpaper.vfx.VfxLibrary;
import com.wave.livewallpaper.vfx.VfxParticle;

public class BaseAppListener implements ApplicationListener, AndroidWallpaperListener {

    public String wallpaperDiskPath;
    public Context context;
    private LiveWallpaperConfig wallpaperConfig;
    protected KeyguardManager keyguardManager;
    protected AssetManager assetManager;
    protected Viewport viewport;
    protected FrameBuffer frameBuffer;
    protected LockscreenRenderer lockscreenRenderer;
    protected float deltaTime;
    private RewardPopup rewardPopup;
    private VfxParticleRenderer vfxParticleRenderer;
    private VfxWater vfxWater;
    private BitmapFont fpsFont;
    private SpriteBatch spriteBatch;
    private OrthographicCamera debugCamera;
    private PreviewState previewState = PreviewState.UNKNOWN;
    private boolean showFps = false;
    protected final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean frameBufferActive = false;

    protected final FileHandleResolver fileHandleResolver = new FileHandleResolver() {
        @Override
        public FileHandle resolve(String str) {
            FileHandle absoluteHandle = Gdx.files.absolute(str);
            if (absoluteHandle.exists()) {
                return absoluteHandle;
            }
            FileHandle wallpaperHandle = Gdx.files.absolute(BaseAppListener.this.wallpaperDiskPath + "/" + str);
            return wallpaperHandle.exists() ? wallpaperHandle : Gdx.files.internal(str);
        }
    };

    enum PreviewState {
        UNKNOWN,
        PREVIEW,
        APPLIED;

        boolean isApplied() {
            return APPLIED.equals(this);
        }
    }

    public interface ScreenshotListener {
    }

    public BaseAppListener(String wallpaperDiskPath, LiveWallpaperConfig config, Context context) {
        this.wallpaperDiskPath = wallpaperDiskPath;
        this.wallpaperConfig = config;
        this.context = context;
    }

    private void initRewardPopup() {
    }

    private RewardPopup getOrCreateRewardPopup() {
        if (this.rewardPopup == null) {
            initRewardPopup();
        }
        return this.rewardPopup;
    }

    private void initAssetManager() {
        AssetManager am = new AssetManager(this.fileHandleResolver);
        this.assetManager = am;
        am.setErrorListener(new AssetErrorListener() {
            @Override
            public final void error(AssetDescriptor assetDescriptor, Throwable th) {
                BaseAppListener.onAssetError(assetDescriptor, th);
            }
        });
        // Register GLTF/GLB loaders so AssetManager can load SceneAsset
        am.setLoader(SceneAsset.class, ".gltf", new GLTFAssetLoader(this.fileHandleResolver));
        am.setLoader(SceneAsset.class, ".glb", new GLBAssetLoader(this.fileHandleResolver));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void onAssetError(AssetDescriptor assetDescriptor, Throwable th) {
        Log.d("SceneAppListener", "assets error: " + assetDescriptor.toString());
    }

    private void sendShowEventOnResume() {
        try {
            Context ctx = this.context;
            if (ctx == null) {
                return;
            }
            FirebaseAnalytics.getInstance(ctx).logEvent("Live_Wallpaper_Show_Ok", new Bundle());
        } catch (Exception e2) {
            Log.e("SceneAppListener", "sendShowEventOnResume", e2);
        }
    }

    private void sendShowEventPreviewPlusApplied() {
        try {
            Context ctx = this.context;
            if (ctx == null) {
                return;
            }
            FirebaseAnalytics.getInstance(ctx).logEvent("Live_Wallpaper_Show", new Bundle());
        } catch (Exception e2) {
            Log.e("SceneAppListener", "sendShowEventPreviewPlusApplied", e2);
        }
    }

    private boolean isWaterEnabled() {
        VfxWater water = this.vfxWater;
        if (water == null) {
            return false;
        }
        return water.isEnabled();
    }

    @Override
    public void create() {
        initAssetManager();
        this.viewport = new ScreenViewport();
        this.spriteBatch = new SpriteBatch();
        this.keyguardManager = (KeyguardManager) this.context.getSystemService("keyguard");
        if (this.rewardPopup == null) {
            initRewardPopup();
        }
        if (isVfxEnabled() && this.vfxParticleRenderer == null) {
            VfxParticleRenderer renderer = new VfxParticleRenderer(this.context, this.assetManager, this.wallpaperDiskPath);
            this.vfxParticleRenderer = renderer;
            try {
                renderer.create();
            } catch (Exception e2) {
                Log.e("SceneAppListener", "create", e2);
            }
        }
        if (this.showFps) {
            BitmapFont font = new BitmapFont();
            this.fpsFont = font;
            font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            this.fpsFont.getData().setScale(2.5f);
            OrthographicCamera cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            this.debugCamera = cam;
            cam.position.set(cam.viewportWidth / 2.0f, cam.viewportHeight / 2.0f, 0.0f);
            this.debugCamera.update();
        }
        VfxWater water = new VfxWater(this.wallpaperDiskPath);
        this.vfxWater = water;
        water.create();
        sendShowEventPreviewPlusApplied();
        if (this.debugCamera == null) {
            OrthographicCamera cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            this.debugCamera = cam;
            cam.position.set(cam.viewportWidth / 2.0f, cam.viewportHeight / 2.0f, 0.0f);
        }
        LockscreenRenderer renderer = new LockscreenRenderer(this.context, this.wallpaperDiskPath, this.wallpaperConfig, this.assetManager, this.debugCamera, this.spriteBatch, this.keyguardManager, this.mainHandler);
        this.lockscreenRenderer = renderer;
        renderer.create();
    }

    protected void disableFps() {
        this.showFps = false;
    }

    @Override
    public void dispose() {
        RewardPopup popup = this.rewardPopup;
        if (popup != null) {
            popup.dispose();
        }
        VfxParticleRenderer particleRenderer = this.vfxParticleRenderer;
        if (particleRenderer != null) {
            particleRenderer.dispose();
        }
        BitmapFont font = this.fpsFont;
        if (font != null) {
            font.dispose();
        }
        SpriteBatch batch = this.spriteBatch;
        if (batch != null) {
            batch.dispose();
        }
        VfxWater water = this.vfxWater;
        if (water != null) {
            water.dispose();
        }
        FrameBuffer fb = this.frameBuffer;
        if (fb != null) {
            fb.dispose();
        }
        AssetManager am = this.assetManager;
        if (am != null) {
            am.dispose();
        }
        LockscreenRenderer lockscreen = this.lockscreenRenderer;
        if (lockscreen != null) {
            lockscreen.dispose();
        }
    }

    public void loadBackgroundVfx(VfxParticle vfxParticle) {
        VfxParticleRenderer renderer = this.vfxParticleRenderer;
        if (renderer == null) {
            return;
        }
        renderer.loadBackgroundParticle(vfxParticle);
    }

    public void loadTouchVfx(VfxParticle vfxParticle) {
        if (vfxParticle.isWaterVfx && VfxLibrary.WATER_VFX.name.equals(vfxParticle.name)) {
            this.vfxWater.initWater();
        }
        this.vfxWater.setEnabled(vfxParticle.isWaterVfx);
        VfxParticleRenderer renderer = this.vfxParticleRenderer;
        if (renderer != null) {
            renderer.loadTouchParticle(vfxParticle);
        }
    }

    public void onCommand(String command, int x, int y, int z, Bundle extras, boolean result) {
        command.equals("android.wallpaper.tap");
    }

    @Override
    public void iconDropped(int x, int y) {
    }

    public void onTouchEvent(MotionEvent motionEvent) {
        try {
            this.vfxWater.addRipple(motionEvent.getX(), motionEvent.getY());
        } catch (Exception e2) {
            Log.e("SceneAppListener", "onTouchEvent - water", e2);
        }
        try {
            RewardPopup popup = getOrCreateRewardPopup();
            if (popup != null) {
                popup.v(motionEvent);
            }
        } catch (Exception e3) {
            Log.e("SceneAppListener", "onTouchEvent", e3);
            FirebaseHelper.logException(e3);
        }
        try {
            VfxParticleRenderer renderer = this.vfxParticleRenderer;
            if (renderer != null) {
                renderer.onTouchEvent(motionEvent);
            }
        } catch (Exception e4) {
            Log.e("SceneAppListener", "onTouchEvent - vfx", e4);
        }
    }

    protected void beginFrameBuffer() {
        if (isWaterEnabled()) {
            if (this.frameBuffer == null) {
                FrameBuffer fb = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
                this.frameBuffer = fb;
                this.vfxWater.setFrameBuffer(fb);
            }
            this.frameBuffer.begin();
            this.frameBufferActive = true;
        }
    }

    protected void endFrameBuffer() {
        if (this.frameBufferActive) {
            this.frameBuffer.end();
            this.frameBufferActive = false;
        }
    }

    protected void disableFpsOverlay() {
        this.showFps = false;
    }

    @Override
    public void offsetChange(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
    }

    public boolean isVfxEnabled() {
        return true;
    }

    @Override
    public void pause() {
        RewardPopup popup = this.rewardPopup;
        if (popup != null) {
            popup.pause();
        }
        VfxParticleRenderer renderer = this.vfxParticleRenderer;
        if (renderer != null) {
            renderer.pause();
        }
        LockscreenRenderer lockscreen = this.lockscreenRenderer;
        if (lockscreen != null) {
            lockscreen.pause();
        }
    }

    @Override
    public void previewStateChange(boolean isPreview) {
        RewardPopup popup = this.rewardPopup;
        if (popup != null) {
            popup.previewStateChange(isPreview);
        }
        if (isPreview) {
            this.previewState = PreviewState.PREVIEW;
        } else {
            this.previewState = PreviewState.APPLIED;
        }
    }

    @Override
    public void render() {
        try {
            this.deltaTime = Gdx.graphics.getDeltaTime();
            this.lockscreenRenderer.render();
            VfxWater water = this.vfxWater;
            if (water != null) {
                water.render();
            }
            RewardPopup popup = this.rewardPopup;
            if (popup != null) {
                popup.render();
            }
            VfxParticleRenderer renderer = this.vfxParticleRenderer;
            if (renderer != null) {
                renderer.render();
                Viewport vp = this.viewport;
                if (vp != null) {
                    vp.apply();
                }
            }
            if (this.showFps) {
                try {
                    this.debugCamera.update();
                    if (this.spriteBatch.isDrawing()) {
                        this.spriteBatch.end();
                    }
                    this.spriteBatch.setProjectionMatrix(this.debugCamera.combined);
                    this.spriteBatch.begin();
                    BitmapFont font = this.fpsFont;
                    if (font != null) {
                        font.draw(this.spriteBatch, "FPS: " + String.valueOf(Gdx.graphics.getFramesPerSecond()) + " | " + String.valueOf((int) (Gdx.graphics.getRawDeltaTime() * 1000.0f)) + " ms", 20.0f, Gdx.graphics.getHeight() - 100.0f);
                    }
                    this.spriteBatch.end();
                } catch (Exception e2) {
                    Log.e("SceneAppListener", "fpsFont.draw()", e2);
                    if (this.spriteBatch.isDrawing()) {
                        this.spriteBatch.end();
                    }
                }
            }
        } catch (Throwable th) {
            throw th;
        }
    }

    @Override
    public void resize(int width, int height) {
        Viewport vp = this.viewport;
        if (vp != null) {
            vp.update(width, height, true);
        }
        RewardPopup popup = this.rewardPopup;
        if (popup != null) {
            popup.resize(width, height);
        }
        VfxParticleRenderer renderer = this.vfxParticleRenderer;
        if (renderer != null) {
            renderer.resize(width, height);
        }
        VfxWater water = this.vfxWater;
        if (water != null) {
            water.resize(width, height);
        }
        LockscreenRenderer lockscreen = this.lockscreenRenderer;
        if (lockscreen != null) {
            lockscreen.resize(width, height);
        }
        if (this.showFps) {
            OrthographicCamera cam = this.debugCamera;
            float w = width;
            cam.viewportWidth = w;
            float h = height;
            cam.viewportHeight = h;
            cam.position.set(w / 2.0f, h / 2.0f, 0.0f);
            this.debugCamera.update();
        }
    }

    @Override
    public void resume() {
        if (Gdx.gl != null) {
            RewardPopup popup = this.rewardPopup;
            if (popup != null) {
                popup.resume();
            }
            VfxParticleRenderer renderer = this.vfxParticleRenderer;
            if (renderer != null) {
                renderer.resume();
            }
        }
        LockscreenRenderer lockscreen = this.lockscreenRenderer;
        if (lockscreen != null) {
            lockscreen.resume();
        }
        if (this.previewState.isApplied()) {
            sendShowEventOnResume();
        }
    }
}
