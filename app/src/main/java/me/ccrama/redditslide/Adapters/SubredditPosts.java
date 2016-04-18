package me.ccrama.redditslide.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.SubredditPaginator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.NetworkUtil;

/**
 * This class is reponsible for loading subreddit specific submissions
 * {@link loadMore(Context, SubmissionDisplay, boolean, String)} is implemented
 * asynchronously.
 * <p/>
 * Created by ccrama on 9/17/2015.
 */
public class SubredditPosts implements PostLoader {
    public List<Submission> posts;
    public String subreddit;
    public boolean nomore = false;
    public boolean stillShow;
    public boolean offline;
    public boolean forced;
    public boolean loading;
    private SubredditPaginator paginator;
    public OfflineSubreddit cached;
    boolean doneOnce;
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
    public void loadMore(Context context, SubmissionDisplay display, boolean reset) {
        new LoadData(context, display, reset).execute(subreddit);
    }

    public void loadMore(Context context, SubmissionDisplay display, boolean reset, String subreddit) {
        this.subreddit = subreddit;
        loadMore(context, display, reset);
    }

    public void loadPhotos(List<Submission> submissions) {
        for (Submission submission : submissions) {
            String url;
            ContentType.Type type = ContentType.getContentType(submission);
            if (submission.getThumbnails() != null) {

                if (type == ContentType.Type.IMAGE || type == ContentType.Type.SELF || (submission.getThumbnailType() == Submission.ThumbnailType.URL)) {
                    if (type == ContentType.Type.IMAGE) {
                        if (((!NetworkUtil.isConnectedWifi(c) && SettingValues.lowResMobile) || SettingValues.lowResAlways) && submission.getThumbnails() != null && submission.getThumbnails().getVariations() != null && submission.getThumbnails().getVariations().length > 0) {

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
    }

    @Override
    public List<Submission> getPosts() {
        return posts;
    }

    @Override
    public boolean hasMore() {
        return !nomore;
    }

    public boolean skipOne;
    boolean usedOffline;
    public long currentid;

    /**
     * Asynchronous task for loading data
     */
    private class LoadData extends AsyncTask<String, Void, List<Submission>> {
        final boolean reset;
        Context context;
        final SubmissionDisplay displayer;

        public LoadData(Context context, SubmissionDisplay displayer, boolean reset) {
            this.context = context;
            this.displayer = displayer;
            this.reset = reset;
        }

        public int start;

        @Override
        public void onPostExecute(final List<Submission> submissions) {

            loading = false;
            context = null;

            if (submissions != null && !submissions.isEmpty()) {
                String[] ids = new String[submissions.size()];
                int i = 0;
                for (Submission s : submissions) {
                    ids[i] = s.getId();
                    i++;
                }
                if (!SettingValues.synccitName.isEmpty() && !offline) {
                    new MySynccitReadTask().execute(ids);
                }
                // update online

                displayer.updateSuccess(posts, start);
                currentid = 0;
                OfflineSubreddit.currentid = currentid;


            } else if (submissions != null) {
                // end of submissions
                nomore = true;
            } else if (!OfflineSubreddit.getAll(subreddit).isEmpty() && !nomore && SettingValues.cache) {
                offline = true;

                ArrayList<String> all = OfflineSubreddit.getAll(subreddit);
                final String[] titles = new String[all.size()];
                final String[] base = new String[all.size()];
                int i = 0;
                for (String s : all) {
                    String[] split = s.split(",");
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(Long.valueOf(split[1]));
                    titles[i] = ("/r/" + split[0] + " " + (Long.valueOf(split[1]) != 0 ? new SimpleDateFormat("dd-MM").format(c.getTime()) : ""));
                    base[i] = s;
                    i++;
                }

                if (titles.length > 1) {
                    new AlertDialogWrapper.Builder(c).setTitle("Which save would you like to use?")
                            .setItems(titles, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final OfflineSubreddit cached = OfflineSubreddit.getSubreddit(subreddit, Long.valueOf(base[which].split(",")[1]), true);
                                    List<Submission> finalSubs = new ArrayList<>();
                                    for (Submission s : cached.submissions) {
                                        if (!PostMatch.doesMatch(s, subreddit, force18)) {
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
                                    displayer.updateOffline(submissions, Long.valueOf(base[which].split(",")[1]));
                                    dialog.dismiss();
                                    OfflineSubreddit.currentid = Long.valueOf(base[which].split(",")[1]);
                                    currentid = OfflineSubreddit.currentid;
                                }
                            }).setCancelable(false).show();
                } else {
                    String[] s2 = titles[0].split(",");
                    final OfflineSubreddit cached = OfflineSubreddit.getSubreddit(subreddit, Long.valueOf(s2[1]), true);

                    List<Submission> finalSubs = new ArrayList<>();
                    for (Submission s : cached.submissions) {
                        if (!PostMatch.doesMatch(s, subreddit, force18)) {
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
                    displayer.updateOffline(submissions, Long.valueOf(s2[1]));
                    OfflineSubreddit.currentid = Long.valueOf(s2[1]);
                    currentid = OfflineSubreddit.currentid;

                }

            } else if (!nomore) {
                // error
                displayer.updateError();
            }
        }

        @Override
        protected List<Submission> doInBackground(String... subredditPaginators) {

            if (!NetworkUtil.isConnected(context)) {
                Log.v(LogUtil.getTag(), "Using offline data");
                offline = true;
                return null;
            } else {
                offline = false;
            }


            stillShow = true;

            if (reset || paginator == null) {
                offline = false;
                if (subredditPaginators[0].toLowerCase().equals("frontpage")) {
                    paginator = new SubredditPaginator(Authentication.reddit);
                } else {
                    paginator = new SubredditPaginator(Authentication.reddit, subredditPaginators[0]);

                }
                paginator.setSorting(Reddit.getSorting(subreddit));
                paginator.setTimePeriod(Reddit.getTime(subreddit));
                paginator.setLimit(50);

            }

            List<Submission> things = new ArrayList<>();


            try {
                if (paginator != null && paginator.hasNext()) {
                    if (force18) {
                        paginator.setObeyOver18(false);
                    }
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


            List<Submission> filteredSubmissions = new ArrayList<>();
            for (Submission s : things) {
                if (!PostMatch.doesMatch(s, paginator.getSubreddit(), force18)) {
                    filteredSubmissions.add(s);
                }
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

            if (!usedOffline)
                OfflineSubreddit.getSubNoLoad(subreddit.toLowerCase()).overwriteSubmissions(posts).writeToMemory();
            start = 0;
            if (posts != null) {
                start = posts.size() + 1;
            }
            return things;
        }
    }
}