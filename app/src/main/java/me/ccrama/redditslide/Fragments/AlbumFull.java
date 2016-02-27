package me.ccrama.redditslide.Fragments;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.dean.jraw.models.Submission;

import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.Activities.CommentsScreenPopup;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Views.PopulateShadowboxInfo;
import me.ccrama.redditslide.util.AlbumUtils;


/**
 * Created by ccrama on 6/2/2015.
 */
public class AlbumFull extends Fragment {

    boolean gallery = false;
    View placeholder;
    private View list;
    private int i = 0;
    private Submission s;
    boolean hidden;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.submission_albumcard, container, false);

        PopulateShadowboxInfo.doActionbar(s, rootView, getActivity());

        if (s.getUrl().contains("gallery")) {
            gallery = true;
        }

        list = rootView.findViewById(R.id.images);

        list.setVisibility(View.VISIBLE);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        ((RecyclerView)list).setLayoutManager(layoutManager);
        new AlbumUtils.LoadAlbumFromUrl(s.getUrl(), getActivity(), false, false, null, (RecyclerView) list).execute(s.getUrl());

        ((RecyclerView) list).setOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                ValueAnimator va = null;

                if (dy > 0 && !hidden) {
                    hidden = true;

                    if (va != null && va.isRunning())
                        va.cancel();

                    final View base = rootView.findViewById(R.id.base);
                    va = ValueAnimator.ofFloat(1.0f, 0.2f);
                    int mDuration = 250; //in millis
                    va.setDuration(mDuration);
                    va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        public void onAnimationUpdate(ValueAnimator animation) {
                            Float value = (Float) animation.getAnimatedValue();
                            base.setAlpha(value);
                        }
                    });

                    va.start();

                } else if (hidden && dy <= 0) {
                    final View base = rootView.findViewById(R.id.base);

                    if (va != null && va.isRunning())
                        va.cancel();

                    hidden = false;
                    va = ValueAnimator.ofFloat(0.2f, 1.0f);
                    int mDuration = 250; //in millis
                    va.setDuration(mDuration);
                    va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        public void onAnimationUpdate(ValueAnimator animation) {
                            Float value = (Float) animation.getAnimatedValue();
                            base.setAlpha(value);
                        }
                    });

                    va.start();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        rootView.findViewById(R.id.base).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SettingValues.tabletUI && getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    Intent i2 = new Intent(getActivity(), CommentsScreenPopup.class);
                    i2.putExtra(CommentsScreenPopup.EXTRA_PAGE, i);
                    (getActivity()).startActivity(i2);

                } else {
                    Intent i2 = new Intent(getActivity(), CommentsScreen.class);
                    i2.putExtra(CommentsScreen.EXTRA_PAGE, i);
                    i2.putExtra(CommentsScreen.EXTRA_SUBREDDIT, s.getSubredditName());
                    (getActivity()).startActivity(i2);
                }
            }
        });
        return rootView;
    }


    private String cutEnds(String s) {
        if (s.endsWith("/")) {
            return s.substring(0, s.length() - 1);
        } else {
            return s;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        i = bundle.getInt("page", 0);

        s = OfflineSubreddit.getSubreddit(bundle.getString("sub")).submissions.get(bundle.getInt("page", 0));

    }


}
