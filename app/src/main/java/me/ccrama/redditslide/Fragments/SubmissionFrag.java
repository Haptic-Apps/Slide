package me.ccrama.redditslide.Fragments;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.cocosw.bottomsheet.BottomSheet;

import net.dean.jraw.models.Submission;

import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.Adapters.SubmissionAdapter;
import me.ccrama.redditslide.Adapters.SubmissionAdapterPager;
import me.ccrama.redditslide.Adapters.SubmissionViewHolder;
import me.ccrama.redditslide.Adapters.SubredditPosts;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.Hidden;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Views.CreateCardView;
import me.ccrama.redditslide.Views.PopulateSubmissionFragment;
import me.ccrama.redditslide.Visuals.Palette;

public class SubmissionFrag extends Fragment {
    public SubredditPosts posts;
    public SubmissionAdapter adapter;

    public Boolean custom;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final Submission submission = DataShare.sharedSub.posts.get(loc);
        final View v = CreateCardView.CreateView(container, custom, submission.getSubredditName());


        CreateCardView.resetColorCard(v);
        CreateCardView.colorCard(submission.getSubredditName().toLowerCase(), v, submission.getSubredditName(), true);
        v.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                v.setAlpha(0.5f);

                Intent i2 = new Intent(getContext(), CommentsScreen.class);
                i2.putExtra("page", loc);
                (getActivity()).startActivity(i2);


            }
        });
        final boolean saved = submission.isSaved();


        v.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                LayoutInflater inflater = getActivity().getLayoutInflater();
                final View dialoglayout = inflater.inflate(R.layout.postmenu, null);
                AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(getActivity());
                final TextView title = (TextView) dialoglayout.findViewById(R.id.title);
                title.setText(Html.fromHtml(submission.getTitle()));

                ((TextView) dialoglayout.findViewById(R.id.userpopup)).setText("/u/" + submission.getAuthor());
                ((TextView) dialoglayout.findViewById(R.id.subpopup)).setText("/r/" + submission.getSubredditName());
                dialoglayout.findViewById(R.id.sidebar).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent i = new Intent(getActivity(), Profile.class);
                        i.putExtra("profile", submission.getAuthor());
                        getActivity().startActivity(i);
                    }
                });

                dialoglayout.findViewById(R.id.wiki).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getActivity(), SubredditView.class);
                        i.putExtra("subreddit", submission.getSubredditName());
                        getActivity().startActivity(i);
                    }
                });

                dialoglayout.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (saved) {
                            ((TextView) dialoglayout.findViewById(R.id.savedtext)).setText(R.string.submission_save);
                        } else {
                            ((TextView) dialoglayout.findViewById(R.id.savedtext)).setText(R.string.submission_post_saved);

                        }
                        new SubmissionAdapterPager.AsyncSave(v).execute(submission);

                    }
                });
                if (saved) {
                    ((TextView) dialoglayout.findViewById(R.id.savedtext)).setText(R.string.submission_post_saved);
                }
                dialoglayout.findViewById(R.id.gild).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String urlString = "https://reddit.com" + submission.getPermalink();
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setPackage("com.android.chrome"); //Force open in chrome so it doesn't open back in Slide
                        try {
                            getActivity().startActivity(intent);
                        } catch (ActivityNotFoundException ex) {
                            intent.setPackage(null);
                            getActivity().startActivity(intent);
                        }
                    }
                });
                dialoglayout.findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (submission.isSelfPost())
                            Reddit.defaultShareText("http://reddit.com" + submission.getPermalink(), getActivity());
                        else {
                            new BottomSheet.Builder(getActivity(), R.style.BottomSheet_Dialog)
                                    .title(R.string.submission_share_title)
                                    .grid()
                                    .sheet(R.menu.share_menu)
                                    .listener(new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case R.id.reddit_url:
                                                    Reddit.defaultShareText("http://reddit.com" + submission.getPermalink(), getActivity());
                                                    break;
                                                case R.id.link_url:
                                                    Reddit.defaultShareText(submission.getUrl(), getActivity());
                                                    break;
                                            }
                                        }
                                    }).show();
                        }

                    }
                });
                dialoglayout.findViewById(R.id.copy).setVisibility(View.GONE);
                if (!Authentication.isLoggedIn) {
                    dialoglayout.findViewById(R.id.save).setVisibility(View.GONE);
                    dialoglayout.findViewById(R.id.gild).setVisibility(View.GONE);

                }
                title.setBackgroundColor(Palette.getColor(submission.getSubredditName()));

                builder.setView(dialoglayout);
                final Dialog d = builder.show();
                dialoglayout.findViewById(R.id.hide).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final int pos = DataShare.sharedSub.posts.indexOf(submission);
                        final Submission old = DataShare.sharedSub.posts.get(pos);
                        DataShare.sharedSub.posts.remove(submission);
                        d.dismiss();
                        Hidden.setHidden(old);

                        Snackbar.make(v, R.string.submission_info_hidden, Snackbar.LENGTH_LONG).setAction(R.string.btn_undo, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                DataShare.sharedSub.posts.add(pos, old);
                                Hidden.undoHidden(old);

                            }
                        }).show();

                    }
                });

                return true;
            }

        });
        SubmissionViewHolder holder = new SubmissionViewHolder(v);

        new PopulateSubmissionFragment().PopulateSubmissionViewHolder(holder, submission, getActivity(), false, false, DataShare.sharedSub.posts, custom, !DataShare.sharedSub.stillShow);




        return v;
    }

    int loc;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        loc = bundle.getInt("loc", 0);
        custom = bundle.getBoolean("custsom", false);
    }


}