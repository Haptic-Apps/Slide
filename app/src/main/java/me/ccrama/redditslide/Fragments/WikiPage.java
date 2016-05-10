package me.ccrama.redditslide.Fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import me.ccrama.redditslide.Activities.Wiki;
import me.ccrama.redditslide.Constants;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.Views.CommentOverflow;
import me.ccrama.redditslide.Views.GeneralSwipeRefreshLayout;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.SubmissionParser;

public class WikiPage extends Fragment {
    private String title;
    private String subreddit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.justtext, container, false);

        final SpoilerRobotoTextView body = (SpoilerRobotoTextView) v.findViewById(R.id.body);
        final CommentOverflow commentOverflow = (CommentOverflow) v.findViewById(R.id.commentOverflow);
        final GeneralSwipeRefreshLayout ref = (GeneralSwipeRefreshLayout) v.findViewById(R.id.ref);

        ref.setColorSchemeColors(Palette.getColors(subreddit, getActivity()));

        //If we use 'findViewById(R.id.header).getMeasuredHeight()', 0 is always returned.
        //So, we estimate the height of the header in dp
        //Something isn't right with the Wiki layout though, so use the SINGLE_HEADER instead.
        ref.setProgressViewOffset(false,
                Constants.SINGLE_HEADER_VIEW_OFFSET - Constants.PTR_OFFSET_TOP,
                Constants.SINGLE_HEADER_VIEW_OFFSET + Constants.PTR_OFFSET_BOTTOM);

        ref.post(new Runnable() {
            @Override
            public void run() {
                ref.setRefreshing(true);
            }
        });
        new AsyncTask<Void, Void, Void>() {
            String text;

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    text = ((Wiki) getActivity()).wiki.get(subreddit, title).getDataNode().get("content_html").asText();
                } catch(Exception ignored){

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                setViews(text, subreddit, body, commentOverflow);

                ref.setRefreshing(false);
                ref.setEnabled(false);

            }
        }.execute();

        return v;
    }

    private void setViews(String rawHTML, String subredditName, SpoilerRobotoTextView firstTextView, CommentOverflow commentOverflow) {
        if (rawHTML ==null || rawHTML.isEmpty()) {
            return;
        }

        List<String> blocks = SubmissionParser.getBlocks(rawHTML);

        int startIndex = 0;
        // the <div class="md"> case is when the body contains a table or code block first
        if (!blocks.get(0).equals("<div class=\"md\">")) {
            firstTextView.setVisibility(View.VISIBLE);
            firstTextView.setTextHtml(blocks.get(0), subredditName);
            startIndex = 1;
        } else {
            firstTextView.setText("");
            firstTextView.setVisibility(View.GONE);
        }

        if (blocks.size() > 1) {
            if (startIndex == 0) {
                commentOverflow.setViews(blocks, subredditName);
            } else {
                commentOverflow.setViews(blocks.subList(startIndex, blocks.size()), subredditName);
            }
        } else {
            commentOverflow.removeAllViews();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        title = bundle.getString("title", "");
        subreddit = bundle.getString("subreddit", "");

    }

}