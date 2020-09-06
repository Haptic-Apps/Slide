package me.ccrama.redditslide.Adapters;

/**
 * Created by ccrama on 3/22/2015.
 */

import android.app.Activity;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import net.dean.jraw.models.ModAction;

import java.util.Locale;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Views.RoundedBackgroundSpan;
import me.ccrama.redditslide.Visuals.Palette;


public class ModLogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements BaseAdapter {

    private final       int SPACER  = 6;
    public static final int MESSAGE = 2;
    public final  Activity     mContext;
    private final RecyclerView listView;
    public        ModLogPosts  dataSet;

    public ModLogAdapter(Activity mContext, ModLogPosts dataSet, RecyclerView listView) {
        this.mContext = mContext;
        this.listView = listView;
        this.dataSet = dataSet;
    }

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
        if (position == 0 && !dataSet.posts.isEmpty()) {
            return SPACER;
        } else if (!dataSet.posts.isEmpty()) {
            position -= 1;
        }
        return MESSAGE;
    }

    public static class SpacerViewHolder extends RecyclerView.ViewHolder {
        public SpacerViewHolder(View itemView) {
            super(itemView);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (i == SPACER) {
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.spacer, viewGroup, false);
            return new SpacerViewHolder(v);

        } else {
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.mod_action, viewGroup, false);
            return new ModLogViewHolder(v);
        }
    }

    public static class ModLogViewHolder extends RecyclerView.ViewHolder {

        SpoilerRobotoTextView body;
        ImageView             icon;

        public ModLogViewHolder(View itemView) {
            super(itemView);
            body = itemView.findViewById(R.id.body);
            icon = itemView.findViewById(R.id.action);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder firstHold, final int pos) {
        int i = pos != 0 ? pos - 1 : pos;

        if (firstHold instanceof ModLogViewHolder) {
            ModLogViewHolder holder = (ModLogViewHolder) firstHold;
            final ModAction a = dataSet.posts.get(i);
            SpannableStringBuilder b = new SpannableStringBuilder();
            SpannableStringBuilder titleString = new SpannableStringBuilder();

            String spacer = mContext.getString(R.string.submission_properties_seperator);

            String timeAgo = TimeUtils.getTimeAgo(a.getCreated().getTime(), mContext);
            String time = ((timeAgo == null || timeAgo.isEmpty()) ? "just now"
                    : timeAgo); //some users were crashing here
            titleString.append(time);
            titleString.append(spacer);

            if (a.getSubreddit() != null) {
                String subname = a.getSubreddit();
                SpannableStringBuilder subreddit = new SpannableStringBuilder("/r/" + subname);
                if ((SettingValues.colorSubName
                        && Palette.getColor(subname) != Palette.getDefaultColor())) {
                    subreddit.setSpan(new ForegroundColorSpan(Palette.getColor(subname)), 0,
                            subreddit.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    subreddit.setSpan(new StyleSpan(Typeface.BOLD), 0, subreddit.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                titleString.append(subreddit);
            }

            b.append(titleString);
            b.append(spacer);
            SpannableStringBuilder author = new SpannableStringBuilder(a.getModerator());
            final int authorcolor = Palette.getFontColorUser(a.getModerator());

            author.setSpan(new TypefaceSpan("sans-serif-condensed"), 0, author.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            author.setSpan(new StyleSpan(Typeface.BOLD), 0, author.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (Authentication.name != null && a.getModerator()
                    .toLowerCase(Locale.ENGLISH)
                    .equals(Authentication.name.toLowerCase(Locale.ENGLISH))) {
                author.replace(0, author.length(), " " + a.getModerator() + " ");
                author.setSpan(new RoundedBackgroundSpan(mContext, R.color.white,
                                R.color.md_deep_orange_300, false), 0, author.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (authorcolor != 0) {
                author.setSpan(new ForegroundColorSpan(authorcolor), 0, author.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            author.setSpan(new RelativeSizeSpan(0.8f), 0, author.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            b.append(author);
            b.append("\n\n");
            b.append(a.getAction()).append(" ").append(!a.getDataNode().get("target_title").isNull() ?
                    "\""
                            + a.getDataNode().get("target_title").asText()
                            + "\"" : "").append(a.getTargetAuthor() != null ? " by /u/"
                    + a.getTargetAuthor() : "");
            if (a.getTargetPermalink() != null) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        OpenRedditLink.openUrl(mContext, a.getTargetPermalink(), true);
                    }
                });
            }

            if (a.getDetails() != null) {
                SpannableStringBuilder description =
                        new SpannableStringBuilder(" (" + a.getDetails() + ")");
                description.setSpan(new StyleSpan(Typeface.ITALIC), 0, description.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                description.setSpan(new RelativeSizeSpan(0.8f), 0, description.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                b.append(description);
            }

            holder.body.setText(b);

            String action = a.getAction();
            switch (action) {
                case "removelink":
                    holder.icon.setImageDrawable(
                            ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.close,
                                    null));
                    break;
                case "approvecomment":
                case "approvelink":
                    holder.icon.setImageDrawable(
                            ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.support,
                                    null));
                    break;
                case "removecomment":
                    holder.icon.setImageDrawable(ResourcesCompat.getDrawable(mContext.getResources(),
                            R.drawable.commentchange, null));
                    break;
                case "editflair":
                    holder.icon.setImageDrawable(
                            ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.flair,
                                    null));
                    break;
                case "distinguish":
                    holder.icon.setImageDrawable(ResourcesCompat.getDrawable(mContext.getResources(),
                            R.drawable.star, null));
                    break;
                case "sticky":
                case "unsticky":
                    holder.icon.setImageDrawable(
                            ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.lock,
                                    null));
                    break;
                case "ignorereports":
                    holder.icon.setImageDrawable(
                            ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.ignore,
                                    null));
                    break;
                case "unignorereports":
                    holder.icon.setImageDrawable(
                            ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.unignore,
                                    null));
                    break;
                case "marknsfw":
                case "unmarknsfw":
                    holder.icon.setImageDrawable(
                            ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.hide,
                                    null));
                    break;
                default:
                    holder.icon.setImageDrawable(
                            ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.mod, null));
                    break;
            }

        }
        if (firstHold instanceof SpacerViewHolder) {
            firstHold.itemView.findViewById(R.id.height)
                    .setLayoutParams(new LinearLayout.LayoutParams(firstHold.itemView.getWidth(),
                            mContext.findViewById(R.id.header).getHeight()));
        }
    }

    @Override
    public int getItemCount() {
        if (dataSet.posts == null || dataSet.posts.isEmpty()) {
            return 0;
        } else {
            return dataSet.posts.size() + 1;
        }
    }


}
