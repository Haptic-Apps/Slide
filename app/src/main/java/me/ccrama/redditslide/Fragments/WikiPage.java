package me.ccrama.redditslide.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.ccrama.redditslide.ActiveTextView;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Views.MakeTextviewClickable;

public class WikiPage extends Fragment {


    private String text;
    private String subreddit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.justtext, container, false);
        ActiveTextView body = (ActiveTextView) v.findViewById(R.id.body);
        new MakeTextviewClickable().ParseTextWithLinksTextView(text, body, getActivity(), subreddit);


        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        text = bundle.getString("text", "");
        subreddit = bundle.getString("sibreddit", ""); //fixme??

    }

}