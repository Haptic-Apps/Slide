package me.ccrama.redditslide.Adapters;

/**
 * Created by ccrama on 3/22/2015.
 */

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.cocosw.bottomsheet.BottomSheet;
import com.devspark.robototextview.util.RobotoTypefaceManager;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Submission;

import java.util.List;

import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Hidden;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Views.CreateCardView;
import me.ccrama.redditslide.Views.PopulateSubmissionViewHolder;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.SubmissionParser;


public class ContributionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements BaseAdapter {

    private static final int COMMENT = 1;
    public final Activity mContext;
    private final RecyclerView listView;
    private final Boolean isHiddenPost;
    public GeneralPosts dataSet;

    public ContributionAdapter(Activity mContext, GeneralPosts dataSet, RecyclerView listView) {
        this.mContext = mContext;
        this.listView = listView;
        this.dataSet = dataSet;

        this.isHiddenPost = false;
    }

    public ContributionAdapter(Activity mContext, GeneralPosts dataSet, RecyclerView listView, Boolean isHiddenPost) {
        this.mContext = mContext;
        this.listView = listView;
        this.dataSet = dataSet;

        this.isHiddenPost = isHiddenPost;
    }
    private final int LOADING_SPINNER = 5;
    private final int NO_MORE = 3;
    @Override
    public int getItemViewType(int position) {
        if (position == dataSet.posts.size() && dataSet.posts.size() != 0 && !dataSet.nomore) {
            return LOADING_SPINNER;
        } else if (position == dataSet.posts.size() &&  dataSet.nomore) {
            return NO_MORE;
        }
        if (dataSet.posts.get(position) instanceof Comment)
            return COMMENT;

        return 2;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        if (i == COMMENT) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.profile_comment, viewGroup, false);
            return new ProfileCommentViewHolder(v);
        } else if (i == LOADING_SPINNER) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.loadingmore, viewGroup, false);
            return new SubmissionFooterViewHolder(v);
        } else if (i == NO_MORE) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.nomoreposts, viewGroup, false);
            return new SubmissionFooterViewHolder(v);
        }  else {
            View v = CreateCardView.CreateView(viewGroup);
            return new SubmissionViewHolder(v);

        }

    }
    public class SubmissionFooterViewHolder extends RecyclerView.ViewHolder {
        public SubmissionFooterViewHolder(View itemView) {
            super(itemView);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder firstHolder, final int i) {

        if (firstHolder instanceof SubmissionViewHolder) {
            SubmissionViewHolder holder = (SubmissionViewHolder) firstHolder;
            final Submission submission = (Submission) dataSet.posts.get(i);
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
/*TODO Single comment screen
                    Intent i2 = new Intent(mContext, CommentsScreen.class);
                    DataShare.sharedSubreddit = dataSet;
                    i2.putExtra(CommentsScreen.EXTRA_PAGE, i);
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
                    title.setText(Html.fromHtml(submission.getTitle()));

                    ((TextView) dialoglayout.findViewById(R.id.userpopup)).setText("/u/" + submission.getAuthor());
                    ((TextView) dialoglayout.findViewById(R.id.subpopup)).setText("/r/" + submission.getSubredditName());
                    dialoglayout.findViewById(R.id.sidebar).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(mContext, Profile.class);
                            i.putExtra(Profile.EXTRA_PROFILE, submission.getAuthor());
                            mContext.startActivity(i);
                        }
                    });


                    dialoglayout.findViewById(R.id.wiki).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(mContext, SubredditView.class);
                            i.putExtra(SubredditView.EXTRA_SUBREDDIT, submission.getSubredditName());
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
                            new SubmissionAdapter.AsyncSave(firstHolder.itemView).execute(submission);

                        }
                    });
                    dialoglayout.findViewById(R.id.copy).setVisibility(View.GONE);
                    if (submission.isSaved()) {
                        ((TextView) dialoglayout.findViewById(R.id.savedtext)).setText(R.string.submission_post_saved);
                    }
                    dialoglayout.findViewById(R.id.gild).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String urlString = "https://reddit.com" + submission.getPermalink();
                            OpenRedditLink.customIntentChooser(urlString, mContext);
                        }
                    });
                    dialoglayout.findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (submission.isSelfPost())
                                Reddit.defaultShareText("https://reddit.com" + submission.getPermalink(), mContext);
                            else {
                                new BottomSheet.Builder(mContext, R.style.BottomSheet_Dialog)
                                        .title(R.string.submission_share_title)
                                        .grid()
                                        .sheet(R.menu.share_menu)
                                        .listener(new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case R.id.reddit_url:
                                                        Reddit.defaultShareText("https://reddit.com" + submission.getPermalink(), mContext);
                                                        break;
                                                    case R.id.link_url:
                                                        Reddit.defaultShareText(submission.getUrl(), mContext);
                                                        break;
                                                }
                                            }
                                        }).show();
                            }
                        }
                    });
                    if (!Authentication.isLoggedIn || !Authentication.didOnline ) {
                        dialoglayout.findViewById(R.id.save).setVisibility(View.GONE);
                        dialoglayout.findViewById(R.id.gild).setVisibility(View.GONE);

                    }
                    title.setBackgroundColor(Palette.getColor(submission.getSubredditName()));

                    builder.setView(dialoglayout);
                    final Dialog d = builder.show();
                    dialoglayout.findViewById(R.id.hide).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final int pos = dataSet.posts.indexOf(submission);
                            final Contribution old = dataSet.posts.get(pos);
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
            new PopulateSubmissionViewHolder().populateSubmissionViewHolder(holder, submission, mContext, false, false, dataSet.posts, listView, false, false);

            final ImageView hideButton = (ImageView) holder.itemView.findViewById(R.id.hide);
            if (hideButton != null && isHiddenPost) {
                hideButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final int pos = dataSet.posts.indexOf(submission);
                        final Contribution old = dataSet.posts.get(pos);
                        dataSet.posts.remove(submission);
                        notifyItemRemoved(pos);

                        Hidden.undoHidden(old);
                    }
                });
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = "www.reddit.com" + submission.getPermalink();
                    url = url.replace("?ref=search_posts", "");
                    new OpenRedditLink(mContext,url);
                }
            });

        } else if (firstHolder instanceof ProfileCommentViewHolder) {
            //IS COMMENT
            ProfileCommentViewHolder holder = (ProfileCommentViewHolder) firstHolder;
            final Comment comment = (Comment) dataSet.posts.get(i);
            holder.score.setText(comment.getScore() + "");

            holder.time.setText(TimeUtils.getTimeAgo(comment.getCreated().getTime(), mContext));

            setViews(comment.getDataNode().get("body_html").asText(), holder, comment.getSubredditName());

            if (comment.getTimesGilded() > 0) {
                holder.gild.setVisibility(View.VISIBLE);
                ((TextView) holder.gild.findViewById(R.id.gildtext)).setText("" + comment.getTimesGilded());
            } else if (holder.gild.getVisibility() == View.VISIBLE)
                holder.gild.setVisibility(View.GONE);

            holder.title.setText(Html.fromHtml(comment.getSubmissionTitle()));
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

    private void setViews(String rawHTML, ProfileCommentViewHolder holder, String subreddit) {
        List<String> blocks = SubmissionParser.getBlocks(rawHTML);

        boolean firstTextViewPopulated = false;

        for (String block : blocks) {
            if (block.startsWith("<table>")) {
                HorizontalScrollView scrollView = new HorizontalScrollView(mContext);
                TableLayout table = formatTable(block, mContext, subreddit);
                scrollView.addView(table);
                scrollView.setPadding(0, 0, 8, 0);
                holder.overflow.addView(scrollView);
                holder.overflow.setVisibility(View.VISIBLE);
            } else {
                if (firstTextViewPopulated) {
                    SpoilerRobotoTextView newTextView = new SpoilerRobotoTextView(mContext);
                    //textView.setMovementMethod(new MakeTextviewClickable.TextViewLinkHandler(c, subreddit, null));
                    newTextView.setLinkTextColor(new ColorPreferences(mContext).getColor(subreddit));
                    newTextView.setTypeface(RobotoTypefaceManager.obtainTypeface(mContext,
                            new FontPreferences(mContext).getFontTypeComment().getTypeface()));
                    newTextView.setTextHtml(block, subreddit);
                    newTextView.setPadding(0, 0, 8, 0);
                    holder.overflow.addView(newTextView);
                    holder.overflow.setVisibility(View.VISIBLE);
                } else {
                    holder.content.setLinkTextColor(new ColorPreferences(mContext).getColor(subreddit));
                    holder.content.setTypeface(RobotoTypefaceManager.obtainTypeface(mContext,
                            new FontPreferences(mContext).getFontTypeComment().getTypeface()));
                    holder.content.setTextHtml(block, subreddit);
                    firstTextViewPopulated = true;
                }
            }
        }
    }

    private TableLayout formatTable(String text, Activity context, String subreddit) {
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

        TableLayout table = new TableLayout(context);
        TableLayout.LayoutParams params = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);

        table.setLayoutParams(params);

        final String tableStart = "<table>";
        final String tableEnd = "</table>";
        final String tableHeadStart = "<thead>";
        final String tableHeadEnd = "</thead>";
        final String tableRowStart = "<tr>";
        final String tableRowEnd = "</tr>";
        final String tableColumnStart = "<td>";
        final String tableColumnEnd = "</td>";
        final String tableHeaderStart = "<th>";
        final String tableHeaderEnd = "</th>";

        int i = 0;
        int columnStart = 0;
        int columnEnd;
        TableRow row = null;
        while (i < text.length()) {
            if (text.charAt(i) != '<') { // quick check otherwise it falls through to else
                i += 1;
            } else if (text.subSequence(i, i + tableStart.length()).toString().equals(tableStart)) {
                i += tableStart.length();
            } else if (text.subSequence(i, i + tableHeadStart.length()).toString().equals(tableHeadStart)) {
                i += tableHeadStart.length();
            } else if (text.subSequence(i, i + tableRowStart.length()).toString().equals(tableRowStart)) {
                row = new TableRow(context);
                row.setLayoutParams(rowParams);
                i += tableRowStart.length();
            } else if (text.subSequence(i, i + tableRowEnd.length()).toString().equals(tableRowEnd)) {
                table.addView(row);
                i += tableRowEnd.length();
            } else if (text.subSequence(i, i + tableEnd.length()).toString().equals(tableEnd)) {
                i += tableEnd.length();
            } else if (text.subSequence(i, i + tableHeadEnd.length()).toString().equals(tableHeadEnd)) {
                i += tableHeadEnd.length();
            } else if (text.subSequence(i, i + tableColumnStart.length()).toString().equals(tableColumnStart)
                    || text.subSequence(i, i + tableHeaderStart.length()).toString().equals(tableHeaderStart)) {
                i += tableColumnStart.length();
                columnStart = i;
            } else if (text.subSequence(i, i + tableColumnEnd.length()).toString().equals(tableColumnEnd)
                    || text.subSequence(i, i + tableHeaderEnd.length()).toString().equals(tableHeaderEnd)) {
                columnEnd = i;

                SpoilerRobotoTextView textView = new SpoilerRobotoTextView(context);
                //textView.setMovementMethod(new TextViewLinkHandler(context, subreddit, null));
                textView.setLinkTextColor(new ColorPreferences(mContext).getColor(subreddit));
                textView.setTextHtml(text.subSequence(columnStart, columnEnd), subreddit);
                textView.setPadding(3, 0 ,0 , 0);

                row.addView(textView);

                columnStart = 0;
                i += tableColumnEnd.length();
            } else {
                i += 1;
            }
        }

        return table;
    }

    @Override
    public int getItemCount() {
        if (dataSet.posts == null || dataSet.posts.size() == 0) {
            return 0;
        } else {
            return dataSet.posts.size() + 1;
        }
    }

    @Override
    public void setError(Boolean b) {
        listView.setAdapter(new ErrorAdapter());
    }

    @Override
    public void undoSetError() {
        listView.setAdapter(this);
    }

    public static class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }
}