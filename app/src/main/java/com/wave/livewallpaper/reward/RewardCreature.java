package com.wave.livewallpaper.reward;

public class RewardCreature {
    public String name = "";
    public String atlasPath = "";
    public float scale = 0.1f;
    public int imageRes = 0;

    public boolean isEmpty() {
        return name == null || name.isEmpty();
    }
}
