package me.ccrama.redditslide.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;


/**
 * Created by ccrama on 6/2/2015.
 */
public class TutorialFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.tutorial_inside, container, false);
        doPage(getPage(), rootView);

        return rootView;
    }
    private void doPage(int i, View container){
        ((TextView) container.findViewById(R.id.submission_title)).setText(titles[i]);
        Ion.with((ImageView) container.findViewById(R.id.image)).load("android.resource://" + getActivity().getPackageName() + "/" + dataBits[i]);

    }
    private static final String[] titles = new String[]{
            "Swipe to switch subreddits",
            "Tap a submission to open in in full view",
            "Slide up to see comments in full view",
            "Tap a name to view user",
            "Hide comments with a single tap",
            "You can swipe to the next submission in full view",

            "Open content from full view",
            "Open content from list view"
    };
    private static final int[] dataBits = new int[]{
            R.drawable.swipe_subreddit,
            R.drawable.open_post,
            R.drawable.swipe_comments,
            R.drawable.tap_user_info,
            R.drawable.hide_replies,
            R.drawable.swipe_post,

            R.drawable.open_fullscreen_2,
            R.drawable.open_fullscreen_1

    };

    public static TutorialFragment newInstance(int page) {
        TutorialFragment f=new TutorialFragment();

        Bundle args=new Bundle();

        args.putInt("PAGE", page);

        f.setArguments(args);

        return(f);
    }

    private int getPage() {
        return(getArguments().getInt("PAGE"));
    }
}