package me.ccrama.redditslide;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.PublicContribution;
import net.dean.jraw.models.VoteDirection;

import me.ccrama.redditslide.util.LayoutUtils;

/**
 * Created by ccrama on 9/19/2015.
 */
public class Vote extends AsyncTask<PublicContribution, Void, Void> {

    private final VoteDirection direction;
    private View v;
    private Context c;

    public Vote(Boolean b, View v, Context c) {
        direction = b ? VoteDirection.UPVOTE : VoteDirection.DOWNVOTE;
        this.v = v;
        this.c = c;
        Reddit.setDefaultErrorHandler(c);
    }

    public Vote(View v, Context c) {
        direction = VoteDirection.NO_VOTE;
        this.v = v;
        this.c = c;
    }

    @Override
    protected Void doInBackground(PublicContribution... sub) {

        if (Authentication.isLoggedIn) {
            try {
                new AccountManager(Authentication.reddit).vote(sub[0], direction);
            } catch (ApiException | RuntimeException e) {
                createVoteSnackbar(R.string.vote_err);
                e.printStackTrace();
            }
        } else {
            createVoteSnackbar(R.string.vote_err_login);
        }
        return null;
    }

    private void createVoteSnackbar(final int i) {
        ((Activity) c).runOnUiThread(() -> {
            try {
                if (v != null && c != null && v.getContext() != null) {
                    Snackbar snackbar = Snackbar.make(v, i, Snackbar.LENGTH_SHORT);
                    LayoutUtils.showSnackbar(snackbar);
                }
            } catch (Exception ignored) {
            }
            c = null;
            v = null;
        });
    }
}
