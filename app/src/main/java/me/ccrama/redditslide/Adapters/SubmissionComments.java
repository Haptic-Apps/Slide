package me.ccrama.redditslide.Adapters;

import android.graphics.Color;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.android.material.snackbar.Snackbar;

import net.dean.jraw.http.RestResponse;
import net.dean.jraw.http.SubmissionRequest;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.CommentSort;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.meta.SubmissionSerializer;
import net.dean.jraw.util.JrawUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Fragments.CommentPage;
import me.ccrama.redditslide.LastComments;
import me.ccrama.redditslide.util.NetworkUtil;

/**
 * Created by ccrama on 9/17/2015.
 */
public class SubmissionComments {

    public boolean single;
    public final SwipeRefreshLayout refreshLayout;
    private final String fullName;
    private final CommentPage page;
    public ArrayList<CommentObject> comments;
    public HashMap<String, String> commentOPs = new HashMap<>();
    public Submission submission;
    private String context;
    private CommentSort defaultSorting = CommentSort.CONFIDENCE;
    private CommentAdapter adapter;
    public LoadData mLoadData;
    public boolean online = true;
    int contextNumber = 5;

    public SubmissionComments(String fullName, CommentPage commentPage, SwipeRefreshLayout layout, Submission s) {
        this.fullName = fullName;
        this.page = commentPage;
        online = NetworkUtil.isConnected(page.getActivity());

        this.refreshLayout = layout;

        if (s.getComments() != null) {
            submission = s;
            CommentNode baseComment = s.getComments();
            comments = new ArrayList<>();
            Map<Integer, MoreChildItem> waiting = new HashMap<>();

            for (CommentNode n : baseComment.walkTree()) {

                CommentObject obj = new CommentItem(n);
                List<Integer> removed = new ArrayList<>();
                Map<Integer, MoreChildItem> map = new TreeMap<>(Collections.reverseOrder());
                map.putAll(waiting);

                for (Integer i : map.keySet()) {
                    if (i >= n.getDepth()) {
                        comments.add(waiting.get(i));
                        removed.add(i);
                        waiting.remove(i);

                    }
                }

                comments.add(obj);

                if (n.hasMoreComments() && online) {
                    waiting.put(n.getDepth(), new MoreChildItem(n, n.getMoreChildren()));
                }
            }
            Map<Integer, MoreChildItem> map = new TreeMap<>(Collections.reverseOrder());
            map.putAll(waiting);
            for (Integer i : map.keySet()) {
                comments.add(waiting.get(i));
                waiting.remove(i);
            }
            if (baseComment.hasMoreComments() && online) {
                comments.add(new MoreChildItem(baseComment, baseComment.getMoreChildren()));
            }

            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }

            refreshLayout.setRefreshing(false);
            refreshLayout.setEnabled(false);
        }
    }

    public SubmissionComments(String fullName, CommentPage commentPage, SwipeRefreshLayout layout) {
        this.fullName = fullName;
        this.page = commentPage;
        this.refreshLayout = layout;
    }

    public SubmissionComments(String fullName, CommentPage commentPage, SwipeRefreshLayout layout, String context) {
        this.fullName = fullName;
        this.page = commentPage;
        this.context = context;
        this.refreshLayout = layout;
    }

    public SubmissionComments(String fullName, CommentPage commentPage, SwipeRefreshLayout layout, String context, int contextNumber) {
        this.fullName = fullName;
        this.page = commentPage;
        this.context = context;
        this.refreshLayout = layout;
        this.contextNumber = contextNumber;
    }

    public void cancelLoad() {
        if (mLoadData != null) {
            mLoadData.cancel(true);
        }
    }

    public void setSorting(CommentSort sort) {
        defaultSorting = sort;
        mLoadData = new LoadData(true);
        mLoadData.execute(fullName);

    }

    public void loadMore(CommentAdapter adapter, String subreddit) {
        this.adapter = adapter;
        mLoadData = new LoadData(true);
        mLoadData.execute(fullName);
    }

    public void loadMoreReply(CommentAdapter adapter) {
        this.adapter = adapter;
        adapter.currentSelectedItem = context;
        mLoadData = new LoadData(false);
        mLoadData.execute(fullName);
    }

    public void loadMoreReplyTop(CommentAdapter adapter, String context) {
        this.adapter = adapter;
        adapter.currentSelectedItem = context;
        mLoadData = new LoadData(true);
        mLoadData.execute(fullName);
        if (context == null || context.isEmpty()) {
            Snackbar s = Snackbar.make(page.rv, "Comment submitted", Snackbar.LENGTH_SHORT);
            View view = s.getView();
            TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
            tv.setTextColor(Color.WHITE);
            s.show();
        }
    }

    public void loadMore(CommentAdapter adapter, String subreddit, boolean forgetPlace) {
        adapter.currentSelectedItem = "";
        this.adapter = adapter;
        mLoadData = new LoadData(true);
        mLoadData.execute(fullName);

    }

    public JsonNode getSubmissionNode(SubmissionRequest request) {
        Map<String, String> args = new HashMap<>();
        if (request.getDepth() != null)
            args.put("depth", Integer.toString(request.getDepth()));
        if (request.getContext() != null)
            args.put("context", Integer.toString(request.getContext()));
        if (request.getLimit() != null)
            args.put("limit", Integer.toString(request.getLimit()));
        if (request.getFocus() != null && request.getFocus().length() >= 3 && !JrawUtils.isFullname(request.getFocus()))
            args.put("comment", request.getFocus());
        args.put("feature", "link_preview");
        args.put("sr_detail", "true");
        args.put("expand_srs", "true");
        args.put("from_detail", "true");
        args.put("always_show_media", "1");


        CommentSort sort = request.getSort();
        if (sort == null)
            // Reddit sorts by confidence by default
            sort = CommentSort.CONFIDENCE;
        args.put("sort", sort.name().toLowerCase(Locale.ENGLISH));

        RestResponse response = Authentication.reddit.execute(Authentication.reddit.request()
                .path(String.format("/comments/%s", request.getId()))
                .query(args)
                .build());


        return response.getJson();
    }

    public void reloadSubmission(CommentAdapter commentAdapter) {
        commentAdapter.submission = Authentication.reddit.getSubmission(submission.getFullName().substring(3));
    }

    public boolean forceSorting = false;

    public class LoadData extends AsyncTask<String, Void, ArrayList<CommentObject>> {
        final boolean reset;

        public LoadData(boolean reset) {
            this.reset = reset;
        }

        @Override
        public void onPostExecute(ArrayList<CommentObject> subs) {
            if (page.isVisible() && submission != null) {
                refreshLayout.setRefreshing(false);
                page.doRefresh(false);
                if ((submission.isArchived() && !page.archived) || (submission.isLocked() && !page.locked) || (submission.getDataNode().get("contest_mode").asBoolean() && !page.contest))
                    page.doTopBarNotify(submission, adapter);

                page.doData(reset);
                LastComments.setComments(submission);
            }
        }

        @Override
        protected ArrayList<CommentObject> doInBackground(String... subredditPaginators) {
            SubmissionRequest.Builder builder;
            if (context == null) {
                single = false;
                builder = new SubmissionRequest.Builder(fullName).sort(defaultSorting);
            } else {
                single = true;
                builder = new SubmissionRequest.Builder(fullName).sort(defaultSorting).focus(context).context(contextNumber);
            }
            try {

                JsonNode node = getSubmissionNode(builder.build());
                submission = SubmissionSerializer.withComments(node, defaultSorting);
                CommentNode baseComment = submission.getComments();

              /* if (page.o != null)
                    page.o.setCommentAndWrite(submission.getFullName(), node, submission).writeToMemory();*/

                comments = new ArrayList<>();
                Map<Integer, MoreChildItem> waiting = new HashMap<>();
                commentOPs = new HashMap<>();
                String currentOP = "";

                for (CommentNode n : baseComment.walkTree()) {
                    if(n.getDepth() == 1){
                        currentOP = n.getComment().getAuthor();
                    }
                    commentOPs.put(n.getComment().getId(), currentOP);
                    CommentObject obj = new CommentItem(n);
                    List<Integer> removed = new ArrayList<>();
                    Map<Integer, MoreChildItem> map = new TreeMap<>(Collections.reverseOrder());
                    map.putAll(waiting);

                    for (Integer i : map.keySet()) {
                        if (i >= n.getDepth()) {
                            comments.add(waiting.get(i));
                            removed.add(i);
                            waiting.remove(i);

                        }
                    }

                    comments.add(obj);

                    if (n.hasMoreComments()) {
                        waiting.put(n.getDepth(), new MoreChildItem(n, n.getMoreChildren()));
                    }
                }
                Map<Integer, MoreChildItem> map = new TreeMap<>(Collections.reverseOrder());
                map.putAll(waiting);
                for (Integer i : map.keySet()) {
                    comments.add(waiting.get(i));
                    waiting.remove(i);
                }
                if (baseComment.hasMoreComments()) {
                    comments.add(new MoreChildItem(baseComment, baseComment.getMoreChildren()));
                }


                return comments;
            } catch (Exception e) {
                //Todo reauthenticate
            }
            return null;
        }
    }


}
