package me.ccrama.redditslide.Adapters;

/**
 * Created by ccrama on 3/22/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.devspark.robototextview.RobotoTypefaces;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.DistinguishedStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.UserTags;
import me.ccrama.redditslide.Views.RoundedBackgroundSpan;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.SubmissionParser;


public class CommentAdapterSearch extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements Filterable {

    private final Context           mContext;
    private final List<CommentNode> originalDataSet;
    private String search = "";


    ///... other methods
    private List<CommentNode> dataSet;


    public CommentAdapterSearch(Context mContext, List<CommentNode> dataSet) {

        this.mContext = mContext;
        this.originalDataSet = dataSet;

    }

    public void setResult(String result) {
        search = result;
    }

    @Override
    public Filter getFilter() {
        return new UserFilter(this, originalDataSet);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.comment, viewGroup, false);
        return new CommentViewHolder(v);

    }

    public void doScoreText(CommentViewHolder holder, Comment comment, int offset) {
        String spacer =
                " " + mContext.getString(R.string.submission_properties_seperator_comments) + " ";
        SpannableStringBuilder titleString = new SpannableStringBuilder();

        SpannableStringBuilder author = new SpannableStringBuilder(comment.getAuthor());
        final int authorcolor = Palette.getFontColorUser(comment.getAuthor());

        author.setSpan(new TypefaceSpan("sans-serif-condensed"), 0, author.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        author.setSpan(new StyleSpan(Typeface.BOLD), 0, author.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (comment.getDistinguishedStatus() == DistinguishedStatus.ADMIN) {
            author.replace(0, author.length(), " " + comment.getAuthor() + " ");
            author.setSpan(
                    new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_red_300, false),
                    0, author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (comment.getDistinguishedStatus() == DistinguishedStatus.SPECIAL) {
            author.replace(0, author.length(), " " + comment.getAuthor() + " ");
            author.setSpan(
                    new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_red_500, false),
                    0, author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (comment.getDistinguishedStatus() == DistinguishedStatus.MODERATOR) {
            author.replace(0, author.length(), " " + comment.getAuthor() + " ");
            author.setSpan(
                    new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_green_300, false),
                    0, author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (Authentication.name != null && comment.getAuthor()
                .toLowerCase(Locale.ENGLISH)
                .equals(Authentication.name.toLowerCase(Locale.ENGLISH))) {
            author.replace(0, author.length(), " " + comment.getAuthor() + " ");
            author.setSpan(
                    new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_deep_orange_300,
                            false), 0, author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } /* todoelse if (submission != null && comment.getAuthor()
                .toLowerCase(Locale.ENGLISH)
                .equals(submission.getAuthor().toLowerCase(Locale.ENGLISH)) && !comment.getAuthor().equals("[deleted]")) {
            author.replace(0, author.length(), " " + comment.getAuthor() + " ");
            author.setSpan(
                    new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_blue_300, false),
                    0, author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } */ else if (authorcolor != 0) {
            author.setSpan(new ForegroundColorSpan(authorcolor), 0, author.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        titleString.append(author);

        titleString.append(spacer);

        String scoreText;
        if (comment.isScoreHidden()) {
            scoreText = "[" + mContext.getString(R.string.misc_score_hidden).toUpperCase() + "]";
        } else {
            scoreText = String.format(Locale.getDefault(), "%d", comment.getScore() + offset);
        }
        SpannableStringBuilder score = new SpannableStringBuilder(scoreText);


        titleString.append(score);
        if (!scoreText.contains("[")) {
            titleString.append(mContext.getResources()
                    .getQuantityString(R.plurals.points, comment.getScore()));
        }
        titleString.append((comment.isControversial() ? " †" : ""));

        titleString.append(spacer);
        String timeAgo = TimeUtils.getTimeAgo(comment.getCreated().getTime(), mContext);
        titleString.append((timeAgo == null || timeAgo.isEmpty()) ? "just now"
                : timeAgo); //some users were crashing here

        titleString.append(((comment.getEditDate() != null) ? " (edit " + TimeUtils.getTimeAgo(
                comment.getEditDate().getTime(), mContext) + ")" : ""));
        titleString.append("  ");

        if (comment.getDataNode().get("stickied").asBoolean()) {
            SpannableStringBuilder pinned = new SpannableStringBuilder("\u00A0"
                    + mContext.getString(R.string.submission_stickied).toUpperCase()
                    + "\u00A0");
            pinned.setSpan(
                    new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_green_300, false),
                    0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(pinned);
            titleString.append(" ");
        }
        if (UserTags.isUserTagged(comment.getAuthor())) {
            SpannableStringBuilder pinned = new SpannableStringBuilder(
                    "\u00A0" + UserTags.getUserTag(comment.getAuthor()) + "\u00A0");
            pinned.setSpan(
                    new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_blue_500, false),
                    0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(pinned);
            titleString.append(" ");
        }
        if (comment.getTimesSilvered() > 0 || comment.getTimesGilded() > 0  || comment.getTimesPlatinized() > 0) {
            TypedArray a = mContext.obtainStyledAttributes(
                    new FontPreferences(mContext).getPostFontStyle().getResId(),
                    R.styleable.FontStyle);
            int fontsize =
                    (int) (a.getDimensionPixelSize(R.styleable.FontStyle_font_cardtitle, -1) * .75);
            a.recycle();
            // Add silver, gold, platinum icons and counts in that order
            if (comment.getTimesSilvered() > 0) {
                final String timesSilvered = (comment.getTimesSilvered() == 1) ? ""
                        : "\u200Ax" + comment.getTimesSilvered();
                SpannableStringBuilder silvered =
                        new SpannableStringBuilder("\u00A0★" + timesSilvered + "\u00A0");
                Bitmap image = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.silver);
                float aspectRatio = (float) (1.00 * image.getWidth() / image.getHeight());
                image = Bitmap.createScaledBitmap(image, (int) Math.ceil(fontsize * aspectRatio),
                        (int) Math.ceil(fontsize), true);
                silvered.setSpan(new ImageSpan(mContext, image, ImageSpan.ALIGN_BASELINE), 0, 2,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                silvered.setSpan(new RelativeSizeSpan(0.75f), 3, silvered.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                titleString.append(silvered);
                titleString.append(" ");
            }
            if (comment.getTimesGilded() > 0) {
                final String timesGilded = (comment.getTimesGilded() == 1) ? ""
                        : "\u200Ax" + comment.getTimesGilded();
                SpannableStringBuilder gilded =
                        new SpannableStringBuilder("\u00A0★" + timesGilded + "\u00A0");
                Bitmap image = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.gold);
                float aspectRatio = (float) (1.00 * image.getWidth() / image.getHeight());
                image = Bitmap.createScaledBitmap(image, (int) Math.ceil(fontsize * aspectRatio),
                        (int) Math.ceil(fontsize), true);
                gilded.setSpan(new ImageSpan(mContext, image, ImageSpan.ALIGN_BASELINE), 0, 2,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                gilded.setSpan(new RelativeSizeSpan(0.75f), 3, gilded.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                titleString.append(gilded);
                titleString.append(" ");
            }
            if (comment.getTimesPlatinized() > 0) {
                final String timesPlatinized = (comment.getTimesPlatinized() == 1) ? ""
                        : "\u200Ax" + comment.getTimesPlatinized();
                SpannableStringBuilder platinized =
                        new SpannableStringBuilder("\u00A0★" + timesPlatinized + "\u00A0");
                Bitmap image = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.platinum);
                float aspectRatio = (float) (1.00 * image.getWidth() / image.getHeight());
                image = Bitmap.createScaledBitmap(image, (int) Math.ceil(fontsize * aspectRatio),
                        (int) Math.ceil(fontsize), true);
                platinized.setSpan(new ImageSpan(mContext, image, ImageSpan.ALIGN_BASELINE), 0, 2,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                platinized.setSpan(new RelativeSizeSpan(0.75f), 3, platinized.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                titleString.append(platinized);
                titleString.append(" ");
            }
        }
        if (UserSubscriptions.friends.contains(comment.getAuthor())) {
            SpannableStringBuilder pinned = new SpannableStringBuilder(
                    "\u00A0" + mContext.getString(R.string.profile_friend) + "\u00A0");
            pinned.setSpan(
                    new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_deep_orange_500,
                            false), 0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(pinned);
            titleString.append(" ");
        }
        if (comment.getAuthorFlair() != null && comment.getAuthorFlair().getText() != null
                && !comment.getAuthorFlair().getText().isEmpty()) {
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = mContext.getTheme();
            theme.resolveAttribute(R.attr.activity_background, typedValue, true);
            int color = typedValue.data;
            SpannableStringBuilder pinned = new SpannableStringBuilder(
                    "\u00A0" + Html.fromHtml(comment.getAuthorFlair().getText()) + "\u00A0");
            pinned.setSpan(
                    new RoundedBackgroundSpan(holder.firstTextView.getCurrentTextColor(), color,
                            false, mContext), 0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(pinned);
            titleString.append(" ");
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

        String body = comment.getDataNode().get("body_html").asText();
        if (!search.isEmpty() && StringUtils.isAlphanumericSpace(search)) {
            body = body.replaceAll(search, "[[h[" + search + "]h]]");
        }

        int type = new FontPreferences(mContext).getFontTypeComment().getTypeface();
        Typeface typeface;
        if (type >= 0) {
            typeface = RobotoTypefaces.obtainTypeface(mContext, type);
        } else {
            typeface = Typeface.DEFAULT;
        }
        holder.firstTextView.setTypeface(typeface);

        setViews(body, comment.getSubredditName(), holder);

        holder.childrenNumber.setVisibility(View.GONE);


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
                holder.dot.setBackgroundColor(
                        ContextCompat.getColor(mContext, R.color.md_blue_500));
            } else if (i22 % 4 == 0) {
                holder.dot.setBackgroundColor(
                        ContextCompat.getColor(mContext, R.color.md_green_500));

            } else if (i22 % 3 == 0) {
                holder.dot.setBackgroundColor(
                        ContextCompat.getColor(mContext, R.color.md_yellow_500));

            } else if (i22 % 2 == 0) {
                holder.dot.setBackgroundColor(
                        ContextCompat.getColor(mContext, R.color.md_orange_500));

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
                holder.commentOverflow.setViews(blocks, subredditName);
            } else {
                holder.commentOverflow.setViews(blocks.subList(startIndex, blocks.size()),
                        subredditName);
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

    private static class UserFilter extends Filter {

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
                final String filterPattern =
                        constraint.toString().toLowerCase(Locale.ENGLISH).trim();

                for (final CommentNode user : originalList) {
                    if (StringEscapeUtils.unescapeHtml4(
                            user.getComment().getBody().toLowerCase(Locale.ENGLISH))
                            .contains(filterPattern)) {
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

            adapter.dataSet = new ArrayList<>();
            adapter.dataSet.addAll((ArrayList<CommentNode>) results.values);
            adapter.notifyDataSetChanged();
        }
    }


}
