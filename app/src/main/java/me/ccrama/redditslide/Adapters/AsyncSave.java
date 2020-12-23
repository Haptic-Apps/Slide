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
            boolean subSaved = ActionStates.isSaved(submissions[0]);
            final Snackbar s;

            if (subSaved) {
                new AccountManager(Authentication.reddit).unsave(submissions[0]);
                s = Snackbar.make(v, R.string.submission_info_unsaved, Snackbar.LENGTH_SHORT);
            } else {
                new AccountManager(Authentication.reddit).save(submissions[0]);
                s = Snackbar.make(v, R.string.submission_info_saved, Snackbar.LENGTH_SHORT);
            }
            runUiThread(s);
            submissions[0].saved = !subSaved;
            v = null;

        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private void runUiThread(final Snackbar s) {
        mContext.runOnUiThread(() -> {
            View view = s.getView();
            TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
            tv.setTextColor(Color.WHITE);
            s.show();
        });
    }
}
