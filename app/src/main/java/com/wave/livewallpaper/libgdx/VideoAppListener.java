package com.wave.livewallpaper.libgdx;

import android.content.Context;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.video.ExoplayerLibgdx;
import com.badlogic.gdx.video.VideoPlayer;
import com.wave.livewallpaper.data.LiveWallpaperConfig;

public class VideoAppListener extends BaseAppListener {
    private VideoPlayer videoPlayer;

    public VideoAppListener(String str, LiveWallpaperConfig config, Context context) {
        super(str, config, context);
    }

    @Override
    public void create() {
        super.create();
        videoPlayer = new ExoplayerLibgdx(context, 0, 0, 1);
        videoPlayer.setFadeEnabled(false);
        videoPlayer.setLooping(true);
        videoPlayer.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        
        // Movie is always movie.mp4 in the folder
        videoPlayer.play(Gdx.files.absolute(this.wallpaperDiskPath + "/movie.mp4"));
    }

    @Override
    protected void renderCore() {
        // Render video first as background
        if (videoPlayer != null && videoPlayer.isPlaying()) {
            videoPlayer.render();
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (videoPlayer != null) {
            videoPlayer.resize(width, height);
        }
    }

    @Override
    public void pause() {
        super.pause();
        if (videoPlayer != null) {
            videoPlayer.releaseMediaPlayer();
            videoPlayer = null; // Forces recreate on resume
        }
    }

    @Override
    public void resume() {
        super.resume();
        if (videoPlayer == null) {
            videoPlayer = new ExoplayerLibgdx(context, 0, 0, 1);
            videoPlayer.setFadeEnabled(false);
            videoPlayer.setLooping(true);
            videoPlayer.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            videoPlayer.play(Gdx.files.absolute(this.wallpaperDiskPath + "/movie.mp4"));
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (videoPlayer != null) {
            videoPlayer.releaseMediaPlayer();
            videoPlayer = null;
        }
    }
}
