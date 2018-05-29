package me.ccrama.redditslide.Synccit.http;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.internal.Util;

class HttpClientFactory {

    @SuppressWarnings("unused")
    private static final String TAG = HttpClientFactory.class.getSimpleName();

    private static OkHttpClient client;
    private static final int            SOCKET_OPERATION_TIMEOUT = 60 * 1000;
    private static final List<Protocol> PROTOCOLS                =
            Util.immutableList(Protocol.HTTP_1_1);

    synchronized static OkHttpClient getOkHttpClient() {
        if (client == null) {
            client = createOkHttpClient();
        }
        return client;
    }

    private static OkHttpClient createOkHttpClient() {
        return new OkHttpClient.Builder().protocols(PROTOCOLS)
                .connectTimeout(SOCKET_OPERATION_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(SOCKET_OPERATION_TIMEOUT, TimeUnit.MILLISECONDS)
                .build();
    }
}

