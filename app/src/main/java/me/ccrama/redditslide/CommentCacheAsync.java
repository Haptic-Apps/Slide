package me.ccrama.redditslide;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.fasterxml.jackson.databind.JsonNode;

import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.RestResponse;
import net.dean.jraw.http.SubmissionRequest;
import net.dean.jraw.models.CommentSort;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.meta.SubmissionSerializer;
import net.dean.jraw.paginators.SubredditPaginator;
import net.dean.jraw.util.JrawUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import me.ccrama.redditslide.util.GifUtils;
import me.ccrama.redditslide.util.LogUtil;

/**
 * Created by carlo_000 on 4/18/2016.
 */
public class CommentCacheAsync extends AsyncTask {

    public static final String SAVED_SUBMISSIONS = "read later";
    List<Submission> alreadyReceived;

    NotificationManager mNotifyManager;

    public CommentCacheAsync(List<Submission> submissions, Context c, String subreddit,
            boolean[] otherChoices) {
        alreadyReceived = submissions;
        this.context = c;
        this.subs = new String[]{subreddit};
        this.otherChoices = otherChoices;
    }

    public CommentCacheAsync(List<Submission> submissions, Activity mContext, String baseSub,
            String alternateSubName) {
        this(submissions, mContext, baseSub, new boolean[]{true, true});

    }

    public CommentCacheAsync(Context c, String[] subreddits) {
        this.context = c;
        this.subs = subreddits;
    }

    String[] subs;

    Context                    context;
    NotificationCompat.Builder mBuilder;

    boolean[] otherChoices;

    @Override
    public Void doInBackground(Object[] params) {
        if (Authentication.isLoggedIn && Authentication.me == null || Authentication.reddit == null) {

            if (Authentication.reddit == null) {
                new Authentication(context);
            }
            if(Authentication.reddit != null) {
                try {
                    Authentication.me = Authentication.reddit.me();
                    Authentication.mod = Authentication.me.isMod();

                    Authentication.authentication.edit()
                            .putBoolean(Reddit.SHARED_PREF_IS_MOD, Authentication.mod)
                            .apply();
                    final String name = Authentication.me.getFullName();
                    Authentication.name = name;
                    LogUtil.v("AUTHENTICATED");

                    if (Authentication.reddit.isAuthenticated()) {
                        final Set<String> accounts =
                                Authentication.authentication.getStringSet("accounts", new HashSet<String>());
                        if (accounts.contains(name)) { //convert to new system
                            accounts.remove(name);
                            accounts.add(name + ":" + Authentication.refresh);
                            Authentication.authentication.edit().putStringSet("accounts", accounts).apply(); //force commit
                        }
                        Authentication.isLoggedIn = true;
                        Reddit.notFirst = true;
                    }
                } catch(Exception e){
                    new Authentication(context);
                }
            }
        }

        Map<String, String> multiNameToSubsMap = UserSubscriptions.getMultiNameToSubs(true);
        if (Authentication.reddit == null) Reddit.authentication = new Authentication(context);

        ArrayList<String> success = new ArrayList<>();
        ArrayList<String> error = new ArrayList<>();

        for (final String fSub : subs) {
            final String sub;
            CommentSort sortType = SettingValues.getCommentSorting(fSub);

            if (multiNameToSubsMap.containsKey(fSub)) {
                sub = multiNameToSubsMap.get(fSub);
            } else {
                sub = fSub;
            }

            if (!sub.isEmpty()) {
                if (!sub.equals(SAVED_SUBMISSIONS)) {
                    mNotifyManager = ContextCompat.getSystemService(context, NotificationManager.class);
                    mBuilder = new NotificationCompat.Builder(context, Reddit.CHANNEL_COMMENT_CACHE);
                    mBuilder.setOngoing(true);
                    mBuilder.setContentTitle(context.getString(R.string.offline_caching_title,
                            sub.equalsIgnoreCase("frontpage") ? fSub
                                    : (fSub.contains("/m/") ? fSub : "/r/" + fSub)))
                            .setSmallIcon(R.drawable.save_content);
                }
                List<Submission> submissions = new ArrayList<>();
                ArrayList<String> newFullnames = new ArrayList<>();
                int count = 0;
                if (alreadyReceived != null) {
                    submissions.addAll(alreadyReceived);
                } else {
                    SubredditPaginator p;
                    if (fSub.equalsIgnoreCase("frontpage")) {
                        p = new SubredditPaginator(Authentication.reddit);
                    } else {
                        p = new SubredditPaginator(Authentication.reddit, sub);
                    }
                    p.setLimit(Constants.PAGINATOR_POST_LIMIT);
                    try {
                        submissions.addAll(p.next());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                int commentDepth = Integer.parseInt(
                        SettingValues.prefs.getString(SettingValues.COMMENT_DEPTH, "5"));
                int commentCount = Integer.parseInt(
                        SettingValues.prefs.getString(SettingValues.COMMENT_COUNT, "50"));

                Log.v("CommentCacheAsync", "comment count " + commentCount);
                int random = (int) (Math.random() * 100);

                for (final Submission s : submissions) {
                    try {
                        JsonNode n = getSubmission(
                                new SubmissionRequest.Builder(s.getId()).limit(commentCount)
                                        .depth(commentDepth)
                                        .sort(sortType)
                                        .build());
                        Submission s2 =
                                SubmissionSerializer.withComments(n, CommentSort.CONFIDENCE);
                        OfflineSubreddit.writeSubmission(n, s2, context);
                        newFullnames.add(s2.getFullName());
                        if (!SettingValues.noImages) PhotoLoader.loadPhoto(context, s);
                        switch (ContentType.getContentType(s)) {
                            case VREDDIT_DIRECT:
                            case VREDDIT_REDIRECT:
                            case GIF:
                                if (otherChoices[0]) {
                                    if (context instanceof Activity) {
                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                GifUtils.cacheSaveGif(
                                                        Uri.parse(GifUtils.AsyncLoadGif.formatUrl(s.getUrl())),
                                                        (Activity) context,
                                                        s.getSubredditName(),
                                                        false
                                                );
                                            }
                                        });
                                    }
                                }
                                break;
                            case ALBUM:
                                if (otherChoices[1])
                                //todo this AlbumUtils.saveAlbumToCache(context, s.getUrl());
                                {
                                    break;
                                }
                        }
                    } catch (Exception ignored) {
                    }
                    count = count + 1;
                    if (mBuilder != null) {
                        mBuilder.setProgress(submissions.size(), count, false);
                        mNotifyManager.notify(random, mBuilder.build());
                    }

                }

                OfflineSubreddit.newSubreddit(sub).writeToMemory(newFullnames);
                if (mBuilder != null) {
                    mNotifyManager.cancel(random);
                }
                if (!submissions.isEmpty()) success.add(sub);
            }
        }
        if (mBuilder != null) {
            mBuilder.setContentText(context.getString(R.string.offline_caching_complete))
                    // Removes the progress bar
                    .setSubText(success.size() + " subreddits cached").setProgress(0, 0, false);
            mBuilder.setOngoing(false);
            mNotifyManager.notify(2001, mBuilder.build());
        }

        return null;
    }

    public JsonNode getSubmission(SubmissionRequest request) throws NetworkException {
        Map<String, String> args = new HashMap<>();
        if (request.getDepth() != null) args.put("depth", Integer.toString(request.getDepth()));
        if (request.getContext() != null) {
            args.put("context", Integer.toString(request.getContext()));
        }
        if (request.getLimit() != null) args.put("limit", Integer.toString(request.getLimit()));
        if (request.getFocus() != null && !JrawUtils.isFullname(request.getFocus())) {
            args.put("comment", request.getFocus());
        }

        CommentSort sort = request.getSort();
        if (sort == null)
        // Reddit sorts by confidence by default
        {
            sort = CommentSort.CONFIDENCE;
        }
        args.put("sort", sort.name().toLowerCase(Locale.ENGLISH));

        try {

            RestResponse response = Authentication.reddit.execute(Authentication.reddit.request()
                    .path(String.format("/comments/%s", request.getId()))
                    .query(args)
                    .build());
            return response.getJson();
        } catch (Exception e) {
            return null;
        }
    }
}

