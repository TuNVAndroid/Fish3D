package com.wave.livewallpaper.libgdx.reward;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import androidx.core.view.GestureDetectorCompat;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidWallpaperListener;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mbridge.msdk.foundation.entity.RewardPlus;
import com.wave.keyboard.theme.supercolor.callscreen.CalendarDayCounter;
import com.wave.keyboard.theme.supercolor.callscreen.MultiprocessPreferences;
import com.wave.keyboard.theme.supercolor.reward.RewardItem;
import com.wave.keyboard.theme.supercolor.settings.ThemeSettings;
import com.wave.keyboard.theme.supercolor.splittest.SplitPopupExit;
import com.wave.keyboard.theme.utils.FirebaseHelper;
import com.wave.keyboard.theme.utils.Utils;
import com.wave.livewallpaper.data.AppAttrib;
import com.wave.livewallpaper.reward.DailyIntervals;
import com.wave.livewallpaper.reward.RewardCreature;
import com.wave.livewallpaper.reward.RewardPopupConfirmationDialog;
import com.wave.livewallpaper.reward.RewardSettings;
import com.wave.livewallpaper.reward.SplitRewardIcon;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class RewardPopup implements ApplicationListener, AndroidWallpaperListener, MultiprocessPreferences.OnMultiprocessPreferenceChangeListener {
    private DailyIntervals.TimeInterval currentTimeInterval;
    private boolean wasChestVisible;
    private long lastClickTimestamp;
    private FirebaseAnalytics firebaseAnalytics;
    private boolean isPaused;
    private boolean isDisabled;
    private GestureDetector.SimpleOnGestureListener gestureListener;

    private Context context;
    private MultiprocessPreferences.MultiprocessSharedPreferences sharedPreferences;
    private CalendarDayCounter dayCounter;
    private GestureDetectorCompat gestureDetector;
    private OrthographicCamera camera;
    private SpriteBatch spriteBatch;
    private TextureAtlas textureAtlas;
    private Animation animation;
    private float animationTime;
    private RewardCreature currentCreature;
    private RewardCreature pendingCreature;
    private float spriteWidth;
    private float spriteHeight;
    private Position position;
    private Rectangle rewardBounds;
    private boolean hasUnlockables;
    private boolean isPreview;
    private boolean isNewDay;
    private boolean isWithinTimeInterval;
    private boolean hasBeenShown;
    private boolean isPopupEnabled;
    private boolean hasPassedCooldown;
    private float checkIntervalTimer;
    private float updateTimer;
    private DailyIntervals dailyIntervals;

    class AnonymousClass2 extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            try {
                boolean withinBounds = RewardPopup.this.getRewardBounds().contains(motionEvent.getX(), (Gdx.graphics.getHeight() - 1) - motionEvent.getY());
                long currentTime = System.currentTimeMillis();
                boolean notDoubleTap = Math.abs(currentTime - RewardPopup.this.lastClickTimestamp) > TimeUnit.SECONDS.toMillis(1L);
                if (withinBounds && notDoubleTap) {
                    RewardPopup.this.lastClickTimestamp = currentTime;
                    RewardPopup.this.onChestClicked();
                }
            } catch (Exception e2) {
                Log.e("RewardPopup", "onTouch", e2);
                FirebaseHelper.logException(e2);
            }
            return false;
        }
    }

    private static class BottomLeftPosition extends Position {
        BottomLeftPosition(float width, float height) {
            super(width, height);
            this.posX = Gdx.graphics.getWidth() * 0.15f;
            this.posY = Gdx.graphics.getHeight() * 0.15f;
        }
    }

    private static class BottomRightPosition extends Position {
        BottomRightPosition(float width, float height) {
            super(width, height);
            this.posX = Gdx.graphics.getWidth() - width;
            this.posY = Gdx.graphics.getHeight() * 0.15f;
        }
    }

    private static abstract class Position {

        private float width;
        private float height;
        protected float posX;
        protected float posY;

        static Position create(int type, float width, float height) {
            if (type == 0) {
                return new TopLeftPosition(width, height);
            }
            if (type == 1) {
                return new TopRightPosition(width, height);
            }
            if (type == 2) {
                return new BottomLeftPosition(width, height);
            }
            if (type == 3) {
                return new BottomRightPosition(width, height);
            }
            throw new IllegalArgumentException("Incorrect type value");
        }

        private Position(float width, float height) {
            this.width = width;
            this.height = height;
        }
    }

    private static class TopLeftPosition extends Position {
        TopLeftPosition(float width, float height) {
            super(width, height);
            float screenHeight = Gdx.graphics.getHeight();
            this.posX = Gdx.graphics.getWidth() * 0.15f;
            this.posY = (screenHeight - (0.1f * screenHeight)) - height;
        }
    }

    private static class TopRightPosition extends Position {
        TopRightPosition(float width, float height) {
            super(width, height);
            float screenHeight = Gdx.graphics.getHeight();
            this.posX = Gdx.graphics.getWidth() - width;
            this.posY = (screenHeight - (0.1f * screenHeight)) - height;
        }
    }

    private void resetUpdateTimer() {
        this.updateTimer = TimeUnit.MINUTES.toSeconds(30L);
    }

    private void sendFirebaseEvent(String eventName, String action) {
        try {
            String shortname = ThemeSettings.getCurrentTheme(this.context).shortname;
            int day = RewardSettings.getDay(this.context);
            Bundle bundle = new Bundle();
            bundle.putString("action", action);
            bundle.putString("shortname", shortname);
            bundle.putString(RewardPlus.ICON, this.currentCreature.name);
            bundle.putString("type", "download_livewallpaper");
            bundle.putInt("day", day);
            this.firebaseAnalytics.logEvent(eventName, bundle);
        } catch (Exception e2) {
            FirebaseHelper.logException(e2);
            Log.e("RewardPopup", "sendFirebaseEvent", e2);
        }
    }

    private void sendFirebaseEventClickChest() {
        try {
            Log.d("RewardPopup", "sendFirebaseEventClickChest");
            sendFirebaseEvent("Daily_Reward", "click_chest");
        } catch (Exception e2) {
            Log.e("RewardPopup", "sendFirebaseEventClickChest", e2);
        }
    }

    private void sendFirebaseEventViewChest() {
        try {
            Log.d("RewardPopup", "sendFirebaseEventViewChest");
            sendFirebaseEvent("Daily_Reward", "show_chest");
        } catch (Exception e2) {
            Log.e("RewardPopup", "sendFirebaseEventViewChest", e2);
        }
    }

    private void loadCreatureAnimation() {
        TextureAtlas atlas = this.textureAtlas;
        if (atlas != null) {
            atlas.dispose();
        }
        TextureAtlas newAtlas = new TextureAtlas(Gdx.files.internal(getOrLoadCreature().atlasPath));
        this.textureAtlas = newAtlas;

        this.animation = new Animation(0.033333335f, newAtlas.getRegions());
        this.animationTime = 0.0f;
        float size = getOrLoadCreature().scale * Gdx.graphics.getWidth();
        this.spriteWidth = size;
        this.spriteHeight = size;
    }

    private DailyIntervals.TimeInterval createTimeInterval(long timestamp) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        if (timestamp > 0) {
            calendar.setTimeInMillis(timestamp);
        }
        int hour = calendar.get(11);
        int minute = calendar.get(12);
        calendar.add(12, 30);
        return new DailyIntervals.TimeInterval(hour, minute, calendar.get(11), calendar.get(12));
    }

    private void updateDailyRewardAvailability() {
        try {
            if (isNewDayCheck()) {
                saveLastRewardTimestamp();
                DailyIntervals.TimeInterval interval = createTimeInterval(0L);
                getOrCreateDailyIntervals().clear();
                getOrCreateDailyIntervals().addInterval(interval);
                RewardCreature splitCreature = SplitRewardIcon.getInstance().creature;
                this.pendingCreature = splitCreature;
                if (!splitCreature.equals(this.currentCreature)) {
                    RewardSettings.saveCreature(this.context, this.pendingCreature);
                }
            } else {
                Log.d("RewardPopup", "isSameDay");
            }
            boolean expired = getOrCreateDailyIntervals().isExpired();
            boolean cooldownPassed = hasCooldownPassed();
            boolean newDay = true;
            if (!this.hasPassedCooldown && cooldownPassed) {
                this.hasPassedCooldown = true;
                saveLastRewardTimestamp();
            }
            if (expired && cooldownPassed) {
                DailyIntervals.TimeInterval interval = createTimeInterval(RewardSettings.getLastRewardTime(this.context));
                getOrCreateDailyIntervals().clear();
                getOrCreateDailyIntervals().addInterval(interval);
            }
            if (getOrCreateDayCounter().getDaysSinceStart() != 0) {
                newDay = false;
            }
            this.isNewDay = newDay;
        } catch (Exception e2) {
            Log.e("RewardPopup", "updateDailyRewardAvailability", e2);
            FirebaseHelper.logException(e2);
        }
    }

    private void updateTimeInterval() {
        DailyIntervals.TimeInterval activeInterval = getOrCreateDailyIntervals().getCurrentInterval(Calendar.getInstance(Locale.getDefault()));
        if (activeInterval != this.currentTimeInterval) {
            this.currentTimeInterval = activeInterval;
            resetUpdateTimer();
            updateBounds();
        }
        this.isWithinTimeInterval = this.currentTimeInterval != null;
    }

    private void saveLastRewardTimestamp() {
        RewardSettings.saveLastRewardTime(this.context, Calendar.getInstance(Locale.getDefault()).getTimeInMillis());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void refreshRewardState() {
        updateDailyRewardAvailability();
        updateTimeInterval();
        syncPreferences();
    }

    private boolean isNewDayCheck() {
        return System.currentTimeMillis() > RewardSettings.getLastRewardTime(this.context) + TimeUnit.DAYS.toMillis(1L);
    }

    private RewardItem findNextUnlockedReward(List list) {
        Iterator it = list.iterator();
        while (it.hasNext()) {
            RewardItem rewardItem = (RewardItem) it.next();
            if (rewardItem.isUnlocked && !rewardItem.isClaimed) {
                return rewardItem;
            }
        }
        return RewardItem.EMPTY;
    }

    private DailyIntervals getOrCreateDailyIntervals() {
        if (this.dailyIntervals == null) {
            this.dailyIntervals = new DailyIntervals();
        }
        return this.dailyIntervals;
    }

    private CalendarDayCounter getOrCreateDayCounter() {
        if (this.dayCounter == null) {
            this.dayCounter = RewardSettings.getDayCounter(this.context);
        }
        return this.dayCounter;
    }

    private GestureDetectorCompat getOrCreateGestureDetector() {
        if (this.gestureDetector == null) {
            this.gestureDetector = new GestureDetectorCompat(this.context, this.gestureListener);
        }
        return this.gestureDetector;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Rectangle getRewardBounds() {
        try {
            if (this.rewardBounds == null) {
                updateBounds();
            }
            return this.rewardBounds;
        } catch (Exception e2) {
            Log.e("RewardPopup", "getRewardBounds", e2);
            FirebaseHelper.logException(e2);
            return new Rectangle();
        }
    }

    private RewardCreature getOrLoadCreature() {
        if (this.currentCreature == null) {
            this.currentCreature = RewardSettings.getCreature(this.context);
        }
        return this.currentCreature;
    }

    private MultiprocessPreferences.MultiprocessSharedPreferences getOrCreatePreferences() {
        if (this.sharedPreferences == null) {
            MultiprocessPreferences.MultiprocessSharedPreferences prefs = MultiprocessPreferences.getPreferences(this.context, "reward_popup");
            this.sharedPreferences = prefs;
            prefs.registerListener(this);
        }
        return this.sharedPreferences;
    }

    private void initTimestamps() {
        if (-1 == RewardSettings.getLastRewardTime(this.context)) {
            saveLastRewardTimestamp();
        }
        if (-1 == RewardSettings.getInstallTime(this.context)) {
            RewardSettings.saveInstallTime(this.context, System.currentTimeMillis());
        }
    }

    private boolean isFirstDay() {
        return RewardSettings.getDay(this.context) < 1;
    }

    private boolean hasCooldownPassed() {
        if (isFirstDay()) {
            return System.currentTimeMillis() > RewardSettings.getInstallTime(this.context) + TimeUnit.MINUTES.toMillis(20L);
        }
        return true;
    }

    private boolean shouldThrottleCheck(float deltaTime) {
        float timer = this.checkIntervalTimer;
        if (timer > 0.0f) {
            this.checkIntervalTimer = timer - deltaTime;
            return false;
        }
        this.checkIntervalTimer = 5.0f;
        return true;
    }

    private boolean isChestVisible() {
        return !this.isPreview && this.isNewDay && this.isWithinTimeInterval;
    }

    private void syncPreferences() {
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onChestClicked() {
        sendFirebaseEventClickChest();
        sendFirebaseEventClickChest();
        Intent intent = new Intent(this.context, (Class<?>) RewardPopupConfirmationDialog.class);
        intent.setFlags(268468224);
        intent.putExtra("extra_image_res", getOrLoadCreature().imageRes);
        this.context.startActivity(intent);
    }

    private Position randomPosition() {
        return Position.create(new Random().nextInt(4), this.spriteWidth, this.spriteHeight);
    }

    private void updateBounds() {
        this.position = randomPosition();
        Position pos = this.position;
        this.rewardBounds = new Rectangle(pos.posX, pos.posY, this.spriteWidth, this.spriteHeight);
    }

    private void refreshUnlockableReward() {
        List listY = ThemeSettings.getRewardItems(this.context);
        if (Utils.isNullOrEmpty(listY)) {
            Log.v("RewardPopup", "refreshUnlockableReward - Reward items empty");
            this.hasUnlockables = false;
            return;
        }
        if (ThemeSettings.getCurrentTheme(this.context).isEmpty() || isNewDayCheck()) {
            AppAttrib appAttrib = findNextUnlockedReward(ThemeSettings.filterRewardItems(this.context, listY)).appAttrib;
            if (appAttrib.isEmpty()) {
                Log.v("RewardPopup", "refreshUnlockableReward - No more rewards to unlock");
                this.hasUnlockables = false;
            } else {
                Log.v("RewardPopup", "refreshUnlockableReward - Saving next reward " + appAttrib.shortname);
                ThemeSettings.saveNextReward(this.context, appAttrib);
                this.hasUnlockables = true;
            }
        } else {
            Log.v("RewardPopup", "refreshUnlockableReward - Reward still available today");
            this.hasUnlockables = true;
        }
        Log.v("RewardPopup", "refreshUnlockableReward - hasUnlockables " + this.hasUnlockables);
    }

    @Override
    public void onPreferenceChanged(String key, String value) {
        if (key.equals("last_date")) {
            refreshRewardState();
            this.updateTimer = 0.0f;
        }
    }

    @Override
    public void onPreferencesCleared() {
    }

    @Override
    public void create() {
        OrthographicCamera cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.camera = cam;
        cam.position.set(cam.viewportWidth / 2.0f, cam.viewportHeight / 2.0f, 0.0f);
        this.camera.update();
        this.spriteBatch = new SpriteBatch();
        loadCreatureAnimation();
        updateBounds();
        this.sharedPreferences = getOrCreatePreferences();
        this.dailyIntervals = new DailyIntervals();
        initTimestamps();
        this.hasPassedCooldown = hasCooldownPassed();
    }

    @Override
    public void dispose() {
        SpriteBatch batch = this.spriteBatch;
        if (batch != null) {
            batch.dispose();
        }
        TextureAtlas atlas = this.textureAtlas;
        if (atlas != null) {
            atlas.dispose();
        }
        MultiprocessPreferences.MultiprocessSharedPreferences prefs = this.sharedPreferences;
        if (prefs != null) {
            prefs.unregisterListener(this);
        }
    }

    @Override
    public void iconDropped(int x, int y) {
    }

    @Override
    public void offsetChange(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
    }

    @Override
    public void pause() {
        this.hasBeenShown = false;
        this.isPaused = true;
    }

    @Override
    public void previewStateChange(boolean isPreview) {
        Log.d("RewardPopup", "previewStateChange " + isPreview);
        this.isPreview = isPreview;
    }

    @Override
    public void render() {
        if (!this.isDisabled && this.isPopupEnabled && this.hasUnlockables && !this.isPaused) {
            float deltaTime = Gdx.graphics.getDeltaTime();
            if (shouldThrottleCheck(deltaTime)) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        RewardPopup.this.refreshRewardState();
                    }
                }).start();
            }
            if (this.isPreview) {
                return;
            }
            if (!isChestVisible()) {
                this.wasChestVisible = false;
                return;
            }
            if (!this.wasChestVisible || !this.hasBeenShown) {
                this.wasChestVisible = true;
                this.hasBeenShown = true;
                sendFirebaseEventViewChest();
            }
            RewardCreature pending = this.pendingCreature;
            if (pending != null && !this.currentCreature.equals(pending)) {
                this.currentCreature = this.pendingCreature;
                loadCreatureAnimation();
            }
            this.camera.update();
            this.spriteBatch.setProjectionMatrix(this.camera.combined);
            this.spriteBatch.begin();
            float time = this.animationTime + deltaTime;
            this.animationTime = time;
            SpriteBatch batch = this.spriteBatch;
            TextureRegion frame = (TextureRegion) this.animation.getKeyFrame(time, true);
            Position pos = this.position;
            batch.draw(frame, pos.posX, pos.posY, this.spriteWidth, this.spriteHeight);
            this.spriteBatch.end();
        }
    }

    @Override
    public void resize(int width, int height) {
        OrthographicCamera cam = this.camera;
        float w = width;
        cam.viewportWidth = w;
        float h = height;
        cam.viewportHeight = h;
        cam.position.set(w / 2.0f, h / 2.0f, 0.0f);
        this.camera.update();
    }

    @Override
    public void resume() {
        this.isPopupEnabled = SplitPopupExit.getInstance().isEnabled;
        refreshUnlockableReward();
        this.isPaused = false;
    }

    public void v(MotionEvent motionEvent) {
        if (isChestVisible()) {
            getOrCreateGestureDetector().onTouchEvent(motionEvent);
        }
    }
}
