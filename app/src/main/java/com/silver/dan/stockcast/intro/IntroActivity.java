package com.silver.dan.stockcast.intro;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.model.SliderPage;
import com.silver.dan.stockcast.R;

/**
 * Created by dan on 12/30/17.
 */

public class IntroActivity extends AppIntro {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.hide();
        }

        // Note here that we DO NOT use setContentView();

        // Add your slide fragments here.
        // AppIntro will automatically generate the dots indicator and buttons.

        SliderPage pageCast = new SliderPage();
        pageCast.setTitle("Cast to your TV");
        pageCast.setDescription("Use the Chromecast icon in the toolbar. Make sure you're on Wi-Fi!");
        pageCast.setImageDrawable(R.drawable.app_intro_cast);
        pageCast.setBgColor(getResources().getColor(R.color.primary_text));

        SliderPage pageMultiSelect = new SliderPage();
        pageMultiSelect.setTitle("Compare stocks");
        pageMultiSelect.setDescription("Tap stock to view. Hold to compare");
        pageMultiSelect.setImageDrawable(R.drawable.multi_select_demo);
        pageMultiSelect.setBgColor(getResources().getColor(R.color.primary_dark));

        addSlide(AppIntroFragment.newInstance(pageCast));
        addSlide(AppIntroFragment.newInstance(pageMultiSelect));

        // Hide Skip/Done button.
        showSkipButton(false);
        setProgressButtonEnabled(true);

        // Turn vibration on and set intensity.
        // NOTE: you will probably need to ask VIBRATE permission in Manifest.
        setVibrate(false);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        finish();
        // Do something when users tap on Skip button.
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // Do something when users tap on Done button.
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }
}