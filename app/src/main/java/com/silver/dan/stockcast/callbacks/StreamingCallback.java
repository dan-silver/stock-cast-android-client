package com.silver.dan.stockcast.callbacks;

public interface StreamingCallback<T> {
    void onData(T result);
    void onError(Exception e);
}
