package me.ccrama.redditslide.Adapters;

/**
 * Created by ccrama on 3/22/2015.
 */

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Submission;

import java.util.ArrayList;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Fragments.SubmissionFrag;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;


public class SubmissionAdapterPager extends FragmentPagerAdapter  {

    public static Activity mContext;

    private final boolean custom;
    public SubredditPosts dataSet;
    public ArrayList<Submission> seen;
    public ArrayList<RecyclerView.ViewHolder> views;


    public SubmissionAdapterPager(FragmentManager manager, Activity mContext, SubredditPosts dataSet,  String subreddit) {
        super(manager);

        this.views = new ArrayList<>();
        this.mContext = mContext;
        this.dataSet = dataSet;
        this.seen = new ArrayList<>();
        custom = SettingValues.prefs.contains("PRESET" + subreddit.toLowerCase());

    }




    @Override
    public Fragment getItem(int position) {
        SubmissionFrag view = new SubmissionFrag();
        Bundle b = new Bundle();
        b.putInt("loc", position);
        b.putBoolean("custom", custom);
        view.setArguments(b);
       return view;
    }






    @Override
    public int getCount() {
        if (dataSet.posts == null || dataSet.posts.size() == 0) {
            return 0;
        } else {
            return dataSet.posts.size() + 1; // Always account for footer
        }    }




    public static class AsyncSave extends AsyncTask<Submission, Void, Void> {
        View v;

        public AsyncSave(View v) {
            this.v = v;
        }

        @Override
        protected Void doInBackground(Submission... submissions) {
            try {
                if (submissions[0].isSaved()) {
                    new AccountManager(Authentication.reddit).unsave(submissions[0]);
                    Snackbar.make(v, R.string.submission_info_unsaved, Snackbar.LENGTH_SHORT).show();

                    submissions[0].saved = false;
                    v = null;
                } else {
                    new AccountManager(Authentication.reddit).save(submissions[0]);
                    Snackbar.make(v, R.string.submission_info_saved, Snackbar.LENGTH_SHORT).show();

                    submissions[0].saved = true;
                    v = null;
                }
            } catch (Exception e) {
                return null;
            }
            return null;
        }
    }



}