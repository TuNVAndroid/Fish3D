package com.genesys.v1.codebase.presenter.components.wallpaperpreview

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.backends.android.AndroidFragmentApplication
import com.genesys.v1.codebase.R
import com.wave.livewallpaper.libgdx.GenericAppListener
import com.wave.livewallpaper.libgdx.LibGdxLiveWallpaper

class WallpaperPreviewActivity : AppCompatActivity(), AndroidFragmentApplication.Callbacks {

    companion object {
        private const val TAG = "WallpaperPreview"
        private const val EXTRA_WALLPAPER_PATH = "wallpaper_path"
        private const val REQUEST_SET_WALLPAPER = 1001

        fun launch(context: Context, wallpaperPath: String) {
            val intent = Intent(context, WallpaperPreviewActivity::class.java).apply {
                putExtra(EXTRA_WALLPAPER_PATH, wallpaperPath)
            }
            context.startActivity(intent)
        }
    }

    private var gdxFragment: WallpaperPreviewFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make fullscreen immersive
        setupImmersiveMode()

        setContentView(R.layout.activity_wallpaper_preview)

        val wallpaperPath = intent.getStringExtra(EXTRA_WALLPAPER_PATH) ?: run {
            Log.e(TAG, "No wallpaper path provided")
            finish()
            return
        }

        setupFragment(wallpaperPath, savedInstanceState)
        setupButtons()
        animateUI()
    }

    private fun setupImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.navigationBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun setupFragment(wallpaperPath: String, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            gdxFragment = WallpaperPreviewFragment.newInstance(wallpaperPath)
            supportFragmentManager.beginTransaction()
                .replace(R.id.gdxContainer, gdxFragment!!)
                .commit()
        } else {
            gdxFragment = supportFragmentManager.findFragmentById(R.id.gdxContainer) as? WallpaperPreviewFragment
        }
    }

    private fun setupButtons() {
        findViewById<View>(R.id.btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<View>(R.id.btnApplyWallpaper).setOnClickListener {
            applyWallpaper()
        }
    }

    private fun animateUI() {
        // Hide loading overlay after a short delay
        val loadingOverlay = findViewById<View>(R.id.loadingOverlay)
        loadingOverlay.postDelayed({
            loadingOverlay.animate()
                .alpha(0f)
                .setDuration(500)
                .withEndAction { loadingOverlay.visibility = View.GONE }
                .start()
        }, 2000)

        // Animate bottom bar in
        val bottomBar = findViewById<View>(R.id.bottomBar)
        bottomBar.translationY = 200f
        bottomBar.alpha = 0f
        bottomBar.postDelayed({
            bottomBar.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(600)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }, 1500)

        // Animate title in
        val title = findViewById<View>(R.id.tvTitle)
        title.alpha = 0f
        title.translationY = -30f
        title.postDelayed({
            title.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .start()
        }, 800)

        // Animate back button in
        val btnBack = findViewById<View>(R.id.btnBack)
        btnBack.alpha = 0f
        btnBack.scaleX = 0.5f
        btnBack.scaleY = 0.5f
        btnBack.postDelayed({
            btnBack.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .start()
        }, 600)
    }

    private fun applyWallpaper() {
        try {
            // Always use the main LibGdxLiveWallpaper service
            // The wallpaper selection is managed by WallpaperSelectionManager
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(this@WallpaperPreviewActivity, LibGdxLiveWallpaper::class.java)
                )
            }
            startActivityForResult(intent, REQUEST_SET_WALLPAPER)
        } catch (e: Exception) {
            Log.e(TAG, "Error launching wallpaper picker", e)
            // Fallback: try the generic live wallpaper picker
            try {
                val fallbackIntent = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
                startActivityForResult(fallbackIntent, REQUEST_SET_WALLPAPER)
            } catch (e2: Exception) {
                Log.e(TAG, "Error launching fallback wallpaper picker", e2)
                Toast.makeText(this, R.string.wallpaper_set_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPause() {
        Log.d(TAG, "onPause() called")
        // Dispose fragment properly before pausing to avoid ANR
        try {
            gdxFragment?.let { fragment ->
                if (fragment.isAdded) {
                    supportFragmentManager.beginTransaction()
                        .remove(fragment)
                        .commitAllowingStateLoss()
                }
            }
            gdxFragment = null
        } catch (e: Exception) {
            Log.e(TAG, "Error removing fragment in onPause", e)
        }
        
        try {
            super.onPause()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onPause", e)
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy() called")
        try {
            gdxFragment?.let { fragment ->
                if (fragment.isAdded) {
                    supportFragmentManager.beginTransaction()
                        .remove(fragment)
                        .commitAllowingStateLoss()
                }
            }
            gdxFragment = null
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up fragment", e)
        }
        
        try {
            super.onDestroy()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy", e)
        }
    }

    @Deprecated("Use Activity Result API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SET_WALLPAPER) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, R.string.wallpaper_set_success, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun exit() {
        // Required by AndroidFragmentApplication.Callbacks
        finish()
    }
}