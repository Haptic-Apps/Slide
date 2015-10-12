package me.ccrama.redditslide.Adapters;

/**
 * Created by ccrama on 3/22/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.dean.jraw.models.CommentMessage;
import net.dean.jraw.models.Message;
import net.dean.jraw.models.PrivateMessage;

import java.util.ArrayList;

import me.ccrama.redditslide.Activities.Sendmessage;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Views.MakeTextviewClickable;


public class InboxAdapter extends RecyclerView.Adapter<MessageViewHolder> {

    public Context mContext;
    public ArrayList<Message> dataSet;
    RecyclerView listView;


    static int TOP_LEVEL = 1;
    @Override
    public int getItemViewType(int position) {
        if (!dataSet.get(position).getSubject().toLowerCase().contains("re:"))//IS COMMENT
            return TOP_LEVEL;

        return 2;
    }
    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        if (i == TOP_LEVEL) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.top_level_message, viewGroup, false);
            return new MessageViewHolder(v);
        } else {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_reply, viewGroup, false);
            return new MessageViewHolder(v);

        }

    }




    public InboxAdapter(Context mContext, InboxMessages dataSet, RecyclerView listView) {

        this.mContext = mContext;
        this.listView = listView;
        this.dataSet = dataSet.posts;

        isSame = false;

    }


    boolean isSame;


    @Override
    public void onBindViewHolder(final MessageViewHolder holder, final int i) {


            final Message comment = dataSet.get(i);
            holder.time.setText(TimeUtils.getTimeAgo(comment.getCreatedUtc().getTime()));


            new MakeTextviewClickable().ParseTextWithLinksTextViewComment(comment.getDataNode().get("body_html").asText(), holder.content, (Activity) mContext, "");

           holder.title.setText(comment.getSubject());

        holder.content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(comment instanceof PrivateMessage){
                    DataShare.sharedMessage =  (PrivateMessage) comment;
                    Intent i = new Intent(mContext, Sendmessage.class);
                    i.putExtra("name", comment.getAuthor());
                    i.putExtra("reply" , true);
                    ((Activity) mContext).startActivity(i);
                } else {
                    CommentMessage m = (CommentMessage) comment;
                    new OpenRedditLink(mContext,comment.getDataNode().get("context").asText());
                }
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(comment instanceof PrivateMessage){
                    DataShare.sharedMessage =  (PrivateMessage) comment;
                    Intent i = new Intent(mContext, Sendmessage.class);
                    i.putExtra("name", comment.getAuthor());
                    i.putExtra("reply" , true);
                    ((Activity) mContext).startActivity(i);
                } else {
                    CommentMessage m = (CommentMessage) comment;
                    new OpenRedditLink(mContext,comment.getDataNode().get("context").asText());
                }
                return;
            }
        });



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