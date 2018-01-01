package com.silver.dan.stockcast;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.silver.dan.stockcast.callbacks.StreamingCallback;
import com.silver.dan.stockcast.db.ChartRangeEnum;
import com.silver.dan.stockcast.db.DisplayMode;
import com.silver.dan.stockcast.db.UserData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AppSettingsTheme extends Fragment implements GoogleApiClient.OnConnectionFailedListener {

    DatabaseService mDb;
    private ChartRangeEnum chartRange;
    private DisplayMode displayMode;
    private int slideshowSpeed;

    @BindView(R.id.chart_range_btn)
    TwoLineSettingItem chartRangeBtn;

    @BindView(R.id.displayModeSetting)
    TwoLineSettingItem displayModeSetting;

    @BindView(R.id.slideshowDuration)
    TwoLineSettingItem slideshowDuration;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Context ctx = this.getContext();

        mDb = new DatabaseService(this.getContext());
        mDb.addListener(new StreamingCallback<UserData>() {
            @Override
            public void onData(UserData result) {
                // for new users, this will be empty. Fall back to app defaults
                if (result == null) {
                    result = new UserData();
                }

                // chart range
                chartRange = ChartRangeEnum.fromInt(result.chartConfig.range.rangeFlag);
                chartRangeBtn.setSubHeaderText(chartRange.stringRes);

                // display mode
                displayMode = DisplayMode.fromInt(result.displayModeConfig.displayMode);
                displayModeSetting.setSubHeaderText(displayMode.stringRes);


                // slideshow duration
                slideshowSpeed = result.displayModeConfig.slideshowDuration;
                slideshowDuration.setSubHeaderText(slideshowSpeed + " " + ctx.getString(R.string.seconds));
                slideshowDuration.setVisibility(displayMode == DisplayMode.SlideShow ? View.VISIBLE : View.GONE);

            }

            @Override
            public void onError(Exception e) {

            }
        });

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.theme_settings, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @OnClick(R.id.chart_range_btn)
    public void selectChartRange() {

        // define array for backwards compat
        final ArrayList<ChartRangeEnum> rangeOptions = new ArrayList<>(Arrays.asList(
                ChartRangeEnum.ONE_WEEK,
                ChartRangeEnum.TWO_WEEKS,
                ChartRangeEnum.ONE_MONTH,
                ChartRangeEnum.TWO_MONTHS
        ));

        int preselectedOption = -1;
        if (rangeOptions.contains(this.chartRange)) {
            preselectedOption = rangeOptions.indexOf(this.chartRange);
        }

        new MaterialDialog.Builder(getContext())
                .title(R.string.chartRange)
                .theme(Theme.LIGHT)
                .items(R.array.chartRangeOptions)
                .itemsCallbackSingleChoice(preselectedOption, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        mDb.updateChartRange(rangeOptions.get(which));
                        return true;
                    }
                })
                .show();
    }


    @OnClick(R.id.displayModeSetting)
    public void selectDisplayMode() {
        // define array for backwards compat
        final ArrayList<DisplayMode> displayModes = new ArrayList<>(Arrays.asList(DisplayMode.FOCUS, DisplayMode.SlideShow));

        int preselectedOption = -1;
        if (displayModes.contains(this.displayMode)) {
            preselectedOption = displayModes.indexOf(this.displayMode);
        }

        new MaterialDialog.Builder(getContext())
                .title(R.string.displayMode)
                .theme(Theme.LIGHT)
                .items(R.array.displayModeOptions)
                .itemsCallbackSingleChoice(preselectedOption, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        mDb.updateDisplayMode(displayModes.get(which));
                        slideshowDuration.setVisibility(displayModes.get(which) == DisplayMode.SlideShow ? View.VISIBLE : View.GONE);
                        return true;
                    }
                })
                .show();
    }

    @OnClick(R.id.slideshowDuration)
    public void setSlideshowDuration() {
        // define array for backwards compat
        final ArrayList<Integer> speedOptions = new ArrayList<>(Arrays.asList(
                3,
                4,
                5,
                6,
                7,
                8,
                9,
                10

        ));

        List<String> speedOptionsStrArr = new ArrayList<>();
        for (Integer speedOption : speedOptions) {
            speedOptionsStrArr.add(speedOption + " " + getContext().getString(R.string.seconds));
        }

        int preselectedOption = -1;
        if (speedOptions.contains(this.slideshowSpeed)) {
            preselectedOption = speedOptions.indexOf(this.slideshowSpeed);
        }

        new MaterialDialog.Builder(getContext())
                .title(R.string.slideshowDuration)
                .theme(Theme.LIGHT)
                .items(speedOptionsStrArr)
                .itemsCallbackSingleChoice(preselectedOption, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        mDb.updateSlideshowSpeed(speedOptions.get(which));
                        return true;
                    }
                })
                .show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

}