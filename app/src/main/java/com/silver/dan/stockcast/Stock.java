package com.silver.dan.stockcast;

import com.silver.dan.stockcast.callbacks.SimpleCallbackNoType;
import com.silver.dan.stockcast.callbacks.StockSelectedCallback;

public class Stock {
    public String symbol;
    public String company;
    public String exchange;

    private boolean isSelected = false;
    private StockSelectedCallback onSelectionChange;
    private SimpleCallbackNoType onStockInfoUpdated;

    public Stock() {

    }

    public void setOnStockInfoUpdatedCallback(SimpleCallbackNoType callback) {
        this.onStockInfoUpdated = callback;
    }

    public Stock(String ticker) {
        this.symbol = ticker;
    }

    public Stock(String symbol, String exchange) {
        this.symbol = symbol;
        this.exchange = exchange;
    }


    public Stock(String symbol, String company, String exchange) {
        this.symbol = symbol;
        this.company = company;
        this.exchange = exchange;
    }

    public void toggleSelected() {
        setIsSelected(!this.isSelected);
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    public void setIsSelected(boolean b) {
        this.isSelected = b;
        if (onSelectionChange != null) {
            onSelectionChange.onSelectionChange(isSelected);
        }
    }

    public void setOnStockSelectionChangeListener(StockSelectedCallback onSelectionChange) {
        this.onSelectionChange = onSelectionChange;
        onSelectionChange.onSelectionChange(this.isSelected);
    }

    public void onInfoUpdated() {
        if (this.onStockInfoUpdated != null)
            this.onStockInfoUpdated.onComplete();
    }

    public String getDatabaseKey() {
        return this.exchange + ":" + this.symbol;
    }

    public void setIsSelectedUIOnly(boolean b) {
        this.isSelected = b;
    }
}