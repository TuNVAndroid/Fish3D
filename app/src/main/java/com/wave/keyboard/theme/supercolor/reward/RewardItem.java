package com.wave.keyboard.theme.supercolor.reward;

import com.wave.livewallpaper.data.AppAttrib;

public class RewardItem {

    public static final RewardItem EMPTY = new RewardItem();

    public AppAttrib appAttrib = new AppAttrib();
    public boolean isUnlocked = false;
    public boolean isClaimed = false;
}
