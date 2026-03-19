package com.google.firebase.analytics;

import android.content.Context;
import android.os.Bundle;

public class FirebaseAnalytics {

    private static FirebaseAnalytics sInstance;

    public static FirebaseAnalytics getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new FirebaseAnalytics();
        }
        return sInstance;
    }

    public void logEvent(String name, Bundle params) {
    }
}
