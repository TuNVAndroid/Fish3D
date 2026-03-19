package com.wave.livewallpaper.libgdx;

import com.badlogic.gdx.Gdx;

/* loaded from: classes6.dex */
public class LibgdxUtils {
    public static void clearScreen() {
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        Gdx.gl.glClear(16640);
    }
}
