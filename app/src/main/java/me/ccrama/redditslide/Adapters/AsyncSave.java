package me.ccrama.redditslide.Adapters;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Submission;

import me.ccrama.redditslide.ActionStates;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.R;

public class AsyncSave extends AsyncTask<Submission, Void, Void> {
    final Activity mContext;
    View v;

    public AsyncSave(Activity mContext, View v) {
        this.mContext = mContext;
        this.v = v;
    }

    @Override
    protected Void doInBackground(Submission... submissions) {
        try {
            if (ActionStates.isSaved(submissions[0])) {
                new AccountManager(Authentication.reddit).unsave(submissions[0]);
                final Snackbar s = Snackbar.make(v, R.string.submission_info_unsaved, Snackbar.LENGTH_SHORT);
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        View view = s.getView();
                        TextView tv =
                                view.findViewById(com.google.android.material.R.id.snackbar_text);
                        tv.setTextColor(Color.WHITE);
                        s.show();
                    }
                });
                submissions[0].saved = false;

            } else {
                new AccountManager(Authentication.reddit).save(submissions[0]);
                final Snackbar s = Snackbar.make(v, R.string.submission_info_saved, Snackbar.LENGTH_SHORT);
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        View view = s.getView();
                        TextView tv =
                                view.findViewById(com.google.android.material.R.id.snackbar_text);
                        tv.setTextColor(Color.WHITE);
                        s.show();
                    }
                });
                submissions[0].saved = true;
            }
            v = null;

        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
