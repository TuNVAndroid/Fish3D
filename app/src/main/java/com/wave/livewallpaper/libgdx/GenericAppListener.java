package com.wave.livewallpaper.libgdx;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidWallpaperListener;
import com.mbridge.msdk.newreward.player.view.hybrid.util.MRAIDCommunicatorUtil;
import com.mbridge.msdk.playercommon.exoplayer2.util.MimeTypes;
import com.wave.data.CustomResFileName;
import com.wave.keyboard.theme.utils.FirebaseHelper;
import com.wave.livewallpaper.WallpaperPlaybackManager;
import com.wave.livewallpaper.WallpaperSelectionManager;
import com.wave.livewallpaper.data.LiveWallpaperConfig;
import com.wave.livewallpaper.data.LiveWallpaperConfigReader;
import com.wave.livewallpaper.vfx.VfxParticle;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

public class GenericAppListener implements ApplicationListener, AndroidWallpaperListener {

    public Context context;
    private String wallpaperPrefsKey;
    private String wallpaperDiskPath;
    private volatile BaseAppListener delegate;
    private volatile boolean pendingRestart;
    private final Object lock = new Object();
    private boolean isPreview = true;

    public GenericAppListener(String prefsKey, Context context) {
        this.wallpaperPrefsKey = prefsKey;
        this.context = context;
        initFromPrefsKey(context, prefsKey);
    }

    public GenericAppListener(Context context, String diskPath) throws IOException {
        this.context = context;
        this.wallpaperDiskPath = diskPath;
        initListenerFromPath(context, diskPath);
    }

    private void initListenerFromPath(Context context, String path) {
        FirebaseHelper.logDebug("GenericAppListener", "initListenerFromPath");
        try {
            File file = new File(path, CustomResFileName.RES_NAME_CONFIG);
            LiveWallpaperConfig config = LiveWallpaperConfigReader.read(file);
            String type = config.type;
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            JSONObject json = new JSONObject(new String(data, StandardCharsets.UTF_8));
            if (type == null || type.equals(MimeTypes.BASE_TYPE_VIDEO)) {
                this.delegate = new VideoAppListener(path, config, context);
            } else if (type.equals("3dscene")) {
                this.delegate = new SceneFishesAppListener(path, config, context);
            } else if (type.equals("parallax2d") || type.equals("parallax3d")) {
                this.delegate = new SceneParallaxAppListener(path, config, json, context);
            } else if (type.equals("slideshow")) {
                this.delegate = new SceneSlideshowAppListener(path, config, context);
            } else if (type.equals("camera")) {
                this.delegate = new CameraAppListener(path, config, context);
            }
            if (this.delegate == null) {
                FirebaseHelper.logDebug("GenericAppListener", "initListenerFromPath > listener should not be null. WLP type " + type);
            }
        } catch (Exception e2) {
            Log.e("GenericAppListener", "initListenerFromPath > error reading config", e2);
            FirebaseHelper.logException(e2);
        }
    }

    private void initFromPrefsKey(Context context, String prefsKey) {
        // Use WallpaperSelectionManager to get the correct wallpaper path
        WallpaperSelectionManager selectionManager = new WallpaperSelectionManager(context);
        String path = selectionManager.getSelectedWallpaperPath();
        
        // Fallback to legacy method if no path found
        if (path.equals("")) {
            path = WallpaperPlaybackManager.getDbValue(context, prefsKey);
            if (path.equals("")) {
                path = getOldVersionPath();
                WallpaperPlaybackManager.setDbValue(context, prefsKey, path);
            }
        }
        
        Log.d("GenericAppListener", "Loading wallpaper from path: " + path);
        this.wallpaperDiskPath = path;
        initListenerFromPath(context, path);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void delegateTouchEvent(MotionEvent motionEvent) {
        this.delegate.onTouchEvent(motionEvent);
    }

    private void notifyDelegateCreate() {
        if (this.delegate != null) {
            FirebaseHelper.logDebug("GenericAppListener", "notifyAppCreate");
            this.delegate.create();
        }
    }

    private void notifyDelegateDispose() {
        if (this.delegate != null) {
            FirebaseHelper.logDebug("GenericAppListener", "notifyAppDispose");
            this.delegate.dispose();
            this.delegate = null;
        }
    }

    private void notifyDelegatePause() {
        if (this.delegate != null) {
            FirebaseHelper.logDebug("GenericAppListener", "notifyAppPause");
            this.delegate.pause();
        }
    }

    private void notifyDelegateResize(int width, int height) {
        if (this.delegate == null) {
            FirebaseHelper.logDebug("GenericAppListener", "notifyAppResize > listener == null");
            FirebaseHelper.logException(new NullPointerException("Libgdx listener should not be null!"));
        } else {
            FirebaseHelper.logDebug("GenericAppListener", "notifyAppResize");
            this.delegate.resize(width, height);
        }
    }

    private void notifyDelegateResume() {
        if (this.delegate != null) {
            FirebaseHelper.logDebug("GenericAppListener", "notifyAppResume");
            this.delegate.resume();
        }
    }

    public String getOldVersionPath() {
        String path;
        String wallpaperSet = PreferenceManager.getDefaultSharedPreferences(this.context).getString("walpaper_set", "");
        Log.d("GenericAppListener", "checkOldVersionPath " + wallpaperSet);
        if (wallpaperSet.equals("")) {
            path = "";
        } else {
            if (wallpaperSet.equals("alternate")) {
                path = PreferenceManager.getDefaultSharedPreferences(this.context).getString("movie_disk_path_alternate", "");
                Log.d("GenericAppListener", "alternate");
            } else {
                path = "";
            }
            if (wallpaperSet.equals(MRAIDCommunicatorUtil.STATES_DEFAULT)) {
                path = PreferenceManager.getDefaultSharedPreferences(this.context).getString("movie_disk_path", "");
                Log.d("GenericAppListener", MRAIDCommunicatorUtil.STATES_DEFAULT);
            }
            PreferenceManager.getDefaultSharedPreferences(this.context).edit().putString("walpaper_set", "").apply();
        }
        return path.replace("/movie.mp4", "");
    }

    @Override
    public void create() {
        notifyDelegateCreate();
    }

    @Override
    public void dispose() {
        notifyDelegateDispose();
    }

    public void loadBackgroundVfx(VfxParticle vfxParticle) {
        this.delegate.loadBackgroundVfx(vfxParticle);
    }

    public void loadTouchVfx(VfxParticle vfxParticle) {
        this.delegate.loadTouchVfx(vfxParticle);
    }

    @Override
    public void iconDropped(int x, int y) {
    }

    public void onCommand(String command, int x, int y, int z, Bundle extras, boolean result) {
        BaseAppListener listener = this.delegate;
        if (listener != null) {
            listener.onCommand(command, x, y, z, extras, result);
        }
    }

    public void onTouchEvent(final MotionEvent motionEvent) {
        try {
            if (this.delegate != null && Gdx.app != null) {
                // Create a copy of the motion event to avoid issues with recycling
                final MotionEvent eventCopy = MotionEvent.obtain(motionEvent);
                
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public final void run() {
                        try {
                            if (GenericAppListener.this.delegate != null) {
                                GenericAppListener.this.delegate.onTouchEvent(eventCopy);
                            }
                        } catch (Exception e) {
                            Log.e("GenericAppListener", "Error handling touch event", e);
                        } finally {
                            // Recycle the copied event
                            eventCopy.recycle();
                        }
                    }
                });
            }
        } catch (Exception e) {
            Log.e("GenericAppListener", "Error posting touch event", e);
        }
    }

    public void requestRestart() {
        this.pendingRestart = true;
    }

    @Override
    public void offsetChange(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
        BaseAppListener listener = this.delegate;
        if (listener != null) {
            listener.offsetChange(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
        }
    }

    @Override
    public void pause() {
        notifyDelegatePause();
    }

    @Override
    public void previewStateChange(boolean isPreview) {
        this.isPreview = isPreview;
        BaseAppListener listener = this.delegate;
        if (listener != null) {
            listener.previewStateChange(isPreview);
        }
    }

    @Override
    public void render() {
        BaseAppListener listener = this.delegate;
        if (listener == null) {
            LibgdxUtils.clearScreen();
        } else {
            listener.render();
        }
        if (this.pendingRestart) {
            this.pendingRestart = false;
            pause();
            dispose();
            initListenerFromPath(this.context, this.wallpaperDiskPath);
            create();
            resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            resume();
        }
    }

    @Override
    public void resize(int width, int height) {
        notifyDelegateResize(width, height);
    }

    @Override
    public void resume() {
        notifyDelegateResume();
    }
}
