package me.ccrama.redditslide.Adapters;

/**
 * Created by ccrama on 3/22/2015.
 */

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.dean.jraw.managers.InboxManager;
import net.dean.jraw.models.Message;
import net.dean.jraw.models.PrivateMessage;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Activities.Sendmessage;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.util.SubmissionParser;


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
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int i) {
        if (!(viewHolder instanceof ContributionAdapter.EmptyViewHolder)) {
            final MessageViewHolder messageViewHolder = (MessageViewHolder) viewHolder;

            final Message comment = dataSet.get(i);
            messageViewHolder.time.setText(TimeUtils.getTimeAgo(comment.getCreated().getTime(), mContext));
            messageViewHolder.user.setText(comment.getAuthor());
            messageViewHolder.title.setText(comment.getSubject());


            if (comment.isRead()) {
                messageViewHolder.title.setTextColor(messageViewHolder.content.getCurrentTextColor());
            } else {
                messageViewHolder.title.setTextColor(ContextCompat.getColor(mContext, R.color.md_red_500));
            }

            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (comment.isRead()) {
                        if (comment instanceof PrivateMessage) {
                            DataShare.sharedMessage = (PrivateMessage) comment;
                            Intent i = new Intent(mContext, Sendmessage.class);
                            i.putExtra(Sendmessage.EXTRA_NAME, comment.getAuthor());
                            i.putExtra(Sendmessage.EXTRA_REPLY, true);
                            mContext.startActivity(i);
                        } else {
                            new OpenRedditLink(mContext, comment.getDataNode().get("context").asText());
                        }
                    } else {
                        comment.read = true;
                        new AsyncSetRead().execute(comment);

                        messageViewHolder.title.setTextColor(messageViewHolder.content.getCurrentTextColor());

                    }
                }
            });

            setViews(comment.getDataNode().get("body_html").asText(), "", messageViewHolder);


        }

    }

    private void setViews(String rawHTML, String subredditName, MessageViewHolder holder) {
        if (rawHTML.isEmpty()) {
            return;
        }

        List<String> blocks = SubmissionParser.getBlocks(rawHTML);

        int startIndex = 0;
        // the <div class="md"> case is when the body contains a table or code block first
        if (!blocks.get(0).equals("<div class=\"md\">")) {
            holder.content.setVisibility(View.VISIBLE);
            holder.content.setTextHtml(blocks.get(0), subredditName);
            startIndex = 1;
        } else {
            holder.content.setText("");
            holder.content.setVisibility(View.GONE);
        }

        if (blocks.size() > 1) {
            if (startIndex == 0) {
                holder.commentOverflow.setViews(blocks, subredditName);
            } else {
                holder.commentOverflow.setViews(blocks.subList(startIndex, blocks.size()), subredditName);
            }
        } else {
            holder.commentOverflow.removeAllViews();
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
            new InboxManager(Authentication.reddit).setRead(true, params[0]);
            return null;
        }
    }


}