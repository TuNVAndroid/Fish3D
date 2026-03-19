package com.wave.keyboard.theme.utils;

import android.content.Intent;
import java.util.List;

public class Utils {
    public static boolean isNullOrEmpty(List list) {
        return list == null || list.isEmpty();
    }

    public static String intentToString(Intent intent) {
        return intent != null ? intent.toString() : "";
    }
}
