package com.wave.livewallpaper.vfx;

public class VfxParticle {

    public static final VfxParticle EMPTY = builder().name("").type(0).particlePath("").description("").atlasPath("").cooldown(VfxCooldown.DEFAULT).randomize(false).sequential(false).isWaterVfx(false).build();

    public String name;
    public int type;
    public String particlePath;
    public String description;
    public String atlasPath;
    public VfxCooldown cooldown;
    public boolean randomize;
    public boolean sequential;
    public boolean isWaterVfx;

    public static final class Builder {

        private int type;
        private String name;
        private String particlePath;
        private String description;
        private String atlasPath;
        private VfxCooldown cooldown;
        private boolean randomize;
        private boolean sequential;
        private boolean isWaterVfx;

        public Builder description(String str) {
            this.description = str;
            return this;
        }

        public VfxParticle build() {
            return new VfxParticle(this);
        }

        public Builder cooldown(VfxCooldown vfxCooldown) {
            this.cooldown = vfxCooldown;
            return this;
        }

        public Builder particlePath(String str) {
            this.particlePath = str;
            return this;
        }

        public Builder sequential(boolean z2) {
            this.sequential = z2;
            return this;
        }

        public Builder type(int i2) {
            this.type = i2;
            return this;
        }

        public Builder atlasPath(String str) {
            this.atlasPath = str;
            return this;
        }

        public Builder isWaterVfx(boolean z2) {
            this.isWaterVfx = z2;
            return this;
        }

        public Builder name(String str) {
            this.name = str;
            return this;
        }

        public Builder randomize(boolean z2) {
            this.randomize = z2;
            return this;
        }

        private Builder() {
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isEmpty() {
        return EMPTY.equals(this);
    }

    private VfxParticle(Builder builder) {
        this.name = builder.name;
        this.type = builder.type;
        this.particlePath = builder.particlePath;
        this.description = builder.description;
        this.atlasPath = builder.atlasPath;
        this.cooldown = builder.cooldown;
        this.randomize = builder.randomize;
        this.sequential = builder.sequential;
        this.isWaterVfx = builder.isWaterVfx;
    }
}
