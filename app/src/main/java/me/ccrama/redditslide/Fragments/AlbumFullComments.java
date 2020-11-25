package me.ccrama.redditslide.Fragments;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import net.dean.jraw.models.Comment;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import me.ccrama.redditslide.Activities.ShadowboxComments;
import me.ccrama.redditslide.Adapters.AlbumView;
import me.ccrama.redditslide.Adapters.CommentUrlObject;
import me.ccrama.redditslide.ImgurAlbum.AlbumUtils;
import me.ccrama.redditslide.ImgurAlbum.Image;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SubmissionViews.PopulateShadowboxInfo;


/**
 * Created by ccrama on 6/2/2015.
 */
public class AlbumFullComments extends Fragment {

    boolean gallery = false;
    private View list;
    private CommentUrlObject s;
    boolean hidden;
    View rootView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(
                R.layout.submission_albumcard, container, false);
        PopulateShadowboxInfo.doActionbar(s.comment, rootView, getActivity(), true);

        String url = s.url;

        if (url.contains("gallery")) {
            gallery = true;
        }

        list = rootView.findViewById(R.id.images);

        list.setVisibility(View.VISIBLE);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        ((RecyclerView) list).setLayoutManager(layoutManager);

        ((RecyclerView) list).addOnScrollListener(new RecyclerView.OnScrollListener() {

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

        final View.OnClickListener openClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SlidingUpPanelLayout) rootView.findViewById(R.id.sliding_layout)).setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }
        };
        rootView.findViewById(R.id.base).setOnClickListener(openClick);
        final View title = rootView.findViewById(R.id.title);
        title.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ((SlidingUpPanelLayout) rootView.findViewById(R.id.sliding_layout)).setPanelHeight(title.getMeasuredHeight());
                title.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        ((SlidingUpPanelLayout) rootView.findViewById(R.id.sliding_layout)).addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    final Comment c = s.comment.getComment();
                    rootView.findViewById(R.id.base)
                            .setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String url = "https://reddit.com"
                                            + "/r/"
                                            + c.getSubredditName()
                                            + "/comments/"
                                            + c.getDataNode()
                                            .get("link_id")
                                            .asText()
                                            .substring(3)
                                            + "/nothing/"
                                            + c.getId()
                                            + "?context=3";
                                    new OpenRedditLink(getActivity(), url);
                                }
                            });
                } else {
                    rootView.findViewById(R.id.base).setOnClickListener(openClick);
                }
            }
        });

        new LoadIntoRecycler(url, getActivity()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        return rootView;
    }

    public class LoadIntoRecycler extends AlbumUtils.GetAlbumWithCallback {

        String url;

        public LoadIntoRecycler(@NotNull String url, @NotNull Activity baseActivity) {
            super(url, baseActivity);
            //todo htis dontClose = true;
            this.url = url;
        }

        @Override
        public void doWithData(final List<Image> jsonElements) {
            super.doWithData(jsonElements);
            //May be a bug with downloading multiple comment albums off the same submission
            AlbumView adapter = new AlbumView(baseActivity, jsonElements, 0, s.getSubredditName(),
                    s.comment.getComment().getSubmissionTitle());
            ((RecyclerView) list).setAdapter(adapter);
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        s = ShadowboxComments.comments.get(bundle.getInt("page", 0));
    }


}
