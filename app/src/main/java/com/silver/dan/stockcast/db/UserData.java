package com.silver.dan.stockcast.db;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;

/**
 * Created by dan on 12/23/17.
 */
@IgnoreExtraProperties
public class UserData {

    public HashMap<String, Boolean> stocks = new HashMap<>();
    public ChartConfig chartConfig = new ChartConfig();
    public DisplayModeConfig displayModeConfig = new DisplayModeConfig();


    public UserData() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

}