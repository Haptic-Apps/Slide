package me.ccrama.redditslide.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Carlos on 9/10/2016.
 */

public class NetworkStateReceiver extends BroadcastReceiver {

    protected List<NetworkStateReceiverListener> listeners;
    protected Boolean                            connected;

    public NetworkStateReceiver() {
        listeners = new ArrayList<NetworkStateReceiverListener>();
        connected = null;
    }

    public void onReceive(Context context, Intent intent) {
        if(intent == null || intent.getExtras() == null)
            return;
        connected = NetworkUtil.isConnected(context);
        notifyStateToAll();
    }

    private void notifyStateToAll() {
        for(NetworkStateReceiverListener listener : listeners)
            notifyState(listener);
    }

    private void notifyState(NetworkStateReceiverListener listener) {
        if(connected == null || listener == null)
            return;

        if(connected)
            listener.networkAvailable();
        else
            listener.networkUnavailable();
    }

    public void addListener(NetworkStateReceiverListener l) {
        listeners.add(l);
        notifyState(l);
    }

    public void removeListener(NetworkStateReceiverListener l) {
        listeners.remove(l);
    }

    public interface NetworkStateReceiverListener {
        void networkAvailable();
        void networkUnavailable();
    }
}
