package com.wave.livewallpaper.vfx;

import android.graphics.PointF;

public class VfxCooldown {

    public static VfxCooldown DEFAULT = builder().duration(0.3f).minDistance(25.0f).build();

    private final float cooldownDuration;
    private final float minDistance;
    private PointF lastTouchPoint;
    private PointF currentTouchPoint;
    private float remainingCooldown;

    public static final class Builder {

        private float duration;
        private float distance;

        public VfxCooldown build() {
            return new VfxCooldown(this);
        }

        public Builder minDistance(float f2) {
            this.distance = f2;
            return this;
        }

        public Builder duration(float f2) {
            this.duration = f2;
            return this;
        }

        private Builder() {
            this.duration = 0.3f;
            this.distance = Float.POSITIVE_INFINITY;
        }
    }

    private static float distance(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float) Math.sqrt((dx * dx) + (dy * dy));
    }

    private boolean isDistanceMet() {
        PointF current = this.currentTouchPoint;
        float x1 = current.x;
        float y1 = current.y;
        PointF last = this.lastTouchPoint;
        return distance(x1, y1, last.x, last.y) >= this.minDistance;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isReady() {
        return ((this.remainingCooldown > 0.0f ? 1 : (this.remainingCooldown == 0.0f ? 0 : -1)) <= 0) || isDistanceMet();
    }

    public void reset() {
        if (this.lastTouchPoint == null) {
            this.lastTouchPoint = new PointF();
        }
        PointF pointF = this.lastTouchPoint;
        pointF.x = Float.POSITIVE_INFINITY;
        pointF.y = Float.POSITIVE_INFINITY;
        this.remainingCooldown = 0.0f;
    }

    public void setCurrentTouch(float x, float y) {
        PointF pointF = this.currentTouchPoint;
        pointF.x = x;
        pointF.y = y;
    }

    public void setLastTouch(float x, float y) {
        PointF pointF = this.lastTouchPoint;
        pointF.x = x;
        pointF.y = y;
    }

    public void startCooldown() {
        float duration = this.cooldownDuration;
        if (duration == -1.0f) {
            duration = Float.MAX_VALUE;
        }
        this.remainingCooldown = duration;
    }

    public void tick(float delta) {
        float remaining = this.remainingCooldown;
        if (remaining > 0.0f) {
            this.remainingCooldown = remaining - delta;
        }
    }

    private VfxCooldown(Builder builder) {
        this.lastTouchPoint = new PointF(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        this.currentTouchPoint = new PointF(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        this.remainingCooldown = 0.0f;
        this.cooldownDuration = builder.duration;
        this.minDistance = builder.distance;
        reset();
    }
}
