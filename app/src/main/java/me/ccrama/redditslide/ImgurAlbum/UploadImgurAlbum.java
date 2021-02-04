package me.ccrama.redditslide.ImgurAlbum;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.util.ImgurUtils;
import me.ccrama.redditslide.util.ProgressRequestBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class UploadImgurAlbum extends AsyncTask<Uri, Integer, String> {
    public String finalUrl;
    public Context c;
    public int totalCount;
    public int uploadCount;
    public MaterialDialog dialog;

    @Override
    protected String doInBackground(Uri... sub) {
        totalCount = sub.length;
        final OkHttpClient client = Reddit.client;

        String albumurl;
        {
            Request request = new Request.Builder().header("Authorization",
                    "Client-ID bef87913eb202e9")
                    .url("https://api.imgur.com/3/album")
                    .post(new RequestBody() {
                        @Override
                        public MediaType contentType() {
                            return null;
                        }

                        @Override
                        public void writeTo(BufferedSink sink) {
                        }
                    })
                    .build();

            Response response = null;
            try {
                response = client.newCall(request).execute();

                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                JSONObject album = new JSONObject(response.body().string());
                albumurl = album.getJSONObject("data").getString("deletehash");
                finalUrl = "http://imgur.com/a/" + album.getJSONObject("data").getString("id");
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        try {
            MultipartBody.Builder formBodyBuilder =
                    new MultipartBody.Builder().setType(MultipartBody.FORM);
            for (Uri uri : sub) {
                File bitmap = ImgurUtils.createFile(uri, c);
                formBodyBuilder.addFormDataPart("image", bitmap.getName(),
                        RequestBody.create(MediaType.parse("image/*"), bitmap));
                formBodyBuilder.addFormDataPart("album", albumurl);
                MultipartBody formBody = formBodyBuilder.build();

                ProgressRequestBody body =
                        new ProgressRequestBody(formBody, this::publishProgress);

                Request request = new Request.Builder().header("Authorization",
                        "Client-ID bef87913eb202e9")
                        .url("https://api.imgur.com/3/image")
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if (progress < dialog.getCurrentProgress() || uploadCount == 0) {
            uploadCount += 1;
        }
        dialog.setContent("Image " + uploadCount + "/" + totalCount);
        dialog.setProgress(progress);
    }
}
