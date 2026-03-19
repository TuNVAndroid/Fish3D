package com.genesys.v1.codebase.presenter.components.wallpaperpreview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.backends.android.AndroidFragmentApplication
import com.wave.livewallpaper.libgdx.GenericAppListener
import java.io.IOException

/**
 * Fragment that hosts the LibGDX rendering surface for the wallpaper preview.
 * Uses GenericAppListener (the same one used by the actual wallpaper service)
 * so that the preview is a pixel-perfect representation of the live wallpaper.
 */
class WallpaperPreviewFragment : AndroidFragmentApplication() {

    companion object {
        private const val ARG_WALLPAPER_PATH = "wallpaper_path"

        fun newInstance(wallpaperPath: String): WallpaperPreviewFragment {
            return WallpaperPreviewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_WALLPAPER_PATH, wallpaperPath)
                }
            }
        }
    }

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

        val listener = try {
            GenericAppListener(requireContext(), wallpaperPath)
        } catch (e: IOException) {
            // Fallback: use the other constructor overload
            GenericAppListener(wallpaperPath, requireContext())
        }

        return initializeForView(listener, config)
    }
}
