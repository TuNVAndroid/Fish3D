package com.badlogic.gdx.video;

import com.badlogic.gdx.files.FileHandle;

public interface VideoPlayer {

    boolean play(FileHandle file);
    void render();
    void resize(int width, int height);
    void releaseMediaPlayer();
    void setLooping(boolean looping);
    void setFadeEnabled(boolean enabled);
    void setOnVideoSizeListener(VideoSizeListener listener);
    void setOnErrorListener(ErrorListener listener);
    boolean isPlaying();
    long getCurrentPosition();

    interface VideoSizeListener {
        void onVideoSize(float width, float height);
    }

    interface ErrorListener {
        void onError(Exception e);
    }
}
