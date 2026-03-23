package com.wave.livewallpaper.data;

/* loaded from: classes4.dex */
public class SceneConfig {
    public static final SceneConfig EMPTY = new SceneConfig();
    public static final String FILE_NAME = "scene.json";
    public float cameraDistance = 1000.0f;
    public float cameraAngleX = -15.0f;
    public float spawnAreaWidth = 1000.0f;
    public float spawnAreaDepth = 2000.0f;
    public float noSpawnAreaWidth = 600.0f;
    public float noSpawnAreaDepth = 1600.0f;
    public float spawnYStart = -150.0f;
    public float spawnYIncrement = 75.0f;
    public float floorPosX = 0.0f;
    public float floorPosY = -500.0f;
    public float floorPosZ = 100.0f;
    public float floorRotationX = 0.0f;
    public float floorRotationY = 90.0f;
    public float floorScaleX = 1.0f;
    public float floorScaleZ = 1.0f;
    public boolean floorFlipX = false;
    public boolean floorFlipZ = false;

    public boolean isEmpty() {
        return this == EMPTY;
    }
}
