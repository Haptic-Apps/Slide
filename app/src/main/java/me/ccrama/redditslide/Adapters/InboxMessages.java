package me.ccrama.redditslide.Adapters;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;

import com.fasterxml.jackson.databind.JsonNode;

import net.dean.jraw.managers.InboxManager;
import net.dean.jraw.models.Message;
import net.dean.jraw.models.PrivateMessage;
import net.dean.jraw.paginators.InboxPaginator;
import net.dean.jraw.paginators.Paginator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Reddit;

/**
 * Created by ccrama on 9/17/2015.
 */
public class InboxMessages {
    public ArrayList<Message> posts;
    private Paginator<Message> paginator;
    private SwipeRefreshLayout refreshLayout;

    public InboxMessages(ArrayList<Message> firstData, InboxPaginator paginator) {
        posts = firstData;
        this.paginator = paginator;
    }

    private String where;

    public InboxMessages(String where) {
        this.where = where;
    }

    private InboxAdapter adapter;

    public void bindAdapter(InboxAdapter a, SwipeRefreshLayout layout) throws ExecutionException, InterruptedException {
        this.adapter = a;
        this.refreshLayout = layout;
        loadMore(a, where);
    }

    public void loadMore(InboxAdapter adapter, String where) {
        if (Reddit.online) {

            new LoadData(true).execute(where);

        } else {
            adapter.setError(true);
            refreshLayout.setRefreshing(false);
        }

    }

    public boolean loading;

    public class LoadData extends AsyncTask<String, Void, ArrayList<Message>> {
        final boolean reset;

        public LoadData(boolean reset) {
            this.reset = reset;
        }

        @Override
        public void onPostExecute(ArrayList<Message> subs) {
            if (subs == null) {
                adapter.setError(true);
            } else {

                if (reset) {
                    posts = subs;

                    ((Activity) adapter.mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshLayout.setRefreshing(false);

                            adapter.dataSet = posts;

                            loading = false;
                            adapter.notifyDataSetChanged();

                        }
                    });
                } else {
                    posts.addAll(subs);
                    ((Activity) adapter.mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshLayout.setRefreshing(false);

                            adapter.dataSet = posts;
                            loading = false;

                            adapter.notifyDataSetChanged();

                        }
                    });
                }
            }
        }

        @Override
        protected ArrayList<Message> doInBackground(String... subredditPaginators) {
            try {
                if (reset || paginator == null) {
                    paginator = new InboxManager(Authentication.reddit).read(where);
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

                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public void addData(List<Message> data) {
        posts.addAll(data);
    }
}
