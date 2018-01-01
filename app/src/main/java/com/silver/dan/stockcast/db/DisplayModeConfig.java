package com.silver.dan.stockcast.db;

/**
 * Created by dan on 12/26/17.
 */

public class DisplayModeConfig {
    public DisplayModeConfig() {

    }
    public String focusedStock;
    public int displayMode = DisplayMode.FOCUS.value;
    public int slideshowDuration = 5;
}
