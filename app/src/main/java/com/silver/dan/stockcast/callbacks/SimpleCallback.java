package com.silver.dan.stockcast.callbacks;

/**
 * Created by dan on 8/12/17.
 */

public interface SimpleCallback<T> {
    void onComplete(T result);
    void onError(Exception e);
}

