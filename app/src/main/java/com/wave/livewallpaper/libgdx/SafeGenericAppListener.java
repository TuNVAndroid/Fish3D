package com.wave.livewallpaper.libgdx;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import com.wave.livewallpaper.libgdx.GenericAppListener;
import java.io.IOException;

/**
 * Wrapper around GenericAppListener that handles disposal safely to avoid ANR
 */
public class SafeGenericAppListener extends GenericAppListener {
    
    private static final String TAG = "SafeGenericAppListener";
    private static final int DISPOSE_TIMEOUT_MS = 2000;
    
    private volatile boolean isDisposed = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    public SafeGenericAppListener(String prefsKey, Context context) {
        super(prefsKey, context);
    }
    
    public SafeGenericAppListener(Context context, String diskPath) throws IOException {
        super(context, diskPath);
    }
    
    @Override
    public void dispose() {
        if (isDisposed) {
            Log.d(TAG, "Already disposed, skipping");
            return;
        }
        
        isDisposed = true;
        Log.d(TAG, "Starting safe disposal");
        
        // Try to dispose with timeout
        Thread disposeThread = new Thread(() -> {
            try {
                Log.d(TAG, "Disposing on background thread");
                super.dispose();
                Log.d(TAG, "Disposal completed successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error during disposal", e);
            }
        });
        
        disposeThread.start();
        
        // Set timeout for disposal
        mainHandler.postDelayed(() -> {
            if (disposeThread.isAlive()) {
                Log.w(TAG, "Disposal timeout, interrupting thread");
                disposeThread.interrupt();
            }
        }, DISPOSE_TIMEOUT_MS);
    }
    
    @Override
    public void pause() {
        if (isDisposed) {
            Log.d(TAG, "Already disposed, skipping pause");
            return;
        }
        
        try {
            super.pause();
        } catch (Exception e) {
            Log.e(TAG, "Error during pause", e);
        }
    }
    
    @Override
    public void resume() {
        if (isDisposed) {
            Log.d(TAG, "Already disposed, skipping resume");
            return;
        }
        
        try {
            super.resume();
        } catch (Exception e) {
            Log.e(TAG, "Error during resume", e);
        }
    }
    
    @Override
    public void onTouchEvent(final MotionEvent motionEvent) {
        if (isDisposed) {
            Log.d(TAG, "Already disposed, skipping touch event");
            return;
        }
        
        try {
            super.onTouchEvent(motionEvent);
        } catch (Exception e) {
            Log.e(TAG, "Error handling touch event", e);
        }
    }
}