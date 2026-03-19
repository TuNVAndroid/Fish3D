package com.wave.keyboard.theme.supercolor.settings;

import android.content.Context;
import com.wave.livewallpaper.data.AppAttrib;
import com.wave.keyboard.theme.supercolor.reward.RewardItem;
import java.util.ArrayList;
import java.util.List;

public class ThemeSettings {

    public static String getShortname(Context context) {
        return "";
    }

    public static AppAttrib getCurrentTheme(Context context) {
        return new AppAttrib();
    }

    public static List getRewardItems(Context context) {
        return new ArrayList();
    }

    public static List filterRewardItems(Context context, List items) {
        return new ArrayList();
    }

    public static void saveNextReward(Context context, AppAttrib attrib) {
    }
}
