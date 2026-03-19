package com.genesys.v1.codebase.presenter.components.wallpaperpreview

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.backends.android.AndroidFragmentApplication
import com.wave.livewallpaper.libgdx.GenericAppListener
import com.wave.livewallpaper.libgdx.SafeGenericAppListener
import java.io.IOException

/**
 * Fragment that hosts the LibGDX rendering surface for the wallpaper preview.
 * Uses GenericAppListener (the same one used by the actual wallpaper service)
 * so that the preview is a pixel-perfect representation of the live wallpaper.
 */
class WallpaperPreviewFragment : AndroidFragmentApplication() {

    companion object {
        private const val TAG = "WallpaperPreviewFragment"
        private const val ARG_WALLPAPER_PATH = "wallpaper_path"

        fun newInstance(wallpaperPath: String): WallpaperPreviewFragment {
            return WallpaperPreviewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_WALLPAPER_PATH, wallpaperPath)
                }
            }
        }
    }

    private var listener: SafeGenericAppListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val wallpaperPath = arguments?.getString(ARG_WALLPAPER_PATH)
            ?: throw IllegalStateException("Wallpaper path is required")

        val config = AndroidApplicationConfiguration().apply {
            useAccelerometer = false
            useGyroscope = false
            useCompass = false
            numSamples = 2 // Anti-aliasing for smoother preview
        }

        listener = try {
            SafeGenericAppListener(requireContext(), wallpaperPath)
        } catch (e: IOException) {
            Log.w(TAG, "IOException with first constructor, trying fallback", e)
            // Fallback: use the other constructor overload
            SafeGenericAppListener(wallpaperPath, requireContext())
        }

        return initializeForView(listener!!, config)
    }

    override fun onPause() {
        Log.d(TAG, "onPause() called")
        try {
            super.onPause()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onPause", e)
        }
    }

    override fun onResume() {
        Log.d(TAG, "onResume() called")
        try {
            super.onResume()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onResume", e)
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy() called")
        try {
            listener?.dispose()
            listener = null
        } catch (e: Exception) {
            Log.e(TAG, "Error disposing listener", e)
        }
        
        try {
            super.onDestroy()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy", e)
        }
    }
}
