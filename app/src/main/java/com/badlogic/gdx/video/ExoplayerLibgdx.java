package com.badlogic.gdx.video;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.VideoSize;
import androidx.media3.exoplayer.ExoPlayer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class ExoplayerLibgdx implements VideoPlayer {

    private static final String TAG = "ExoPlayerLibgdx";
    private static final int GL_TEXTURE_EXTERNAL_OES = 36197;

    private Context context;
    private ExoPlayer exoPlayer;
    private SurfaceTexture surfaceTexture;
    private Surface surface;
    private int textureId = -1;
    private boolean isPrepared = false;
    private boolean isPlaying = false;
    private boolean isLooping = false;
    private boolean updateTexture = false;
    
    private SpriteBatch spriteBatch;
    private ShaderProgram videoShader;
    private Camera camera;
    private float videoWidth, videoHeight;
    private float screenWidth, screenHeight;
    
    private VideoSizeListener videoSizeListener;
    private ErrorListener errorListener;

    public ExoplayerLibgdx(Object contextObj, float x, float y, float volume) {
        this.context = (Context) contextObj;
        initGraphics();
        
        // Initialize on Main Thread
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            initPlayer();
        });
    }

    private void initPlayer() {
        if (exoPlayer != null) return;
        try {
            exoPlayer = new ExoPlayer.Builder(context).build();
            exoPlayer.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int state) {
                    if (state == Player.STATE_READY) {
                        isPrepared = true;
                        isPlaying = true;
                        // Re-apply repeat mode again just in case
                        exoPlayer.setRepeatMode(isLooping ? Player.REPEAT_MODE_ALL : Player.REPEAT_MODE_OFF);
                        Log.d(TAG, "ExoPlayer Ready. Auto-start. Loop: " + isLooping);
                    } else if (state == Player.STATE_ENDED) {
                        Log.d(TAG, "ExoPlayer State Ended. Loop: " + isLooping);
                        if (!isLooping) isPlaying = false;
                    }
                }

                @Override
                public void onVideoSizeChanged(VideoSize videoSize) {
                    videoWidth = videoSize.width;
                    videoHeight = videoSize.height;
                    if (videoWidth > 0 && videoHeight > 0) {
                        if (videoSizeListener != null) {
                            videoSizeListener.onVideoSize(videoWidth, videoHeight);
                        }
                        Log.d(TAG, "Video Size: " + videoWidth + "x" + videoHeight);
                    }
                }

                @Override
                public void onPlayerError(androidx.media3.common.PlaybackException error) {
                    Log.e(TAG, "ExoPlayer error", error);
                    if (errorListener != null) errorListener.onError(error);
                }
            });

            if (surface != null) {
                exoPlayer.setVideoSurface(surface);
            }
            exoPlayer.setRepeatMode(isLooping ? Player.REPEAT_MODE_ALL : Player.REPEAT_MODE_OFF);
            exoPlayer.setPlayWhenReady(true);
        } catch (Exception e) {
            Log.e(TAG, "initPlayer failed", e);
        }
    }

    private void initGraphics() {
        spriteBatch = new SpriteBatch();
        camera = new OrthographicCamera();
        
        try {
            String vertexShader = Gdx.files.internal("shaders/video.vert").readString();
            String fragmentShader = Gdx.files.internal("shaders/video.frag").readString();
            videoShader = new ShaderProgram(vertexShader, fragmentShader);
            if (!videoShader.isCompiled()) {
                Log.e(TAG, "Shader compile error: " + videoShader.getLog());
            }

            textureId = Gdx.gl.glGenTexture();
            Gdx.gl.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId);
            Gdx.gl.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_LINEAR);
            Gdx.gl.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_LINEAR);
            Gdx.gl.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL20.GL_TEXTURE_WRAP_S, GL20.GL_CLAMP_TO_EDGE);
            Gdx.gl.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL20.GL_TEXTURE_WRAP_T, GL20.GL_CLAMP_TO_EDGE);

            surfaceTexture = new SurfaceTexture(textureId);
            surfaceTexture.setOnFrameAvailableListener(st -> updateTexture = true);
            surface = new Surface(surfaceTexture);
        } catch (Exception e) {
            Log.e(TAG, "initGraphics failed", e);
        }
    }

    @Override
    public boolean play(FileHandle file) {
        final String path = file.file().getAbsolutePath();
        Log.d(TAG, "ExoPlayer play: " + path + " Loop: " + isLooping);
        
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            if (exoPlayer == null) initPlayer();
            if (exoPlayer != null) {
                isPrepared = false;
                MediaItem mediaItem = MediaItem.fromUri(Uri.fromFile(new java.io.File(path)));
                exoPlayer.setMediaItem(mediaItem);
                exoPlayer.prepare();
                // Safety: re-apply repeat mode
                exoPlayer.setRepeatMode(isLooping ? Player.REPEAT_MODE_ALL : Player.REPEAT_MODE_OFF);
                exoPlayer.play();
                Log.d(TAG, "ExoPlayer play() called. RepeatMode: " + (isLooping ? "ALL" : "OFF"));
            }
        });
        return true;
    }

    @Override
    public void render() {
        if (!isPrepared) return;

        if (updateTexture) {
            try {
                surfaceTexture.updateTexImage();
            } catch (Exception e) {}
            updateTexture = false;
        }

        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
        Gdx.gl.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId);

        spriteBatch.setShader(videoShader);
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        spriteBatch.draw(getDummyTexture(), 0, 0, screenWidth, screenHeight);
        spriteBatch.end();
        spriteBatch.setShader(null);
    }
    
    private Texture dummyTexture;
    private Texture getDummyTexture() {
        if (dummyTexture == null) {
            dummyTexture = new Texture(1, 1, Pixmap.Format.RGBA8888);
        }
        return dummyTexture;
    }

    @Override
    public void resize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.position.set(width / 2f, height / 2f, 0);
        camera.update();
    }

    @Override
    public void releaseMediaPlayer() {
        Log.d(TAG, "Releasing ExoPlayer");
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            if (exoPlayer != null) {
                exoPlayer.release();
                exoPlayer = null;
            }
        });
        
        if (surface != null) { surface.release(); surface = null; }
        if (surfaceTexture != null) { surfaceTexture.release(); surfaceTexture = null; }
        if (spriteBatch != null) { spriteBatch.dispose(); spriteBatch = null; }
        if (videoShader != null) { videoShader.dispose(); videoShader = null; }
        if (dummyTexture != null) { dummyTexture.dispose(); dummyTexture = null; }
    }

    @Override
    public void setLooping(boolean looping) {
        this.isLooping = looping;
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            if (exoPlayer != null) {
                exoPlayer.setRepeatMode(looping ? Player.REPEAT_MODE_ALL : Player.REPEAT_MODE_OFF);
            }
        });
    }

    @Override
    public void setFadeEnabled(boolean enabled) {}

    @Override
    public void setOnVideoSizeListener(VideoSizeListener listener) {
        this.videoSizeListener = listener;
    }

    @Override
    public void setOnErrorListener(ErrorListener listener) {
        this.errorListener = listener;
    }

    @Override
    public boolean isPlaying() {
        return isPrepared && isPlaying;
    }

    @Override
    public long getCurrentPosition() {
        // Current position handling on main thread might be needed but we can try returning local cached if needed
        return 0; 
    }
}
