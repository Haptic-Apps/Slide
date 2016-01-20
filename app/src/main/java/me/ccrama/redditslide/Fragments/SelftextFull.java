package me.ccrama.redditslide.Fragments;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.dean.jraw.models.Submission;

import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.Activities.CommentsScreenPopup;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Views.MakeTextviewClickable;


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

        TextView title = (TextView) rootView.findViewById(R.id.title);
        TextView desc = (TextView) rootView.findViewById(R.id.desc);

        title.setText(s.getTitle());
        desc.setText(s.getAuthor() + " " + TimeUtils.getTimeAgo(s.getCreated().getTime(), getContext()));

        SpoilerRobotoTextView bod = ((SpoilerRobotoTextView) rootView.findViewById(R.id.imagearea));
        if (!s.getSelftext().isEmpty()) {
            new MakeTextviewClickable().ParseTextWithLinksTextView(s.getDataNode().get("selftext_html").asText(), bod, getActivity(), s.getSubredditName());
        }
        rootView.findViewById(R.id.imagearea).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Reddit.tabletUI && getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    Intent i2 = new Intent(getActivity(), CommentsScreenPopup.class);
                    i2.putExtra("page", i);
                    (getActivity()).startActivity(i2);

                } else {
                    Intent i2 = new Intent(getActivity(), CommentsScreen.class);
                    i2.putExtra("page", i);
                    i2.putExtra("subreddit", s.getSubredditName());
                    (getActivity()).startActivity(i2);
                }
            }
        });
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        i = bundle.getInt("page", 0);
        s = DataShare.sharedSubreddit.get(i);

    }

}
