package com.wave.livewallpaper.reward;

public class SplitRewardIcon {

    public RewardCreature creature = new RewardCreature();

    private static final SplitRewardIcon sInstance = new SplitRewardIcon();

    public static SplitRewardIcon getInstance() {
        return sInstance;
    }
}
