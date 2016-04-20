package me.ccrama.redditslide;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.NotificationCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
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
import java.util.List;
import java.util.Map;

/**
 * Created by carlo_000 on 4/18/2016.
 */
public class CommentCacheAsync extends AsyncTask<String, Void, Void> {
    List<Submission> alreadyReceived;

    NotificationManager mNotifyManager;

    public CommentCacheAsync(List<Submission> submissions, Context c, String subreddit) {
        alreadyReceived = submissions;
        this.context = c;
        this.subs = new String[]{subreddit};
        this.modal = true;
    }
    String[] subs;

    Context context;
    NotificationCompat.Builder mBuilder;
    MaterialDialog dialog;
    boolean modal;

    public CommentCacheAsync(Context c, String[] subreddits, boolean modal) {
        this.context = c;
        this.subs = subreddits;
        this.modal = modal;
    }


    @Override
    protected Void doInBackground(String... params) {

        for(final String sub : subs) {
            if (!sub.isEmpty()) {

                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (modal) {
                            dialog = new MaterialDialog.Builder(context).title("Caching /r/" + sub)
                                    .progress(false, 50)
                                    .cancelable(false)
                                    .positiveText(R.string.btn_cancel)
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            CommentCacheAsync.this.cancel(true);
                                            dialog.dismiss();
                                        }
                                    })
                                    .show();
                        } else {
                            mNotifyManager =
                                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                            mBuilder = new NotificationCompat.Builder(context);
                            mBuilder.setContentTitle("Caching /r/" + sub)
                                    .setSmallIcon(R.drawable.save);
                        }
                    }
                });
                ArrayList<Submission> submissions = new ArrayList<>();
                ArrayList<String> newFullnames = new ArrayList<>();
                int count = 0;
                if (alreadyReceived != null) {
                    submissions.addAll(alreadyReceived);
                } else {
                    SubredditPaginator p = new SubredditPaginator(Authentication.reddit, sub);
                    p.setLimit(50);
                    submissions.addAll(p.next());
                }

                if (!modal) {
                    mBuilder.setProgress(submissions.size(), 0, false);
                    mNotifyManager.notify(1, mBuilder.build());
                } else {
                    dialog.setMaxProgress(submissions.size());
                }
                for (final Submission s : submissions) {
                    try {
                        JsonNode n = getSubmission(new SubmissionRequest.Builder(s.getId()).sort(CommentSort.CONFIDENCE).build());
                        Submission s2 = SubmissionSerializer.withComments(n, CommentSort.CONFIDENCE);
                        OfflineSubreddit.writeSubmission(n, s2, context);
                        newFullnames.add(s2.getFullName());
                /* todo maybe
                switch (ContentType.getContentType(s)) {
                    case GIF:
                        if (chosen[0])
                            GifUtils.saveGifToCache(MainActivity.this, s.getUrl());
                        break;
                    case ALBUM:
                        if (chosen[1])

                            AlbumUtils.saveAlbumToCache(MainActivity.this, s.getUrl());
                        break;
                }*/
                    } catch (Exception e) {
                    }
                    count++;
                    if (modal) {
                        dialog.setProgress(count);
                    } else {
                        mBuilder.setProgress(submissions.size(), count, false);
                        mNotifyManager.notify(1, mBuilder.build());
                    }

                }
                if (modal) {
                    dialog.dismiss();
                } else {
                    mBuilder.setContentText("Caching complete")
                            // Removes the progress bar
                            .setProgress(0, 0, false);
                    mNotifyManager.notify(1, mBuilder.build());
                }
                OfflineSubreddit.newSubreddit(sub).writeToMemory(newFullnames);
            }
        }
        return null;
    }

    public JsonNode getSubmission(SubmissionRequest request) throws NetworkException {
        Map<String, String> args = new HashMap<>();
        if (request.getDepth() != null)
            args.put("depth", Integer.toString(request.getDepth()));
        if (request.getContext() != null)
            args.put("context", Integer.toString(request.getContext()));
        if (request.getLimit() != null)
            args.put("limit", Integer.toString(request.getLimit()));
        if (request.getFocus() != null && !JrawUtils.isFullname(request.getFocus()))
            args.put("comment", request.getFocus());

        CommentSort sort = request.getSort();
        if (sort == null)
            // Reddit sorts by confidence by default
            sort = CommentSort.CONFIDENCE;
        args.put("sort", sort.name().toLowerCase());

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

