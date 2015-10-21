package me.ccrama.redditslide.Adapters;

import net.dean.jraw.models.Message;
import net.dean.jraw.paginators.InboxPaginator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ccrama on 9/17/2015.
 */
class InboxContributions {
    private final ArrayList<Message> messages;
    private final InboxPaginator paginator;
    public InboxContributions(ArrayList<Message> firstData, InboxPaginator paginator){
        messages = firstData;
        this.paginator = paginator;
    }
    public void addData(List<Message> data){
        messages.addAll(data);
    }
}
