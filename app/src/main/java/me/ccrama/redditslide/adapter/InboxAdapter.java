package me.ccrama.redditslide.adapter;

/**
 * Created by ccrama on 3/22/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.dean.jraw.managers.InboxManager;
import net.dean.jraw.models.Message;
import net.dean.jraw.models.PrivateMessage;

import java.util.ArrayList;

import me.ccrama.redditslide.activity.SendMessage;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.view.MakeTextviewClickable;


public class InboxAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements BaseAdapter {

    private static final int TOP_LEVEL = 1;
    public final Context mContext;
    private final RecyclerView listView;
    public ArrayList<Message> dataSet;

    public InboxAdapter(Context mContext, InboxMessages dataSet, RecyclerView listView) {

        this.mContext = mContext;
        this.listView = listView;
        this.dataSet = dataSet.posts;

        boolean isSame = false;

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

        if (position == dataSet.size()  &&dataSet.size() != 0) {
            return 5;
        }

        if (!dataSet.get(position).getSubject().toLowerCase().contains("re:"))//IS COMMENT
            return TOP_LEVEL;

        return 2;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        if (i == TOP_LEVEL) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.top_level_message, viewGroup, false);
            return new MessageViewHolder(v);
        } else if (i == 5) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.loadingmore, viewGroup, false);
            return new ContributionAdapter.EmptyViewHolder(v);
        } else {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_reply, viewGroup, false);
            return new MessageViewHolder(v);

        }

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder2, final int i) {

        if (!(holder2 instanceof ContributionAdapter.EmptyViewHolder)) {

            final MessageViewHolder holder = (MessageViewHolder) holder2;

            final Message comment = dataSet.get(i);
            holder.time.setText(TimeUtils.getTimeAgo(comment.getCreatedUtc().getTime(), mContext));

            holder.user.setText(comment.getAuthor());

            new MakeTextviewClickable().ParseTextWithLinksTextViewComment(comment.getDataNode().get("body_html").asText(), holder.content, (Activity) mContext, "");

            holder.title.setText(comment.getSubject());
            if (comment.isRead()) {
                holder.title.setTextColor(holder.content.getCurrentTextColor());
            } else {
                holder.title.setTextColor(mContext.getResources().getColor(R.color.md_red_500));
            }

            holder.content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (comment.isRead()) {
                        if (comment instanceof PrivateMessage) {
                            DataShare.sharedMessage = (PrivateMessage) comment;
                            Intent i = new Intent(mContext, SendMessage.class);
                            i.putExtra("name", comment.getAuthor());
                            i.putExtra("reply", true);
                            mContext.startActivity(i);
                        } else {
                            new OpenRedditLink(mContext, comment.getDataNode().get("context").asText());
                        }
                    } else {
                        comment.read = true;
                        new AsyncSetRead().execute(comment);

                        holder.title.setTextColor(holder.content.getCurrentTextColor());

                    }
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (comment.isRead()) {
                        if (comment instanceof PrivateMessage) {
                            DataShare.sharedMessage = (PrivateMessage) comment;
                            Intent i = new Intent(mContext, SendMessage.class);
                            i.putExtra("name", comment.getAuthor());
                            i.putExtra("reply", true);
                            mContext.startActivity(i);
                        } else {
                            new OpenRedditLink(mContext, comment.getDataNode().get("context").asText());
                        }

                    } else {
                        comment.read = true;
                        new AsyncSetRead().execute(comment);
                        holder.title.setTextColor(holder.content.getCurrentTextColor());

                    }
                }
            });
        }


    }

    @Override
    public int getItemCount() {
        if (dataSet == null || dataSet.size() == 0) {
            return 0;
        } else {
            return dataSet.size() + 1;
        }
    }

    private class AsyncSetRead extends AsyncTask<Message, Void, Void> {

        @Override
        protected Void doInBackground(Message... params) {
            new InboxManager(Authentication.reddit).setRead(params[0], true);
            return null;
        }
    }


}