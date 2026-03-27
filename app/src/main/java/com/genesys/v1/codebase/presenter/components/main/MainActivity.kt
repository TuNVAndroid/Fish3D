package com.genesys.v1.codebase.presenter.components.main

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.genesys.v1.codebase.R
import com.genesys.v1.codebase.databinding.ActivityMainBinding
import com.genesys.v1.codebase.presenter.base.common.BaseActivity
import com.genesys.v1.codebase.presenter.components.wallpaperpreview.WallpaperPreviewActivity
import com.wave.livewallpaper.WallpaperPlaybackManager
import com.wave.livewallpaper.WallpaperSelectionManager
import com.wave.livewallpaper.libgdx.LibGdxLiveWallpaper
import com.wave.livewallpaper.libgdx.LibGdxLiveWallpaperAlternate

class MainActivity : BaseActivity<ActivityMainBinding>() {

    companion object {
        /** Clownfish bundle — uses GLTF models */
        private const val ASSETS_CLOWNFISH = "com.wave.livewallpaper.clownfishes"
        /** Goldfish bundle — uses G3DB models */
        private const val ASSETS_GOLDFISH = "com.wave.livewallpaper.livefisheslivewallpaper"
        /** Test Fish bundle — prototype GLTF */
        private const val ASSETS_TEST_FISH = "test_fish"
        /** Red Devil bundle — Video */
        private const val ASSETS_RED_DEVIL = "reddevil"

        private const val REQUEST_SET_WALLPAPER = 1002
    }

    private lateinit var wallpaperSelectionManager: WallpaperSelectionManager

    override fun getLazyViewBinding(): Lazy<ActivityMainBinding> = lazy<ActivityMainBinding> {
        DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

    override fun initViews(savedInstanceState: Bundle?) {
        wallpaperSelectionManager = WallpaperSelectionManager(this)
        loadPreviewImage(ASSETS_CLOWNFISH, viewBinding.ivPreviewGltf)
        loadPreviewImage(ASSETS_GOLDFISH, viewBinding.ivPreviewG3db)
        loadPreviewImage(ASSETS_TEST_FISH, viewBinding.ivPreviewTestFish)
        loadPreviewImage(ASSETS_RED_DEVIL, viewBinding.ivPreviewRedDevil)
    }

    override fun initListeners() {
        super.initListeners()

        // ---- GLTF card (Clownfish) ----
        viewBinding.cardGltf.setOnClickListener { openPreview(ASSETS_CLOWNFISH) }
        viewBinding.btnPreviewGltf.setOnClickListener { openPreview(ASSETS_CLOWNFISH) }
        viewBinding.btnSetGltf.setOnClickListener { applyWallpaperDirect(ASSETS_CLOWNFISH) }

        // ---- G3DB card (Goldfish) ----
        viewBinding.cardG3db.setOnClickListener { openPreview(ASSETS_GOLDFISH) }
        viewBinding.btnPreviewG3db.setOnClickListener { openPreview(ASSETS_GOLDFISH) }
        viewBinding.btnSetG3db.setOnClickListener { applyWallpaperDirect(ASSETS_GOLDFISH) }

        // ---- Test Fish card ----
        viewBinding.cardTestFish.setOnClickListener { openPreview(ASSETS_TEST_FISH) }
        viewBinding.btnPreviewTestFish.setOnClickListener { openPreview(ASSETS_TEST_FISH) }
        viewBinding.btnSetTestFish.setOnClickListener { applyWallpaperDirect(ASSETS_TEST_FISH) }

        // ---- Red Devil card ----
        viewBinding.cardRedDevil.setOnClickListener { openPreview(ASSETS_RED_DEVIL) }
        viewBinding.btnPreviewRedDevil.setOnClickListener { openPreview(ASSETS_RED_DEVIL) }
        viewBinding.btnSetRedDevil.setOnClickListener { applyWallpaperDirect(ASSETS_RED_DEVIL) }
    }

    private fun loadPreviewImage(assetsPath: String, imageView: android.widget.ImageView) {
        // Try preview_img.jpg first, then fallback to previewLW.jpg or preview.jpg
        val candidates = listOf("preview_img.jpg", "previewLW.jpg", "preview.jpg")

        for (name in candidates) {
            try {
                val inputStream = assets.open("$assetsPath/$name")
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                imageView.setImageBitmap(bitmap)
                return
            } catch (_: Exception) {
                // Try next candidate
            }
        }
    }

    private fun openPreview(assetsPath: String) {
        val assetsDir = prepareWallpaperAssets(assetsPath)
        if (assetsDir != null) {
            // Set the selected wallpaper so preview knows which one to show
            val wallpaperId = WallpaperSelectionManager.getWallpaperIdFromPath(assetsPath)
            wallpaperSelectionManager.setSelectedWallpaper(wallpaperId, assetsDir)
            
            WallpaperPreviewActivity.launch(this, assetsDir)
        } else {
            Toast.makeText(this, R.string.wallpaper_set_failed, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Copy wallpaper assets from the APK assets directory to internal storage
     * so that GenericAppListener can read them via File paths.
     */
    private fun prepareWallpaperAssets(assetsPath: String): String? {
        return try {
            val destDir = java.io.File(filesDir, assetsPath)
            if (!destDir.exists()) {
                destDir.mkdirs()
            }

            val assetFiles = assets.list(assetsPath) ?: return null

            for (fileName in assetFiles) {
                val destFile = java.io.File(destDir, fileName)
                if (!destFile.exists()) {
                    assets.open("$assetsPath/$fileName").use { input ->
                        destFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }

            destDir.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun applyWallpaperDirect(assetsPath: String) {
        try {
            val prepared = prepareWallpaperAssets(assetsPath)
            if (prepared != null) {
                // Determine wallpaper ID and service class based on assets path
                val wallpaperId = WallpaperSelectionManager.getWallpaperIdFromPath(assetsPath)
                val serviceClass = if (wallpaperId == WallpaperSelectionManager.WALLPAPER_GOLDFISH) {
                    LibGdxLiveWallpaperAlternate::class.java
                } else {
                    LibGdxLiveWallpaper::class.java
                }

                // Use WallpaperSelectionManager to properly set the selected wallpaper
                wallpaperSelectionManager.setSelectedWallpaper(wallpaperId, prepared)

                // Also save to legacy shared preferences for backward compatibility
                val prefs = getSharedPreferences("wallpaper_prefs", MODE_PRIVATE)
                prefs.edit().putString("wallpaper_disk_path", prepared).apply()

                val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                    putExtra(
                        WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        ComponentName(this@MainActivity, serviceClass)
                    )
                }
                startActivityForResult(intent, REQUEST_SET_WALLPAPER)
            }
        } catch (e: Exception) {
            try {
                val fallbackIntent = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
                startActivityForResult(fallbackIntent, REQUEST_SET_WALLPAPER)
            } catch (e2: Exception) {
                Toast.makeText(this, R.string.wallpaper_set_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Deprecated("Use Activity Result API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SET_WALLPAPER && resultCode == RESULT_OK) {
            Toast.makeText(this, R.string.wallpaper_set_success, Toast.LENGTH_SHORT).show()
        }
    }
}