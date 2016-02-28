package me.ccrama.redditslide.Fragments;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import net.dean.jraw.models.Submission;

import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.Activities.CommentsScreenPopup;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Views.MediaVideoView;
import me.ccrama.redditslide.Views.PopulateShadowboxInfo;
import me.ccrama.redditslide.util.GifUtils;


/**
 * Created by ccrama on 6/2/2015.
 */
public class Gif extends Fragment {

    private int i = 0;
    private Submission s;
    private View gif;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (this.isVisible()) {
            if (!isVisibleToUser)   // If we are becoming invisible, then...
            {
                ((MediaVideoView) gif).pause();
                gif.setVisibility(View.GONE);
            }

            if (isVisibleToUser) // If we are becoming visible, then...
            {
                ((MediaVideoView) gif).start();
                gif.setVisibility(View.VISIBLE);

            }
        }
    }

    ViewGroup rootView;
    ProgressBar loader;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(
                R.layout.submission_gifcard, container, false);
        loader = (ProgressBar) rootView.findViewById(R.id.gifprogress);

        PopulateShadowboxInfo.doActionbar(s, rootView, getActivity());

        gif = rootView.findViewById(R.id.gif);


        gif.setVisibility(View.VISIBLE);
        final MediaVideoView v = (MediaVideoView) gif;
        v.clearFocus();


        String dat = s.getUrl();

        new GifUtils.AsyncLoadGif(getActivity(), (MediaVideoView) v.findViewById(R.id.gif), loader, rootView.findViewById(R.id.placeholder), null, false, true).execute(dat);

        rootView.findViewById(R.id.desc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SettingValues.tabletUI && getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    Intent i2 = new Intent(getActivity(), CommentsScreenPopup.class);
                    i2.putExtra(CommentsScreenPopup.EXTRA_PAGE, i);
                    i2.putExtra(CommentsScreen.EXTRA_SUBREDDIT, sub);
                    (getActivity()).startActivity(i2);

                } else {
                    Intent i2 = new Intent(getActivity(), CommentsScreen.class);
                    i2.putExtra(CommentsScreen.EXTRA_PAGE, i);
                    i2.putExtra(CommentsScreen.EXTRA_SUBREDDIT, sub);
                    (getActivity()).startActivity(i2);
                }
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
        s = OfflineSubreddit.getSubreddit(sub).submissions.get(bundle.getInt("page", 0));

    }


}
