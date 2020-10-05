package me.ccrama.redditslide.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.core.text.HtmlCompat;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.DomainPaginator;
import net.dean.jraw.paginators.Paginator;
import net.dean.jraw.paginators.SubredditPaginator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

import me.ccrama.redditslide.Activities.BaseActivity;
import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.BuildConfig;
import me.ccrama.redditslide.Constants;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.Fragments.SubmissionsView;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.LastComments;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.PostLoader;
import me.ccrama.redditslide.PostMatch;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SubmissionCache;
import me.ccrama.redditslide.Synccit.MySynccitReadTask;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.NetworkUtil;

/**
 * This class is reponsible for loading subreddit specific submissions {@link loadMore(Context,
 * SubmissionDisplay, boolean, String)} is implemented asynchronously. <p/> Created by ccrama on
 * 9/17/2015.
 */
public class SubredditPosts implements PostLoader {
    public List<Submission> posts;
    public String           subreddit;
    public String           subredditRandom;
    public boolean nomore = false;
    public  boolean          offline;
    public  boolean          forced;
    public  boolean          loading;
    public  boolean          error;
    private Paginator        paginator;
    public  OfflineSubreddit cached;
    Context c;
    boolean force18;

    public SubredditPosts(String subreddit, Context c) {
        posts = new ArrayList<>();
        this.subreddit = subreddit;
        this.c = c;
    }

    public SubredditPosts(String subreddit, Context c, boolean force18) {
        posts = new ArrayList<>();
        this.subreddit = subreddit;
        this.c = c;
        this.force18 = force18;
    }

    @Override
    public void loadMore(final Context context, final SubmissionDisplay display,
            final boolean reset) {
        new LoadData(context, display, reset).execute(subreddit);
    }

    public void loadMore(Context context, SubmissionDisplay display, boolean reset,
            String subreddit) {
        this.subreddit = subreddit;
        loadMore(context, display, reset);
    }

    public void loadPhotos(List<Submission> submissions) {
        for (Submission submission : submissions) {
            String url;
            ContentType.Type type = ContentType.getContentType(submission);
            if (submission.getThumbnails() != null) {

                if (type == ContentType.Type.IMAGE
                        || type == ContentType.Type.SELF
                        || (submission.getThumbnailType() == Submission.ThumbnailType.URL)) {
                    if (type == ContentType.Type.IMAGE) {
                        if (((!NetworkUtil.isConnectedWifi(c) && SettingValues.lowResMobile)
                                || SettingValues.lowResAlways)
                                && submission.getThumbnails() != null
                                && submission.getThumbnails().getVariations() != null
                                && submission.getThumbnails().getVariations().length > 0) {

                            int length = submission.getThumbnails().getVariations().length;
                            if (SettingValues.lqLow && length >= 3) {
                                url = HtmlCompat.fromHtml(
                                        submission.getThumbnails().getVariations()[2].getUrl(), HtmlCompat.FROM_HTML_MODE_LEGACY)
                                        .toString(); //unescape url characters
                            } else if (SettingValues.lqMid && length >= 4) {
                                url = HtmlCompat.fromHtml(
                                        submission.getThumbnails().getVariations()[3].getUrl(), HtmlCompat.FROM_HTML_MODE_LEGACY)
                                        .toString(); //unescape url characters
                            } else if (length >= 5) {
                                url = HtmlCompat.fromHtml(submission.getThumbnails().getVariations()[
                                        length
                                                - 1].getUrl(), HtmlCompat.FROM_HTML_MODE_LEGACY).toString(); //unescape url characters
                            } else {
                                url = HtmlCompat.fromHtml(submission.getThumbnails().getSource().getUrl(), HtmlCompat.FROM_HTML_MODE_LEGACY)
                                        .toString(); //unescape url characters
                            }

                        } else {
                            if (submission.getDataNode().has("preview") && submission.getDataNode()
                                    .get("preview")
                                    .get("images")
                                    .get(0)
                                    .get("source")
                                    .has("height")) { //Load the preview image which has probably already been cached in memory instead of the direct link
                                url = submission.getDataNode()
                                        .get("preview")
                                        .get("images")
                                        .get(0)
                                        .get("source")
                                        .get("url")
                                        .asText();
                            } else {
                                url = submission.getUrl();
                            }
                        }


                        ((Reddit) c.getApplicationContext()).getImageLoader()
                                .loadImage(url, new ImageLoadingListener() {
                                    @Override
                                    public void onLoadingStarted(String imageUri, View view) {

                                    }

                                    @Override
                                    public void onLoadingFailed(String imageUri, View view,
                                            FailReason failReason) {

                                    }

                                    @Override
                                    public void onLoadingComplete(String imageUri, View view,
                                            Bitmap loadedImage) {

                                    }

                                    @Override
                                    public void onLoadingCancelled(String imageUri, View view) {

                                    }
                                });

                    } else if (submission.getThumbnails() != null) {

                        if (((!NetworkUtil.isConnectedWifi(c) && SettingValues.lowResMobile)
                                || SettingValues.lowResAlways)
                                && submission.getThumbnails().getVariations().length != 0) {

                            int length = submission.getThumbnails().getVariations().length;
                            if (SettingValues.lqLow && length >= 3) {
                                url = HtmlCompat.fromHtml(
                                        submission.getThumbnails().getVariations()[2].getUrl(), HtmlCompat.FROM_HTML_MODE_LEGACY)
                                        .toString(); //unescape url characters
                            } else if (SettingValues.lqMid && length >= 4) {
                                url = HtmlCompat.fromHtml(
                                        submission.getThumbnails().getVariations()[3].getUrl(), HtmlCompat.FROM_HTML_MODE_LEGACY)
                                        .toString(); //unescape url characters
                            } else if (length >= 5) {
                                url = HtmlCompat.fromHtml(submission.getThumbnails().getVariations()[
                                        length
                                                - 1].getUrl(), HtmlCompat.FROM_HTML_MODE_LEGACY).toString(); //unescape url characters
                            } else {
                                url = HtmlCompat.fromHtml(submission.getThumbnails().getSource().getUrl(), HtmlCompat.FROM_HTML_MODE_LEGACY)
                                        .toString(); //unescape url characters
                            }

                        } else {
                            url = HtmlCompat.fromHtml(submission.getThumbnails().getSource().getUrl(), HtmlCompat.FROM_HTML_MODE_LEGACY)
                                    .toString(); //unescape url characters
                        }

                        ((Reddit) c.getApplicationContext()).getImageLoader()
                                .loadImage(url, new ImageLoadingListener() {
                                    @Override
                                    public void onLoadingStarted(String imageUri, View view) {

                                    }

                                    @Override
                                    public void onLoadingFailed(String imageUri, View view,
                                            FailReason failReason) {

                                    }

                                    @Override
                                    public void onLoadingComplete(String imageUri, View view,
                                            Bitmap loadedImage) {

                                    }

                                    @Override
                                    public void onLoadingCancelled(String imageUri, View view) {

                                    }
                                });

                    } else if (submission.getThumbnail() != null && (submission.getThumbnailType()
                            == Submission.ThumbnailType.URL
                            || submission.getThumbnailType() == Submission.ThumbnailType.NSFW)) {

                        ((Reddit) c.getApplicationContext()).getImageLoader()
                                .loadImage(submission.getUrl(), new ImageLoadingListener() {
                                    @Override
                                    public void onLoadingStarted(String imageUri, View view) {

                                    }

                                    @Override
                                    public void onLoadingFailed(String imageUri, View view,
                                            FailReason failReason) {

                                    }

                                    @Override
                                    public void onLoadingComplete(String imageUri, View view,
                                            Bitmap loadedImage) {

                                    }

                                    @Override
                                    public void onLoadingCancelled(String imageUri, View view) {

                                    }
                                });
                    }
                }
            }
        }
    }

    public ArrayList<String> all;

    @Override
    public List<Submission> getPosts() {
        return posts;
    }

    @Override
    public boolean hasMore() {
        return !nomore;
    }

    public boolean skipOne;
    boolean authedOnce = false;
    boolean usedOffline;
    public long              currentid;
    public SubmissionDisplay displayer;

    /**
     * Asynchronous task for loading data
     */
    private class LoadData extends AsyncTask<String, Void, List<Submission>> {
        final boolean reset;
        Context context;

        public LoadData(Context context, SubmissionDisplay display, boolean reset) {
            this.context = context;
            displayer = display;
            this.reset = reset;
        }

        public int start;

        @Override
        public void onPreExecute()
        {
            if(reset) {
                posts.clear();
                displayer.onAdapterUpdated();
            }
        }

        @Override
        public void onPostExecute(final List<Submission> submissions) {
            boolean success = true;
            loading = false;

            if (error != null) {
                if (error instanceof NetworkException) {
                    NetworkException e = (NetworkException) error;
                    if (e.getResponse().getStatusCode() == 403 && !authedOnce) {
                        if (Reddit.authentication != null && Authentication.didOnline) {
                            Reddit.authentication.updateToken(context);
                        } else if (NetworkUtil.isConnected(context)
                                && Reddit.authentication == null) {
                            Reddit.authentication = new Authentication(context);
                        }
                        authedOnce = true;
                        loadMore(context, displayer, reset, subreddit);
                        return;
                    } else {
                        Toast.makeText(context,
                                "A server error occurred, " + e.getResponse().getStatusCode() + (
                                        e.getResponse().getStatusMessage().isEmpty() ? ""
                                                : ": " + e.getResponse().getStatusMessage()),
                                Toast.LENGTH_SHORT).show();
                    }
                }
                success = false;
            } else if (submissions != null && !submissions.isEmpty()) {
                if (displayer instanceof SubmissionsView
                        && ((SubmissionsView) displayer).adapter.isError) {
                    ((SubmissionsView) displayer).adapter.undoSetError();
                }

                String[] ids = new String[submissions.size()];
                int i = 0;
                for (Submission s : submissions) {
                    ids[i] = s.getId();
                    i++;
                }

                // update online

                displayer.updateSuccess(posts, start);
                currentid = 0;
                OfflineSubreddit.currentid = currentid;

                if (c instanceof BaseActivity) {
                    ((BaseActivity) c).setShareUrl("https://reddit.com/r/" + subreddit);
                }

                if (subreddit.equals("random") || subreddit.equals("myrandom") || subreddit.equals(
                        "randnsfw")) {
                    subredditRandom = submissions.get(0).getSubredditName();
                }

                MainActivity.randomoverride = subredditRandom;

                if (context instanceof SubredditView && (subreddit.equals("random")
                        || subreddit.equals("myrandom")
                        || subreddit.equals("randnsfw"))) {
                    ((SubredditView) context).subreddit = subredditRandom;
                    ((SubredditView) context).executeAsyncSubreddit(subredditRandom);
                }
                if (!SettingValues.synccitName.isEmpty() && !offline) {
                    new MySynccitReadTask(displayer).execute(ids);
                }

            } else if (submissions != null) {
                // end of submissions
                nomore = true;
                displayer.updateSuccess(posts, posts.size() + 1);
            } else if (MainActivity.isRestart) {
                posts = new ArrayList<>();
                cached = OfflineSubreddit.getSubreddit(subreddit, 0L, true, c);
                for (Submission s : cached.submissions) {
                    if (!PostMatch.doesMatch(s, subreddit, force18)) {
                        posts.add(s);
                    }
                }
                offline = false;
                usedOffline = false;
                displayer.updateSuccess(posts, start);
            } else {
                if (!all.isEmpty() && !nomore && SettingValues.cache) {
                    if (context instanceof MainActivity) {
                        doMainActivityOffline(context, displayer);
                    }
                } else if (!nomore) {
                    // error
                    LogUtil.v("Setting error");
                    success = false;
                }
            }

            SubredditPosts.this.error = !success;
        }

        @Override
        protected List<Submission> doInBackground(String... subredditPaginators) {
            if (BuildConfig.DEBUG) LogUtil.v("Loading data");
            if ((!NetworkUtil.isConnected(context) && !Authentication.didOnline)
                    || MainActivity.isRestart) {
                Log.v(LogUtil.getTag(), "Using offline data");
                offline = true;
                usedOffline = true;
                all = OfflineSubreddit.getAll(subreddit);
                return null;
            } else {
                offline = false;
                usedOffline = false;
            }

            if (reset || paginator == null) {
                offline = false;
                nomore = false;
                String sub = subredditPaginators[0].toLowerCase(Locale.ENGLISH);
                if ((sub.equals("random") || sub.equals("randnsfw"))
                        && MainActivity.randomoverride != null
                        && !MainActivity.randomoverride.isEmpty()) {
                    sub = MainActivity.randomoverride;
                    MainActivity.randomoverride = "";
                }
                if (sub.equals("frontpage")) {
                    paginator = new SubredditPaginator(Authentication.reddit);
                } else if (!sub.contains(".")) {
                    paginator = new SubredditPaginator(Authentication.reddit, sub);
                } else {
                    paginator = new DomainPaginator(Authentication.reddit, sub);
                }
                paginator.setSorting(SettingValues.getSubmissionSort(subreddit));
                paginator.setTimePeriod(SettingValues.getSubmissionTimePeriod(subreddit));
                paginator.setLimit(Constants.PAGINATOR_POST_LIMIT);

            }

            List<Submission> filteredSubmissions = getNextFiltered();


            if (!(SettingValues.noImages && ((!NetworkUtil.isConnectedWifi(c)
                    && SettingValues.lowResMobile) || SettingValues.lowResAlways))) {
                loadPhotos(filteredSubmissions);
            }
            if (SettingValues.storeHistory) {
                HasSeen.setHasSeenSubmission(filteredSubmissions);
                LastComments.setCommentsSince(filteredSubmissions);
            }
            SubmissionCache.cacheSubmissions(filteredSubmissions, context, subreddit);

            if (reset || offline || posts == null) {
                posts = new ArrayList<>(new LinkedHashSet(filteredSubmissions));
                start = -1;
            } else {
                posts.addAll(filteredSubmissions);
                posts = new ArrayList<>(new LinkedHashSet(posts));
                offline = false;
            }

            if (!usedOffline) {
                OfflineSubreddit.getSubNoLoad(subreddit.toLowerCase(Locale.ENGLISH))
                        .overwriteSubmissions(posts)
                        .writeToMemory(context);
            }
            start = 0;
            if (posts != null) {
                start = posts.size() + 1;
            }

            return filteredSubmissions;
        }

        public ArrayList<Submission> getNextFiltered() {
            ArrayList<Submission> filteredSubmissions = new ArrayList<>();
            ArrayList<Submission> adding = new ArrayList<>();

            try {
                if (paginator != null && paginator.hasNext()) {
                    if (force18 && paginator instanceof SubredditPaginator) {
                        ((SubredditPaginator) paginator).setObeyOver18(false);
                    }
                    adding.addAll(paginator.next());
                } else {
                    nomore = true;
                }


                for (Submission s : adding) {
                    if (!PostMatch.doesMatch(s, paginator instanceof SubredditPaginator
                            ? ((SubredditPaginator) paginator).getSubreddit()
                            : ((DomainPaginator) paginator).getDomain(), force18)) {
                        filteredSubmissions.add(s);
                    }
                }
                if (paginator != null && paginator.hasNext() && filteredSubmissions.isEmpty()) {
                    filteredSubmissions.addAll(getNextFiltered());
                }
            } catch (Exception e) {
                e.printStackTrace();
                error = e;
                if (e.getMessage() != null && e.getMessage().contains("Forbidden")) {
                    Reddit.authentication.updateToken(context);
                }

            }
            return filteredSubmissions;
        }

        Exception error;
    }


    public void doMainActivityOffline(final Context c, final SubmissionDisplay displayer) {
        LogUtil.v(subreddit);
        if (all == null) {
            all = OfflineSubreddit.getAll(subreddit);
        }
        Collections.rotate(all, -1); //Move 0, or "submission only", to the end
        offline = true;

        final String[] titles = new String[all.size()];
        final String[] base = new String[all.size()];
        int i = 0;
        for (String s : all) {
            String[] split = s.split(",");
            titles[i] = (Long.parseLong(split[1]) == 0 ? c.getString(
                    R.string.settings_backup_submission_only)
                    : TimeUtils.getTimeAgo(Long.parseLong(split[1]), c) + c.getString(
                            R.string.settings_backup_comments));
            base[i] = s;
            i++;
        }
        ((MainActivity) c).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        ((MainActivity) c).getSupportActionBar()
                .setListNavigationCallbacks(
                        new OfflineSubAdapter(c, android.R.layout.simple_list_item_1, titles),
                        new ActionBar.OnNavigationListener() {

                            @Override
                            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                                final String[] s2 = base[itemPosition].split(",");
                                OfflineSubreddit.currentid = Long.valueOf(s2[1]);
                                currentid = OfflineSubreddit.currentid;

                                new AsyncTask<Void, Void, Void>() {
                                    OfflineSubreddit cached;

                                    @Override
                                    protected Void doInBackground(Void... params) {
                                        cached = OfflineSubreddit.getSubreddit(subreddit,
                                                Long.valueOf(s2[1]), true, c);
                                        List<Submission> finalSubs = new ArrayList<>();
                                        for (Submission s : cached.submissions) {
                                            if (!PostMatch.doesMatch(s, subreddit, force18)) {
                                                finalSubs.add(s);
                                            }
                                        }

                                        posts = finalSubs;

                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Void aVoid) {

                                        if (cached.submissions.isEmpty()) {
                                            displayer.updateOfflineError();
                                        }
                                        // update offline
                                        displayer.updateOffline(posts, Long.parseLong(s2[1]));
                                    }
                                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                return true;
                            }
                        });
    }
}
