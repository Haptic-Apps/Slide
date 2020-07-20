package me.ccrama.redditslide.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import net.dean.jraw.models.Submission;

import java.util.List;

import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.Activities.Shadowbox;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.SubmissionViews.PopulateShadowboxInfo;
import me.ccrama.redditslide.Views.CommentOverflow;
import me.ccrama.redditslide.util.SubmissionParser;


/**
 * Created by ccrama on 6/2/2015.
 */
public class SelftextFull extends Fragment {

    private int i = 0;
    private Submission s;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.submission_textcard, container, false);

        PopulateShadowboxInfo.doActionbar(s, rootView, getActivity(), true);

        if (!s.getSelftext().isEmpty()) {

            setViews(s.getDataNode().get("selftext_html").asText(), s.getSubredditName(), rootView);

        }
        rootView.findViewById(R.id.desc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i2 = new Intent(getActivity(), CommentsScreen.class);
                i2.putExtra(CommentsScreen.EXTRA_PAGE, i);
                i2.putExtra(CommentsScreen.EXTRA_SUBREDDIT, sub);
                (getActivity()).startActivity(i2);

            }
        });
        return rootView;
    }

    public String sub;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        i = bundle.getInt("page", 0);
        sub = bundle.getString("sub");
        if (((Shadowbox) getActivity()).subredditPosts == null
                || ((Shadowbox) getActivity()).subredditPosts.getPosts().size() < bundle.getInt(
                "page", 0)) {
            getActivity().finish();
        } else {
            s = ((Shadowbox) getActivity()).subredditPosts.getPosts().get(bundle.getInt("page", 0));
        }
    }

    private void setViews(String rawHTML, String subredditName, View base) {
        if (rawHTML.isEmpty()) {
            return;
        }


        List<String> blocks = SubmissionParser.getBlocks(rawHTML);

        int startIndex = 0;
        if (!blocks.get(0).startsWith("<table>") && !blocks.get(0).startsWith("<pre>")) {
            ((SpoilerRobotoTextView) base.findViewById(R.id.firstTextView)).setTextHtml(blocks.get(0), subredditName);
            startIndex = 1;
        }

        CommentOverflow overflow = (CommentOverflow) base.findViewById(R.id.commentOverflow);
        if (blocks.size() > 1) {
            if (startIndex == 0) {
                overflow.setViews(blocks, subredditName);
            } else {
                overflow.setViews(blocks.subList(startIndex, blocks.size()), subredditName);
            }
        }
    }

}
