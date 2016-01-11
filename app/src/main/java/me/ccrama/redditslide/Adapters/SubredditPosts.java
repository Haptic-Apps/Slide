package me.ccrama.redditslide.Adapters;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.JsonNode;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.SubredditPaginator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Cache;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Views.CreateCardView;
import me.ccrama.redditslide.util.NetworkUtil;

/**
 * Created by ccrama on 9/17/2015.
 */
public class SubredditPosts {
    public ArrayList<Submission> posts;

    public boolean loading;
    public String subreddit;
    public boolean nomore = false;
    CommentsScreen pagerad;
    private SubredditPaginator paginator;
    private SwipeRefreshLayout refreshLayout;
    private SubmissionAdapter adapter;

    public SubredditPosts(ArrayList<Submission> firstData, SubredditPaginator paginator) {
        posts = firstData;
        this.paginator = paginator;
    }

    public SubredditPosts(String subreddit) {
        this.subreddit = subreddit;
    }

    public ArrayList<Submission> getPosts() {
        try {
            return new LoadData(true).execute(subreddit).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void bindAdapter(SubmissionAdapter a, SwipeRefreshLayout layout) throws ExecutionException, InterruptedException {
        this.adapter = a;
        this.refreshLayout = layout;

        loadMore(a, true, subreddit);
    }

    public void loadMore(SubmissionAdapter adapter, boolean reset, String subreddit) {
        this.adapter = adapter;
        new LoadData(reset).execute(subreddit);


    }

    public boolean stillShow;
    public boolean offline;

    public void loadMore(CommentsScreen adapter, boolean reset) {
        this.pagerad = adapter;
        new LoadData(reset).execute(subreddit);

    }
    private static boolean isBlurry(JsonNode s, Context mC, String title) {
        if(Reddit.blurCheck){
            return false;
        } else {
            int pixesl = s.get("preview").get("images").get(0).get("source").get("width").asInt();
            float density = mC.getResources().getDisplayMetrics().density;
            float dp = pixesl / density;
            Configuration configuration = mC.getResources().getConfiguration();
            int screenWidthDp = configuration.screenWidthDp; //The current width of the available screen space, in dp units, corresponding to screen width resource qualifier.

            return dp < screenWidthDp / 3;
        }
    }
    public OfflineSubreddit cached;

    public ArrayList<String> contained;
    public boolean forced;

    public void addData(List<Submission> data) {
        posts.addAll(data);
    }

    public class LoadData extends AsyncTask<String, Void, ArrayList<Submission>> {
        final boolean reset;

        public LoadData(boolean reset) {
            this.reset = reset;
        }

        @Override
        public void onPostExecute(ArrayList<Submission> subs) {
            loading = false;



            if (subs != null && subs.size() > 0) {

                int start = 0;
                if(posts != null){
                    start = posts.size() + 1;
                }
                if (reset || offline) {
                    posts = subs;
                    contained = new ArrayList<>();
                    for(Submission s : posts){
                        contained.add(s.getFullName());
                    }
                    start = -1;
                } else {
                   for(Submission s : subs){
                       if(contained.contains(s.getFullName())){

                           subs.remove(s);
                       }
                   }
                    posts.addAll(subs);
                    offline = false;


                }


                final int finalStart = start;
                (adapter.mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (refreshLayout != null) {

                            refreshLayout.setRefreshing(false);

                        }


                        if(finalStart != -1) {
                            adapter.notifyItemRangeInserted(finalStart, posts.size());
                        } else {
                            adapter.notifyDataSetChanged();
                        }

                    }
                });
            } else if (subs != null) {
                nomore = true;
            } else if (Cache.hasSub(subreddit.toLowerCase()) && !nomore && Reddit.cache) {

                Log.v("Slide", "GETTING SUB " + subreddit.toLowerCase());
                offline = true;
                cached = Cache.getSubreddit(subreddit.toLowerCase());
                posts = cached.submissions;
                if (cached.submissions.size() > 0 ) {
                    stillShow = true;
                } else {
                    refreshLayout.setRefreshing(false);

                    adapter.setError(true);
                }
                (SubmissionAdapter.mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (refreshLayout != null) {

                            refreshLayout.setRefreshing(false);
                            Toast.makeText(refreshLayout.getContext(), "Last updated " + TimeUtils.getTimeAgo(cached.time, refreshLayout.getContext()), Toast.LENGTH_SHORT).show();

                        }



                            adapter.notifyDataSetChanged();


                    }
                });
            } else if (!nomore) {
                if (refreshLayout != null)

                    refreshLayout.setRefreshing(false);
                adapter.setError(true);

            }
            if (subs != null && refreshLayout != null)
                for (Submission s : subs) {

                    ContentType.ImageType type = ContentType.getImageType(s);

                    String url = "";

                    boolean big = true;

                    ImageLoadingListener l = new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {

                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

                        }

                        @Override
                        public void onLoadingCancelled(String imageUri, View view) {

                        }
                    };
                    SettingValues.InfoBar typ = SettingValues.InfoBar.valueOf(SettingValues.prefs.getString(subreddit + "infoBarTypeNew", SettingValues.infoBar.toString()).toUpperCase());
                    if (typ == SettingValues.InfoBar.INFO_BAR || typ == SettingValues.InfoBar.THUMBNAIL) {
                        big = false;
                    }

                    if (!(typ == SettingValues.InfoBar.NONE)) {

                        boolean bigAtEnd = false;
                        if (!s.isNsfw() || SettingValues.NSFWPreviews) {
                            if (type == ContentType.ImageType.IMAGE) {
                                url = ContentType.getFixedUrl(s.getUrl());
                                if (CreateCardView.getInfoBar(true) == SettingValues.InfoBar.THUMBNAIL) {
                                    ((Reddit) refreshLayout.getContext().getApplicationContext()).getImageLoader().loadImage(url, l);

                                } else if (big) {
                                    ((Reddit) refreshLayout.getContext().getApplicationContext()).getImageLoader().loadImage(url, l);


                                } else {

                                    ((Reddit) refreshLayout.getContext().getApplicationContext()).getImageLoader().loadImage(url, l);

                                }
                            } else if (s.getDataNode().has("preview") && s.getDataNode().get("preview").get("images").get(0).get("source").has("height") ) {

                                boolean blurry = isBlurry(s.getDataNode(), refreshLayout.getContext(), s.getTitle());
                                url = s.getDataNode().get("preview").get("images").get(0).get("source").get("url").asText();
                                if (CreateCardView.getInfoBar(true) == SettingValues.InfoBar.THUMBNAIL ) {
                                    ((Reddit) refreshLayout.getContext().getApplicationContext()).getImageLoader().loadImage(url, l);
                                } else if ((big ) && !blurry) {
                                    ((Reddit) refreshLayout.getContext().getApplicationContext()).getImageLoader().loadImage(url, l);


                                } else {
                                    ((Reddit) refreshLayout.getContext().getApplicationContext()).getImageLoader().loadImage(url, l);

                                }
                            } else if (s.getThumbnail() != null && (s.getThumbnailType() == Submission.ThumbnailType.URL || s.getThumbnailType() == Submission.ThumbnailType.NSFW)) {

                                if ((SettingValues.NSFWPreviews && s.getThumbnailType() == Submission.ThumbnailType.NSFW) || s.getThumbnailType() == Submission.ThumbnailType.URL) {
                                    bigAtEnd = false;
                                    if (CreateCardView.getInfoBar(true) == SettingValues.InfoBar.THUMBNAIL ) {
                                        ((Reddit) refreshLayout.getContext().getApplicationContext()).getImageLoader().loadImage(url, l);

                                    } else {
                                        ((Reddit) refreshLayout.getContext().getApplicationContext()).getImageLoader().loadImage(url, l);

                                    }

                                }
                            }
                            
                        }

                    }
                }


        }

        @Override
        protected ArrayList<Submission> doInBackground(String... subredditPaginators) {
            ArrayList<Submission> things = new ArrayList<>();

            Log.v("Slide", "DOING FOR " + subredditPaginators[0]);

            if (posts == null) {
                posts = new ArrayList<>();
            }

            if (NetworkUtil.getConnectivityStatus(refreshLayout.getContext()) ) {
                stillShow = true;
                if (Reddit.cacheDefault && reset && !forced) {
                    offline = true;
                    return null;
                }
                offline = false;

                if (reset || paginator == null) {

                    offline = false;
                    if (subredditPaginators[0].toLowerCase().equals("frontpage")) {
                        paginator = new SubredditPaginator(Authentication.reddit);
                    } else {
                        paginator = new SubredditPaginator(Authentication.reddit, subredditPaginators[0]);

                    }
                    paginator.setSorting(Reddit.defaultSorting);
                    paginator.setTimePeriod(Reddit.timePeriod);
                    paginator.setLimit(25);
                }

                if (paginator != null && paginator.hasNext()) {
                    if (reset) {
                        try {
                            for (Submission c : paginator.next()) {
                                Submission s = c;
                                if (SettingValues.NSFWPosts && s.isNsfw()) {
                                    things.add(s);
                                } else if (!s.isNsfw()) {
                                    things.add(s);
                                }


                            }
                        } catch (Exception ignored) {
                            //gets caught above
                        }
                    } else {


                        try {
                            for (Submission c : paginator.next()) {
                                Submission s = c;

                                if (SettingValues.NSFWPosts && s.isNsfw()) {
                                    things.add(s);
                                } else if (!s.isNsfw()) {
                                    things.add(s);

                                }

                            }
                        } catch (Exception ignored) {
                            //gets caught above
                        }
                    }
                } else {
                    nomore = true;
                }

                if (Reddit.cache)
                    Cache.writeSubreddit(things, subredditPaginators[0]);
                return things;


            } else {
                offline = true;
            }
            return null;

        }
    }
}