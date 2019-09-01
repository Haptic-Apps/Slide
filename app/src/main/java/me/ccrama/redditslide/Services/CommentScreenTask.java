package me.ccrama.redditslide.Services;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.models.Submission;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import me.ccrama.redditslide.Activities.CommentsScreenSingle;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.LastComments;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;

public class CommentScreenTask {

    public static class AsyncGetSubredditName extends AsyncTask<String, Void, String> {

        private WeakReference<CommentsScreenSingle> activity;

        public AsyncGetSubredditName(@NotNull CommentsScreenSingle activity) {
            this.activity = new WeakReference<>(activity);
        }

        @Override
        protected void onPostExecute(String s) {

        }

        @Override
        protected String doInBackground(String... params) {
            // Calling get() method just one time so it wont produce NPEs
            // As subsequent access may produce NPEs
            CommentsScreenSingle screenSingle = this.activity.get();
            if (screenSingle != null) {
                try {
                    final Submission s = Authentication.reddit.getSubmission(params[0]);
                    if (SettingValues.storeHistory) {
                        if (SettingValues.storeNSFWHistory && s.isNsfw() || !s.isNsfw()) {
                            HasSeen.addSeen(s.getFullName());
                        }
                        LastComments.setComments(s);
                    }
                    HasSeen.setHasSeenSubmission(new ArrayList<Submission>() {{
                        this.add(s);
                    }});

                    if(s.getSubredditName() == null){
                        //subreddit = "Promoted";
                        screenSingle.update(s, "Promoted");
                    } else {//
                        //subreddit = s.getSubredditName();
                        screenSingle.update(s, s.getSubredditName());
                    }
                    return "";

                } catch (Exception e) {
                    try {
                        screenSingle.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialogWrapper.Builder(screenSingle).setTitle(
                                        R.string.submission_not_found)
                                        .setMessage(R.string.submission_not_found_msg)
                                        .setPositiveButton(R.string.btn_ok,
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog,
                                                                        int which) {
                                                        screenSingle.finish();
                                                    }
                                                })
                                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                            @Override
                                            public void onDismiss(DialogInterface dialog) {
                                                screenSingle.finish();
                                            }
                                        })
                                        .show();
                            }
                        });
                    } catch (Exception ignored) {

                    }
                }
            }
            return null;
        }
    }

}
