package com.silver.dan.stockcast;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.silver.dan.stockcast.callbacks.StockSearchCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StockSearchActivity extends AppCompatActivity {

    @BindView(R.id.stock_search_results)
    RecyclerView mSearchResultsRecyclerView;

    @BindView(R.id.stock_search_input)
    EditText stockSearchInput;

    @BindView(R.id.loading_layout)
    View loadingIcon;

    private StockSearchListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_stock_search);


        ButterKnife.bind(this);

        loadingIcon.setVisibility(View.GONE);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mSearchResultsRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new StockSearchListAdapter(new StockSearchCallback() {
            @Override
            public void onStockSelected(Stock s) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("symbol", s.symbol);
                returnIntent.putExtra("company", s.company);
                returnIntent.putExtra("exchange", s.exchange);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
        mSearchResultsRecyclerView.setAdapter(mAdapter);

        stockSearchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchForStocks(v.getText().toString());
                    return true;
                }
                return false;
            }
        });


    }

    private void searchForStocks(String query) {
        loadingIcon.setVisibility(View.VISIBLE);
        Ion.with(this)
                .load(this.getApplicationContext().getString(R.string.APP_URL) + "/stockSearch")
                .setBodyParameter("q", query)
                .asJsonArray()
                .withResponse()
                .setCallback(new FutureCallback<Response<JsonArray>>() {

                    @Override
                    public void onCompleted(Exception e, Response<JsonArray> result) {

                        loadingIcon.setVisibility(View.GONE);
                        if ( e != null || result.getException() != null) {

                            // @todo
                            return;
                        }

                        List<Stock> searchResults = new ArrayList<>();
                        JsonArray stockArray = result.getResult();
                        for (JsonElement stock : stockArray) {
                            JsonObject rawStock = stock.getAsJsonObject();
                            String symbol = rawStock.get("symbol").getAsString();
                            String company = rawStock.get("company").getAsString();
                            String exchange = rawStock.get("exchange").getAsString();

                            Stock parsedStock = new Stock(symbol, company, exchange);
                            searchResults.add(parsedStock);
                        }
                        mAdapter.setStocksList(searchResults);
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}