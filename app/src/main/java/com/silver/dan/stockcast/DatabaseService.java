package com.silver.dan.stockcast;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.silver.dan.stockcast.callbacks.SimpleCallbackNoType;
import com.silver.dan.stockcast.callbacks.StreamingCallback;
import com.silver.dan.stockcast.db.ChartRangeEnum;
import com.silver.dan.stockcast.db.DisplayMode;
import com.silver.dan.stockcast.db.UserData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.silver.dan.stockcast.MainActivity.TAG;

/**
 * Created by dan on 12/23/17.
 */

public class DatabaseService {
    private static DatabaseReference mDatabase;
    private static DatabaseReference mUserReference;

    DatabaseService(Context context) {
        FirebaseUser user = AuthHelper.with(context).getFirebaseUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mUserReference = mDatabase
                .child("users").child(user.getUid());
    }



    void setFocusedStock(final Stock focusedStock) {
        setFocusedStocks(new ArrayList<>(Collections.singletonList(focusedStock)));
    }

    void setFocusedStocks(List<Stock> selectedStocks) {
        List<String> selectedStockTickers = new ArrayList<>();

        for (Stock s : selectedStocks) {
            selectedStockTickers.add(s.getDatabaseKey());
        }

        StringBuilder csvBuilder = new StringBuilder();

        for(String ticker : selectedStockTickers){
            csvBuilder.append(ticker);
            csvBuilder.append(",");
        }

        String focusedStocks = csvBuilder.toString();

        // Remove last comma
        if (selectedStockTickers.size() > 0)
            focusedStocks = focusedStocks.substring(0, focusedStocks.length() - 1);

        mUserReference
                .child("displayModeConfig")
                .child("focusedStock")
                .setValue(focusedStocks);
    }

    void updateChartRange(ChartRangeEnum rangeFlag) {
        mUserReference
                .child("chartConfig")
                .child("range")
                .child("rangeFlag")
                .setValue(rangeFlag.value);
    }

    void getStockInfo(final Stock stock, final SimpleCallbackNoType callback) {
        mDatabase
            .child("stocks")
            .child(stock.getDatabaseKey())
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Stock stockWithData = dataSnapshot.getValue(Stock.class);
                    if (stockWithData == null) {
                        callback.onError(new Exception("Error fetching cached stock"));
                        return;
                    }

                    stock.company = stockWithData.company;
                    stock.exchange = stockWithData.exchange;
                    callback.onComplete();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    callback.onError(databaseError.toException());
                }
            });
    }

    void addListener(final StreamingCallback<UserData> listener) {
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserData data = dataSnapshot.getValue(UserData.class);
                listener.onData(data);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                listener.onError(databaseError.toException());
            }
        };

        mUserReference.addValueEventListener(postListener);
    }

    void saveNewStock(Stock stock) {
        mUserReference
                .child("stocks")
                .child(stock.getDatabaseKey())
                .setValue(true); // https://stackoverflow.com/a/41781692/2517012
    }

    void removeUserStock(Stock stock) {
        mUserReference
                .child("stocks")
                .child(stock.getDatabaseKey())
                .removeValue();
    }

    public void updateDisplayMode(DisplayMode displayMode) {
        mUserReference
                .child("displayModeConfig")
                .child("displayMode")
                .setValue(displayMode.value);
    }

    public void updateSlideshowSpeed(Integer speed) {
        mUserReference
                .child("displayModeConfig")
                .child("slideshowDuration")
                .setValue(speed);

    }
}
