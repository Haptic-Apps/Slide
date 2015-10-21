package me.ccrama.redditslide.Activities;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.GridView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import net.dean.jraw.models.CommentNode;

import java.util.ArrayList;

import me.ccrama.redditslide.Adapters.AlbumView;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Views.PreCachingLayoutManager;


/**
 * Created by ccrama on 3/5/2015.
 */
public class Album extends BaseActivity {
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);

        setContentView(R.layout.album);

        final Toolbar b = (Toolbar) findViewById(R.id.toolbar);
        b.setTitle("Loading album...");
        setSupportActionBar(b);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        String rawDat = cutEnds(getIntent().getExtras().getString("url", ""));
        String rawdat2 = rawDat;
        if(rawdat2.substring(rawDat.lastIndexOf("/"), rawdat2.length()).length() < 4){
            rawDat = rawDat.replace(rawDat.substring(rawDat.lastIndexOf("/"), rawdat2.length()), "");
        }
        if(rawDat.isEmpty()){
            finish();
        } else {

                new AsyncImageLoader().execute(getHash(rawDat));

        }

    }

    private String getHash(String s){
        String next = s.substring(s.lastIndexOf("/"), s.length());
        if(next.length() < 5){
            return getHash(s.replace(next, ""));
        } else {
            return next;
        }

    }
    private String cutEnds(String s){
        if(s.endsWith("/")){
            return s.substring(0, s.length() - 1);
        } else {
            return s;
        }
    }

    public GridView gridView;

    private class AsyncImageLoader extends AsyncTask<String, Void, Void> {


        CommentNode top;



        @Override
        protected Void doInBackground(String... sub) {
                Log.v("Slide", "http://api.imgur.com/2/album" + sub[0] + ".json");
                Ion.with(Album.this)
                        .load("http://api.imgur.com/2/album" + sub[0] + ".json")
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                Log.v("Slide", result.toString());


                                ArrayList<JsonElement> jsons = new ArrayList<>();

                                if (result.has("album")) {
                                    if (result.get("album").getAsJsonObject().has("title") && !result.get("album").isJsonNull() && !result.get("album").getAsJsonObject().get("title").isJsonNull()) {
                                        getSupportActionBar().setTitle(result.get("album").getAsJsonObject().get("title").getAsString());
                                    }
                                    JsonObject obj = result.getAsJsonObject("album");
                                    if (obj != null && !obj.isJsonNull() && obj.has("images")) {

                                        final JsonArray jsonAuthorsArray = obj.get("images").getAsJsonArray();

                                        for (JsonElement o : jsonAuthorsArray) {
                                            jsons.add(o);
                                        }


                                        RecyclerView v = (RecyclerView) findViewById(R.id.images);
                                        final PreCachingLayoutManager mLayoutManager;
                                        mLayoutManager = new PreCachingLayoutManager(Album.this);
                                        v.setLayoutManager(mLayoutManager);
                                        v.setAdapter(new AlbumView(Album.this, jsons));

                                    } else {

                                        new AlertDialogWrapper.Builder(Album.this)
                                                .setTitle("Album not found...")
                                                .setMessage("An error occured when loading this album. Please re-open the album and retry. If this problem persists, please report to /r/slideforreddit")
                                                .setCancelable(false)
                                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        finish();
                                                    }
                                                }).create().show();
                                    }
                                } else {

                                    new AlertDialogWrapper.Builder(Album.this)
                                            .setTitle("Album not found...")
                                            .setMessage("An error occured when loading this album. Please re-open the album and retry. If this problem persists, please report to /r/slideforreddit")
                                            .setCancelable(false)
                                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    finish();
                                                }
                                            }).create().show();
                                }
                            }
                        });

            return null;

        }


    }


}