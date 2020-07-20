package me.ccrama.redditslide.Adapters;

import android.app.Activity;
import android.os.AsyncTask;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.fasterxml.jackson.databind.JsonNode;

import net.dean.jraw.models.Message;
import net.dean.jraw.models.PrivateMessage;
import net.dean.jraw.paginators.InboxPaginator;
import net.dean.jraw.paginators.Paginator;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Authentication;

/**
 * Created by ccrama on 9/17/2015.
 */
public class InboxMessages extends GeneralPosts {
    public ArrayList<Message> posts;
    public boolean loading;
    private Paginator<Message> paginator;
    private SwipeRefreshLayout refreshLayout;
    public String where;
    private InboxAdapter adapter;

    public InboxMessages(ArrayList<Message> firstData, InboxPaginator paginator) {
        posts = firstData;
        this.paginator = paginator;
    }

    public InboxMessages(String where) {
        this.where = where;
    }

    public void bindAdapter(InboxAdapter a, SwipeRefreshLayout layout) {
        this.adapter = a;
        this.refreshLayout = layout;
        loadMore(a, where, true);
    }

    public void loadMore(InboxAdapter adapter, String where, boolean refresh) {

            new LoadData(refresh).execute(where);



    }

    public void addData(List<Message> data) {
        posts.addAll(data);
    }

    public class LoadData extends AsyncTask<String, Void, ArrayList<Message>> {
        final boolean reset;

        public LoadData(boolean reset) {
            this.reset = reset;
        }

        @Override
        public void onPostExecute(ArrayList<Message> subs) {
            if (subs == null && !nomore) {
                adapter.setError(true);
                refreshLayout.setRefreshing(false);
            } else if(!nomore) {

                if(subs.size() < 25){
                    nomore = true;
                }
                if (reset) {
                    posts = subs;

                } else {
                    if(posts == null){
                        posts =new ArrayList<>();
                    }
                    posts.addAll(subs);
                }
                ((Activity) adapter.mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.setRefreshing(false);
                        loading = false;
                        adapter.notifyDataSetChanged();

                    }
                });
            }
        }

        @Override
        protected ArrayList<Message> doInBackground(String... subredditPaginators) {
            try {
                if (reset || paginator == null) {
                    paginator = new InboxPaginator(Authentication.reddit, where);
                    paginator.setLimit(25);
                    nomore = false;
                }
                if (paginator.hasNext()) {
                    ArrayList<Message> done = new ArrayList<>();
                    for (Message m : paginator.next()) {
                        done.add(m);
                        if (m.getDataNode().has("replies") && !m.getDataNode().get("replies").toString().isEmpty() && m.getDataNode().get("replies").has("data") && m.getDataNode().get("replies").get("data").has("children")) {
                            JsonNode n = m.getDataNode().get("replies").get("data").get("children");

                            for (JsonNode o : n) {
                                done.add(new PrivateMessage(o.get("data")));
                            }

                        }

                    }
                    return done;

                } else {
                    nomore = true;
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
