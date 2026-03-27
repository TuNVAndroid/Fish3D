package com.wave.livewallpaper.libgdx;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.wave.keyboard.theme.utils.StringUtils;
import com.wave.livewallpaper.vfx.VfxConfigFile;
import com.wave.livewallpaper.vfx.VfxCooldown;
import com.wave.livewallpaper.vfx.VfxLibrary;
import com.wave.livewallpaper.vfx.VfxParticle;
import com.wave.livewallpaper.wallpaperpreview.VfxParticleConfigFile;
import java.io.File;

public class VfxParticleRenderer implements LibgdxLifecycle {

    private static String downloadsDir;
    private Context context;
    private String wallpaperDiskPath;
    private VfxParticle activeTouchVfx;
    private AssetManager assetManager;
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch spriteBatch;
    private TextureAtlas textureAtlas;
    private ShaderProgram shader;
    private ParticleEffect touchParticleTemplate;
    private ParticleEffectPool touchParticlePool;
    private ParticleEffect backgroundParticle;
    private String asyncParticlePath;
    private boolean isLoadingAsync;
    private Array activeTouchEffects = new Array();
    private VfxCooldown cooldown = VfxCooldown.DEFAULT;
    private Vector3 touchUnprojectVec = new Vector3(-1000.0f, -1000.0f, 0.0f);
    private boolean randomEmitter = false;
    private boolean sequentialEmitter = false;
    private int currentEmitterIndex = -1;
    private boolean isContinuousAnimated = false;
    private boolean hasLastTouch = false;
    private float lastTouchX = 0.0f;
    private float lastTouchY = 0.0f;

    private GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent motionEvent) {
            VfxParticleRenderer.this.spawnTouchEffect(motionEvent.getX(), (Gdx.graphics.getHeight() - 1) - motionEvent.getY());
            return true;
        }
    };

    public VfxParticleRenderer(Context context, AssetManager assetManager, String wallpaperDiskPath) {
        this.context = context;
        this.wallpaperDiskPath = wallpaperDiskPath;
        this.assetManager = assetManager;
        initDownloadsDir();
    }

    private void initDownloadsDir() {
        if (StringUtils.isNullOrEmpty(downloadsDir)) {
            downloadsDir = new File(this.context.getFilesDir(), "downloads").getAbsolutePath();
        }
    }

    private void disposeBackground() {
        ParticleEffect particle = this.backgroundParticle;
        if (particle != null) {
            particle.allowCompletion();
            if (StringUtils.isNotEmpty(this.asyncParticlePath)) {
                this.assetManager.unload(this.asyncParticlePath);
            } else {
                this.backgroundParticle.dispose();
            }
            this.backgroundParticle = null;
        }
    }

    private void disposeTouchPool() {
        ParticleEffect template = this.touchParticleTemplate;
        ParticleEffectPool pool = this.touchParticlePool;
        Array effects = this.activeTouchEffects;
        this.touchParticleTemplate = null;
        this.touchParticlePool = null;
        this.activeTouchEffects = new Array();
        freeActiveEffects(effects);
        clearPool(pool);
        if (template != null) {
            template.dispose();
        }
    }

    private void clearPool(ParticleEffectPool pool) {
        if (pool != null) {
            pool.clear();
        }
    }

    private void freeActiveEffects(Array effects) {
        if (effects == null) {
            return;
        }
        for (int i = effects.size - 1; i >= 0; i--) {
            ((ParticleEffectPool.PooledEffect) effects.get(i)).free();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: doLoadBackground, reason: merged with bridge method [inline-methods] */
    public void doLoadBackground(VfxParticle vfxParticle) {
        FileHandle atlasHandle;
        try {
            if (StringUtils.isNullOrEmpty(vfxParticle.particlePath)) {
                disposeBackground();
                return;
            }
            FileHandle particleHandle = Gdx.files.internal(vfxParticle.particlePath);
            if (!particleHandle.exists()) {
                particleHandle = Gdx.files.absolute(vfxParticle.particlePath);
            }
            if (!particleHandle.exists()) {
                particleHandle = Gdx.files.absolute(downloadsDir + File.separator + vfxParticle.particlePath);
            }
            boolean hasAtlas = StringUtils.isNotEmpty(vfxParticle.atlasPath);
            if (hasAtlas) {
                atlasHandle = Gdx.files.internal(vfxParticle.atlasPath);
                if (!atlasHandle.exists()) {
                    atlasHandle = Gdx.files.absolute(vfxParticle.atlasPath);
                }
                if (!atlasHandle.exists()) {
                    atlasHandle = Gdx.files.absolute(downloadsDir + File.separator + vfxParticle.atlasPath);
                }
            } else {
                atlasHandle = null;
            }
            boolean useBuiltinAtlas = (hasAtlas && atlasHandle.exists()) ? false : true;
            disposeBackground();
            ParticleEffect particleEffect = new ParticleEffect();
            if (useBuiltinAtlas && this.textureAtlas != null) {
                particleEffect.load(particleHandle, this.textureAtlas);
                startBackgroundParticle(particleEffect);
            } else if (useBuiltinAtlas) {
                // Fallback: load textures from the same folder as the .p file
                particleEffect.load(particleHandle, particleHandle.parent());
                startBackgroundParticle(particleEffect);
            } else {
                String path = particleHandle.path();
                this.asyncParticlePath = path;
                this.isLoadingAsync = true;
                this.assetManager.load(path, ParticleEffect.class);
            }
        } catch (Exception e) {
            Log.e("VfxParticleRenderer", "loadBackgroundParticles", e);
        }
    }

    private void loadFromConfig() {
        VfxConfigFile configFile = VfxConfigFile.forPath(this.wallpaperDiskPath);
        if (configFile.hasOverlay()) {
            VfxParticle overlayParticle = VfxParticleConfigFile.readVfxParticleConfig(this.context, configFile.overlayVfxName, "overlay");
            if (overlayParticle == null || overlayParticle.isEmpty()) {
                overlayParticle = VfxLibrary.getByName(configFile.overlayVfxName);
            }
            doLoadBackground(overlayParticle);
        }
        if (configFile.hasTouch()) {
            VfxParticle touchParticle = VfxParticleConfigFile.readVfxParticleConfig(this.context, configFile.touchVfxName, "touch");
            if (touchParticle == null || touchParticle.isEmpty()) {
                touchParticle = VfxLibrary.getByName(configFile.touchVfxName);
            }
            doLoadTouch(touchParticle);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: doLoadTouchInternal, reason: merged with bridge method [inline-methods] */
    public void doLoadTouch(VfxParticle vfxParticle) {
        FileHandle atlasHandle;
        try {
            if (!StringUtils.isNullOrEmpty(vfxParticle.particlePath) && !vfxParticle.isWaterVfx) {
                ParticleEffect oldTemplate = this.touchParticleTemplate;
                ParticleEffectPool oldPool = this.touchParticlePool;
                Array oldEffects = this.activeTouchEffects;
                FileHandle particleHandle = Gdx.files.internal(vfxParticle.particlePath);
                if (!particleHandle.exists()) {
                    particleHandle = Gdx.files.absolute(vfxParticle.particlePath);
                }
                if (!particleHandle.exists()) {
                    particleHandle = Gdx.files.absolute(downloadsDir + File.separator + vfxParticle.particlePath);
                }
                boolean hasAtlas = StringUtils.isNotEmpty(vfxParticle.atlasPath);
                if (hasAtlas) {
                    atlasHandle = Gdx.files.internal(vfxParticle.atlasPath);
                    if (!atlasHandle.exists()) {
                        atlasHandle = Gdx.files.absolute(vfxParticle.atlasPath);
                    }
                    if (!atlasHandle.exists()) {
                        atlasHandle = Gdx.files.absolute(downloadsDir + File.separator + vfxParticle.atlasPath);
                    }
                } else {
                    atlasHandle = null;
                }
                boolean useBuiltinAtlas = (hasAtlas && atlasHandle.exists()) ? false : true;
                ParticleEffect newTemplate = new ParticleEffect();
                this.touchParticleTemplate = newTemplate;
                if (useBuiltinAtlas && this.textureAtlas != null) {
                    newTemplate.load(particleHandle, this.textureAtlas);
                } else {
                    Log.d("VfxParticleRenderer", "doLoadTouch: " +particleHandle +",particleHandle.parent(): "+particleHandle.parent());
                    // Fallback: load textures from the same folder as the .p file
                    newTemplate.load(particleHandle, particleHandle.parent());
                }
                this.touchParticlePool = new ParticleEffectPool(this.touchParticleTemplate, 30, 100);
                Log.d("VfxParticleRenderer", "Successfully loaded touch particle: " + vfxParticle.name + " from " + particleHandle.path());
                this.activeTouchEffects = new Array();
                VfxCooldown vfxCooldown = vfxParticle.cooldown;
                if (vfxCooldown != null) {
                    this.cooldown = vfxCooldown;
                } else {
                    this.cooldown = VfxCooldown.DEFAULT;
                }
                this.cooldown.reset();
                this.randomEmitter = vfxParticle.randomize;
                this.sequentialEmitter = vfxParticle.sequential;
                freeActiveEffects(oldEffects);
                clearPool(oldPool);
                if (oldTemplate != null) {
                    oldTemplate.dispose();
                    return;
                }
                return;
            }
            disposeTouchPool();
        } catch (Exception e) {
            Log.e("VfxParticleRenderer", "loadTouchParticle", e);
        }
    }

    private void startBackgroundParticle(ParticleEffect particleEffect) {
        OrthographicCamera cam = this.camera;
        particleEffect.setPosition(cam.viewportWidth / 2.0f, cam.viewportHeight / 2.0f);
        particleEffect.start();
        this.backgroundParticle = particleEffect;
        this.isContinuousAnimated = false;
        Array.ArrayIterator<ParticleEmitter> it = particleEffect.getEmitters().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            ParticleEmitter next = it.next();
            if (next.isContinuous() && next.getSpriteMode().equals(ParticleEmitter.SpriteMode.animated)) {
                this.isContinuousAnimated = true;
                break;
            }
        }
        this.isLoadingAsync = false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void spawnTouchEffect(float x, float y) {
        if (this.touchParticlePool == null) {
            Log.w("VfxParticleRenderer", "spawnTouchEffect failed: touchParticlePool is null");
            return;
        }

        this.cooldown.setCurrentTouch(x, y);
        if (this.cooldown.isReady()) {
            doSpawnEffect(x, y);
            this.cooldown.setLastTouch(x, y);
            this.cooldown.startCooldown();
        }
    }

    private void doSpawnEffect(float x, float y) {
        if (this.touchParticlePool == null) {
            return;
        }
        ParticleEffectPool.PooledEffect pooledEffect = this.touchParticlePool.obtain();
        if (this.randomEmitter) {
            Array<ParticleEmitter> emitters = pooledEffect.getEmitters();
            emitters.clear();
            ParticleEmitter emitter = new ParticleEmitter((ParticleEmitter) this.touchParticleTemplate.getEmitters().random());
            emitter.reset();
            emitters.add(emitter);
        } else if (this.sequentialEmitter) {
            Array<ParticleEmitter> emitters = pooledEffect.getEmitters();
            emitters.clear();
            Array<ParticleEmitter> templateEmitters = this.touchParticleTemplate.getEmitters();
            int nextIndex = this.currentEmitterIndex + 1;
            this.currentEmitterIndex = nextIndex;
            if (nextIndex >= templateEmitters.size) {
                this.currentEmitterIndex = 0;
            }
            ParticleEmitter emitter = new ParticleEmitter((ParticleEmitter) templateEmitters.get(this.currentEmitterIndex));
            emitter.reset();
            emitters.add(emitter);
        }
        pooledEffect.setPosition(x, y);
        this.activeTouchEffects.add(pooledEffect);
    }

    @Override
    public void create() {
        OrthographicCamera cam = new OrthographicCamera();
        this.camera = cam;
        ScalingViewport scalingViewport = new ScalingViewport(Scaling.fillY, 1080.0f, 1920.0f, cam);
        this.viewport = scalingViewport;
        scalingViewport.apply(true);
        this.spriteBatch = new SpriteBatch();
        loadFromConfig();
    }

    @Override
    public void dispose() {
        freeActiveEffects(this.activeTouchEffects);
        clearPool(this.touchParticlePool);
        SpriteBatch batch = this.spriteBatch;
        if (batch != null) {
            batch.dispose();
        }
        ParticleEffect template = this.touchParticleTemplate;
        if (template != null) {
            template.dispose();
        }
        disposeBackground();
        TextureAtlas atlas = this.textureAtlas;
        if (atlas != null) {
            atlas.dispose();
        }
        ShaderProgram shaderProg = this.shader;
        if (shaderProg != null) {
            shaderProg.dispose();
        }
    }

    public void loadBackgroundParticle(final VfxParticle vfxParticle) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public final void run() {
                try {
                    VfxParticleRenderer.this.doLoadBackground(vfxParticle);
                } catch (Throwable t) {
                    Log.e("VfxParticleRenderer", "loadBackgroundParticles", t);
                }
            }
        });
    }

    public void loadTouchParticle(final VfxParticle vfxParticle) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public final void run() {
                try {
                    VfxParticleRenderer.this.doLoadTouch(vfxParticle);
                } catch (Throwable t) {
                    Log.e("VfxParticleRenderer", "loadTouchParticle", t);
                }
            }
        });
    }

    @Override
    public void pause() {
    }

    public void onTouchEvent(MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        float screenX = motionEvent.getX();
        float screenY = motionEvent.getY();
        
        Vector3 vec = this.touchUnprojectVec;
        vec.x = screenX;
        vec.y = screenY;
        Vector3 unprojected = this.camera.unproject(vec);
        float x = unprojected.x;
        float y = unprojected.y;

        if (action == MotionEvent.ACTION_DOWN) {
            this.hasLastTouch = true;
            this.lastTouchX = x;
            this.lastTouchY = y;
            spawnTouchEffect(x, y);
        } else if (action == MotionEvent.ACTION_MOVE && this.hasLastTouch) {
            float dx = x - this.lastTouchX;
            float dy = y - this.lastTouchY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            
            // If the swipe is fast, interpolate points to fill gaps
            // Use a density of about 1 particle per 20-50 units of distance
            float stepSize = 40.0f; 
            if (distance > stepSize) {
                int steps = (int) (distance / stepSize);
                for (int i = 1; i <= steps; i++) {
                    float lerpX = this.lastTouchX + (dx * i / steps);
                    float lerpY = this.lastTouchY + (dy * i / steps);
                    doSpawnEffect(lerpX, lerpY);
                }
            } else {
                spawnTouchEffect(x, y);
            }
            this.lastTouchX = x;
            this.lastTouchY = y;
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            this.hasLastTouch = false;
        }
    }

    @Override
    public void render() {
        if (this.isLoadingAsync && this.assetManager.update() && this.assetManager.isLoaded(this.asyncParticlePath, ParticleEffect.class)) {
            startBackgroundParticle((ParticleEffect) this.assetManager.get(this.asyncParticlePath, ParticleEffect.class));
        }
        float deltaTime = Gdx.graphics.getDeltaTime();
        this.viewport.apply();
        this.spriteBatch.setProjectionMatrix(this.camera.combined);
        this.spriteBatch.begin();
        ParticleEffect bgParticle = this.backgroundParticle;
        if (bgParticle != null) {
            bgParticle.update(deltaTime);
            this.backgroundParticle.draw(this.spriteBatch);
        }
        this.cooldown.tick(deltaTime);
        for (int i = this.activeTouchEffects.size - 1; i >= 0; i--) {
            ParticleEffectPool.PooledEffect pooledEffect = (ParticleEffectPool.PooledEffect) this.activeTouchEffects.get(i);
            pooledEffect.draw(this.spriteBatch, deltaTime);
            if (pooledEffect.isComplete()) {
                pooledEffect.free();
                this.activeTouchEffects.removeIndex(i);
            }
        }
        this.spriteBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        this.viewport.update(width, height);
        this.viewport.apply(true);
    }

    @Override
    public void resume() {
    }
}
