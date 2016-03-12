package me.ccrama.redditslide.Adapters;

/**
 * Created by ccrama on 3/22/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.DistinguishedStatus;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Views.RoundedBackgroundSpan;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.SubmissionParser;


public class CommentAdapterSearch extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private final Context mContext;
    private final List<CommentNode> originalDataSet;
    private final String subAuthor;


    ///... other methods
    private List<CommentNode> dataSet;


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
    public void doScoreText(CommentViewHolder holder, Comment comment, int offset) {
        String spacer = " " + mContext.getString(R.string.submission_properties_seperator_comments) + " ";
        SpannableStringBuilder titleString = new SpannableStringBuilder();

        String distingush = "";
        if (comment.getDistinguishedStatus() == DistinguishedStatus.MODERATOR) {
            distingush = "[M]";
        } else if (comment.getDistinguishedStatus() == DistinguishedStatus.ADMIN) {
            distingush = "[A]";
        }

        SpannableStringBuilder author = new SpannableStringBuilder(distingush + comment.getAuthor());
        int authorcolor = Palette.getFontColorUser(comment.getAuthor());

        author.setSpan(new TypefaceSpan("sans-serif-condensed"), 0, author.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        author.setSpan(new StyleSpan(Typeface.BOLD), 0, author.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        if (authorcolor != 0) {
            author.setSpan(new ForegroundColorSpan(authorcolor), 0, author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        titleString.append(author);

        titleString.append(spacer);

        String scoreText;
        if (comment.isScoreHidden()) {
            scoreText = "[" + mContext.getString(R.string.misc_score_hidden).toUpperCase() + "]";
        } else {
            scoreText = Integer.toString(comment.getScore() + offset);
        }
        SpannableStringBuilder score = new SpannableStringBuilder(scoreText);

        titleString.append(score);
        titleString.append((comment.isControversial() ? "†" : ""));

        titleString.append(spacer);
        titleString.append(TimeUtils.getTimeAgo(comment.getCreated().getTime(), mContext));

        titleString.append(((comment.hasBeenEdited() && comment.getEditDate() != null) ? " *" + TimeUtils.getTimeAgo(comment.getEditDate().getTime(), mContext) : ""));
        if (comment.getDataNode().get("stickied").asBoolean()) {
            SpannableStringBuilder pinned = new SpannableStringBuilder(" " + mContext.getString(R.string.sidebar_pinned) + " ");
            pinned.setSpan(new TypefaceSpan("sans-serif-condensed"), 0, pinned.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            pinned.setSpan(new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_green_300, false, true), 0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(" ");
            pinned.setSpan(new RelativeSizeSpan(0.5f), 0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(pinned);
        }
        titleString.append(" ");
        if (comment.getTimesGilded() > 0) {
            SpannableStringBuilder pinned = new SpannableStringBuilder(" " + comment.getTimesGilded() + " ");
            pinned.setSpan(new TypefaceSpan("sans-serif-condensed"), 0, pinned.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            pinned.setSpan(new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_orange_500, false, true), 0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(" ");
            titleString.append(pinned);
        }
        titleString.append(" ");
        if (comment.getAuthor().toLowerCase().equals(Authentication.name.toLowerCase())) {
            SpannableStringBuilder pinned = new SpannableStringBuilder(" " + mContext.getString(R.string.misc_you) + " ");
            pinned.setSpan(new TypefaceSpan("sans-serif-condensed"), 0, pinned.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            pinned.setSpan(new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_deep_orange_500, false, true), 0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(" ");
            titleString.append(pinned);
        }
        titleString.append(" ");
        if (comment.getAuthorFlair() != null && comment.getAuthorFlair().getText() != null && !comment.getAuthorFlair().getText().isEmpty()) {
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = mContext.getTheme();
            theme.resolveAttribute(R.attr.activity_background, typedValue, true);
            int color = typedValue.data;
            SpannableStringBuilder pinned = new SpannableStringBuilder(" " + comment.getAuthorFlair().getText() + " ");
            pinned.setSpan(new TypefaceSpan("sans-serif-condensed"), 0, pinned.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            pinned.setSpan(new RoundedBackgroundSpan(holder.firstTextView.getCurrentTextColor(), color, false, false), 0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(" ");
            titleString.append(pinned);
        }
        holder.content.setText(titleString);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder firstHolder, int pos) {

        final CommentViewHolder holder = (CommentViewHolder) firstHolder;

        final CommentNode baseNode = dataSet.get(pos);
        final Comment comment = baseNode.getComment();

        doScoreText(holder, comment, 0);

        if (baseNode.isTopLevel()) {
            holder.itemView.findViewById(R.id.next).setVisibility(View.VISIBLE);
        } else {
            holder.itemView.findViewById(R.id.next).setVisibility(View.GONE);

        }

        setViews(comment.getDataNode().get("body_html").asText(), comment.getSubredditName(), holder);

        holder.children.setVisibility(View.GONE);


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

    }

    /**
     * Set the text for the corresponding views
     *
     * @param rawHTML
     * @param subredditName
     * @param holder
     */
    private void setViews(String rawHTML, String subredditName, CommentViewHolder holder) {
        if (rawHTML.isEmpty()) {
            return;
        }

        List<String> blocks = SubmissionParser.getBlocks(rawHTML);

        int startIndex = 0;
        // the <div class="md"> case is when the body contains a table or code block first
        if (!blocks.get(0).equals("<div class=\"md\">")) {
            holder.firstTextView.setVisibility(View.VISIBLE);
            holder.firstTextView.setTextHtml(blocks.get(0), subredditName);
            startIndex = 1;
        } else {
            holder.firstTextView.setText("");
            holder.firstTextView.setVisibility(View.GONE);
        }

        if (blocks.size() > 1) {
            if (startIndex == 0) {
                holder.commentOverflow.setViews(blocks, subredditName, mContext);
            } else {
                holder.commentOverflow.setViews(blocks.subList(startIndex, blocks.size()), subredditName, mContext);
            }
        } else {
            holder.commentOverflow.removeAllViews();
        }
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