package com.wave.livewallpaper.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import java.nio.ByteBuffer;

public class Water {

    private OrthographicCamera camera;
    private SpriteBatch spriteBatch;
    private ShaderProgram shader;
    private TextureRegion textureRegion;
    private int screenWidth = Gdx.graphics.getWidth();
    private int screenHeight = Gdx.graphics.getHeight();
    private int[] waveBuffer1 = new int[GL20.GL_CW];
    private int[] waveBuffer2 = new int[GL20.GL_CW];
    private int[] smoothedBuffer = new int[GL20.GL_CW];
    private ByteBuffer textureData = ByteBuffer.allocateDirect(GL20.GL_CW);
    private int textureId;
    private boolean needsTextureUpdate;
    private float pixelSize;
    private long lastRippleTime;

    public Water() {
        int texId = Gdx.gl.glGenTexture();
        this.textureId = texId;
        Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, texId);
        Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAG_FILTER, 9729.0f);
        Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MIN_FILTER, 9729.0f);
        Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_S, GL20.GL_CLAMP_TO_EDGE);
        Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_T, GL20.GL_CLAMP_TO_EDGE);
        Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_2D, 0, GL20.GL_LUMINANCE, 48, 48, 0, GL20.GL_LUMINANCE, 5121, null);
        this.needsTextureUpdate = true;
        this.pixelSize = 0.03472222f;
        this.shader = ShaderUtil.loadShader("water", "");
        this.spriteBatch = new SpriteBatch(1000, this.shader);
        TextureRegion region = new TextureRegion();
        this.textureRegion = region;
        region.flip(false, true);
        OrthographicCamera cam = new OrthographicCamera(this.screenWidth, this.screenHeight);
        this.camera = cam;
        cam.position.set(cam.viewportWidth / 2.0f, cam.viewportHeight / 2.0f, 0.0f);
        this.camera.update();
    }

    private void addRipple(float x, float y) {
        int gridX = (int) ((x / this.screenWidth) * 47.0f);
        int gridY = (int) ((1.0f - (y / this.screenHeight)) * 47.0f);
        int maxY = Math.max(1, Math.min(46, gridY + 1));
        int minX = Math.max(1, Math.min(46, gridX - 1));
        int maxX = Math.max(1, Math.min(46, gridX + 1));
        for (int minY = Math.max(1, Math.min(46, gridY - 1)); minY <= maxY; minY++) {
            for (int ix = minX; ix <= maxX; ix++) {
                int index = (minY * 48) + ix;
                this.waveBuffer1[index] = 2260;
                this.waveBuffer2[index] = 2260;
            }
        }
    }

    public void dispose() {
        SpriteBatch batch = this.spriteBatch;
        if (batch != null) {
            batch.dispose();
        }
        ShaderProgram shaderProg = this.shader;
        if (shaderProg != null) {
            shaderProg.dispose();
        }
    }

    public void addRippleScaled(float x, float y) {
        addRipple((x * 0.6f) + ((this.screenWidth * 0.39999998f) / 2.0f), (y * 0.6f) + ((this.screenHeight * 0.39999998f) / 2.0f));
    }

    public void renderWater(FrameBuffer frameBuffer) {
        if (frameBuffer == null) {
            return;
        }
        if (this.textureRegion.getTexture() != frameBuffer.getColorBufferTexture()) {
            this.textureRegion.setRegion(frameBuffer.getColorBufferTexture());
            this.textureRegion.flip(false, true);
        }
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE1);
        Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, this.textureId);
        if (this.needsTextureUpdate) {
            this.needsTextureUpdate = false;
            for (int row = 0; row < 48; row++) {
                int col = 0;
                while (col < 48) {
                    int maxRow = Math.max(0, Math.min(47, row + 1));
                    int minCol = Math.max(0, Math.min(47, col - 1));
                    int nextCol = col + 1;
                    int maxCol = Math.max(0, Math.min(47, nextCol));
                    int sum = 0;
                    int count = 0;
                    for (int minRow = Math.max(0, Math.min(47, row - 1)); minRow <= maxRow; minRow++) {
                        for (int ic = minCol; ic <= maxCol; ic++) {
                            sum += this.waveBuffer2[(minRow * 48) + ic];
                            count++;
                        }
                    }
                    this.smoothedBuffer[(row * 48) + col] = sum / (count + 3);
                    col = nextCol;
                }
            }
            for (int r = 0; r < 48; r++) {
                for (int c = 0; c < 48; c++) {
                    int idx = (r * 48) + c;
                    this.textureData.put(idx, (byte) (((this.smoothedBuffer[idx] + 3046) * 255) / 6159));
                }
            }
            Gdx.gl.glTexSubImage2D(GL20.GL_TEXTURE_2D, 0, 0, 0, 48, 48, GL20.GL_LUMINANCE, 5121, this.textureData);
        }
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        Gdx.gl.glClear(16384);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
        this.shader.begin();
        this.shader.setUniformi("u_map", 1);
        this.shader.setUniformf("u_map_size", 0.6f);
        this.shader.setUniformf("u_pixel", this.pixelSize);
        this.shader.end();
        this.camera.update();
        this.spriteBatch.setProjectionMatrix(this.camera.combined);
        this.spriteBatch.begin();
        this.spriteBatch.draw(this.textureRegion, 0.0f, 0.0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.spriteBatch.end();
    }

    public void resize(int width, int height) {
        OrthographicCamera cam = this.camera;
        float w = width;
        cam.viewportWidth = w;
        float h = height;
        cam.viewportHeight = h;
        cam.position.set(w / 2.0f, h / 2.0f, 0.0f);
        this.camera.update();
    }

    public void updateSimulation(float damping) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastRippleTime > 2000.0f) {
            if (Math.random() > 0.5d) {
                addRipple(((double) ((float) Math.random())) > 0.5d ? Gdx.graphics.getWidth() : 0, ((float) Math.random()) * Gdx.graphics.getHeight());
            } else {
                addRipple(((float) Math.random()) * Gdx.graphics.getWidth(), ((double) ((float) Math.random())) > 0.5d ? Gdx.graphics.getHeight() : 0);
            }
            this.lastRippleTime = currentTime;
        }
        for (int row = 1; row < 47; row++) {
            for (int col = 1; col < 47; col++) {
                int idx = (row * 48) + col;
                int[] buf1 = this.waveBuffer1;
                int avg = (((buf1[idx - 1] + buf1[idx + 1]) + buf1[idx - 48]) + buf1[idx + 48]) >> 1;
                int[] buf2 = this.waveBuffer2;
                int oldVal = buf2[idx];
                float diff = avg - oldVal;
                int newVal = (int) (diff - (diff * damping));
                if (newVal <= -3046) {
                    newVal = -3046;
                } else if (newVal > 3113) {
                    newVal = 3113;
                }
                if (oldVal != newVal) {
                    this.needsTextureUpdate = true;
                }
                buf2[idx] = newVal;
            }
        }
        int[] temp = this.waveBuffer1;
        this.waveBuffer1 = this.waveBuffer2;
        this.waveBuffer2 = temp;
    }
}
