package me.ccrama.redditslide.util;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import me.ccrama.redditslide.Constants;
import me.ccrama.redditslide.SecretConstants;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

/**
 * A class that helps with HTTP requests and response parsing.
 *
 * Created by Fernando Barillas on 7/13/16.
 */
public class HttpUtil {

    /**
     * Gets a JsonObject by calling apiUrl and parsing the JSON response String. This method should
     * be used when calling the Imgur Mashape API (https://imgur-apiv3.p.mashape.com/) since it
     * requires special headers in the requests.
     *
     * @param client     The OkHTTP client to use to make the request
     * @param gson       The GSON instance to use to parse the response String
     * @param apiUrl     The URL to call to get the response from
     * @param mashapeKey The Mashape API key to use when the request is made
     * @return A JsonObject representation of the API response, null when there was an error or
     * Exception thrown by the HTTP call
     */
    public static JsonObject getImgurMashapeJsonObject(final OkHttpClient client, final Gson gson,
            final String apiUrl, final String mashapeKey) {
        Map<String, String> imgurHeadersMap = new HashMap<>();
        imgurHeadersMap.put("X-Mashape-Key", mashapeKey);
        imgurHeadersMap.put("Authorization", "Client-ID " + Constants.IMGUR_MASHAPE_CLIENT_ID);
        return getJsonObject(client, gson, apiUrl, imgurHeadersMap);
    }

    private static String gfycatToken = "";
    private static long expires;

    public static JsonObject transcodeGfycatGetJsonObject(final Context context, final OkHttpClient client, final Gson gson,
            final String apiUrl) {

        if(System.currentTimeMillis() > expires || gfycatToken.isEmpty()){
            Map<String, String> headers = new HashMap<>();
            headers.put("grant_type", "client_credentials");
            headers.put("client_id", "2_fEI_VM");
            headers.put("content_type", "application/json");
            headers.put("client_secret", SecretConstants.getGyfcatAPIKey(context));
            JsonObject o = getPostJsonObject(client, gson, "https://api.gfycat.com/v1/oauth/token", headers, new HashMap<String, String>());
            if(o.has("expires_in")){
                expires = System.currentTimeMillis()  + o.get("expires_in").getAsLong();
                gfycatToken = o.get("access_token").getAsString();
            }
        }
        Map<String, String> imgurHeadersMap = new HashMap<>();
        LogUtil.v("Converting " + apiUrl);
        imgurHeadersMap.put("Authorization", gfycatToken);
        imgurHeadersMap.put("content_type", "application/json");
        JsonObject o =  getPostJsonObject(client, gson, "https://api.gfycat.com/v1/gfycats", new HashMap<String, String>(){{put("fetchUrl", apiUrl); put("noMd5", "false");}}, imgurHeadersMap);

        imgurHeadersMap.clear();
        imgurHeadersMap.put("Authorization", gfycatToken);
        imgurHeadersMap.put("content_type", "application/json");

        String gfyname = o.get("gfyname").getAsString();
        LogUtil.v(gfyname);

        int seconds = 0;
        while(seconds < 45) {

            JsonObject o2 =
                    getJsonObject(client, gson, "https://api.gfycat.com/v1/gfycats/fetch/status/" + gfyname, imgurHeadersMap);
            if(o2.get("task").getAsString().equals("complete")){
                return o2;
            } else if(o2.get("task").getAsString().equals("NotFoundo")){
                return null;
            }
            try {
                Thread.sleep(500);
                seconds += 1;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    /**
     * Gets a JsonObject by calling apiUrl and parsing the JSON response String. This method accepts
     * a Map that can contain custom headers to include in the request.
     *
     * @param client     The OkHTTP client to use to make the request
     * @param gson       The GSON instance to use to parse the response String
     * @param apiUrl     The URL to call to get the response from
     * @param headersMap The headers to include in the request. Can be null to not add any headers
     * @return A JsonObject representation of the API response, null when there was an error or
     * Exception thrown by the HTTP call
     */
    public static JsonObject getJsonObject(final OkHttpClient client, final Gson gson,
            final String apiUrl, @Nullable final Map<String, String> headersMap) {
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
            ResponseBody responseBody = response.body();
            String json = responseBody.string();
            responseBody.close();
            return gson.fromJson(json, JsonObject.class);
        } catch (JsonSyntaxException | IOException e) {
            LogUtil.e(e, "Error " + apiUrl);
        }
        return null;
    }


    public static JsonObject getPostJsonObject(final OkHttpClient client, final Gson gson,
            final String apiUrl, @Nullable final Map<String, String> bodyMap, @Nullable final Map<String, String> headersMap) {
        if (client == null || gson == null || TextUtils.isEmpty(apiUrl)) return null;
        MediaType MIMEType= MediaType.parse("application/json; charset=utf-8");
        String body = "";

        if(!bodyMap.isEmpty()) {
            for (String s : bodyMap.keySet()) {
                body = body + "\"" + s + "\"" + ":" + "\"" + bodyMap.get(s) + "\"" + ",";
            }
            body = body.substring(0, body.length() - 1);
        }
        RequestBody requestBody = RequestBody.create (MIMEType,"{" + body+"}");
        Request.Builder requestB = new Request.Builder().url(apiUrl);
        if (headersMap != null && headersMap.size() > 0) {
            // Add headers to the request if headers are available
            for (Map.Entry<String, String> entry : headersMap.entrySet()) {
                requestB.addHeader(entry.getKey(), entry.getValue());
            }
        }


        Request request= requestB.post(requestBody).build();

        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            ResponseBody responseBody = response.body();
            String json = responseBody.string();
            responseBody.close();
            return gson.fromJson(json, JsonObject.class);
        } catch (JsonSyntaxException | IOException e) {
            LogUtil.e(e, "Error " + apiUrl);
        }
        return null;
    }
    private static String bodyToString(final RequestBody request){
        try {
            final RequestBody copy = request;
            final Buffer buffer = new Buffer();
            copy.writeTo(buffer);
            return buffer.readUtf8();
        }
        catch (final IOException e) {
            return "did not work";
        }
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
    public static JsonObject getJsonObject(final OkHttpClient client, final Gson gson,
            final String apiUrl) {
        return getJsonObject(client, gson, apiUrl, null);
    }
}
