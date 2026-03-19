package com.badlogic.gdx.video;

import com.badlogic.gdx.files.FileHandle;

public class ExoplayerLibgdx implements VideoPlayer {

    public ExoplayerLibgdx(Object context, float x, float y, float volume) {
    }

    @Override
    public boolean play(FileHandle file) {
        return false;
    }

    @Override
    public void render() {
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void releaseMediaPlayer() {
    }

    @Override
    public void setLooping(boolean looping) {
    }

    @Override
    public void setFadeEnabled(boolean enabled) {
    }

    @Override
    public void setOnVideoSizeListener(VideoSizeListener listener) {
    }

    @Override
    public void setOnErrorListener(ErrorListener listener) {
    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public long getCurrentPosition() {
        return 0;
    }
}
