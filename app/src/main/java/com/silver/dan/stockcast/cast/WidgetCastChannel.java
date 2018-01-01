package com.silver.dan.stockcast.cast;

import android.util.Log;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.framework.CastSession;
import com.silver.dan.stockcast.MainActivity;

/**
 * Created by dan on 8/12/17.
 */
public class WidgetCastChannel implements Cast.MessageReceivedCallback {
    String namespace;

    public String getNamespace() {
        return namespace;
    }

    public WidgetCastChannel(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public void onMessageReceived(CastDevice castDevice, String namespace,
                                  String message) {
        Log.d(MainActivity.TAG, "onMessageReceived: " + message);
    }

    public void sendMessage(CastSession session, String message) {
        session.sendMessage(this.getNamespace(), message);
    }

}