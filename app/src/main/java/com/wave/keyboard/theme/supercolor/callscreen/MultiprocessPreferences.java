package com.wave.keyboard.theme.supercolor.callscreen;

import android.content.Context;

public class MultiprocessPreferences {

    public interface OnMultiprocessPreferenceChangeListener {
        void onPreferenceChanged(String key, String value);
        void onPreferencesCleared();
    }

    public static class MultiprocessSharedPreferences {
        public void registerListener(OnMultiprocessPreferenceChangeListener listener) {
        }

        public void unregisterListener(OnMultiprocessPreferenceChangeListener listener) {
        }
    }

    public static MultiprocessSharedPreferences getPreferences(Context context, String name) {
        return new MultiprocessSharedPreferences();
    }
}
