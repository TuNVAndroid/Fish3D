package com.wave.livewallpaper.data;

import com.google.gson.annotations.SerializedName;
import com.mbridge.msdk.foundation.entity.CampaignEx;
import com.wave.data.CustomResFileName;
import com.wave.keyboard.theme.utils.StringUtils;
import com.wave.livewallpaper.WebReadPack.ReadTopNewJson;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes4.dex */
public class AppAttrib implements Serializable {
    public static final AppAttrib EMPTY = new AppAttrib();
    public boolean badgeVisible;

    @SerializedName("categ")
    public String categ;
    public String categoryTop;
    public boolean downloadNowButton;
    public String icon;
    public String id;

    @SerializedName("is_premium")
    public int isPremium;

    @SerializedName("lw_type")
    public String lw_type;
    public String packageName;
    public String paired_keyboard;

    @SerializedName("paired_keyboard_preview")
    public String paired_keyboard_preview;

    @SerializedName("paired_keyboard_preview_video")
    public String paired_keyboard_preview_video;
    public String paired_keyboard_video;

    @SerializedName("paired_livewallpaper")
    public String paired_livewallpaper;

    @SerializedName("popularity")
    public float popularity;
    public int position;

    @SerializedName(CustomResFileName.PREVIEW_IMG)
    public String preview_img;

    @SerializedName("preview_video")
    public String preview_video;
    public int price;

    @SerializedName(CampaignEx.JSON_KEY_STAR)
    public float rating;

    @SerializedName("resource")
    public String resource;
    public String selectedForCategory;

    @SerializedName("shared_ct")
    public boolean shared_ct;
    public ReadTopNewJson.Source source;
    public int specialViewId;

    @SerializedName(CampaignEx.JSON_KEY_TITLE)
    public String title;

    @SerializedName("uuid")
    public String uuid;

    @SerializedName(CampaignEx.JSON_KEY_VIDEO_URL)
    public String video_url;

    @SerializedName("wallpaper")
    public AppAttrib wallpaper;

    @SerializedName("shortname")
    public String shortname = "";

    @SerializedName("cover")
    public String cover = "";

    @SerializedName("preview")
    public String preview = "";

    @SerializedName("preview_por")
    public String preview_por = "";
    public List<String> screens = new ArrayList();

    public boolean isEmpty() {
        return EMPTY.equals(this) || StringUtils.isNullOrEmpty(this.shortname);
    }

    public String toString() {
        return ((((("{ " + this.shortname) + " " + this.packageName) + " " + this.categ) + " catTop " + this.categoryTop) + " selectedCat " + this.selectedForCategory) + " }";
    }
}
