package me.ccrama.redditslide.Adapters;

/**
 * Created by ccrama on 3/22/2015.
 */

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
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
import me.ccrama.redditslide.Visuals.Pallete;


public class CommentAdapterSearch extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private final Context mContext;
    private List<CommentNode> dataSet;
    private final RecyclerView listView;
    private final List<CommentNode> originalDataSet;


    ///... other methods

    private String search;

    @Override
    public Filter getFilter() {
        return new UserFilter(this, originalDataSet);
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
            search = constraint.toString();

            adapter.dataSet = new ArrayList<>();
            adapter.dataSet.addAll((ArrayList<CommentNode>) results.values);
            adapter.notifyDataSetChanged();
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comment, viewGroup, false);
        return new CommentViewHolder(v);

    }

    private Submission submission;

    private final String subAuthor;
    public CommentAdapterSearch(Context mContext, List<CommentNode> dataSet, RecyclerView listView, String subAuthor) {

        this.mContext = mContext;
        this.listView = listView;
        this.subAuthor = subAuthor;
        this.originalDataSet = dataSet;
        List<CommentNode> filteredUserList = new ArrayList<>();

    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder firstHolder, int pos) {

        final CommentViewHolder holder = (CommentViewHolder) firstHolder;

        final CommentNode baseNode = dataSet.get(pos);
        final Comment comment = baseNode.getComment();



        firstHolder.itemView.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                final View dialoglayout = inflater.inflate(R.layout.commentmenu, null);
                AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(mContext);
                final TextView title = (TextView) dialoglayout.findViewById(R.id.title);
                title.setText(comment.getBody());

                ((TextView) dialoglayout.findViewById(R.id.userpopup)).setText("/u/" + comment.getAuthor());
                dialoglayout.findViewById(R.id.userpopup).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(mContext, Profile.class);
                        i.putExtra("profile", comment.getAuthor());
                        mContext.startActivity(i);
                    }
                });


                dialoglayout.findViewById(R.id.gild).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String urlString = submission.getUrl() + comment.getFullName().substring(3, comment.getFullName().length()) + "?context=3";

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
                        String urlString = submission.getUrl() + comment.getFullName().substring(3, comment.getFullName().length()) + "?context=3";

                        Reddit.defaultShareText(urlString, mContext);

                    }
                });
                if (!Authentication.isLoggedIn) {
                    dialoglayout.findViewById(R.id.gild).setVisibility(View.GONE);

                }
                title.setBackgroundColor(Pallete.getColor(submission.getSubredditName()));

                builder.setView(dialoglayout);
                builder.show();
            }
        });

        String author = comment.getAuthor();
        holder.author.setText(author);
        if (comment.getAuthorFlair() != null && comment.getAuthorFlair().getText() != null && !comment.getAuthorFlair().getText().isEmpty()) {
            holder.itemView.findViewById(R.id.flairbubble).setVisibility(View.VISIBLE);
            ((TextView) holder.itemView.findViewById(R.id.text)).setText(comment.getAuthorFlair().getText());

        } else {
            holder.itemView.findViewById(R.id.flairbubble).setVisibility(View.GONE);

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

                Intent i2 = new Intent(mContext, Profile.class);
                i2.putExtra("profile", comment.getAuthor());
                mContext.startActivity(i2);
            }
        });
        holder.author.setTextColor(Pallete.getFontColorUser(comment.getAuthor()));
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


}