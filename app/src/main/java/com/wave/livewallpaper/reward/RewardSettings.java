package com.wave.livewallpaper.reward;

import android.content.Context;
import com.wave.keyboard.theme.supercolor.callscreen.CalendarDayCounter;

public class RewardSettings {
    public static int getDay(Context context) {
        return 0;
    }

    public static CalendarDayCounter getDayCounter(Context context) {
        return new CalendarDayCounter();
    }

    public static RewardCreature getCreature(Context context) {
        return new RewardCreature();
    }

    public static long getInstallTime(Context context) {
        return -1;
    }

    public static long getLastRewardTime(Context context) {
        return -1;
    }

    public static void saveCreature(Context context, RewardCreature creature) {
    }

    public static void saveInstallTime(Context context, long time) {
    }

    public static void saveLastRewardTime(Context context, long time) {
    }
}
