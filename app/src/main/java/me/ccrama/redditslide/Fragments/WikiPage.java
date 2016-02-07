package me.ccrama.redditslide.Fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.ccrama.redditslide.Activities.Wiki;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Views.GeneralSwipeRefreshLayout;
import me.ccrama.redditslide.Visuals.Palette;

public class WikiPage extends Fragment {


    private String title;
    private String subreddit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.justtext, container, false);

        //final SubmissionTextViewGroup body = (SubmissionTextViewGroup) v.findViewById(R.id.body); TODO deadleg
        final GeneralSwipeRefreshLayout ref = (GeneralSwipeRefreshLayout) v.findViewById(R.id.ref);
        TypedValue typed_value = new TypedValue();
        getActivity().getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
        ref.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typed_value.resourceId));

        ref.setColorSchemeColors(Palette.getColors(subreddit, getActivity()));

        ref.setRefreshing(true);

        new AsyncTask<Void, Void, Void>() {
            String text;
            @Override
            protected Void doInBackground(Void... params) {
                 text = ((Wiki)getActivity()).wiki.get(subreddit, title).getDataNode().get("content_html").asText();

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                // new MakeTextviewClickable().ParseTextWithLinksTextView(text, body, getActivity(), subreddit); TODO deadleg

                ref.setRefreshing(false);
                ref.setEnabled(false);

            }
        }.execute();


        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        title = bundle.getString("title", "");
        subreddit = bundle.getString("subreddit", "");

    }

}