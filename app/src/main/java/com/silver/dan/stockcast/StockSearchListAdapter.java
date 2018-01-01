package com.silver.dan.stockcast;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.silver.dan.stockcast.callbacks.StockSearchCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dan on 12/28/17.
 */

class StockSearchListAdapter extends RecyclerView.Adapter<StockSearchListAdapter.StockViewHolder> {

    private final StockSearchCallback stockSearchCallback;
    List<Stock> stocks = new ArrayList<>();

    public StockSearchListAdapter(StockSearchCallback callback) {
        this.stockSearchCallback = callback;
    }


    @Override
    public StockSearchListAdapter.StockViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stock_search_item_row, parent, false);

        return new StockSearchListAdapter.StockViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return stocks.size();
    }

    public void setStocksList(List<Stock> stocksList) {
        this.stocks.clear();
        this.stocks.addAll(stocksList);
        notifyDataSetChanged();

    }


    class StockViewHolder extends RecyclerView.ViewHolder {
        TextView topHeader;
        TextView bottomHeader;
        View listItemView;

        StockViewHolder(View view) {
            super(view);
            this.topHeader = view.findViewById(R.id.widget_name);
            this.bottomHeader = view.findViewById(R.id.widget_type);
            this.listItemView = view;
        }
    }

    @Override
    public void onBindViewHolder(final StockViewHolder customViewHolder, int i) {
        final Stock stock = stocks.get(i);

        customViewHolder.topHeader.setText(stock.symbol);
        customViewHolder.bottomHeader.setText(stock.company);

        customViewHolder.listItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stockSearchCallback.onStockSelected(stock);
            }
        });
    }
}
