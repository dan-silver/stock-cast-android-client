package com.silver.dan.stockcast;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.silver.dan.stockcast.callbacks.MultiSelectModeChangeListener;
import com.silver.dan.stockcast.callbacks.StreamingCallback;
import com.silver.dan.stockcast.db.UserData;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by dan on 12/23/17.
 */

public class StockListFragment extends Fragment {

    public static int RESULT_CODE_STOCK_SEARCH = 895;

    DatabaseService mDb;

    @BindView(R.id.stock_list_recycler_view)
    RecyclerView mStockListView;

    private static StockListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private LayoutInflater mInflater;
    private ViewGroup mContainer;
    private ViewGroup placeholder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDb = new DatabaseService(this.getContext());
        mDb.addListener(new StreamingCallback<UserData>() {
            @Override
            public void onData(UserData result) {
                initializeListView(true);

                List<Stock> stocks = new ArrayList<>();
                if (result == null) {
                    return;
                }
                for (String tickerAndExchange : result.stocks.keySet()) {
                    String[] exchangeAndSymbol = tickerAndExchange.split(":");
                    Stock s;
                    if (exchangeAndSymbol.length > 1) {
                        s = new Stock(exchangeAndSymbol[1], exchangeAndSymbol[0]);
                    } else {
                        s = new Stock(tickerAndExchange);
                    }
                    stocks.add(s);
                }

                mAdapter.syncStockList(stocks);
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    public View initializeListView(boolean replacingPlaceholder) {
        View view = mInflater.inflate(R.layout.fragment_stock_list, mContainer, false);
        ButterKnife.bind(this, view);

        if (replacingPlaceholder && placeholder != null) {
            placeholder.removeAllViews();
            placeholder.addView(view);
        }

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this.getContext());
        mStockListView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        if (mAdapter == null) {
            mAdapter = new StockListAdapter(mDb, new MultiSelectModeChangeListener() {
                @Override
                public void onMultiSelectModeChange(boolean inMultiSelectMode) {
                    if (inMultiSelectMode) {
                        ((AppCompatActivity) getActivity()).startSupportActionMode(new ActionMode.Callback() {
                            @Override
                            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                                mode.getMenuInflater().inflate(R.menu.multiselect_menu, menu);
                                ((MainActivity) getActivity()).setMultiSelectMode(true);
                                return true;
                            }

                            @Override
                            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                                return false;
                            }

                            @Override
                            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.delete_stocks:
                                        mAdapter.deleteSelectedStocks();
                                        return true;
                                    default:
                                        return false;
                                }
                            }

                            @Override
                            public void onDestroyActionMode(ActionMode mode) {
                                ((MainActivity) getActivity()).setMultiSelectMode(false);
                                mAdapter.actionModeDismissed();
                            }
                        });
                    }
                }
            });
        }
        mStockListView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        mInflater = inflater;
        mContainer = container;

        View v;
        if (mAdapter == null) {
            v = inflater.inflate(R.layout.loading, container, false);
            placeholder = (ViewGroup) v;
        } else {
            v = initializeListView(false);
        }

        return v;
    }

    @OnClick(R.id.add_stock_fab)
    public void onAddStockClick() {
        Intent myIntent = new Intent(this.getContext(), StockSearchActivity.class);
        this.startActivityForResult(myIntent, RESULT_CODE_STOCK_SEARCH);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == StockListFragment.RESULT_CODE_STOCK_SEARCH) {
            if (resultCode == Activity.RESULT_OK) {
                String ticker = data.getStringExtra("symbol");
                String companyName = data.getStringExtra("company");
                String exchange = data.getStringExtra("exchange");

                Stock s = new Stock(ticker, companyName, exchange);
                mAdapter.addStock(s);

                // save to db
                DatabaseService db = new DatabaseService(getContext());
                db.saveNewStock(s);
            }
        }
    }
}