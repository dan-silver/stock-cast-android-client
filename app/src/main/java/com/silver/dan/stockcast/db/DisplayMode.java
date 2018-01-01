package com.silver.dan.stockcast.db;

import com.silver.dan.stockcast.R;

/**
 * Created by dan on 12/26/17.
 */

public enum DisplayMode {
    FOCUS(0, R.string.Focus),
    SlideShow(1, R.string.Slideshow);

    public int value;
    public int stringRes;

    DisplayMode(int i, int stringRes) {
        this.value = i;
        this.stringRes = stringRes;
    }

    public static DisplayMode fromInt(int displayMode) {
        switch (displayMode) {
            case 0:
                return DisplayMode.FOCUS;
            case 1:
                return DisplayMode.SlideShow;
        }

        return null;
    }
}