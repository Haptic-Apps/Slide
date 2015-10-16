package me.ccrama.redditslide.Adapters;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;

import net.dean.jraw.http.NetworkException;
import net.dean.jraw.managers.InboxManager;
import net.dean.jraw.models.Message;
import net.dean.jraw.models.PrivateMessage;
import net.dean.jraw.paginators.InboxPaginator;
import net.dean.jraw.paginators.Paginator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import me.ccrama.redditslide.Authentication;

public class InboxMessages {
    public ArrayList<Message> posts;
    public Paginator<Message> paginator;
    public SwipeRefreshLayout refreshLayout;

    public InboxMessages(ArrayList<Message> firstData, InboxPaginator paginator) {
        posts = firstData;
        this.paginator = paginator;
    }

    public String where;

    public InboxMessages(String where) {
        this.where = where;
    }

    public InboxAdapter adapter;

    public void bindAdapter(InboxAdapter a, SwipeRefreshLayout layout) throws ExecutionException, InterruptedException {
        this.adapter = a;
        this.refreshLayout=layout;
        loadMore(a, true, where);
    }

    public void loadMore(InboxAdapter adapter, boolean reset, String where) throws ExecutionException, InterruptedException {
        new LoadData(reset).execute(where);


    }

    public class LoadData extends AsyncTask<String, Void, ArrayList<Message>> {
        boolean reset;

        public LoadData(boolean reset) {
            this.reset = reset;
        }

        @Override
        public void onPostExecute(ArrayList<Message> subs) {
            if (reset) {
                posts = subs;
            } else {
                posts.addAll(subs);
            }
            ((Activity) adapter.mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshLayout.setRefreshing(false);

                    adapter.dataSet = posts;

                    adapter.notifyDataSetChanged();

                }
            });
        }

        @Override
        protected ArrayList<Message> doInBackground(String... subredditPaginators) {
            if (reset || paginator == null) {
                    paginator = new InboxManager(Authentication.reddit).read(where);
            }
            if (paginator.hasNext()) {
                try {
                   ArrayList<Message> done = new ArrayList<>(paginator.next());
                    for(Message m : done){
                        if(m instanceof PrivateMessage){
                        }
                    }
                    return done;
                } catch (NetworkException e){

                }
            }
            return null;
        }
    }

    public void addData(List<Message> data) {
        posts.addAll(data);
    }
}
