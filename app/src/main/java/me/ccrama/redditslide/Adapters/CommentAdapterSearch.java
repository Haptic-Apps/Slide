package me.ccrama.redditslide.Adapters;

/**
 * Created by ccrama on 3/22/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.DistinguishedStatus;
import net.dean.jraw.models.Submission;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Views.MakeTextviewClickable;
import me.ccrama.redditslide.Visuals.Palette;


public class CommentAdapterSearch extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private final Context mContext;
    private final List<CommentNode> originalDataSet;
    private final String subAuthor;


    ///... other methods
    private List<CommentNode> dataSet;
    private Submission submission;


    public CommentAdapterSearch(Context mContext, List<CommentNode> dataSet, RecyclerView listView, String subAuthor) {

        this.mContext = mContext;
        RecyclerView listView1 = listView;
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
        holder.time.setText(TimeUtils.getTimeAgo(comment.getCreatedUtc().getTime(), mContext));

        new MakeTextviewClickable().ParseTextWithLinksTextViewComment(comment.getDataNode().get("body_html").asText(), holder.content, (Activity) mContext, comment.getSubredditName());
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
                if (Reddit.click_user_name_to_profile) {
                    Intent i2 = new Intent(mContext, Profile.class);
                    i2.putExtra("profile", comment.getAuthor());
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
        holder.content.setOnClickListener(new View.OnClickListener() {
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
                v.setBackgroundColor(Color.parseColor("#2196F3")); //blue
            } else if (i22 % 4 == 0) {
                v.setBackgroundColor(Color.parseColor("#4CAF50")); //green

            } else if (i22 % 3 == 0) {
                v.setBackgroundColor(Color.parseColor("#FFC107")); //yellow

            } else if (i22 % 2 == 0) {
                v.setBackgroundColor(Color.parseColor("#FF9800")); //orange

            } else {
                v.setBackgroundColor(Color.parseColor("#F44336")); //red
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