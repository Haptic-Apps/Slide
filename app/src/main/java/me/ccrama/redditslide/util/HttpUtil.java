package me.ccrama.redditslide.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.IOException;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A class that helps with HTTP requests and response parsing.
 *
 * Created by Fernando Barillas on 7/13/16.
 */
public class HttpUtil {

    /**
     * Gets a JsonObject by calling apiUrl and parsing the JSON response String. This method
     * accepts
     * a Map that can contain custom headers to include in the request.
     *
     * @param client     The OkHTTP client to use to make the request
     * @param gson       The GSON instance to use to parse the response String
     * @param apiUrl     The URL to call to get the response from
     * @param headersMap The headers to include in the request. Can be null to not add any headers
     * @return A JsonObject representation of the API response, null when there was an error or
     * Exception thrown by the HTTP call
     */
    public static JsonObject getJsonObject(final OkHttpClient client,
                                           final Gson gson,
                                           final String apiUrl,
                                           @Nullable final Map<String, String> headersMap) {
        if (client == null || gson == null || TextUtils.isEmpty(apiUrl)) return null;
        Request.Builder builder = new Request.Builder().url(apiUrl);

        if (headersMap != null && headersMap.size() > 0) {
            // Add headers to the request if headers are available
            for (Map.Entry<String, String> entry : headersMap.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        Request request = builder.build();
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            String json = response.body().string();
            return gson.fromJson(json, JsonObject.class);
        } catch (IOException e) {
            LogUtil.e(e, "Error " + apiUrl);
        }
        return null;
    }

    /**
     * Gets a JsonObject by calling apiUrl and parsing the JSON response String
     *
     * @param client The OkHTTP client to use to make the request
     * @param gson   The GSON instance to use to parse the response String
     * @param apiUrl The URL to call to get the response from
     * @return A JsonObject representation of the API response, null when there was an error or
     * Exception thrown by the HTTP call
     */
    public static JsonObject getJsonObject(final OkHttpClient client,
                                           final Gson gson,
                                           final String apiUrl) {
        return getJsonObject(client, gson, apiUrl, null);
    }
}
