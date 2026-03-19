package com.wave.livewallpaper.libgdx;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Simple settings activity for the live wallpaper.
 * This is referenced in wallpaper.xml but kept minimal.
 */
public class WallpaperSettingsActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // For now, just show a message and close
        Toast.makeText(this, "No settings available", Toast.LENGTH_SHORT).show();
        finish();
    }
}