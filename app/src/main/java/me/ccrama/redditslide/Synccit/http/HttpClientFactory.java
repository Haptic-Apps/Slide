package me.ccrama.redditslide.Synccit.http;

import static org.chromium.net.CronetEngine.Builder.HTTP_CACHE_DISK;

import android.content.Context;
import android.util.Log;

import org.chromium.net.ExperimentalCronetEngine;
import org.chromium.net.RequestFinishedInfo;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import me.ccrama.redditslide.BuildConfig;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Synccit.http.cronet.CronetInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.internal.Util;

public class HttpClientFactory {

    @SuppressWarnings("unused")
    private static final String TAG = HttpClientFactory.class.getSimpleName();

    private static volatile OkHttpClient client;
    private static final int SOCKET_OPERATION_TIMEOUT = 60 * 1000;
    private static final List<Protocol> PROTOCOLS = Util.immutableList(Protocol.HTTP_1_1);
    private static volatile ExperimentalCronetEngine cronetEngine;


   public static OkHttpClient getOkHttpClient() {
        if (client == null) {
            synchronized (HttpClientFactory.class) {
                if (client == null) {
                    client = createOkHttpClient();
                }
            }

        }
        return client;
    }

    static ExperimentalCronetEngine getCronetEngine() {
        if (cronetEngine == null) {
            synchronized (HttpClientFactory.class) {
                if (cronetEngine == null) {
                  cronetEngine=  createCronetEngine();
                  if(BuildConfig.DEBUG){
                      cronetEngine.addRequestFinishedListener(new RequestFinishedInfo.Listener(Executors.newSingleThreadExecutor()) {
                          @Override
                          public void onRequestFinished(RequestFinishedInfo requestInfo) {
                              if (requestInfo.getResponseInfo()!=null){
                                  Log.e("Cronet", requestInfo.getResponseInfo().getNegotiatedProtocol()+"  "+requestInfo.getUrl());
                              }

                          }
                      });
                  }


                }
            }
        }
        return cronetEngine;
    }

    private static OkHttpClient createOkHttpClient() {
        return new OkHttpClient.Builder()
                .protocols(PROTOCOLS)
                .connectTimeout(SOCKET_OPERATION_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(SOCKET_OPERATION_TIMEOUT, TimeUnit.MILLISECONDS)
                .addInterceptor(CronetInterceptor.newBuilder(getCronetEngine()).build())
                .build();
    }

    private static ExperimentalCronetEngine createCronetEngine() {
        Context context=Reddit.getAppContext();
        return (ExperimentalCronetEngine) new ExperimentalCronetEngine.Builder(context)
                .setStoragePath(context.getExternalCacheDir().getAbsolutePath())
                .enableHttpCache(HTTP_CACHE_DISK, (1024 * 1024 * 50))
                .enableHttp2(true)
                .enableBrotli(true)
                .enableQuic(true)
                .addQuicHint("www.reddit.com", 443, 443)
                .addQuicHint("oauth.reddit.com",443,443)
                .addQuicHint("preview.redd.it",443,443)
                .build();
    }
}

