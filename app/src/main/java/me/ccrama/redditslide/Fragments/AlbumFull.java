package me.ccrama.redditslide.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import me.ccrama.redditslide.Activities.FullscreenImage;
import me.ccrama.redditslide.Activities.GifView;
import me.ccrama.redditslide.Activities.Website;
import me.ccrama.redditslide.Adapters.AlbumView;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Views.PopulateSubmissionViewHolder;
import me.ccrama.redditslide.Views.PreCachingLayoutManager;
import me.ccrama.redditslide.util.LogUtil;


/**
 * Created by ccrama on 6/2/2015.
 */
public class AlbumFull extends Fragment {

    boolean gallery = false;
    View placeholder;
    private View list;
    private int i = 0;
    private Submission s;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.submission_albumcard, container, false);

        TextView title = (TextView) rootView.findViewById(R.id.title);
        TextView desc = (TextView) rootView.findViewById(R.id.desc);

        title.setText(s.getTitle());
        desc.setText(s.getSubredditName() + getString(R.string.submission_properties_seperator) + s.getAuthor() + " " + TimeUtils.getTimeAgo(s.getCreated().getTime(), getContext()) +
                getString(R.string.submission_properties_seperator) +
                PopulateSubmissionViewHolder.getSubmissionScoreString(s.getScore(), getActivity().getResources(), s)
                + getString(R.string.submission_properties_seperator)
                + getActivity().getResources().getQuantityString(R.plurals.submission_comment_count, s.getCommentCount(), s.getCommentCount())
                + getString(R.string.submission_properties_seperator)
                + Website.getDomainName(s.getUrl())) ;
        ContentType.ImageType type = ContentType.getImageType(s);

        String url = "";

        if (s.getUrl().contains("gallery")) {
            gallery = true;
        }

        list = rootView.findViewById(R.id.images);

        list.setVisibility(View.VISIBLE);
        String rawDat = cutEnds(s.getUrl());
        String rawdat2 = rawDat;
        if (rawdat2.substring(rawDat.lastIndexOf("/"), rawdat2.length()).length() < 4) {
            rawDat = rawDat.replace(rawDat.substring(rawDat.lastIndexOf("/"), rawdat2.length()), "");
        }
        if (!rawDat.isEmpty()) {

            new AsyncImageLoaderAlbum().execute(getHash(rawDat));
        }


        rootView.findViewById(R.id.base).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SettingValues.tabletUI && getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    Intent i2 = new Intent(getActivity(), CommentsScreenPopup.class);
                    i2.putExtra(CommentsScreenPopup.EXTRA_PAGE, i);
                    (getActivity()).startActivity(i2);

                } else {
                    Intent i2 = new Intent(getActivity(), CommentsScreen.class);
                    i2.putExtra(CommentsScreen.EXTRA_PAGE, i);
                    i2.putExtra(CommentsScreen.EXTRA_SUBREDDIT, s.getSubredditName());
                    (getActivity()).startActivity(i2);
                }
            }
        });
        return rootView;
    }

    private String getHash(String s) {
        String next = s.substring(s.lastIndexOf("/"), s.length());
        if (next.length() < 5) {
            return getHash(s.replace(next, ""));
        } else {
            return next;
        }

    }

    private String cutEnds(String s) {
        if (s.endsWith("/")) {
            return s.substring(0, s.length() - 1);
        } else {
            return s;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        i = bundle.getInt("page", 0);

        s = new OfflineSubreddit(bundle.getString("sub")).submissions.get(bundle.getInt("page", 0));

    }

    private class AsyncImageLoaderAlbum extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... sub) {
            if (gallery) {
                Ion.with(getActivity())
                        .load("https://imgur.com/gallery/" + sub[0] + ".json")
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                Log.v(LogUtil.getTag(), result.toString());


                                ArrayList<JsonElement> jsons = new ArrayList<>();

                                if (result.has("data")) {

                                    if (!result.getAsJsonObject("data").getAsJsonObject("image").get("is_album").getAsBoolean()) {
                                        if (result.getAsJsonObject("data").getAsJsonObject("image").get("mimetype").getAsString().contains("gif")) {
                                            Intent i = new Intent(getActivity(), GifView.class);
                                            i.putExtra(GifView.EXTRA_URL, "http://imgur.com/" + result.getAsJsonObject("data").getAsJsonObject("image").get("hash").getAsString() + ".gif"); //could be a gif
                                            startActivity(i);
                                        } else {
                                            Intent i = new Intent(getActivity(), FullscreenImage.class);
                                            i.putExtra(FullscreenImage.EXTRA_URL, "http://imgur.com/" + result.getAsJsonObject("data").getAsJsonObject("image").get("hash").getAsString() + ".png"); //could be a gif
                                            startActivity(i);
                                        }
                                        getActivity().finish();

                                    } else {

                                        JsonArray obj = result.getAsJsonObject("data").getAsJsonObject("image").getAsJsonObject("album_images").get("images").getAsJsonArray();
                                        if (obj != null && !obj.isJsonNull() && obj.size() > 0) {

                                            for (JsonElement o : obj) {
                                                jsons.add(o);
                                            }


                                            RecyclerView v = (RecyclerView) list;
                                            final PreCachingLayoutManager mLayoutManager;
                                            mLayoutManager = new PreCachingLayoutManager(getActivity());
                                            v.setLayoutManager(mLayoutManager);
                                            v.setAdapter(new AlbumView(getActivity(), jsons, true));

                                        }
                                    }
                                } else {

                                    new AlertDialogWrapper.Builder(getActivity())
                                            .setTitle(R.string.album_err_not_found)
                                            .setMessage(R.string.album_err_msg_not_found)
                                            .setCancelable(false)
                                            .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    getActivity().finish();
                                                }
                                            }).create().show();
                                }
                            }

                        });
            } else {
                Log.v(LogUtil.getTag(), "http://api.imgur.com/2/album" + sub[0] + ".json");
                Ion.with(getActivity())
                        .load("http://api.imgur.com/2/album" + sub[0] + ".json")
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                Log.v(LogUtil.getTag(), result.toString());


                                ArrayList<JsonElement> jsons = new ArrayList<>();

                                if (result.has("album")) {

                                    JsonObject obj = result.getAsJsonObject("album");
                                    if (obj != null && !obj.isJsonNull() && obj.has("images")) {

                                        final JsonArray jsonAuthorsArray = obj.get("images").getAsJsonArray();

                                        for (JsonElement o : jsonAuthorsArray) {
                                            jsons.add(o);
                                        }


                                        RecyclerView v = (RecyclerView) list;
                                        final PreCachingLayoutManager mLayoutManager;
                                        mLayoutManager = new PreCachingLayoutManager(getActivity());
                                        v.setLayoutManager(mLayoutManager);
                                        v.setAdapter(new AlbumView(getActivity(), jsons, false));

                                    } else {

                                        new AlertDialogWrapper.Builder(getActivity())
                                                .setTitle(R.string.album_err_not_found)
                                                .setMessage(R.string.album_err_msg_not_found)
                                                .setCancelable(false)
                                                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        getActivity().finish();
                                                    }
                                                }).create().show();
                                    }
                                } else {

                                    new AlertDialogWrapper.Builder(getActivity())
                                            .setTitle(R.string.album_err_not_found)
                                            .setMessage(R.string.album_err_msg_not_found)
                                            .setCancelable(false)
                                            .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    getActivity().finish();
                                                }
                                            }).create().show();
                                }
                            }
                        });
            }

            return null;

        }


    }
}
