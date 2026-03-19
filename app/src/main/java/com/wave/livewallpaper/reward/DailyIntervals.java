package com.wave.livewallpaper.reward;

import java.util.Calendar;

public class DailyIntervals {

    public static class TimeInterval {
        public int startHour;
        public int startMinute;
        public int endHour;
        public int endMinute;

        public TimeInterval(int startHour, int startMinute, int endHour, int endMinute) {
            this.startHour = startHour;
            this.startMinute = startMinute;
            this.endHour = endHour;
            this.endMinute = endMinute;
        }
    }

    public void addInterval(TimeInterval interval) {
    }

    public void clear() {
    }

    public TimeInterval getCurrentInterval(Calendar calendar) {
        return null;
    }

    public boolean isExpired() {
        return false;
    }
}
