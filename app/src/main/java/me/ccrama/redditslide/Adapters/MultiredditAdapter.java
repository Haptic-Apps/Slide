package me.ccrama.redditslide.Adapters;

/**
 * Created by ccrama on 3/22/2015.
 */

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.models.Submission;

import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.Activities.CommentsScreenPopup;
import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.Hidden;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Views.CreateCardView;
import me.ccrama.redditslide.Views.PopulateSubmissionViewHolder;
import me.ccrama.redditslide.Visuals.Pallete;


public class MultiredditAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements BaseAdapter {

    public final Activity mContext;
    private final MultiredditPosts dataSet;
    private final RecyclerView listView;

    @Override
    public void setError(Boolean b) {
        listView.setAdapter(new ErrorAdapter());
    }

    @Override
    public void undoSetError() {
        listView.setAdapter(this);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == dataSet.posts.size()) {
            return 5;
        }
        return 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (i == 5) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.loadingmore, viewGroup, false);
            return new ContributionAdapter.EmptyViewHolder(v);
        } else {
            View v = CreateCardView.CreateView(viewGroup, false, "nomatching");
            return new SubmissionViewHolder(v);
        }
    }

    public MultiredditAdapter(Activity mContext, MultiredditPosts dataSet, RecyclerView listView) {

        this.mContext = mContext;
        this.listView = listView;
        this.dataSet = dataSet;

        boolean isSame = false;

    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder2, final int i) {

        if (holder2 instanceof SubmissionViewHolder) {
            final SubmissionViewHolder holder = (SubmissionViewHolder) holder2;
            final Submission submission = dataSet.posts.get(i);
            CreateCardView.resetColorCard(holder.itemView);
            CreateCardView.colorCard(submission.getSubredditName().toLowerCase(), holder.itemView, "nomatching", false);
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    DataShare.sharedSubreddit = dataSet.posts;

                    if (Reddit.tabletUI && mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        Intent i2 = new Intent(mContext, CommentsScreenPopup.class);
                        i2.putExtra("page", i);
                        (mContext).startActivity(i2);

                    } else {
                        Intent i2 = new Intent(mContext, CommentsScreen.class);
                        i2.putExtra("page", i);
                        ((Activity) mContext).startActivityForResult(i2, 2);
                    }


                }
            });

            new PopulateSubmissionViewHolder().PopulateSubmissionViewHolder(holder, submission, mContext, false, false, dataSet.posts, listView, false);
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                    final View dialoglayout = inflater.inflate(R.layout.postmenu, null);
                    AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(mContext);
                    final TextView title = (TextView) dialoglayout.findViewById(R.id.title);
                    title.setText(submission.getTitle());

                    ((TextView) dialoglayout.findViewById(R.id.userpopup)).setText("/u/" + submission.getAuthor());
                    ((TextView) dialoglayout.findViewById(R.id.subpopup)).setText("/r/" + submission.getSubredditName());
                    dialoglayout.findViewById(R.id.sidebar).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(mContext, Profile.class);
                            i.putExtra("profile", submission.getAuthor());
                            mContext.startActivity(i);
                        }
                    });

                    dialoglayout.findViewById(R.id.wiki).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(mContext, SubredditView.class);
                            i.putExtra("subreddit", submission.getSubredditName());
                            mContext.startActivity(i);
                        }
                    });

                    dialoglayout.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (submission.isSaved()) {
                                ((TextView) dialoglayout.findViewById(R.id.savedtext)).setText(R.string.submission_save);
                            } else {
                                ((TextView) dialoglayout.findViewById(R.id.savedtext)).setText(R.string.submission_post_saved);

                            }
                            new SubmissionAdapter.AsyncSave(holder.itemView).execute(submission);

                        }
                    });
                    if (submission.isSaved()) {
                        ((TextView) dialoglayout.findViewById(R.id.savedtext)).setText(R.string.submission_post_saved);
                    }
                    dialoglayout.findViewById(R.id.gild).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String urlString = submission.getUrl();
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setPackage("com.android.chrome"); //Force open in chrome so it doesn't open back in Slide
                            try {
                                mContext.startActivity(intent);
                            } catch (ActivityNotFoundException ex) {
                                intent.setPackage(null);
                                mContext.startActivity(intent);
                            }
                        }
                    });
                    dialoglayout.findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new AlertDialogWrapper.Builder(mContext).setTitle(R.string.submission_share_title)
                                    .setNegativeButton(R.string.submission_share_reddit, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Reddit.defaultShareText("http://reddit.com" + submission.getPermalink(), mContext);

                                        }
                                    }).setPositiveButton(R.string.submission_share_content, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Reddit.defaultShareText(submission.getUrl(), mContext);

                                }
                            }).show();

                        }
                    });
                    if (!Authentication.isLoggedIn) {
                        dialoglayout.findViewById(R.id.save).setVisibility(View.GONE);
                        dialoglayout.findViewById(R.id.gild).setVisibility(View.GONE);

                    }
                    title.setBackgroundColor(Pallete.getColor(submission.getSubredditName()));

                    builder.setView(dialoglayout);
                    final Dialog d = builder.show();
                    dialoglayout.findViewById(R.id.hide).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final int pos = dataSet.posts.indexOf(submission);
                            final Submission old = dataSet.posts.get(pos);
                            dataSet.posts.remove(submission);
                            notifyItemRemoved(pos);
                            d.dismiss();
                            Hidden.setHidden(old);

                            Snackbar.make(listView, R.string.submission_info_hidden, Snackbar.LENGTH_LONG).setAction(R.string.btn_undo, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dataSet.posts.add(pos, old);
                                    notifyItemInserted(pos);
                                    Hidden.undoHidden(old);

                                }
                            }).show();


                        }
                    });
                    return true;
                }
            });
            int lastPosition = i;
        }

    }

    @Override
    public int getItemCount() {
        if (dataSet == null || dataSet.posts == null) {
            return 0;
        } else {
            return dataSet.posts.size() + 1;
        }
    }


}