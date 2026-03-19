package com.wave.livewallpaper.libgdx;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import androidx.lifecycle.Lifecycle;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.video.ExoplayerLibgdx;
import com.badlogic.gdx.video.VideoPlayer;
import com.wave.keyboard.theme.utils.FirebaseHelper;
import com.wave.livewallpaper.data.LiveWallpaperConfig;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;

public class LockscreenRenderer implements LibgdxLifecycle {

    private Context context;
    private Handler handler;
    private KeyguardManager keyguardManager;
    private String wallpaperDiskPath;
    private LiveWallpaperConfig wallpaperConfig;
    private VideoPlayer videoPlayer;
    private AssetManager assetManager;
    private Camera camera;
    private SpriteBatch spriteBatch;
    private Sprite lockImageSprite;
    private State lockState = State.UNLOCKED;
    private Lifecycle.State lifecycleState = Lifecycle.State.INITIALIZED;
    private Disposable initDisposable;
    private boolean videoSizeReceived;
    private boolean hasLockscreenAssets;
    private boolean hasLockImage;
    private boolean hasLockVideo;
    private boolean hasLockVideoLoop;
    private boolean hasFromHomeTransition;
    private boolean hasToHomeTransition;
    private boolean isKeyguardLocked;
    private boolean currentKeyguardState;
    private boolean showOverlayImage;
    private float keyguardCheckTimer;

    public enum State {
        LOCKED,
        LOCKING_IN_PROGRESS,
        UNLOCKED,
        UNLOCKING_WAITING_TRANSITION,
        UNLOCKING_IN_PROGRESS
    }

    public LockscreenRenderer(Context context, String wallpaperDiskPath, LiveWallpaperConfig config, AssetManager assetManager, Camera camera, SpriteBatch spriteBatch, KeyguardManager keyguardManager, Handler handler) {
        this.context = context;
        this.wallpaperDiskPath = wallpaperDiskPath;
        this.wallpaperConfig = config;
        this.assetManager = assetManager;
        this.camera = camera;
        this.spriteBatch = spriteBatch;
        this.keyguardManager = keyguardManager;
        this.handler = handler;
    }

    private void createVideoPlayer() {
        ExoplayerLibgdx exoplayerLibgdx = new ExoplayerLibgdx(this.context, 0.0f, 0.0f, 1.0f);
        this.videoPlayer = exoplayerLibgdx;
        exoplayerLibgdx.setFadeEnabled(false);
        this.videoPlayer.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.videoPlayer.setOnVideoSizeListener(new VideoPlayer.VideoSizeListener() {
            @Override
            public final void onVideoSize(float width, float height) {
                LockscreenRenderer.this.onVideoSizeReceived(width, height);
            }
        });
        this.videoPlayer.setOnErrorListener(new VideoPlayer.ErrorListener() {
            @Override
            public final void onError(Exception e) {
                Log.e("LockscreenRenderer", "videoPlayer ERROR", e);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void onInitDelayComplete(Integer num) {
        resolveAndPlayVideo();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void onVideoSizeReceived(float width, float height) {
        this.videoSizeReceived = true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void playVideoFile(String path) {
        try {
            FileHandle fileHandle = Gdx.files.absolute(path);
            Gdx.app.log("LockscreenRenderer", "Loading file : " + fileHandle.file().getAbsolutePath());
            this.videoPlayer.play(fileHandle);
        } catch (Exception e2) {
            Gdx.app.log("LockscreenRenderer", "Err: " + e2);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void onResumeInitDelayComplete1(Integer num) {
        resolveAndPlayVideo();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void onResumeInitDelayComplete2(Integer num) {
        resolveAndPlayVideo();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void setVideoLooping(boolean looping) {
        this.videoPlayer.setLooping(looping);
    }

    private void resolveAndPlayVideo() {
        String videoPath = "";
        boolean looping = false;
        if (this.hasLockVideo) {
            State state = this.lockState;
            if (state == State.LOCKING_IN_PROGRESS) {
                videoPath = this.wallpaperDiskPath + "/" + this.wallpaperConfig.lockscreenAnim.fromHome;
            } else if (state == State.UNLOCKING_IN_PROGRESS) {
                videoPath = this.wallpaperDiskPath + "/" + this.wallpaperConfig.lockscreenAnim.toHome;
            } else if (state == State.LOCKED) {
                if (this.hasLockVideoLoop) {
                    videoPath = this.wallpaperDiskPath + "/" + this.wallpaperConfig.lockscreenAnim.lockVideo;
                }
                if (this.wallpaperConfig.lockscreenAnim.loop) {
                    looping = true;
                }
            }
        }
        if (TextUtils.isEmpty(videoPath)) {
            return;
        }
        postPlayVideo(videoPath);
        postSetLooping(looping);
    }

    private void postPlayVideo(final String path) {
        this.handler.post(new Runnable() {
            @Override
            public final void run() {
                LockscreenRenderer.this.playVideoFile(path);
            }
        });
    }

    private void releaseVideoPlayer() {
        VideoPlayer player = this.videoPlayer;
        if (player != null) {
            player.releaseMediaPlayer();
        }
    }

    private void renderLockImage() {
        State state;
        if (this.lockImageSprite == null && this.assetManager.update()) {
            Texture texture = (Texture) this.assetManager.get(this.wallpaperConfig.lockscreenAnim.lockImage, Texture.class);
            Texture.TextureFilter textureFilter = Texture.TextureFilter.Linear;
            texture.setFilter(textureFilter, textureFilter);
            Sprite sprite = new Sprite(texture);
            this.lockImageSprite = sprite;
            sprite.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            fitSprite(this.lockImageSprite, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        if (this.isKeyguardLocked || (((state = this.lockState) == State.UNLOCKING_IN_PROGRESS || state == State.UNLOCKING_WAITING_TRANSITION) && this.showOverlayImage)) {
            if ((this.hasLockVideo && this.hasLockVideoLoop && !this.showOverlayImage) || this.lockImageSprite == null) {
                return;
            }
            this.camera.update();
            this.spriteBatch.setProjectionMatrix(this.camera.combined);
            this.spriteBatch.begin();
            this.lockImageSprite.draw(this.spriteBatch);
            this.spriteBatch.end();
        }
    }

    private void updateLockState() {
        VideoPlayer player;
        VideoPlayer player2;
        VideoPlayer player3;
        if (this.lockState == State.LOCKING_IN_PROGRESS && isVideoFinished()) {
            this.lockState = State.LOCKED;
            Log.d("LockscreenRenderer", "render > state " + this.lockState + " videoPlayer.getCurrentPosition " + this.videoPlayer.getCurrentPosition());
            if (this.hasLockVideoLoop) {
                postPlayVideo(this.wallpaperDiskPath + "/" + this.wallpaperConfig.lockscreenAnim.lockVideo);
                postSetLooping(this.wallpaperConfig.lockscreenAnim.loop);
            }
        }
        if (!this.isKeyguardLocked) {
            State state = this.lockState;
            if (state == State.LOCKED) {
                if (this.hasToHomeTransition) {
                    if (this.hasLockVideoLoop && this.wallpaperConfig.lockscreenAnim.loop) {
                        postSetLooping(false);
                    }
                    this.lockState = State.UNLOCKING_WAITING_TRANSITION;
                } else {
                    this.lockState = State.UNLOCKED;
                    Log.d("LockscreenRenderer", "render > state " + this.lockState);
                }
            } else if (state == State.UNLOCKING_WAITING_TRANSITION) {
                if (!this.hasLockVideoLoop || isVideoFinished()) {
                    this.lockState = State.UNLOCKING_IN_PROGRESS;
                    Log.d("LockscreenRenderer", "render > state " + this.lockState);
                    postPlayVideo(this.wallpaperDiskPath + "/" + this.wallpaperConfig.lockscreenAnim.toHome);
                    postSetLooping(false);
                    if (!this.hasLockVideoLoop && this.hasLockImage) {
                        this.showOverlayImage = true;
                    }
                }
            } else if (state == State.UNLOCKING_IN_PROGRESS) {
                if (isVideoFinished()) {
                    this.lockState = State.UNLOCKED;
                    Log.d("LockscreenRenderer", "lockToHomeTransition > done! state: " + this.lockState);
                } else if (this.showOverlayImage && (player = this.videoPlayer) != null && player.getCurrentPosition() > 200) {
                    this.showOverlayImage = false;
                }
            }
        } else if (this.showOverlayImage && (player3 = this.videoPlayer) != null && player3.getCurrentPosition() > 100) {
            this.showOverlayImage = false;
        }
        if (!this.videoSizeReceived || (player2 = this.videoPlayer) == null || this.lockState == State.UNLOCKED) {
            return;
        }
        player2.render();
    }

    private void postSetLooping(final boolean looping) {
        this.handler.post(new Runnable() {
            @Override
            public final void run() {
                LockscreenRenderer.this.setVideoLooping(looping);
            }
        });
    }

    private void fitSprite(Sprite sprite, float targetWidth, float targetHeight) {
        float width = sprite.getWidth();
        float height = sprite.getHeight();
        float scale = Math.max(targetWidth / width, targetHeight / height);
        float scaledWidth = (int) (width * scale);
        float scaledHeight = (int) (scale * height);
        sprite.setBounds((targetWidth - scaledWidth) / 2.0f, (targetHeight - scaledHeight) / 2.0f, scaledWidth, scaledHeight);
    }

    private boolean isVideoFinished() {
        VideoPlayer player = this.videoPlayer;
        return (player == null || player.isPlaying() || this.videoPlayer.getCurrentPosition() <= 0) ? false : true;
    }

    @Override
    public void create() {
        this.hasLockImage = this.wallpaperConfig.lockscreenAnim.hasImage();
        boolean videoAvailable = this.wallpaperConfig.lockscreenAnim.hasVideo();
        this.hasLockVideo = videoAvailable;
        if (videoAvailable) {
            this.hasLockVideoLoop = !TextUtils.isEmpty(this.wallpaperConfig.lockscreenAnim.lockVideo);
            this.hasFromHomeTransition = !TextUtils.isEmpty(this.wallpaperConfig.lockscreenAnim.fromHome);
            this.hasToHomeTransition = !TextUtils.isEmpty(this.wallpaperConfig.lockscreenAnim.toHome);
        }
        boolean hasImage = this.hasLockImage;
        this.hasLockscreenAssets = hasImage || this.hasLockVideo;
        if (hasImage) {
            this.assetManager.load(this.wallpaperConfig.lockscreenAnim.lockImage, Texture.class);
        }
        createVideoPlayer();
        this.initDisposable = Observable.just(1).delay(400L, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer() {
            @Override
            public final void accept(Object obj) {
                LockscreenRenderer.this.onInitDelayComplete((Integer) obj);
            }
        });
        this.lifecycleState = Lifecycle.State.CREATED;
    }

    @Override
    public void dispose() {
        releaseVideoPlayer();
    }

    public State getState() {
        return this.lockState;
    }

    public boolean hasVideo() {
        return this.hasLockVideo;
    }

    @Override
    public void pause() {
        Disposable disposable = this.initDisposable;
        if (disposable != null && !disposable.isDisposed()) {
            this.initDisposable.dispose();
        }
        this.initDisposable = null;
        releaseVideoPlayer();
        this.lifecycleState = Lifecycle.State.CREATED;
    }

    @Override
    public void render() {
        if (this.hasLockscreenAssets) {
            boolean locked = this.isKeyguardLocked;
            this.currentKeyguardState = locked;
            if (locked) {
                float deltaTime = this.keyguardCheckTimer - Gdx.graphics.getDeltaTime();
                this.keyguardCheckTimer = deltaTime;
                if (deltaTime <= 0.0f) {
                    this.keyguardCheckTimer = 0.1f;
                    this.currentKeyguardState = this.keyguardManager.isKeyguardLocked();
                }
            }
            if (this.hasLockVideo) {
                updateLockState();
            }
            if (this.hasLockImage) {
                renderLockImage();
            }
            this.isKeyguardLocked = this.currentKeyguardState;
        }
    }

    @Override
    public void resize(int width, int height) {
        Log.d("LockscreenRenderer", "resize w = " + width + " h = " + height);
        VideoPlayer player = this.videoPlayer;
        if (player != null) {
            player.resize(width, height);
        }
    }

    @Override
    public void resume() {
        if (this.hasLockscreenAssets && this.lifecycleState.isAtLeast(Lifecycle.State.CREATED)) {
            KeyguardManager km = this.keyguardManager;
            if (km != null) {
                this.isKeyguardLocked = km.isKeyguardLocked();
                Log.d("LockscreenRenderer", "resume > isKeyguardLocked > " + this.isKeyguardLocked);
            }
            boolean locked = this.isKeyguardLocked;
            State state = locked ? State.LOCKED : State.UNLOCKED;
            if (this.hasLockVideo) {
                if (locked) {
                    state = (TextUtils.isEmpty(this.wallpaperConfig.lockscreenAnim.fromHome) || this.lockState == State.LOCKED) ? State.LOCKED : State.LOCKING_IN_PROGRESS;
                }
                if (this.hasLockImage) {
                    this.showOverlayImage = true;
                }
            }
            this.lockState = state;
            if (this.videoPlayer == null && this.initDisposable == null) {
                Observable.just(1).delay(400L, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer() {
                    @Override
                    public final void accept(Object obj) {
                        LockscreenRenderer.this.onResumeInitDelayComplete1((Integer) obj);
                    }
                });
            } else if (this.initDisposable == null) {
                this.initDisposable = Observable.just(1).delay(400L, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer() {
                    @Override
                    public final void accept(Object obj) {
                        LockscreenRenderer.this.onResumeInitDelayComplete2((Integer) obj);
                    }
                });
            }
            Log.d("LockscreenRenderer", "resume > state " + this.lockState);
            this.lifecycleState = Lifecycle.State.RESUMED;
        }
    }
}
