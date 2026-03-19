package com.wave.livewallpaper.libgdx;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.wave.keyboard.theme.utils.FirebaseHelper;
import com.wave.livewallpaper.WallpaperSelectionManager;
import com.wave.livewallpaper.data.LiveWallpaperConfig;
import com.wave.livewallpaper.data.LiveWallpaperConfigReader;
import com.wave.livewallpaper.data.SceneConfig;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

/* loaded from: classes6.dex */
public class SceneFishesAppListener extends BaseAppListener {
    private ModelBatch shadowReceiveBatch;
    private ModelBatch shadowGenBatch;
    private SpriteBatch waterSpriteBatch;
    private boolean isLoading;
    private final List<FishResource> fishResources;
    private ModelInstance backgroundModel;
    private Array idleFishes;
    private Array activeFishes;
    private Water waterSimulation;
    private Array sceneShaders;
    private Array depthShaders;
    private ShaderProgram waterShader;
    private FrameBuffer shadowFrameBuffer;
    private FrameBuffer sceneFrameBuffer;
    private int shadowQuality;
    private float viewportHalfWidth;
    private float viewportHalfHeight;
    private float viewportNearPlane;
    private float viewportFarPlane;
    private float viewportMidPlane;
    private SceneConfig sceneConfig;
    private final BoundingBox spawnBounds;
    private ModelInstance debugNoSpawnBox;
    private ModelInstance debugSpawnBox;
    private float cameraDistance;
    private float cameraAngleX;
    private float noSpawnAreaWidth;
    private float noSpawnAreaDepth;
    private float spawnAreaWidth;
    private float spawnAreaDepth;
    private float spawnYStart;
    private float spawnYIncrement;
    private float floorPosX;
    private float floorPosY;
    private float floorPosZ;
    private float floorRotationX;
    private float floorRotationY;
    private boolean debugMode;
    private boolean firstFrameAfterPause;
    private Environment environment;
    private PerspectiveCamera lightCamera;
    private PerspectiveCamera sceneCamera;
    private OrthographicCamera orthoCamera;
    private ModelBatch defaultBatch;

    private class Fish {
        private ModelInstance modelInstance;
        private AnimationController animController;
        private ModelInstance shadowInstance;
        private AnimationController shadowAnimController;
        private Vector3 startPos;
        private Vector3 targetPos;
        private Vector3 currentPos;
        private float heading;
        private float turnRate;
        private float baseSpeed;
        private float currentSpeed;
        private ModelInstance debugArrow;

        public boolean isOutOfBounds() {
            return this.currentPos.x < SceneFishesAppListener.this.spawnBounds.min.x || this.currentPos.z < SceneFishesAppListener.this.spawnBounds.min.z || this.currentPos.x > SceneFishesAppListener.this.spawnBounds.max.x || this.currentPos.z > SceneFishesAppListener.this.spawnBounds.max.z;
        }

        public void resetPath() {
            float f2;
            double d2;
            double d3;
            ModelInstance modelInstance;
            this.turnRate = ((((float) Math.random()) * 2.0f) - 1.0f) * 0.02f;
            this.baseSpeed = (((float) Math.random()) * 30.0f) + 50.0f;
            float f3 = -(SceneFishesAppListener.this.spawnAreaWidth / 2.0f);
            float f4 = SceneFishesAppListener.this.spawnAreaWidth / 2.0f;
            BoundingBox boundingBox = new BoundingBox(new Vector3(-(SceneFishesAppListener.this.noSpawnAreaWidth / 2.0f), SceneFishesAppListener.this.spawnYStart, -(SceneFishesAppListener.this.noSpawnAreaDepth / 2.0f)), new Vector3(SceneFishesAppListener.this.noSpawnAreaWidth / 2.0f, SceneFishesAppListener.this.spawnYStart + 5000.0f, SceneFishesAppListener.this.noSpawnAreaDepth / 2.0f));
            do {
                f2 = f4 - f3;
                d2 = f2 + 1.0f;
                d3 = f3;
                this.startPos.x = (float) Math.floor((Math.random() * d2) + d3);
                this.startPos.z = (float) Math.floor((Math.random() * d2) + d3);
                if (!SceneFishesAppListener.this.sceneCamera.frustum.pointInFrustum(this.startPos)) {
                    break;
                }
            } while (boundingBox.contains(this.startPos));
            do {
                this.targetPos.x = (float) Math.floor((Math.random() * d2) + d3);
                this.targetPos.z = (float) Math.floor((Math.random() * d2) + d3);
            } while (!SceneFishesAppListener.this.sceneCamera.frustum.pointInFrustum(this.targetPos));
            Vector3 vector3 = new Vector3(this.targetPos);
            vector3.sub(this.startPos).nor();
            Vector3 vector32 = this.targetPos;
            Vector3 vector33 = this.startPos;
            vector32.x = vector33.x + (vector3.x * f2);
            vector32.z = vector33.z + (f2 * vector3.z);
            if (SceneFishesAppListener.this.debugMode) {
                this.debugArrow = new ModelInstance(new ModelBuilder().createArrow(this.startPos, this.targetPos, new Material(ColorAttribute.createDiffuse(Color.GREEN)), 9L));
            }
            float f5 = this.targetPos.z;
            Vector3 vector34 = this.startPos;
            this.heading = (float) Math.atan2((f5 - vector34.z) * (-1.0f), this.targetPos.x - vector34.x);
            ModelInstance modelInstance2 = this.modelInstance;
            if (modelInstance2 != null) {
                modelInstance2.transform.setToTranslation(this.startPos);
                this.modelInstance.transform.getTranslation(this.currentPos);
                this.modelInstance.transform.rotate(new Vector3(0.0f, 1.0f, 0.0f), (float) Math.toDegrees(this.heading + 1.5707963267948966d));
            }
            if (SceneFishesAppListener.this.shadowQuality <= 0 || (modelInstance = this.shadowInstance) == null) {
                return;
            }
            modelInstance.transform.setToTranslation(this.startPos);
            this.shadowInstance.transform.getTranslation(this.currentPos);
            this.shadowInstance.transform.rotate(new Vector3(0.0f, 1.0f, 0.0f), (float) Math.toDegrees(this.heading + 1.5707963267948966d));
        }

        public void startleBoost() {
            this.currentSpeed = 350.0f;
        }

        public void update(float f2) {
            ModelInstance modelInstance;
            AnimationController animationController = this.animController;
            if (animationController != null) {
                float f3 = this.currentSpeed;
                float f4 = this.baseSpeed;
                animationController.update((f3 > f4 ? f3 / f4 : 1.0f) * f2);
            }
            AnimationController animationController2 = this.shadowAnimController;
            if (animationController2 != null) {
                float f5 = this.currentSpeed;
                float f6 = this.baseSpeed;
                animationController2.update((f5 > f6 ? f5 / f6 : 1.0f) * f2);
            }
            if (isOutOfBounds()) {
                return;
            }
            float f7 = this.heading;
            Vector3 vector3 = new Vector3();
            float f8 = this.currentSpeed;
            float f9 = this.baseSpeed;
            if (f8 > f9) {
                f9 = f8;
            }
            this.currentSpeed = f8 - (f8 * f2);
            float f10 = this.heading + (this.turnRate * f2);
            this.heading = f10;
            vector3.x = ((float) Math.cos(f10)) * f9 * f2;
            vector3.z = ((float) Math.sin(this.heading)) * (-1.0f) * f9 * f2;
            ModelInstance modelInstance2 = this.modelInstance;
            if (modelInstance2 != null) {
                modelInstance2.transform.rotate(new Vector3(0.0f, 1.0f, 0.0f), -((float) Math.toDegrees(f7 + 1.5707963267948966d)));
                this.modelInstance.transform.translate(vector3);
                this.modelInstance.transform.getTranslation(this.currentPos);
                this.modelInstance.transform.rotate(new Vector3(0.0f, 1.0f, 0.0f), (float) Math.toDegrees(this.heading + 1.5707963267948966d));
            }
            if (SceneFishesAppListener.this.shadowQuality <= 0 || (modelInstance = this.shadowInstance) == null) {
                return;
            }
            modelInstance.transform.rotate(new Vector3(0.0f, 1.0f, 0.0f), -((float) Math.toDegrees(f7 + 1.5707963267948966d)));
            this.shadowInstance.transform.translate(vector3);
            this.shadowInstance.transform.getTranslation(this.currentPos);
            this.shadowInstance.transform.rotate(new Vector3(0.0f, 1.0f, 0.0f), (float) Math.toDegrees(this.heading + 1.5707963267948966d));
        }

        private Fish(Model model, Model model2, float f2) {
            if (model != null) {
                ModelInstance modelInstance = new ModelInstance(model);
                this.modelInstance = modelInstance;
                modelInstance.transform.translate(0.0f, 0.0f, 0.0f);
                Array.ArrayIterator<Material> it = this.modelInstance.materials.iterator();
                while (it.hasNext()) {
                    it.next().set(new BlendingAttribute());
                }
                AnimationController animationController = new AnimationController(this.modelInstance);
                this.animController = animationController;
                Array<Animation> array = this.modelInstance.animations;
                if (array.size > 0) {
                    animationController.setAnimation(array.get(0).id, -1);
                }
            }
            if (model2 != null) {
                ModelInstance modelInstance2 = new ModelInstance(model2);
                this.shadowInstance = modelInstance2;
                modelInstance2.transform.translate(0.0f, 0.0f, 0.0f);
                AnimationController animationController2 = new AnimationController(this.shadowInstance);
                this.shadowAnimController = animationController2;
                Array<Animation> array2 = this.shadowInstance.animations;
                if (array2.size > 0) {
                    animationController2.setAnimation(array2.get(0).id, -1);
                }
            }
            this.startPos = new Vector3(0.0f, f2, 0.0f);
            this.targetPos = new Vector3(0.0f, f2, 0.0f);
            this.currentPos = new Vector3();
        }
    }

    private static class FishResource {
        static final FishResource GOLDFISH1;
        static final FishResource GOLDFISH2;
        static final FishResource BLUE_YELLOW;
        static final FishResource TIGER_FISH;
        static final FishResource OIKAWA;
        static final FishResource[] ALL_DEFAULTS;
        String modelName;
        String shadowModelName;

        public static final class Builder {
            private String modelName;
            private String shadowModelName;

            private Builder() {
            }

            public static Builder create() {
                return new Builder();
            }

            public FishResource build() {
                return new FishResource(this);
            }

            public Builder modelName(String str) {
                this.modelName = str;
                return this;
            }

            public Builder shadowModelName(String str) {
                this.shadowModelName = str;
                return this;
            }
        }

        static {
            FishResource goldfish1 = Builder.create().modelName("goldfish1").shadowModelName("goldfish1_shadow").build();
            GOLDFISH1 = goldfish1;
            FishResource goldfish2 = Builder.create().modelName("goldfish2").shadowModelName("goldfish2_shadow").build();
            GOLDFISH2 = goldfish2;
            FishResource blueYellow = Builder.create().modelName("blueyellow").shadowModelName("blueyellow_shadow").build();
            BLUE_YELLOW = blueYellow;
            FishResource tigerFish = Builder.create().modelName("tigerfish").shadowModelName("tigerfish_shadow").build();
            TIGER_FISH = tigerFish;
            FishResource oikawa = Builder.create().modelName("oikawa").shadowModelName("oikawa_shadow").build();
            OIKAWA = oikawa;
            ALL_DEFAULTS = new FishResource[]{goldfish1, goldfish2, blueYellow, tigerFish, oikawa};
        }

        private FishResource(Builder builder) {
            this.modelName = builder.modelName;
            this.shadowModelName = builder.shadowModelName;
        }
    }

    private class Water {
        private int screenWidth;
        private int screenHeight;
        private int[] waveBuffer1;
        private int[] waveBuffer2;
        private int[] smoothedBuffer;
        private ByteBuffer textureData;
        private int textureId;
        private boolean isDirty;
        private float pixelSize;
        private long lastRippleTime;

        public void addRippleAtScreenPos(float f2, float f3) {
            addRipple((f2 * 0.6f) + ((this.screenWidth * 0.39999998f) / 2.0f), (f3 * 0.6f) + ((this.screenHeight * 0.39999998f) / 2.0f));
        }

        private void addRipple(float f2, float f3) {
            int i2 = (int) ((f2 / this.screenWidth) * 47.0f);
            int i3 = (int) ((1.0f - (f3 / this.screenHeight)) * 47.0f);
            int iMax = Math.max(1, Math.min(46, i3 + 1));
            int iMax2 = Math.max(1, Math.min(46, i2 - 1));
            int iMax3 = Math.max(1, Math.min(46, i2 + 1));
            for (int iMax4 = Math.max(1, Math.min(46, i3 - 1)); iMax4 <= iMax; iMax4++) {
                for (int i4 = iMax2; i4 <= iMax3; i4++) {
                    int i5 = (iMax4 * 48) + i4;
                    this.waveBuffer1[i5] = 2260;
                    this.waveBuffer2[i5] = 2260;
                }
            }
        }

        public void renderWaterOverlay() {
            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE1);
            Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, this.textureId);
            if (this.isDirty) {
                this.isDirty = false;
                for (int i2 = 0; i2 < 48; i2++) {
                    int i3 = 0;
                    while (i3 < 48) {
                        int iMax = Math.max(0, Math.min(47, i2 + 1));
                        int iMax2 = Math.max(0, Math.min(47, i3 - 1));
                        int i4 = i3 + 1;
                        int iMax3 = Math.max(0, Math.min(47, i4));
                        int i5 = 0;
                        int i6 = 0;
                        for (int iMax4 = Math.max(0, Math.min(47, i2 - 1)); iMax4 <= iMax; iMax4++) {
                            for (int i7 = iMax2; i7 <= iMax3; i7++) {
                                i5 += this.waveBuffer2[(iMax4 * 48) + i7];
                                i6++;
                            }
                        }
                        this.smoothedBuffer[(i2 * 48) + i3] = i5 / (i6 + 3);
                        i3 = i4;
                    }
                }
                for (int i8 = 0; i8 < 48; i8++) {
                    for (int i9 = 0; i9 < 48; i9++) {
                        int i10 = (i8 * 48) + i9;
                        this.textureData.put(i10, (byte) (((this.smoothedBuffer[i10] + 3046) * 255) / 6159));
                    }
                }
                Gdx.gl.glTexSubImage2D(GL20.GL_TEXTURE_2D, 0, 0, 0, 48, 48, GL20.GL_LUMINANCE, 5121, this.textureData);
            }
            Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            Gdx.gl.glClear(16384);
            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
            SceneFishesAppListener.this.waterShader.begin();
            SceneFishesAppListener.this.waterShader.setUniformi("u_map", 1);
            SceneFishesAppListener.this.waterShader.setUniformf("u_map_size", 0.6f);
            SceneFishesAppListener.this.waterShader.setUniformf("u_pixel", this.pixelSize);
            SceneFishesAppListener.this.waterShader.end();
            SceneFishesAppListener.this.orthoCamera.update();
            SceneFishesAppListener.this.waterSpriteBatch.setProjectionMatrix(SceneFishesAppListener.this.orthoCamera.combined);
            SceneFishesAppListener.this.waterSpriteBatch.begin();
            SceneFishesAppListener.this.waterSpriteBatch.draw(SceneFishesAppListener.this.sceneFrameBuffer.getColorBufferTexture(), 0.0f, 0.0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            SceneFishesAppListener.this.waterSpriteBatch.end();
        }

        public void updateSimulation(float f2) {
            long jCurrentTimeMillis = System.currentTimeMillis();
            if (jCurrentTimeMillis - this.lastRippleTime > 2000.0f) {
                if (Math.random() > 0.5d) {
                    addRipple(((double) ((float) Math.random())) > 0.5d ? Gdx.graphics.getWidth() : 0, ((float) Math.random()) * Gdx.graphics.getHeight());
                } else {
                    addRipple(((float) Math.random()) * Gdx.graphics.getWidth(), ((double) ((float) Math.random())) > 0.5d ? Gdx.graphics.getHeight() : 0);
                }
                this.lastRippleTime = jCurrentTimeMillis;
            }
            for (int i2 = 1; i2 < 47; i2++) {
                for (int i3 = 1; i3 < 47; i3++) {
                    int i4 = (i2 * 48) + i3;
                    int[] iArr = this.waveBuffer1;
                    int i5 = (((iArr[i4 - 1] + iArr[i4 + 1]) + iArr[i4 - 48]) + iArr[i4 + 48]) >> 1;
                    int[] iArr2 = this.waveBuffer2;
                    int i6 = iArr2[i4];
                    float f3 = i5 - i6;
                    int i7 = (int) (f3 - (f3 * f2));
                    if (i7 <= -3046) {
                        i7 = -3046;
                    } else if (i7 > 3113) {
                        i7 = 3113;
                    }
                    if (i6 != i7) {
                        this.isDirty = true;
                    }
                    iArr2[i4] = i7;
                }
            }
            int[] iArr3 = this.waveBuffer1;
            this.waveBuffer1 = this.waveBuffer2;
            this.waveBuffer2 = iArr3;
        }

        private Water() {
            this.screenWidth = Gdx.graphics.getWidth();
            this.screenHeight = Gdx.graphics.getHeight();
            this.waveBuffer1 = new int[GL20.GL_CW];
            this.waveBuffer2 = new int[GL20.GL_CW];
            this.smoothedBuffer = new int[GL20.GL_CW];
            this.textureData = ByteBuffer.allocateDirect(GL20.GL_CW);
            int iGlGenTexture = Gdx.gl.glGenTexture();
            this.textureId = iGlGenTexture;
            Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, iGlGenTexture);
            Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAG_FILTER, 9729.0f);
            Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MIN_FILTER, 9729.0f);
            Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_S, GL20.GL_CLAMP_TO_EDGE);
            Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_T, GL20.GL_CLAMP_TO_EDGE);
            Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_2D, 0, GL20.GL_LUMINANCE, 48, 48, 0, GL20.GL_LUMINANCE, 5121, null);
            this.isDirty = true;
            this.pixelSize = 0.03472222f;
        }
    }

    public SceneFishesAppListener(String str, LiveWallpaperConfig liveWallpaperConfig, Context context) {
        super(str, liveWallpaperConfig, context);
        this.fishResources = new ArrayList();
        this.shadowQuality = 100;
        this.sceneConfig = SceneConfig.EMPTY;
        this.spawnBounds = new BoundingBox();
        this.cameraDistance = 1000.0f;
        this.cameraAngleX = -15.0f;
        this.noSpawnAreaWidth = 600.0f;
        this.noSpawnAreaDepth = 1600.0f;
        this.spawnAreaWidth = 1000.0f;
        this.spawnAreaDepth = 2000.0f;
        this.spawnYStart = -150.0f;
        this.spawnYIncrement = 75.0f;
        this.floorPosX = 0.0f;
        this.floorPosY = -500.0f;
        this.floorPosZ = 100.0f;
        this.floorRotationX = 0.0f;
        this.floorRotationY = 90.0f;
        this.debugMode = false;
        this.firstFrameAfterPause = true;
    }

    private void initScene() {
        Model bgModel = getModel("BG");
        if (bgModel != null) {
            ModelInstance modelInstance = new ModelInstance(bgModel);
            this.backgroundModel = modelInstance;
            
            // Get wallpaper type to apply appropriate scaling and positioning
            String wallpaperType = WallpaperSelectionManager.getWallpaperIdFromPath(this.wallpaperDiskPath);
            
            if (WallpaperSelectionManager.WALLPAPER_GOLDFISH.equals(wallpaperType)) {
                // Goldfish wallpaper - use original scale
                modelInstance.transform.translate(this.floorPosX, this.floorPosY, this.floorPosZ);
                Log.d("SceneFishes", "Goldfish background - original scale");
            } else {
                // Clownfish wallpaper - needs different positioning
                modelInstance.transform.scl(1.0f, 1.0f, 1.0f); // Keep original scale
                modelInstance.transform.translate(this.floorPosX, this.floorPosY - 100, this.floorPosZ);
                Log.d("SceneFishes", "Clownfish background - adjusted position");
            }
            
            this.backgroundModel.transform.rotate(1.0f, 0.0f, 0.0f, this.floorRotationX);
            this.backgroundModel.transform.rotate(0.0f, 1.0f, 0.0f, this.floorRotationY);
            
            Log.d("SceneFishes", "Background model loaded for " + wallpaperType + 
                  " at position: " + this.floorPosX + ", " + this.floorPosY + ", " + this.floorPosZ + 
                  " with materials: " + bgModel.materials.size);
        } else {
            Log.e("SceneFishes", "Failed to load BG model - model is null");
        }
        
        float f2 = this.spawnYStart;
        for (FishResource fishResource : this.fishResources) {
            Model fishModel = getModel(fishResource.modelName);
            Model shadowModel = getModel(fishResource.shadowModelName);
            if (fishModel != null) {
                this.idleFishes.add(new Fish(fishModel, shadowModel, f2));
                f2 += this.spawnYIncrement;
                Log.d("SceneFishes", "Fish model loaded: " + fishResource.modelName);
            } else {
                Log.e("SceneFishes", "Failed to load fish model: " + fishResource.modelName);
            }
        }
        Log.d("SceneFishes", "Scene initialized with " + this.idleFishes.size + " fish");
        this.isLoading = false;
    }

    private Model getModel(String str) {
        if (this.assetManager.contains(str + ".gltf", SceneAsset.class)) {
            SceneAsset sceneAsset = (SceneAsset) this.assetManager.get(str + ".gltf", SceneAsset.class);
            Log.d("SceneFishes", "Loaded GLTF model: " + str + ".gltf");
            return sceneAsset.scene.model;
        }
        if (this.assetManager.contains(str + ".g3db", Model.class)) {
            Model model = (Model) this.assetManager.get(str + ".g3db", Model.class);
            Log.d("SceneFishes", "Loaded G3DB model: " + str + ".g3db");
            return model;
        }
        Log.e("SceneFishes", "Model not found: " + str + " (checked both .gltf and .g3db)");
        return null;
    }

    private void loadModel(String str) {
        if (this.fileHandleResolver.resolve(str + ".gltf").exists()) {
            this.assetManager.load(str + ".gltf", SceneAsset.class);
            Log.d("SceneFishes", "Loading GLTF model: " + str + ".gltf");
            return;
        }
        if (this.fileHandleResolver.resolve(str + ".g3db").exists()) {
            this.assetManager.load(str + ".g3db", Model.class);
            Log.d("SceneFishes", "Loading G3DB model: " + str + ".g3db");
        } else {
            Log.e("SceneFishes", "Model file not found: " + str + " (checked both .gltf and .g3db)");
        }
    }

    private boolean modelExists(String str) {
        if (!this.fileHandleResolver.resolve(str + ".gltf").exists()) {
            if (!this.fileHandleResolver.resolve(str + ".g3db").exists()) {
                return false;
            }
        }
        return true;
    }

    private void loadSceneConfig() {
        if (new File(this.wallpaperDiskPath, SceneConfig.FILE_NAME).exists()) {
            this.sceneConfig = LiveWallpaperConfigReader.readSceneConfig(new File(this.wallpaperDiskPath, SceneConfig.FILE_NAME));
        } else {
            Log.d("SceneFishes", "Scene file is missing. Using default values.");
        }
        SceneConfig config = this.sceneConfig;
        this.cameraDistance = config.cameraDistance;
        this.cameraAngleX = config.cameraAngleX;
        this.spawnAreaWidth = config.spawnAreaWidth;
        this.spawnAreaDepth = config.spawnAreaDepth;
        this.noSpawnAreaWidth = config.noSpawnAreaWidth;
        this.noSpawnAreaDepth = config.noSpawnAreaDepth;
        this.spawnYStart = config.spawnYStart;
        this.spawnYIncrement = config.spawnYIncrement;
        this.floorPosX = config.floorPosX;
        this.floorPosY = config.floorPosY;
        this.floorPosZ = config.floorPosZ;
        this.floorRotationX = config.floorRotationX;
        this.floorRotationY = config.floorRotationY;
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void renderShadowMap() {
        if (this.shadowFrameBuffer == null) {
            this.shadowFrameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, 1024, 1024, true);
        }
        this.shadowFrameBuffer.begin();
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        Gdx.gl.glClear(16640);
        Array.ArrayIterator it = this.depthShaders.iterator();
        while (it.hasNext()) {
            ShaderProgram shaderProgram = (ShaderProgram) it.next();
            shaderProgram.begin();
            shaderProgram.setUniformf("u_cameraFar", this.lightCamera.far);
            shaderProgram.setUniformf("u_lightPosition", this.lightCamera.position);
            shaderProgram.end();
        }
        this.shadowGenBatch.begin(this.lightCamera);
        Array.ArrayIterator it2 = this.activeFishes.iterator();
        while (it2.hasNext()) {
            this.shadowGenBatch.render(((Fish) it2.next()).shadowInstance);
        }
        this.shadowGenBatch.end();
        this.shadowFrameBuffer.end();
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void renderScene() {
        ModelInstance modelInstance;
        ModelInstance modelInstance2;
        if (this.sceneFrameBuffer == null) {
            this.sceneFrameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        }
        this.sceneFrameBuffer.begin();
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        Gdx.gl.glClear(16640);
        
        // Debug GL state
        Log.v("SceneFishes", "GL depth test enabled: " + Gdx.gl.glIsEnabled(GL20.GL_DEPTH_TEST));
        Log.v("SceneFishes", "GL cull face enabled: " + Gdx.gl.glIsEnabled(GL20.GL_CULL_FACE));
        
        ModelBatch modelBatch = this.defaultBatch;
        if (this.shadowQuality > 0) {
            Array.ArrayIterator it = this.sceneShaders.iterator();
            while (it.hasNext()) {
                ShaderProgram shaderProgram = (ShaderProgram) it.next();
                shaderProgram.begin();
                this.shadowFrameBuffer.getColorBufferTexture().bind(2);
                shaderProgram.setUniformi("u_depthMap", 2);
                shaderProgram.setUniformMatrix("u_lightTrans", this.lightCamera.combined);
                shaderProgram.setUniformf("u_cameraFar", this.lightCamera.far);
                shaderProgram.setUniformf("u_lightPosition", this.lightCamera.position);
                shaderProgram.end();
            }
            modelBatch = this.shadowReceiveBatch;
        }
        modelBatch.begin(this.sceneCamera);
        ModelInstance modelInstance3 = this.backgroundModel;
        if (modelInstance3 != null) {
            // Debug texture information
            if (modelInstance3.materials.size > 0) {
                Log.v("SceneFishes", "Background materials count: " + modelInstance3.materials.size);
                for (int i = 0; i < modelInstance3.materials.size; i++) {
                    Material mat = modelInstance3.materials.get(i);
                    Log.v("SceneFishes", "Material " + i + ": " + mat.id + " with " + mat.size() + " attributes");
                }
            }
            
            // Disable depth writing for background to prevent z-fighting
            Gdx.gl.glDepthMask(false);
            modelBatch.render(modelInstance3, this.environment);
            Gdx.gl.glDepthMask(true);
            
            Log.v("SceneFishes", "Background rendered with environment");
        } else {
            Log.w("SceneFishes", "Background model is null, not rendering");
        }
        modelBatch.end();
        Gdx.gl.glClear(256);
        // Second pass: fish rendered with defaultBatch (no shadow reception)
        // Shadows only appear on the background/floor, not on the fish themselves
        this.defaultBatch.begin(this.sceneCamera);
        if (this.debugMode && (modelInstance2 = this.debugSpawnBox) != null) {
            this.defaultBatch.render(modelInstance2);
        }
        if (this.debugMode && (modelInstance = this.debugNoSpawnBox) != null) {
            this.defaultBatch.render(modelInstance);
        }
        Array.ArrayIterator it2 = this.activeFishes.iterator();
        while (it2.hasNext()) {
            Fish fish = (Fish) it2.next();
            this.defaultBatch.render(fish.modelInstance, this.environment);
            if (fish.debugArrow != null) {
                this.defaultBatch.render(fish.debugArrow, this.environment);
            }
        }
        this.defaultBatch.end();
        this.sceneFrameBuffer.end();
    }

    private ShaderProgram compileShader(String str, String str2) {
        ShaderProgram.pedantic = false;
        String string = this.fileHandleResolver.resolve(str + "_v.glsl").readString();
        String string2 = this.fileHandleResolver.resolve(str + "_f.glsl").readString();
        StringBuilder sb = new StringBuilder();
        sb.append(str2);
        sb.append(string);
        ShaderProgram shaderProgram = new ShaderProgram(sb.toString(), str2 + string2);
        if (shaderProgram.isCompiled()) {
            Log.d("SceneFishes", "Shader " + str + " compiled " + shaderProgram.getLog());
        } else {
            Log.d("SceneFishes", "Error with shader " + str + ": " + shaderProgram.getLog());
        }
        return shaderProgram;
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void updateScene() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        if (this.firstFrameAfterPause) {
            Log.d("SceneFishes", "updateScene > firstFrameAfterPause deltaTime " + deltaTime);
            FirebaseHelper.logDebug("SceneFishes", "updateScene > firstFrameAfterPause deltaTime " + deltaTime);
            if (deltaTime == 0.0f) {
                FirebaseHelper.logDebug("SceneFishes", "updateScene > firstFrameAfterPause > skipping update. deltaTime == 0");
                return;
            }
        }
        if (this.idleFishes.size > 0) {
            double dRandom = Math.random();
            int i2 = (int) (dRandom * this.idleFishes.size);
            Fish fish = (Fish) this.idleFishes.get(i2);
            fish.resetPath();
            this.activeFishes.add(fish);
            this.idleFishes.removeIndex(i2);
        }
        Array.ArrayIterator it = this.activeFishes.iterator();
        while (it.hasNext()) {
            Fish fish2 = (Fish) it.next();
            fish2.update(deltaTime);
            if (fish2.isOutOfBounds()) {
                this.activeFishes.removeValue(fish2, false);
                this.idleFishes.add(fish2);
            }
        }
        this.waterSimulation.updateSimulation(deltaTime);
    }

    @Override // com.wave.livewallpaper.libgdx.BaseAppListener, com.badlogic.gdx.ApplicationListener
    public void create() {
        super.create();
        Log.d("SceneFishes", "gdx  w " + Gdx.graphics.getWidth() + " h " + Gdx.graphics.getHeight());
        loadSceneConfig();

        // Environment setup
        Environment environment = new Environment();
        this.environment = environment;
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 1.0f, 1.0f, 1.0f, 1.0f));

        // Light camera for shadow mapping
        PerspectiveCamera lightCam = new PerspectiveCamera(30.0f, 1000.0f, 1000.0f);
        this.lightCamera = lightCam;
        lightCam.position.set(-200.0f, 2000.0f, -200.0f);
        lightCam.lookAt(0.0f, 0.0f, 0.0f);
        lightCam.near = 1.0f;
        lightCam.far = 3000.0f;
        lightCam.update();

        // Main scene camera
        PerspectiveCamera sceneCam = new PerspectiveCamera();
        this.sceneCamera = sceneCam;
        sceneCam.position.set(0.0f, this.cameraDistance, 0.0f);
        sceneCam.lookAt(0.0f, 0.0f, 0.0f);
        sceneCam.rotateAround(Vector3.Zero, Vector3.X, this.cameraAngleX);
        sceneCam.near = 1.0f;
        sceneCam.far = 5000.0f; // Increase far plane to see background better
        sceneCam.update();
        
        Log.d("SceneFishes", "Camera setup - Distance: " + this.cameraDistance + 
              ", Angle: " + this.cameraAngleX + ", Far: " + sceneCam.far);

        // Orthographic camera for water overlay
        float screenW = (float) Gdx.graphics.getWidth();
        float screenH = (float) Gdx.graphics.getHeight();
        OrthographicCamera orthoCam = new OrthographicCamera(screenW, screenH);
        this.orthoCamera = orthoCam;
        orthoCam.position.set(screenW / 2.0f, screenH / 2.0f, 0.0f);
        orthoCam.update();

        // Shader program arrays (populated by custom ShaderProviders below)
        this.sceneShaders = new Array();
        this.depthShaders = new Array();

        // Model batches
        // defaultBatch uses default shader with numBones=20 for GLTF skeletons
        DefaultShader.Config defaultConfig = new DefaultShader.Config();
        defaultConfig.numBones = 20;
        this.defaultBatch = new ModelBatch(new DefaultShaderProvider(defaultConfig));

        // shadowReceiveBatch — Custom ShaderProvider using scene GLSL, registers programs into sceneShaders
        final SceneFishesAppListener self = this;
        final String sceneVertSrc = this.fileHandleResolver.resolve("scene_v.glsl").readString();
        final String sceneFragSrc = this.fileHandleResolver.resolve("scene_f.glsl").readString();
        final DefaultShader.Config sceneConfig = new DefaultShader.Config();
        sceneConfig.numBones = 20;
        sceneConfig.vertexShader = sceneVertSrc;
        sceneConfig.fragmentShader = sceneFragSrc;
        this.shadowReceiveBatch = new ModelBatch(new DefaultShaderProvider(sceneConfig) {
            @Override
            protected com.badlogic.gdx.graphics.g3d.Shader createShader(Renderable renderable) {
                DefaultShader shader = new DefaultShader(renderable, sceneConfig);
                self.sceneShaders.add(shader.program);
                return shader;
            }
        });

        // shadowGenBatch — Custom ShaderProvider using depthmap GLSL, registers programs into depthShaders
        final String depthVertSrc = this.fileHandleResolver.resolve("depthmap_v.glsl").readString();
        final String depthFragSrc = this.fileHandleResolver.resolve("depthmap_f.glsl").readString();
        final DefaultShader.Config depthConfig = new DefaultShader.Config();
        depthConfig.numBones = 20;
        depthConfig.vertexShader = depthVertSrc;
        depthConfig.fragmentShader = depthFragSrc;
        this.shadowGenBatch = new ModelBatch(new DefaultShaderProvider(depthConfig) {
            @Override
            protected com.badlogic.gdx.graphics.g3d.Shader createShader(Renderable renderable) {
                DefaultShader shader = new DefaultShader(renderable, depthConfig);
                self.depthShaders.add(shader.program);
                return shader;
            }
        });

        // Water shader (compiled outside shadow quality check — always available)
        ShaderProgram waterShader = compileShader("water", "");
        this.waterShader = waterShader;
        this.waterSpriteBatch = new SpriteBatch(1000, this.waterShader);

        // Water simulation
        this.waterSimulation = new Water();

        // Fish arrays
        this.idleFishes = new Array();
        this.activeFishes = new Array();

        // GLTF/GLB asset loaders are auto-registered by gdx-gltf library dependency

        // Load BG model
        loadModel("BG");

        // Load known fish resources
        this.fishResources.clear();
        for (FishResource fishResource : FishResource.ALL_DEFAULTS) {
            if (modelExists(fishResource.modelName)) {
                this.fishResources.add(fishResource);
            }
        }

        // Load additional numbered fish (fish1, fish2, etc.)
        int fishIndex = 0;
        boolean fishExists;
        do {
            fishIndex++;
            String fishName = "fish" + fishIndex;
            fishExists = this.fileHandleResolver.resolve(fishName + ".gltf").exists()
                      || this.fileHandleResolver.resolve(fishName + ".g3db").exists();
            if (fishExists) {
                FishResource fr = FishResource.Builder.create().modelName(fishName).shadowModelName(fishName + "_shadow").build();
                this.fishResources.add(fr);
            }
        } while (fishExists);

        // Load fish models
        for (Object obj : this.fishResources) {
            FishResource fr = (FishResource) obj;
            loadModel(fr.modelName);
        }

        // Load shadow models and determine shadow support
        boolean hasShadows = false;
        for (Object obj : this.fishResources) {
            FishResource fr = (FishResource) obj;
            if (this.fileHandleResolver.resolve(fr.shadowModelName + ".gltf").exists()) {
                this.assetManager.load(fr.shadowModelName + ".gltf", SceneAsset.class);
                hasShadows = true;
            } else if (this.fileHandleResolver.resolve(fr.shadowModelName + ".g3db").exists()) {
                this.assetManager.load(fr.shadowModelName + ".g3db", Model.class);
                hasShadows = true;
            }
        }
        this.shadowQuality = hasShadows ? 100 : 0;

        // Mark as loading
        this.isLoading = true;

        // Bounding box for fish spawn area
        this.spawnBounds.clr();
        this.spawnBounds.set(
            new Vector3(-(this.spawnAreaWidth / 2.0f), this.spawnYStart, -(this.spawnAreaDepth / 2.0f)),
            new Vector3(this.spawnAreaWidth / 2.0f, this.spawnYStart + 1000.0f, this.spawnAreaDepth / 2.0f)
        );

        // Debug visualization
        ModelBuilder modelBuilder = new ModelBuilder();
        if (this.debugMode) {
            this.debugNoSpawnBox = new ModelInstance(modelBuilder.createBox(
                this.noSpawnAreaWidth / 2.0f, 4.0f, this.noSpawnAreaDepth / 2.0f,
                new Material(ColorAttribute.createDiffuse(Color.BLACK)), 9L));
            this.debugNoSpawnBox.transform.setTranslation(new Vector3(0.0f, this.spawnYStart, 0.0f));
        }
        if (this.debugMode) {
            this.debugSpawnBox = new ModelInstance(modelBuilder.createBox(
                this.spawnAreaWidth / 2.0f, 2.0f, this.spawnAreaDepth / 2.0f,
                new Material(ColorAttribute.createDiffuse(Color.BLUE)), 9L));
            this.debugSpawnBox.transform.setTranslation(new Vector3(0.0f, this.spawnYStart, 0.0f));
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.wave.livewallpaper.libgdx.BaseAppListener, com.badlogic.gdx.ApplicationListener
    public void dispose() {
        ModelBatch modelBatch = this.defaultBatch;
        if (modelBatch != null) {
            modelBatch.dispose();
        }
        ModelBatch modelBatch2 = this.shadowReceiveBatch;
        if (modelBatch2 != null) {
            modelBatch2.dispose();
        }
        ModelBatch modelBatch3 = this.shadowGenBatch;
        if (modelBatch3 != null) {
            modelBatch3.dispose();
        }
        SpriteBatch spriteBatch = this.waterSpriteBatch;
        if (spriteBatch != null) {
            spriteBatch.dispose();
        }
        AssetManager assetManager = this.assetManager;
        if (assetManager != null) {
            assetManager.dispose();
        }
        FrameBuffer frameBuffer = this.shadowFrameBuffer;
        if (frameBuffer != null) {
            frameBuffer.dispose();
        }
        FrameBuffer frameBuffer2 = this.sceneFrameBuffer;
        if (frameBuffer2 != null) {
            frameBuffer2.dispose();
        }
        Array array = this.idleFishes;
        if (array != null) {
            array.clear();
        }
        Array array2 = this.activeFishes;
        if (array2 != null) {
            array2.clear();
        }
        Array array3 = this.sceneShaders;
        if (array3 != null) {
            Array.ArrayIterator it = array3.iterator();
            while (it.hasNext()) {
                ((ShaderProgram) it.next()).dispose();
            }
            this.sceneShaders.clear();
        }
        Array array4 = this.depthShaders;
        if (array4 != null) {
            Array.ArrayIterator it2 = array4.iterator();
            while (it2.hasNext()) {
                ((ShaderProgram) it2.next()).dispose();
            }
            this.depthShaders.clear();
        }
        ShaderProgram shaderProgram = this.waterShader;
        if (shaderProgram != null) {
            shaderProgram.dispose();
        }
        super.dispose();
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.wave.livewallpaper.libgdx.BaseAppListener
    public void onTouchEvent(MotionEvent motionEvent) {
        if (this.activeFishes == null) {
            return;
        }
        float width = (Gdx.graphics.getWidth() > Gdx.graphics.getHeight() ? Gdx.graphics.getWidth() : Gdx.graphics.getHeight()) / 5.0f;
        Array.ArrayIterator it = this.activeFishes.iterator();
        while (it.hasNext()) {
            Fish fish = (Fish) it.next();
            Vector3 projected = this.sceneCamera.project(new Vector3(fish.currentPos));
            if (Math.sqrt(Math.pow(projected.x - motionEvent.getX(), 2.0d) + Math.pow(projected.y - motionEvent.getY(), 2.0d)) < width) {
                fish.startleBoost();
            }
        }
        this.waterSimulation.addRippleAtScreenPos(motionEvent.getX(), Gdx.graphics.getHeight() - motionEvent.getY());
        super.onTouchEvent(motionEvent);
    }

    @Override // com.wave.livewallpaper.libgdx.BaseAppListener, com.badlogic.gdx.backends.android.AndroidWallpaperListener
    public void offsetChange(float f2, float f3, float f4, float f5, int i2, int i3) {
    }

    @Override // com.wave.livewallpaper.libgdx.BaseAppListener, com.badlogic.gdx.ApplicationListener
    public void pause() {
        this.firstFrameAfterPause = true;
        super.pause();
    }

    @Override // com.wave.livewallpaper.libgdx.BaseAppListener, com.badlogic.gdx.backends.android.AndroidWallpaperListener
    public void previewStateChange(boolean z2) {
        super.previewStateChange(z2);
    }

    @Override // com.wave.livewallpaper.libgdx.BaseAppListener, com.badlogic.gdx.ApplicationListener
    public synchronized void render() {
        try {
            if (this.isLoading) {
                LibgdxUtils.clearScreen();
                if (this.assetManager.update()) {
                    initScene();
                }
            }
            if (!this.isLoading) {
                updateScene();
                if (this.shadowQuality > 0) {
                    renderShadowMap();
                }
                renderScene();
                this.waterSimulation.renderWaterOverlay();
                if (Gdx.graphics.getFramesPerSecond() < 30) {
                    this.shadowQuality--;
                } else if (this.shadowQuality > 0) {
                    this.shadowQuality = 100;
                }
                if (this.firstFrameAfterPause) {
                    this.firstFrameAfterPause = false;
                }
            }
            super.render();
        } catch (Throwable th) {
            throw th;
        }
    }

    @Override // com.wave.livewallpaper.libgdx.BaseAppListener, com.badlogic.gdx.ApplicationListener
    public void resize(int i2, int i3) {
        float f2;
        float f3;
        float f4;
        // Recreate FrameBuffers for new screen size
        if (this.sceneFrameBuffer != null) {
            this.sceneFrameBuffer.dispose();
            this.sceneFrameBuffer = null;
        }
        if (this.shadowFrameBuffer != null) {
            this.shadowFrameBuffer.dispose();
            this.shadowFrameBuffer = null;
        }
        super.resize(i2, i3);
        Log.d("SceneFishes", "gdx  resize w " + i2 + " h " + i3);
        float f5 = (float) i2;
        float f6 = (float) i3;
        float f7 = f5 / f6;
        if (i2 < i3) {
            f2 = f7 * 1000.0f;
            f4 = 30.0f;
            f3 = 1000.0f;
        } else {
            float f8 = 1000.0f / f7;
            float degrees = (float) Math.toDegrees(Math.atan(Math.tan(Math.toRadians(15.0f)) / f7) * 2.0d);
            f2 = 1000.0f;
            f3 = f8;
            f4 = degrees;
        }
        PerspectiveCamera perspectiveCamera = this.sceneCamera;
        perspectiveCamera.fieldOfView = f4;
        perspectiveCamera.viewportWidth = f2;
        perspectiveCamera.viewportHeight = f3;
        perspectiveCamera.update();
        this.viewportHalfWidth = (f2 * 500.0f) / 1000.0f;
        this.viewportHalfHeight = (f3 * 500.0f) / 1000.0f;
        this.viewportNearPlane = 15.0f;
        this.viewportFarPlane = 155.0f;
        this.viewportMidPlane = 75.0f;
        OrthographicCamera orthographicCamera = this.orthoCamera;
        orthographicCamera.viewportWidth = f5;
        orthographicCamera.viewportHeight = f6;
        orthographicCamera.position.set(f5 / 2.0f, f6 / 2.0f, 0.0f);
        this.orthoCamera.update();
    }

    @Override // com.wave.livewallpaper.libgdx.BaseAppListener, com.badlogic.gdx.ApplicationListener
    public void resume() {
        super.resume();
    }
}
