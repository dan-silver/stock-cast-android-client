package com.silver.dan.stockcast;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.silver.dan.stockcast.callbacks.MultiSelectModeChangeListener;
import com.silver.dan.stockcast.callbacks.SimpleCallbackNoType;
import com.silver.dan.stockcast.callbacks.StockSelectedCallback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by dan on 12/23/17.
 */

public class StockListAdapter extends RecyclerView.Adapter<StockListAdapter.StockViewHolder> {

    private static List<Stock> stockList = new ArrayList<>();
    private static boolean inMultiSelectMode = false;
    private MultiSelectModeChangeListener multiSelectModeChangeListener;

    private DatabaseService db;
    private List<MultiSelectModeChangeListener> multiSelectModeListeners = new ArrayList<>();
    private int MAX_STOCKS_SELECTED = 4;

    StockListAdapter(DatabaseService db, MultiSelectModeChangeListener multiSelectModeChangeListener) {
        this.db = db;
        this.multiSelectModeChangeListener = multiSelectModeChangeListener;
    }

    void addStock(final Stock stock) {
        stockList.add(stock);
        notifyItemInserted(stockList.indexOf(stock));

        if (stock.company == null) {
            db.getStockInfo(stock, new SimpleCallbackNoType() {
                @Override
                public void onComplete() {
                    stock.onInfoUpdated();
                }

                @Override
                public void onError(Exception e) {

                }
            });
        }
    }

    void deleteStock(Stock stock) {
        int index = stockList.indexOf(stock);
        stockList.remove(stock);
        notifyItemRemoved(index);
    }

    Stock findStockInListByTicker(Collection<Stock> stocks, String stockDbKey) {
        for (Stock s : stocks) {
            if (s.getDatabaseKey().equals(stockDbKey)) {
                return s;
            }
        }
        return null;
    }

    public void syncStockList(Collection<Stock> stocksToAdd) {
        List<Stock> toAdd = new ArrayList<>();
        List<Stock> toRemove = new ArrayList<>();

        toAdd.addAll(stocksToAdd);
        for (Stock stock : stockList) {

            Stock s = findStockInListByTicker(stocksToAdd, stock.getDatabaseKey());
            if (s != null) {
                // already have it
                toAdd.remove(s);
                continue;
            } else {
                // shouldn't have it
                toRemove.add(stock);
                toAdd.remove(stock);
            }
        }

        for (Stock stock : toAdd) {
            this.addStock(stock);
        }

        for (Stock stock : toRemove) {
            this.deleteStock(stock);
        }
    }

    public void actionModeDismissed() {
        setInMultiSelectMode(false);

        Stock firstSelectedStock = null;

        for (Stock s : stockList) {
            if (firstSelectedStock == null && s.isSelected()) {
                firstSelectedStock = s;
                continue;
            }
            if (firstSelectedStock != null) {
                s.setIsSelected(false);
            }
        }
        db.setFocusedStocks(getSelectedStocks());
    }

    public void deleteSelectedStocks() {
        List<Stock> selectedStocks = getSelectedStocks();

        for (Stock stock : selectedStocks) {
            db.removeUserStock(stock); // database
            this.deleteStock(stock); // UI list
        }
    }

    class StockViewHolder extends RecyclerView.ViewHolder {
        TextView topHeader;
        TextView bottomHeader;
        View listItemView;
        CheckBox multiSelectStockBtn;

        StockViewHolder(View view) {
            super(view);
            this.topHeader = view.findViewById(R.id.widget_name);
            this.bottomHeader = view.findViewById(R.id.widget_type);
            this.multiSelectStockBtn = view.findViewById(R.id.multi_select_stocks);
            this.listItemView = view;
        }
    }

    @Override
    public StockViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_row, viewGroup, false);

        return new StockViewHolder(view);
    }

    void deselectAllStocks() {
        for (Stock s : stockList) {
            s.setIsSelected(false);
        }
    }

    void updateStockViewHeaders(StockViewHolder customViewHolder, Stock stock) {
        customViewHolder.topHeader.setText(stock.symbol);
        customViewHolder.bottomHeader.setText(stock.company);
    }

    // Multi-select mode handlers
    void addMultiSelectModeChangeListener(MultiSelectModeChangeListener multiSelectModeChangeListener) {
        this.multiSelectModeListeners.add(multiSelectModeChangeListener);
    }

    void setInMultiSelectMode(boolean inMultiSelectMode) {
        this.inMultiSelectMode = inMultiSelectMode;
        for (MultiSelectModeChangeListener listener : this.multiSelectModeListeners) {
            listener.onMultiSelectModeChange(inMultiSelectMode);
        }

        // pass up to fragment
        if (inMultiSelectMode)
            this.multiSelectModeChangeListener.onMultiSelectModeChange(inMultiSelectMode);
    }

    @Override
    public void onBindViewHolder(final StockViewHolder customViewHolder, int i) {
        final Stock stock = stockList.get(i);
        final int greyColor = customViewHolder.listItemView.getContext().getResources().getColor(R.color.list_item_selected);
        final int transparentColor = Color.TRANSPARENT;

        MultiSelectModeChangeListener multiSelectModeChangeListener = new MultiSelectModeChangeListener() {
            @Override
            public void onMultiSelectModeChange(boolean inMultiSelectMode) {
                customViewHolder.multiSelectStockBtn.setVisibility(inMultiSelectMode ? View.VISIBLE : View.GONE);
            }
        };

        addMultiSelectModeChangeListener(multiSelectModeChangeListener);
        multiSelectModeChangeListener.onMultiSelectModeChange(inMultiSelectMode);

        stock.setOnStockSelectionChangeListener(new StockSelectedCallback() {
            @Override
            public void onSelectionChange(boolean isSelected) {
//                if (!updateBackgroundColorOnly) {
                customViewHolder.multiSelectStockBtn.setChecked(isSelected);
//                }

                customViewHolder.listItemView.setBackgroundColor(isSelected ? greyColor : transparentColor);
            }
        });

        stock.setOnStockInfoUpdatedCallback(new SimpleCallbackNoType() {
            @Override
            public void onComplete() {
                updateStockViewHeaders(customViewHolder, stock);
            }

            @Override
            public void onError(Exception e) {
                // @todo
            }
        });

        updateStockViewHeaders(customViewHolder, stock);

        // single stock select
        customViewHolder.listItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseService db = new DatabaseService(v.getContext());

                if (!inMultiSelectMode) {
                    db.setFocusedStock(stock);
                    deselectAllStocks();
                }

                stock.toggleSelected();

                if (inMultiSelectMode) {

                    // ensure up to max 4 stocks selected
                    List<Stock> selectedStocks = getSelectedStocks();
                    if (selectedStocks.size() > MAX_STOCKS_SELECTED) {
                        Collections.shuffle(selectedStocks);
                        for (Stock selectedStock : selectedStocks) {
                            if (selectedStock != stock) {
                                // deselect a different stock
                                selectedStock.toggleSelected();
                                break;
                            }
                        }
                    }


                    db.setFocusedStocks(getSelectedStocks());
                }
            }
        });



        customViewHolder.multiSelectStockBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                // you might keep a reference to the CheckBox to avoid this class cast
                boolean checked = ((CheckBox)v).isChecked();
                DatabaseService db = new DatabaseService(v.getContext());
                stock.setIsSelected(checked);
                db.setFocusedStocks(getSelectedStocks());

                //                setSomeBoolean(checked);
            }

        });

        // long click -> select current and show all checkboxes
        customViewHolder.listItemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (inMultiSelectMode) {
                    stock.toggleSelected();
                } else {
                    // launch multiselect mode
                    setInMultiSelectMode(true);
                    // deselect all other stocks
                    for (Stock s : stockList) {
                        if (s == stock) continue;
                        s.setIsSelected(false);
                    }

                    stock.setIsSelected(true);
                }

                return true;
            }
        });

        // select additional stock
        customViewHolder.multiSelectStockBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                DatabaseService db = new DatabaseService(compoundButton.getContext());
                stock.setIsSelectedUIOnly(b);
//                db.setFocusedStocks(getSelectedStocks());
            }
        });
    }

    List<Stock> getSelectedStocks() {
        List<Stock> selectedStocks = new ArrayList<>();
        for (Stock s : stockList) {
            if (s.isSelected()) {
                selectedStocks.add(s);
            }
        }
        return selectedStocks;
    }

    @Override
    public int getItemCount() {
        return (stockList == null ? 0 : stockList.size());
    }
}