package me.ccrama.redditslide.Adapters;

/**
 * Created by ccrama on 3/22/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.HorizontalScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.devspark.robototextview.util.RobotoTypefaceManager;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.DistinguishedStatus;
import net.dean.jraw.models.Submission;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.SubmissionParser;


public class CommentAdapterSearch extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private final Context mContext;
    private final List<CommentNode> originalDataSet;
    private final String subAuthor;


    ///... other methods
    private List<CommentNode> dataSet;
    private Submission submission;


    public CommentAdapterSearch(Context mContext, List<CommentNode> dataSet, RecyclerView listView, String subAuthor) {

        this.mContext = mContext;
        this.subAuthor = subAuthor;
        this.originalDataSet = dataSet;
        List<CommentNode> filteredUserList = new ArrayList<>();

    }

    @Override
    public Filter getFilter() {
        return new UserFilter(this, originalDataSet);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comment, viewGroup, false);
        return new CommentViewHolder(v);

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder firstHolder, int pos) {

        final CommentViewHolder holder = (CommentViewHolder) firstHolder;

        final CommentNode baseNode = dataSet.get(pos);
        final Comment comment = baseNode.getComment();

        String distingush = "";
        if (comment.getDistinguishedStatus() == DistinguishedStatus.MODERATOR)
            distingush = "[M]";
        else if (comment.getDistinguishedStatus() == DistinguishedStatus.ADMIN)
            distingush = "[A]";

        String author = comment.getAuthor();
        holder.author.setText(author + distingush);
        if (comment.getAuthorFlair() != null && comment.getAuthorFlair().getText() != null && !comment.getAuthorFlair().getText().isEmpty()) {
            holder.itemView.findViewById(R.id.flairbubble).setVisibility(View.VISIBLE);
            ((TextView) holder.itemView.findViewById(R.id.text)).setText(Html.fromHtml(comment.getAuthorFlair().getText()));

        } else {
            holder.itemView.findViewById(R.id.flairbubble).setVisibility(View.GONE);

        }

        if (comment.isScoreHidden()) {
            String scoreText = mContext.getString(R.string.misc_score_hidden).toUpperCase();

            holder.score.setText("[" + scoreText + "]");

        } else {
            holder.score.setText(comment.getScore() + "");

        }

        if (baseNode.isTopLevel()) {
            holder.itemView.findViewById(R.id.next).setVisibility(View.VISIBLE);
        } else {
            holder.itemView.findViewById(R.id.next).setVisibility(View.GONE);

        }
        holder.time.setText(TimeUtils.getTimeAgo(comment.getCreated().getTime(), mContext));

        setViews(comment.getDataNode().get("body_html").asText(), comment.getSubredditName(), holder);
        if (comment.getTimesGilded() > 0) {
            holder.gild.setVisibility(View.VISIBLE);
            ((TextView) holder.gild.findViewById(R.id.gildtext)).setText("" + comment.getTimesGilded());
        } else {
            holder.gild.setVisibility(View.GONE);
        }
        holder.children.setVisibility(View.GONE);

        holder.author.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SettingValues.click_user_name_to_profile) {
                    Intent i2 = new Intent(mContext, Profile.class);
                    i2.putExtra(Profile.EXTRA_PROFILE, comment.getAuthor());
                    mContext.startActivity(i2);
                }
            }
        });
        holder.author.setTextColor(Palette.getFontColorUser(comment.getAuthor()));
        if (holder.author.getCurrentTextColor() == 0) {
            holder.author.setTextColor(holder.time.getCurrentTextColor());
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle conData = new Bundle();
                conData.putString("fullname", comment.getFullName());
                Intent intent = new Intent();
                intent.putExtras(conData);
                ((Activity) mContext).setResult(Activity.RESULT_OK, intent);
                ((Activity) mContext).finish();
            }
        });
        holder.firstTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle conData = new Bundle();
                conData.putString("fullname", comment.getFullName());
                Intent intent = new Intent();
                intent.putExtras(conData);
                ((Activity) mContext).setResult(Activity.RESULT_OK, intent);
                ((Activity) mContext).finish();
            }
        });

        holder.itemView.findViewById(R.id.dot).setVisibility(View.VISIBLE);

        if (baseNode.getDepth() - 1 > 0) {
            View v = holder.itemView.findViewById(R.id.dot);
            int i22 = baseNode.getDepth() - 2;
            if (i22 % 5 == 0) {
                holder.dot.setBackgroundColor(ContextCompat.getColor(mContext, R.color.md_blue_500));
            } else if (i22 % 4 == 0) {
                holder.dot.setBackgroundColor(ContextCompat.getColor(mContext, R.color.md_green_500));

            } else if (i22 % 3 == 0) {
                holder.dot.setBackgroundColor(ContextCompat.getColor(mContext, R.color.md_yellow_500));

            } else if (i22 % 2 == 0) {
                holder.dot.setBackgroundColor(ContextCompat.getColor(mContext, R.color.md_orange_500));

            } else {
                holder.dot.setBackgroundColor(ContextCompat.getColor(mContext, R.color.md_red_500));
            }
        } else {
            holder.itemView.findViewById(R.id.dot).setVisibility(View.GONE);
        }
        if (author.toLowerCase().equals(Authentication.name.toLowerCase())) {
            holder.itemView.findViewById(R.id.you).setVisibility(View.VISIBLE);
        } else {
            holder.itemView.findViewById(R.id.you).setVisibility(View.GONE);

        }
        if (author.toLowerCase().equals(subAuthor.toLowerCase())) {

            holder.itemView.findViewById(R.id.op).setVisibility(View.VISIBLE);
        } else {
            holder.itemView.findViewById(R.id.op).setVisibility(View.GONE);

        }
    }

    /**
     * Set the text for the corresponding views
     *
     * @param rawHTML
     * @param subreddit
     * @param holder
     */
    public void setViews(String rawHTML, String subreddit, CommentViewHolder holder) {
        if (rawHTML.isEmpty()) {
            return;
        }

        holder.commentOverflow.removeAllViewsInLayout();

        List<String> blocks = SubmissionParser.getBlocks(rawHTML);

        boolean firstTextViewPopulated = false;
        for (String block : blocks) {
            if (block.startsWith("<table>")) {
                HorizontalScrollView scrollView = new HorizontalScrollView(mContext);
                TableLayout table = formatTable(block, (Activity)mContext, subreddit);
                scrollView.addView(table);
                scrollView.setPadding(0, 0, 8, 0);
                holder.commentOverflow.addView(scrollView);
                holder.commentOverflow.setVisibility(View.VISIBLE);
            } else {
                if (firstTextViewPopulated) {
                    SpoilerRobotoTextView newTextView = new SpoilerRobotoTextView(mContext);
                    //textView.setMovementMethod(new MakeTextviewClickable.TextViewLinkHandler(c, subreddit, null));
                    newTextView.setLinkTextColor(new ColorPreferences(mContext).getColor(subreddit));
                    newTextView.setTypeface(RobotoTypefaceManager.obtainTypeface(mContext,
                            new FontPreferences(mContext).getFontTypeComment().getTypeface()));
                    newTextView.setText(block);
                    newTextView.setPadding(0, 0, 8, 0);
                    holder.commentOverflow.addView(newTextView);
                    holder.commentOverflow.setVisibility(View.VISIBLE);
                } else {
                    holder.firstTextView.setLinkTextColor(new ColorPreferences(mContext).getColor(subreddit));
                    holder.firstTextView.setTypeface(RobotoTypefaceManager.obtainTypeface(mContext,
                            new FontPreferences(mContext).getFontTypeComment().getTypeface()));
                    holder.firstTextView.setText(block);
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
                textView.setText(text.subSequence(columnStart, columnEnd));
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
        if (dataSet == null) {
            return 0;
        }
        return dataSet.size();
    }

    private class UserFilter extends Filter {

        private final CommentAdapterSearch adapter;

        private final List<CommentNode> originalList;

        private final List<CommentNode> filteredList;

        private UserFilter(CommentAdapterSearch adapter, List<CommentNode> originalList) {
            super();
            this.adapter = adapter;
            this.originalList = new LinkedList<>(originalList);
            this.filteredList = new ArrayList<>();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filteredList.clear();
            final FilterResults results = new FilterResults();

            if (constraint.length() == 0) {
                filteredList.addAll(originalList);
            } else {
                final String filterPattern = constraint.toString().toLowerCase().trim();

                for (final CommentNode user : originalList) {
                    if (user.getComment().getBody().toLowerCase().contains(filterPattern)) {
                        filteredList.add(user);
                    }
                }
            }
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            String search = constraint.toString();

            adapter.dataSet = new ArrayList<>();
            adapter.dataSet.addAll((ArrayList<CommentNode>) results.values);
            adapter.notifyDataSetChanged();
        }
    }


}