package me.ccrama.redditslide.Adapters;

/**
 * Created by ccrama on 3/22/2015.
 */

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.PublicContribution;
import net.dean.jraw.models.Submission;

import java.util.ArrayList;

import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Hidden;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Views.CreateCardView;
import me.ccrama.redditslide.Views.MakeTextviewClickable;
import me.ccrama.redditslide.Views.PopulateSubmissionViewHolder;
import me.ccrama.redditslide.Visuals.Pallete;


public class ModeratorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements BaseAdapter{

    public final Activity mContext;
    public ArrayList<PublicContribution> dataSet;
    private final RecyclerView listView;
    @Override
    public void setError(Boolean b) {
        listView.setAdapter(new ErrorAdapter());
    }

    @Override
    public void undoSetError() {
        listView.setAdapter(this);
    }

    public static int COMMENT = 1;
    public static int MESSAGE = 2;
    public static int POST = 3;
    @Override
    public int getItemViewType(int position) {
        if (dataSet.get(position).getFullName().contains("t1"))//IS COMMENT
            return COMMENT;
        if (dataSet.get(position).getFullName().contains("t4"))//IS MESSAGE
            return MESSAGE;
        return POST;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        if (i == MESSAGE) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.top_level_message, viewGroup, false);
            return new MessageViewHolder(v);
        }  if (i == COMMENT) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.profile_comment, viewGroup, false);
            return new ProfileCommentViewHolder(v);
        } else {
            View v = CreateCardView.CreateView(viewGroup);
            return new SubmissionViewHolder(v);

        }



    }




    public ModeratorAdapter(Activity mContext, ModeratorPosts dataSet, RecyclerView listView) {

        this.mContext = mContext;
        this.listView = listView;
        this.dataSet = dataSet.posts;

        boolean isSame = false;

    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder firstHold, final int i) {

       if(firstHold instanceof SubmissionViewHolder){
            SubmissionViewHolder holder = (SubmissionViewHolder) firstHold;
            final Submission submission = (Submission) dataSet.get(i);
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
/*TODO Single comment screen
                    Intent i2 = new Intent(mContext, CommentsScreen.class);
                    DataShare.sharedSubreddit = dataSet;
                    i2.putExtra("page", i);
                    ((Activity) mContext).startActivityForResult(i2, 2);
*/

                }
            });
            CreateCardView.resetColorCard(holder.itemView);
            CreateCardView.colorCard(submission.getSubredditName().toLowerCase(), holder.itemView, "no_subreddit", false);
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
                            new SubmissionAdapter.AsyncSave(firstHold.itemView).execute(submission);

                        }
                    });
                    if (submission.isSaved()) {
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
                            final int pos = dataSet.indexOf(submission);
                            final PublicContribution old  = dataSet.get(pos);
                            dataSet.remove(submission);
                            notifyItemRemoved(pos);
                            d.dismiss();

                            Hidden.setHidden((Contribution) old);

                            Snackbar.make(listView, R.string.submission_info_hidden, Snackbar.LENGTH_LONG).setAction(R.string.btn_undo, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dataSet.add(pos, old);
                                    notifyItemInserted(pos);
                                    Hidden.undoHidden((Contribution)old);

                                }
                            }).show();




                        }
                    });
                    return true;
                }
            });
            new PopulateSubmissionViewHolder().PopulateSubmissionViewHolder(holder, submission, mContext, false, false, dataSet, listView, false);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new OpenRedditLink(mContext, "www.reddit.com" + submission.getPermalink());
                }
            });

        } else {
            //IS COMMENT
            ProfileCommentViewHolder holder = (ProfileCommentViewHolder) firstHold;
            final Comment comment = (Comment) dataSet.get(i);
            holder.score.setText(comment.getScore() + "");

            holder.time.setText(TimeUtils.getTimeAgo(comment.getCreatedUtc().getTime(), mContext));

            new MakeTextviewClickable().ParseTextWithLinksTextViewComment(comment.getDataNode().get("body_html").asText(), holder.content, (Activity) mContext, comment.getSubredditName());
            if (comment.getTimesGilded() > 0) {
                holder.gild.setVisibility(View.VISIBLE);
            } else {
                holder.gild.setVisibility(View.GONE);
            }
            holder.title.setText(comment.getSubmissionTitle());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new OpenRedditLink(mContext, comment.getSubmissionId(), comment.getSubredditName(), comment.getId());
                }
            });
            holder.content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new OpenRedditLink(mContext, comment.getSubmissionId(), comment.getSubredditName(), comment.getId());
                }
            });


        }

    }

    @Override
    public int getItemCount() {
        if (dataSet == null) {
            return 0;
        } else {
            return dataSet.size();
        }
    }


}