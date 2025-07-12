package biz.binarysolutions.vatcalculator.util;

import android.app.Activity;

import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;

import biz.binarysolutions.vatcalculator.R;

public class FlavorSpecific {

    private final Activity activity;

    public FlavorSpecific(Activity activity) {
        this.activity = activity;
    }

    public void initializeBottomBar() {

        MobileAds.initialize(activity);

        String           adId    = activity.getString(R.string.admob_native_ad_id);
        AdLoader.Builder builder = new AdLoader.Builder(activity, adId);
        builder.forNativeAd(nativeAd -> {

            TemplateView template = activity.findViewById(R.id.ad_template);
            template.setStyles(new NativeTemplateStyle.Builder().build());
            template.setNativeAd(nativeAd);
        });

        AdLoader adLoader = builder.build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }
}
