package me.ccrama.redditslide.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import me.ccrama.redditslide.R;


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
    public void doPage(int i, View container){
        ((TextView) container.findViewById(R.id.submission_title)).setText(titles(getContext())[i]);
        Ion.with((ImageView) container.findViewById(R.id.image)).load("android.resource://" + getActivity().getPackageName() + "/" + dataBits[i]);

    }
    public static String[] titles(Context context) {
        return new String[]{
                context.getString(R.string.tutorial_swipe_subreddits),
                context.getString(R.string.tutorial_tap_full_view),
                context.getString(R.string.tutorial_slide_comments),
                context.getString(R.string.tutorial_tap_name),
                context.getString(R.string.tutorial_tap_hide),
                context.getString(R.string.tutorial_swipe_next),

                context.getString(R.string.tutorial_open_full),
                context.getString(R.string.tutoiral_open_list)
        };
    }

    public static int[] dataBits = new int[]{
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

    public int getPage() {
        return(getArguments().getInt("PAGE"));
    }
}