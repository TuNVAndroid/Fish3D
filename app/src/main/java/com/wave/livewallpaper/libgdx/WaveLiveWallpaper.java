package com.wave.livewallpaper.libgdx;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import androidx.core.content.ContextCompat;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidLiveWallpaperService;
import com.wave.keyboard.theme.utils.FirebaseHelper;
import com.wave.keyboard.theme.utils.Utils;

public abstract class WaveLiveWallpaper extends AndroidLiveWallpaperService implements AndroidApplicationLimited {

    public static AssetManager androidAssetManager;
    private GenericAppListener appListener;
    
    // Touch event throttling
    private static final long TOUCH_THROTTLE_MS = 16; // ~60 FPS
    private long lastTouchTime = 0;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("WaveLiveWallpaper", "onReceive " + Utils.intentToString(intent));
            if (intent.hasExtra("stop")) {
                Log.d("WaveLiveWallpaper", "onReceive stop");
                WaveLiveWallpaper.this.stopSelf();
            }
            if (intent.hasExtra("restart")) {
                Log.d("WaveLiveWallpaper", "onReceive restart");
                if (WaveLiveWallpaper.this.appListener != null) {
                    WaveLiveWallpaper.this.appListener.requestRestart();
                }
            }
        }
    };

    public class LiveWallPaperEngine extends AndroidLiveWallpaperService.AndroidWallpaperEngine {
        public LiveWallPaperEngine() {
            super();
        }

        @Override
        public Bundle onCommand(String command, int x, int y, int z, Bundle extras, boolean result) {
            Bundle superResult = super.onCommand(command, x, y, z, extras, result);
            if (WaveLiveWallpaper.this.appListener != null) {
                WaveLiveWallpaper.this.appListener.onCommand(command, x, y, z, extras, result);
            }
            return superResult;
        }

        @Override
        public void onTouchEvent(MotionEvent motionEvent) {
            try {
                if (!this.engineIsVisible) {
                    Log.d("WaveLiveWallpaper", "onTouchEvent > skipping. engineIsVisible = false");
                    FirebaseHelper.logDebug("WaveLiveWallpaper", "onTouchEvent > skipping. engineIsVisible = false");
                    return;
                }
                
                if (WaveLiveWallpaper.this.appListener != null) {
                    // Throttle touch events to prevent overwhelming the system
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastTouchTime < TOUCH_THROTTLE_MS) {
                        return;
                    }
                    lastTouchTime = currentTime;
                    
                    WaveLiveWallpaper.this.appListener.onTouchEvent(motionEvent);
                }
                super.onTouchEvent(motionEvent);
            } catch (Exception e) {
                Log.e("WaveLiveWallpaper", "Error handling touch event", e);
                FirebaseHelper.logException(e);
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            try {
                if (!WaveLiveWallpaper.this.isWallpaperValid()) {
                    WaveLiveWallpaper.this.onWallpaperInvalid();
                    return;
                }
                boolean wasVisible = isVisible();
                super.onVisibilityChanged(visible);
                
                if (!wasVisible && visible) {
                    Log.d("WaveWallpaperService", " > fake visibilityChanged event! Android WaveWallpaperService likes do that!");
                } else {
                    if (WaveLiveWallpaper.this.appListener != null && !visible) {
                        // Pause when not visible to save resources
                        WaveLiveWallpaper.this.appListener.pause();
                    }
                }
            } catch (Exception e) {
                Log.e("WaveLiveWallpaper", "Error handling visibility change", e);
                FirebaseHelper.logException(e);
            }
        }
    }

    public boolean isWallpaperValid() {
        return true;
    }

    public void onWallpaperInvalid() {
    }

    protected void initFromDefaultPrefs() {
        initFromPrefsKey("wallpaper_disk_path");
    }

    protected void initFromPrefsKey(String prefsKey) {
        Log.d("WaveLiveWallpaper", "onInitListenerFromPreferences ");
        this.appListener = new GenericAppListener(prefsKey, getApplicationContext());
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useAccelerometer = false;
        config.useGyroscope = false;
        config.useCompass = false;
        initialize(this.appListener, config);
    }

    @Override
    public AssetManager getAssets() {
        return getApplicationContext().getAssets();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ContextCompat.registerReceiver(this, this.broadcastReceiver, new IntentFilter("LIBGDX_BROADCAST_ACTION"), 2);
    }

    @Override
    public void onCreateApplication() {
        super.onCreateApplication();
        try {
            initFromDefaultPrefs();
            androidAssetManager = getAssets();
        } catch (Exception e) {
            Log.e("WaveLiveWallpaper", "Error during application creation", e);
            FirebaseHelper.logException(e);
            // Try to recover by using a fallback configuration
            try {
                recoverFromError();
            } catch (Exception recoveryError) {
                Log.e("WaveLiveWallpaper", "Recovery failed", recoveryError);
            }
        }
    }
    
    private void recoverFromError() {
        Log.d("WaveLiveWallpaper", "Attempting to recover from error");
        // Clear any corrupted state
        if (this.appListener != null) {
            try {
                this.appListener.dispose();
            } catch (Exception e) {
                Log.e("WaveLiveWallpaper", "Error disposing listener during recovery", e);
            }
            this.appListener = null;
        }
        
        // Try to reinitialize with default settings
        try {
            initFromDefaultPrefs();
        } catch (Exception e) {
            Log.e("WaveLiveWallpaper", "Failed to reinitialize during recovery", e);
        }
    }

    @Override
    public WallpaperService.Engine onCreateEngine() {
        return new LiveWallPaperEngine();
    }

    @Override
    public void onDestroy() {
        Log.d("WaveLiveWallpaper", "onDestroy ");
        super.onDestroy();
        unregisterReceiver(this.broadcastReceiver);
        GenericAppListener listener = this.appListener;
        if (listener != null) {
            listener.dispose();
        }
    }
}
