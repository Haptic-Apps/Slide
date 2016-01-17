package me.ccrama.redditslide.Views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.ImageView;

import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;

import java.util.concurrent.ExecutionException;

import me.ccrama.redditslide.Activities.GifView;

/**
 * Created by carlo_000 on 1/16/2016.
 */
public class OpenImgurLink {
    public static void openImgurLink(Context c, String url){
        if(url.endsWith("/")){
            url = url.substring(0, url.length() - 1);
        }

        String hash =  url.substring(url.lastIndexOf("/"), url.length());

        try {
            JsonObject obj =  Ion.with(c).load("https://api.imgur.com/2/image/" + hash + ".json")
                     .asJsonObject().get();

            if(obj.has("error")){
                ((Activity)c).finish();
            } else {
                String type = obj.get("image").getAsJsonObject().get("type").getAsString();
                String urls = obj.get("image").getAsJsonObject().get("links").getAsJsonObject().get("original").getAsString();

                if (type.contains("gif")) {
                    Intent i = new Intent(c, GifView.class);
                    i.putExtra("url", urls);
                    c.startActivity(i);
                    ((Activity) c).finish();


                } else {
                    Intent i = new Intent(c, ImageView.class);
                    i.putExtra("url", urls);
                    c.startActivity(i);
                    ((Activity) c).finish();

                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


    }
}
