package me.ccrama.redditslide;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.NotificationCompat;
import android.text.Html;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.fasterxml.jackson.databind.JsonNode;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

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

import me.ccrama.redditslide.util.NetworkUtil;

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

    public void loadPhotos(Submission submission, Context c) {
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

    @Override
    protected Void doInBackground(String... params) {

        Map<String, String> multiNameToSubsMap = UserSubscriptions.getMultiNameToSubs(true);
        if (Authentication.reddit == null)
            Reddit.authentication = new Authentication(context);

        for (final String fSub : subs) {
            final String sub;
            final String name = fSub;

            if (multiNameToSubsMap.containsKey(fSub)) {
                sub = multiNameToSubsMap.get(fSub);
            } else {
                sub = fSub;
            }

            if (!sub.isEmpty()) {
                if (modal && context instanceof Activity) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog = new MaterialDialog.Builder(context).title("Caching " + (name.contains("/m/") ? name : "/r/" + name))
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
                        }
                    });
                } else {
                    mNotifyManager =
                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    mBuilder = new NotificationCompat.Builder(context);
                    mBuilder.setContentTitle("Caching " + (sub.equalsIgnoreCase("frontpage") ? name : (name.contains("/m/") ? name : "/r/" + name)))
                            .setSmallIcon(R.drawable.save);
                }
                List<Submission> submissions = new ArrayList<>();
                ArrayList<String> newFullnames = new ArrayList<>();
                int count = 0;
                if (alreadyReceived != null) {
                    submissions.addAll(alreadyReceived);
                } else {
                    SubredditPaginator p;
                    if (name.equalsIgnoreCase("frontpage")) {
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

                if (!modal) {
                    mBuilder.setProgress(submissions.size(), 0, false);
                    mNotifyManager.notify(1, mBuilder.build());
                } else {
                    if (dialog != null)
                        dialog.setMaxProgress(submissions.size());
                }
                for (final Submission s : submissions) {
                    try {
                        JsonNode n = getSubmission(new SubmissionRequest.Builder(s.getId()).sort(CommentSort.CONFIDENCE).build());
                        Submission s2 = SubmissionSerializer.withComments(n, CommentSort.CONFIDENCE);
                        OfflineSubreddit.writeSubmission(n, s2, context);
                        newFullnames.add(s2.getFullName());
                        if (!SettingValues.noImages)
                            loadPhotos(s, context);
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
                    } catch (Exception ignored) {
                    }
                    count++;
                    if (modal) {
                        dialog.setProgress(count);
                    } else {
                        mBuilder.setProgress(submissions.size(), count, false);
                        mNotifyManager.notify(1, mBuilder.build());
                    }

                }
                if (modal && dialog != null) {
                    dialog.dismiss();
                } else if (mBuilder != null) {
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

