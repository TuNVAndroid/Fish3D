package com.wave.livewallpaper.data;

import android.text.TextUtils;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes4.dex */
public class LiveWallpaperConfig {
    public static final LiveWallpaperConfig EMPTY = new LiveWallpaperConfig();
    public String contentSource;
    public long createdAt;
    public boolean draft;
    public boolean fade_enabled;
    public String type = "";
    public String minAppVersion = "";
    public String bundleVersion = "";
    public int moduleVersion = 1;
    public int clwVersion = 0;
    public int buildVersion = 0;
    public int editVersion = 0;
    public List<String> images = new ArrayList();
    public List<ImageOptions> imagesOptions = new ArrayList();
    public BoomerangOptions boomerangOptions = BoomerangOptions.EMPTY;

    @SerializedName("config")
    public ParallaxOptions parallaxOptions = ParallaxOptions.EMPTY;
    public Options options = Options.EMPTY;
    public VideoOptions videoOptions = VideoOptions.EMPTY;
    public AiOptions aiOptions = AiOptions.EMPTY;
    public LockscreenAnimation lockscreenAnim = LockscreenAnimation.EMPTY;

    public static class AiOptions implements Serializable {
        public static final AiOptions EMPTY = new AiOptions();
        public boolean isAiGenerated;
        public String prompt;
    }

    public static class BoomerangOptions {
        public static final BoomerangOptions EMPTY = new BoomerangOptions();
        public int fps;
        public int framesCount;
    }

    public static class LockscreenAnimation implements Serializable {
        public static final LockscreenAnimation EMPTY = new LockscreenAnimation();
        public String fromHome = "";
        public String toHome = "";
        public String lockImage = "";
        public String lockVideo = "";
        public boolean loop = false;

        public boolean hasHomeToLockTransition() {
            return !TextUtils.isEmpty(this.fromHome);
        }

        public boolean hasImage() {
            return !TextUtils.isEmpty(this.lockImage);
        }

        public boolean hasVideo() {
            return (TextUtils.isEmpty(this.lockVideo) && TextUtils.isEmpty(this.fromHome) && TextUtils.isEmpty(this.toHome)) ? false : true;
        }

        public boolean singleFile() {
            return TextUtils.isEmpty(this.toHome) && TextUtils.isEmpty(this.fromHome) && TextUtils.isEmpty(this.lockImage);
        }
    }

    public static class Options implements Serializable {
        public static final Options EMPTY = new Options();
        public float backgroundDepth;
        public float backgroundPosX;
        public float backgroundPosY;
        public float backgroundPosZ;
        public float backgroundScale;
        public float cameraCor;
        public float cameraDistance;
        public float cameraFov;
        public float cameraMaxRotX;
        public float cameraMaxRotY;
        public boolean debugRenderDepthMap;
        public boolean debugRenderSalience;
        public boolean depthMapOnly;
        public float foregroundDepth;
        public float foregroundLeanAngle;
        public float foregroundPosX;
        public float foregroundPosY;
        public float foregroundPosZ;
        public float foregroundScale;
        public float gyroMaxRotX;
        public float gyroMaxRotY;
        public boolean inpaintBorders;
        public boolean mirrorBorders;
        public boolean parallaxEnabled;
        public boolean useParallax;
        public float leanPosY = 0.5f;
        public float imgCenterX = 0.0f;
        public float imgCenterY = 0.0f;
        public float imgScale = 1.0f;
        public float u1 = 0.0f;
        public float v1 = 0.0f;
        public float u2 = 1.0f;
        public float v2 = 1.0f;
        public float displacementX = 50.0f;
        public float displacementY = 50.0f;
        public float layersMoveDistance = 5.0f;
    }

    public static class ParallaxCameraOptions {
        public float angleview;
        public float cor;
        public float maxrotationangle_x;
        public float maxrotationangle_y;
        public float oz;
    }

    public static class ParallaxGyroOptions {
        public float maxgyronangle_x;
        public float maxgyronangle_y;
    }

    public static class ParallaxLayerOptions {
        public String depthmapFilename;
        public String filename;
        public float[] offset;
        public float oz;
        public float rotation;
        public float scale;
        public int steady;
    }

    public static class ParallaxOptions {
        public static final ParallaxOptions EMPTY = new ParallaxOptions();
        public ParallaxCameraOptions camera;
        public ParallaxGyroOptions gyro;
        public List<ParallaxLayerOptions> layers;
        public int version;
    }

    public static class VideoOptions implements Serializable {
        public static final VideoOptions EMPTY = new VideoOptions();
        public float posX = 0.0f;
        public float posY = 0.0f;
        public float scale = 1.0f;
        public float x1 = 0.0f;
        public float y1 = 0.0f;
        public float x2 = 1.0f;
        public float y2 = 1.0f;
    }

    public boolean isEmpty() {
        return EMPTY.equals(this);
    }

    public static class ImageOptions {
        public String image;
        public float u1;
        public float u2;
        public float v1;
        public float v2;

        public ImageOptions(String str, float f2, float f3, float f4, float f5) {
            this.image = str;
            this.u1 = f2;
            this.v1 = f3;
            this.u2 = f4;
            this.v2 = f5;
        }

        public static ImageOptions createDefault(String str) {
            return new ImageOptions(str);
        }

        private ImageOptions(String str) {
            this.u1 = 0.0f;
            this.v1 = 0.0f;
            this.u2 = 1.0f;
            this.v2 = 1.0f;
            this.image = str;
        }
    }
}
