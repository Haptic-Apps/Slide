package me.ccrama.redditslide.Synccit.http;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public abstract class HttpPostTask<Result> extends AsyncTask<String, Long, Result> {

    private static final String TAG                       = HttpPostTask.class.getSimpleName();
    private static final int    CONNECTION_TIMEOUT_MILLIS = 5000;
    private static final int    SOCKET_TIMEOUT_MILLIS     = 20000;

    private final OkHttpClient mClient = HttpClientFactory.getOkHttpClient()
            .newBuilder()
            .readTimeout(SOCKET_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
            .connectTimeout(CONNECTION_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
            .build();

    private String mUrl;

    /**
     * @param url Required.
     */
    public HttpPostTask(String url) {
        mUrl = url;
    }

    /**
     * params come in pairs: key/value
     */
    @Override
    protected Result doInBackground(String... params) {

        List<Pair<String, String>> nvps = getPostArgs(params);
        MultipartBody.Builder formBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        for (Pair<String, String> p : nvps) {
            formBuilder.addFormDataPart(p.first, p.second);
        }

        Request request = new Request.Builder().url(mUrl)
                .header("User-Agent", getUserAgent())
                .addHeader("Content-Encoding", "gzip")
                .post(formBuilder.build())
                .build();
        try {
            Response res = mClient.newCall(request).execute();
            if (!res.isSuccessful()) {
                throw new IOException("Unexpected code " + res);
            }

            return onInput(res.body().string());

        } catch (Exception ex) {
            Log.e(TAG, "Error during POST", ex);
        }
        return null;
    }

    private List<Pair<String, String>> getPostArgs(String... params) {
        List<Pair<String, String>> nvps = new ArrayList<>();

        for (int i = 0; i < params.length; i += 2) {
            try {
                nvps.add(new Pair<>(params[i], params[i + 1]));
            } catch (ArrayIndexOutOfBoundsException ex) {
                Log.e(TAG, "Params didn't come in name/value pairs", ex);
            }
        }
        return nvps;
    }

    protected Result onInput(String in) throws Exception {
        return null;
    }

    protected abstract String getUserAgent();

}
