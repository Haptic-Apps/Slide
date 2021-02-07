package me.ccrama.redditslide.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.core.content.ContextCompat;

import me.ccrama.redditslide.Reddit;

/**
 * Collection of various network utility methods.
 *
 * @author Matthew Dean
 */
public class NetworkUtil {

    // Assigned a random value that is not a value of ConnectivityManager.TYPE_*
    private static final int CONST_NO_NETWORK = 525138;

    private NetworkUtil() {
    }

    /**
     * Uses the provided context to determine the current connectivity status.
     *
     * @param context A context used to retrieve connection information from
     * @return A non-null value defined in {@link Status}
     */
    public static Status getConnectivityStatus(Context context) {
        ConnectivityManager cm = ContextCompat.getSystemService(context, ConnectivityManager.class);

        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        switch (activeNetwork != null ? activeNetwork.getType() : CONST_NO_NETWORK) {
            case ConnectivityManager.TYPE_WIFI: case ConnectivityManager.TYPE_ETHERNET:
                if (cm.isActiveNetworkMetered())
                    return Status.MOBILE; // respect metered wifi networks as mobile
                return Status.WIFI;
            case ConnectivityManager.TYPE_MOBILE: case ConnectivityManager.TYPE_BLUETOOTH: case ConnectivityManager.TYPE_WIMAX:
                return Status.MOBILE;
            default:
                return Status.NONE;
        }
    }

    /**
     * Checks if the network is connected. An application context is said to have connection if
     * {@link #getConnectivityStatus(Context)} does not equal {@link Status#NONE}.
     *
     * @param context The context used to retrieve connection information
     * @return True if the application is connected, false if else.
     */
    public static boolean isConnected(Context context) {
        return !Reddit.appRestart.contains("forceoffline") && getConnectivityStatus(context) != Status.NONE;
    }
    public static boolean isConnectedNoOverride(Context context) {
        return getConnectivityStatus(context) != Status.NONE;
    }
    /**
     * Checks if the network is connected to WiFi.
     *
     * @param context The context used to retrieve connection information
     * @return True if the application is connected, false if else.
     */
    public static boolean isConnectedWifi(Context context) {
        return getConnectivityStatus(context) == Status.WIFI;
    }
    /**
     * A simplified list of connectivity statuses. See {@link ConnectivityManager}'s {@code TYPE_*} for a full list.
     *
     * @author Matthew Dean
     */
    public enum Status {
        /** Operating on a wireless connection */
        WIFI,
        /** Operating on 3G, 4G, 4G LTE, etc. */
        MOBILE,
        /** No connection present */
        NONE
    }
}
