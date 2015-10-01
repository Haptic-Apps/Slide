package me.ccrama.redditslide.Adapters;

import net.dean.jraw.models.Message;
import net.dean.jraw.paginators.InboxPaginator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by carlo_000 on 9/17/2015.
 */
public class InboxContributions {
    public ArrayList<Message> messages;
    public InboxPaginator paginator;
    public InboxContributions(ArrayList<Message> firstData, InboxPaginator paginator){
        messages = firstData;
        this.paginator = paginator;
    }
    public void addData(List<Message> data){
        messages.addAll(data);
    }
}
