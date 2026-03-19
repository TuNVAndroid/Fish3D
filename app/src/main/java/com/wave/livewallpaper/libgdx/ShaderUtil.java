package com.wave.livewallpaper.libgdx;

import android.util.Log;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/* loaded from: classes6.dex */
public class ShaderUtil {
    public static ShaderProgram loadShader(String shaderName, String prefix) {
        ShaderProgram.pedantic = false;
        String vertexSource = Gdx.files.internal("shaders/" + shaderName + "_v.glsl").readString();
        String fragmentSource = Gdx.files.internal("shaders/" + shaderName + "_f.glsl").readString();
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append(vertexSource);
        ShaderProgram shaderProgram = new ShaderProgram(sb.toString(), prefix + fragmentSource);
        if (shaderProgram.isCompiled()) {
            Log.d("ShaderUtil", "Shader " + shaderName + " compiled " + shaderProgram.getLog());
        } else {
            Log.d("ShaderUtil", "Error with shader " + shaderName + ": " + shaderProgram.getLog());
        }
        return shaderProgram;
    }
}
