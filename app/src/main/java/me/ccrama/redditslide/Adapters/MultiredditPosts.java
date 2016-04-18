package me.ccrama.redditslide.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.Html;
import android.view.View;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import net.dean.jraw.models.MultiReddit;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.MultiRedditPaginator;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.PostLoader;
import me.ccrama.redditslide.PostMatch;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Synccit.MySynccitReadTask;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.NetworkUtil;

/**
 * This class is reponsible for loading subreddit specific submissions
 * {@link loadMore(Context, SubmissionDisplay, boolean, String)} is implemented
 * asynchronously.
 * <p/>
 * Created by ccrama on 9/17/2015.
 */
public class MultiredditPosts implements PostLoader {
    public List<Submission> posts;
    public boolean nomore = false;
    public boolean stillShow;
    public boolean offline;
    public boolean loading;
    private MultiRedditPaginator paginator;
    Context c;
    MultiredditAdapter adapter;

    public MultiredditPosts(String multireddit) {
        posts = new ArrayList<>();
        this.multiReddit = UserSubscriptions.getMultiredditByDisplayName(multireddit);

    }

    @Override
    public void loadMore(Context context, SubmissionDisplay displayer, boolean reset) {
        this.c = context;
        new LoadData(context, displayer, reset).execute(multiReddit);
    }

    public void loadMore(Context context, SubmissionDisplay displayer, boolean reset, MultiredditAdapter adapter) {
        this.adapter = adapter;
        this.c = context;
        loadMore(context, displayer, reset);
    }

    public void loadPhotos(List<Submission> submissions) {
        for (Submission submission : submissions) {
            boolean forceThumb = false;
            String url;
            ContentType.Type type = ContentType.getContentType(submission);
            if (submission.getThumbnails() != null) {

                int height = submission.getThumbnails().getSource().getHeight();
                int width = submission.getThumbnails().getSource().getWidth();

                 if (type != ContentType.Type.IMAGE && type != ContentType.Type.SELF && (submission.getThumbnailType() != Submission.ThumbnailType.URL)) {


                } else if (type == ContentType.Type.IMAGE) {
                    if (((!NetworkUtil.isConnectedWifi(c) && SettingValues.lowResMobile) || SettingValues.lowResAlways) && submission.getThumbnails() != null && submission.getThumbnails().getVariations() != null) {

                        int length = submission.getThumbnails().getVariations().length;
                        url = Html.fromHtml(submission.getThumbnails().getVariations()[length / 2].getUrl()).toString(); //unescape url characters

                    } else {
                        if (submission.getDataNode().has("preview") && submission.getDataNode().get("preview").get("images").get(0).get("source").has("height")) { //Load the preview image which has probably already been cached in memory instead of the direct link
                            url = submission.getDataNode().get("preview").get("images").get(0).get("source").get("url").asText();
                        } else {
                            url = submission.getUrl();
                        }
                    }


                    ((Reddit) c.getApplicationContext()).getImageLoader().loadImage(url, new ImageLoadingListener() {
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
                    });

                } else if (submission.getThumbnails() != null) {

                    if (((!NetworkUtil.isConnectedWifi(c) && SettingValues.lowResMobile) || SettingValues.lowResAlways) && submission.getThumbnails().getVariations().length != 0) {

                        int length = submission.getThumbnails().getVariations().length;
                        url = Html.fromHtml(submission.getThumbnails().getVariations()[length / 2].getUrl()).toString(); //unescape url characters

                    } else {
                        url = Html.fromHtml(submission.getThumbnails().getSource().getUrl()).toString(); //unescape url characters
                    }

                    ((Reddit) c.getApplicationContext()).getImageLoader().loadImage(url, new ImageLoadingListener() {
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
                    });

                } else if (submission.getThumbnail() != null && (submission.getThumbnailType() == Submission.ThumbnailType.URL || submission.getThumbnailType() == Submission.ThumbnailType.NSFW)) {

                    ((Reddit) c.getApplicationContext()).getImageLoader().loadImage(submission.getUrl(), new ImageLoadingListener() {
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
                    });
                }
            }
        }
    }

    @Override
    public List<Submission> getPosts() {
        return posts;
    }
    public MultiReddit multiReddit;

    @Override
    public boolean hasMore() {
        return !nomore;
    }

    public boolean skipOne;
    boolean usedOffline;

    /**
     * Asynchronous task for loading data
     */
    private class LoadData extends AsyncTask<MultiReddit, Void, List<Submission>> {
        final boolean reset;
        Context context;
        final SubmissionDisplay displayer;

        public LoadData(Context context, SubmissionDisplay displayer, boolean reset) {
            this.context = context;
            this.displayer = displayer;
            this.reset = reset;
        }

        @Override
        public void onPostExecute(List<Submission> submissions) {
            loading = false;
            context = null;

            if (submissions != null && !submissions.isEmpty()) {
                // new submissions found
                int start = 0;
                if (posts != null) {
                    start = posts.size() + 1;
                }


                List<Submission> filteredSubmissions = new ArrayList<>();
                for (Submission s : submissions) {
                    if (!PostMatch.doesMatch(s, paginator.getMultiReddit().getDisplayName(), false)) {
                        filteredSubmissions.add(s);
                    }
                }
                String[] ids = new String[filteredSubmissions.size()];
                int i = 0;
                for (Submission s : filteredSubmissions) {
                    ids[i] = s.getId();
                    i++;
                }
                if (!SettingValues.synccitName.isEmpty() && !offline) {
                    new MySynccitReadTask().execute(ids);
                }
                loadPhotos(filteredSubmissions);
                if (reset || offline || posts == null) {
                    posts = new ArrayList<>(new LinkedHashSet(filteredSubmissions));
                    start = -1;
                } else {
                    posts.addAll(filteredSubmissions);
                    posts = new ArrayList<>(new LinkedHashSet(posts));
                    offline = false;
                }

                final int finalStart = start;

                if (!usedOffline)
                    OfflineSubreddit.getSubreddit("multi" + multiReddit.getDisplayName().toLowerCase(), false).overwriteSubmissions(posts).writeToMemory();

                // update online
                displayer.updateSuccess(posts, finalStart);

            } else if (submissions != null) {
                // end of submissions
                nomore = true;
            } else if (!OfflineSubreddit.getSubreddit("multi" + multiReddit.getDisplayName().toLowerCase(), false).submissions.isEmpty() && !nomore && SettingValues.cache) {
                offline = true;
                final OfflineSubreddit cached = OfflineSubreddit.getSubreddit("multi" + multiReddit.getDisplayName().toLowerCase(), true);

                List<Submission> finalSubs = new ArrayList<>();
                for (Submission s : cached.submissions) {
                    if (!PostMatch.doesMatch(s, "multi" + multiReddit.getDisplayName().toLowerCase(), false)) {
                        finalSubs.add(s);
                    }
                }

                posts = finalSubs;

                if (cached.submissions.size() > 0) {
                    stillShow = true;
                } else {
                    displayer.updateOfflineError();
                }
                // update offline
                displayer.updateOffline(submissions, cached.time);
            } else if (!nomore) {
                // error
                displayer.updateError();
            }
        }

        @Override
        protected List<Submission> doInBackground(MultiReddit... subredditPaginators) {

            if (!NetworkUtil.isConnected(context)) {
                offline = true;
                return null;
            } else {
                offline = false;
            }

            stillShow = true;

            if (reset || paginator == null) {
                offline = false;
                paginator = new MultiRedditPaginator(Authentication.reddit, subredditPaginators[0]);
                paginator.setSorting(Reddit.getSorting("multi" + subredditPaginators[0].getDisplayName().toLowerCase()));
                paginator.setTimePeriod(Reddit.getTime("multi" + subredditPaginators[0].getDisplayName().toLowerCase()));

                LogUtil.v("Sorting is " + paginator.getSorting().name());
                paginator.setLimit(50);

            }

            List<Submission> things = new ArrayList<>();

            try {
                if (paginator != null && paginator.hasNext()) {
                    things.addAll(paginator.next());
                } else {
                    nomore = true;
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (e.getMessage().contains("Forbidden")) {
                    Reddit.authentication.updateToken(context);
                }

            }

            return things;
        }
    }
}