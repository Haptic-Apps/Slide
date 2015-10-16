package me.ccrama.redditslide.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import net.dean.jraw.models.Submission;

import java.util.ArrayList;

import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.Activities.CommentsScreenPopup;
import me.ccrama.redditslide.Adapters.AlbumView;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.TimeUtils;


/**
 * Created by ccrama on 6/2/2015.
 */
public class AlbumFull extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.submission_albumcard, container, false);

        TextView title = (TextView) rootView.findViewById(R.id.title);
        TextView desc = (TextView) rootView.findViewById(R.id.desc);

        title.setText(s.getTitle());
        desc.setText(s.getAuthor() + " " + TimeUtils.getTimeAgo(s.getCreatedUtc().getTime()));
        ContentType.ImageType type = ContentType.getImageType(s);

        String url = "";

        list = rootView.findViewById(R.id.images);

            list.setVisibility(View.VISIBLE);
            String rawDat = cutEnds(s.getUrl());
            String rawdat2 = rawDat;
            if(rawdat2.substring(rawDat.lastIndexOf("/"), rawdat2.length()).length() < 4){
                rawDat = rawDat.replace(rawDat.substring(rawDat.lastIndexOf("/"), rawdat2.length()), "");
            }
            if(!rawDat.isEmpty()){

                new AsyncImageLoaderAlbum().execute(getHash(rawDat));
            }



        rootView.findViewById(R.id.base).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Reddit.tabletUI && getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ){
                    Intent i2 = new Intent(getActivity(), CommentsScreenPopup.class);
                    i2.putExtra("page", i);
                    (getActivity()).startActivity(i2);

                } else {
                    Intent i2 = new Intent(getActivity(), CommentsScreen.class);
                    i2.putExtra("page", i);
                    (getActivity()).startActivity(i2);
                }
            }
        });
        return rootView;
    }
    public String getHash(String s){
        String next = s.substring(s.lastIndexOf("/"), s.length());
        if(next.length() < 5){
            return getHash(s.replace(next, ""));
        } else {
            return next;
        }

    }
    View list;
    public String cutEnds(String s){
        if(s.endsWith("/")){
            return s.substring(0, s.length() - 1);
        } else {
            return s;
        }
    }


    private class AsyncImageLoaderAlbum extends AsyncTask<String, Void, Void> {




        @Override
        protected Void doInBackground(String... sub) {
            Log.v("Slide", "http://api.imgur.com/2/album" + sub[0] + ".json");
            Ion.with(getActivity())
                    .load("http://api.imgur.com/2/album" + sub[0] + ".json")
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            Log.v("Slide", result.toString());


                            ArrayList<JsonElement> jsons = new ArrayList<>();

                            if ( result.has("album")) {

                                JsonObject obj = result.getAsJsonObject("album");
                                if (obj != null && !obj.isJsonNull() && obj.has("images")) {

                                    final JsonArray jsonAuthorsArray = obj.get("images").getAsJsonArray();

                                    for (JsonElement o : jsonAuthorsArray) {
                                        jsons.add(o);
                                    }


                                    ListView v = (ListView) list;
                                    v.setAdapter(new AlbumView(getActivity(), jsons));

                                } else {

                                    new AlertDialogWrapper.Builder(getActivity())
                                            .setTitle("Album not found...")
                                            .setMessage("An error occured when loading this album. Please re-open the album and retry. If this problem persists, please report to /r/slideforreddit")
                                            .setCancelable(false)
                                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                }
                                            }).create().show();
                                }
                            } else {

                                new AlertDialogWrapper.Builder(getActivity())
                                        .setTitle("Album not found...")
                                        .setMessage("An error occured when loading this album. Please re-open the album and retry. If this problem persists, please report to /r/slideforreddit")
                                        .setCancelable(false)
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        }).create().show();
                            }
                        }
                    });
            return null;

        }


    }
    int i = 0;
    View placeholder;
    Submission s;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        i = bundle.getInt("page", 0);
        s = DataShare.sharedSubreddit.get(bundle.getInt("page", 0));

    }
}
