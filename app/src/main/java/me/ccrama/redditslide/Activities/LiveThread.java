package me.ccrama.redditslide.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketListener;
import com.neovisionaries.ws.client.WebSocketState;

import net.dean.jraw.managers.LiveThreadManager;
import net.dean.jraw.models.LiveUpdate;
import net.dean.jraw.paginators.LiveThreadPaginator;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Services.LiveThreadTask;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Views.CommentOverflow;
import me.ccrama.redditslide.Views.SidebarLayout;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.HttpUtil;
import me.ccrama.redditslide.util.LinkUtil;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.SubmissionParser;
import me.ccrama.redditslide.util.TwitterObject;
import okhttp3.OkHttpClient;

public class LiveThread extends BaseActivityAnim {

    public static final String EXTRA_LIVEURL = "liveurl";
    public net.dean.jraw.models.LiveThread      thread;
    private LiveThreadTask.LoadTwitter          loadTwitter;
    private LiveThreadTask.LiveUpdateThread     liveUpdateThread;
    private LiveThreadTask.DialogThread         dialogThread;
    private LiveThreadTask.PaginatorThread      paginatorThread;
    private PaginatorAdapter                    adapter;


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.info:
                ((DrawerLayout) findViewById(R.id.drawer_layout)).openDrawer(Gravity.RIGHT);
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_info, menu);
        return true;
    }

    public RecyclerView baseRecycler;

    public String term;

    @Override
    public void onDestroy() {
        super.onDestroy();
        //todo finish
        if (loadTwitter != null) {
            loadTwitter.cancel(true);
            loadTwitter = null;
        }
        if (liveUpdateThread != null) {
            liveUpdateThread.cancel(true);
            liveUpdateThread = null;
        }
        if (dialogThread != null) {
            dialogThread.cancel(true);
            paginatorThread = null;
        }
        if (paginatorThread != null) {
            paginatorThread.cancel(true);
            dialogThread = null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        overrideSwipeFromAnywhere();
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().getDecorView().setBackgroundDrawable(null);
        super.onCreate(savedInstanceState);

        applyColorTheme();

        setContentView(R.layout.activity_livethread);
        baseRecycler = (RecyclerView) findViewById(R.id.content_view);
        baseRecycler.setLayoutManager(new LinearLayoutManager(LiveThread.this));

        dialogThread = new LiveThreadTask.DialogThread(this);
        dialogThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }


    public void updateToolBar() {
        if(thread == null){
            new AlertDialogWrapper.Builder(this)
                    .setTitle(R.string.livethread_not_found)
                    .setMessage(R.string.misc_please_try_again_soon)
                    .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                }
            }).setCancelable(false).show();
        } else {
            setupAppBar(R.id.toolbar, thread.getTitle(), true, false);
            (findViewById(R.id.toolbar)).setBackgroundResource(R.color.md_red_300);
            (findViewById(R.id.header_sub)).setBackgroundResource(R.color.md_red_300);
            themeSystemBars(Palette.getDarkerColor(getResources().getColor(R.color.md_red_300)));
            setRecentBar(getString(R.string.livethread_recents_title, thread.getTitle()), getResources().getColor(R.color.md_red_300));
            doPaginator();
        }
    }

    public ArrayList<LiveUpdate> updates;
    public LiveThreadPaginator paginator;

    public void doPaginator() {

        paginatorThread = new LiveThreadTask.PaginatorThread(this);
        paginatorThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    public void doLiveThreadUpdates() {
        adapter = new PaginatorAdapter(this);
        baseRecycler.setAdapter(adapter);
        doLiveSidebar();
        if (thread.getWebsocketUrl() != null && !thread.getWebsocketUrl().isEmpty()) {
            liveUpdateThread = new LiveThreadTask.LiveUpdateThread(this);
            liveUpdateThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    /**
     * Method to update the recyclerView adapter
     *
     * @param s received string
     */
    public void updateLive(String s) {
        final ObjectReader o = new ObjectMapper().reader();
        LogUtil.v("Recieved" + s);
        if (s.contains("\"type\": \"update\"")) {
            try {
                LiveUpdate u = new LiveUpdate(o.readTree(s).get("payload").get("data"));
                updates.add(0, u);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyItemInserted(0);
                        baseRecycler.smoothScrollToPosition(0);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (s.contains("embeds_ready")) {
            String node = updates.get(0).getDataNode().toString();
            LogUtil.v("Getting");
            try {
                node = node.replace("\"embeds\":[]", "\"embeds\":" + o.readTree(s).get("payload").get("media_embeds").toString());
                LiveUpdate u = new LiveUpdate(o.readTree(node));
                updates.set(0, u);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyItemChanged(0);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            } /* todoelse if(s.contains("delete")){
                 updates.remove(0);
                 adapter.notifyItemRemoved(0);
               }*/

        }

    }

    public class PaginatorAdapter extends RecyclerView.Adapter<PaginatorAdapter.ItemHolder> {

        private LayoutInflater layoutInflater;

        public PaginatorAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public PaginatorAdapter.ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.live_list_item, parent, false);
            return new ItemHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final PaginatorAdapter.ItemHolder holder, int position) {
            final LiveUpdate u = updates.get(position);

            holder.title.setText("/u/" + u.getAuthor() + " " + TimeUtils.getTimeAgo(u.getCreated().getTime(), LiveThread.this));
            if (u.getBody().isEmpty()) {
                holder.info.setVisibility(View.GONE);
            } else {
                holder.info.setVisibility(View.VISIBLE);
                holder.info.setTextHtml(Html.fromHtml(u.getDataNode().get("body_html").asText()), "NO SUBREDDIT");
            }
            holder.title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(LiveThread.this, Profile.class);
                    i.putExtra(Profile.EXTRA_PROFILE, u.getAuthor());
                    startActivity(i);
                }
            });
            holder.imageArea.setVisibility(View.GONE);
            holder.twitterArea.setVisibility(View.GONE);
            holder.twitterArea.stopLoading();
            if (u.getEmbeds().size() == 0) {
                holder.go.setVisibility(View.GONE);
            } else {
                final String url = u.getEmbeds().get(0).getUrl();
                holder.go.setVisibility(View.VISIBLE);
                holder.go.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(LiveThread.this, Website.class);
                        i.putExtra(LinkUtil.EXTRA_URL, url);
                        startActivity(i);
                    }
                });
                final String host = URI.create(url).getHost().toLowerCase(Locale.ENGLISH);

                if (ContentType.hostContains(host, "imgur.com")) {
                    LogUtil.v("Imgur");
                    holder.imageArea.setVisibility(View.VISIBLE);
                    holder.imageArea.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            holder.go.callOnClick();
                        }
                    });
                    ((Reddit) getApplicationContext()).getImageLoader().displayImage(url, holder.imageArea);
                } else if (ContentType.hostContains(host, "twitter.com")) {
                    LogUtil.v("Twitter");

                    holder.twitterArea.setVisibility(View.VISIBLE);
                    loadTwitter = new LiveThreadTask.LoadTwitter(holder.twitterArea, url);
                    loadTwitter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }

        }


        @Override
        public int getItemCount() {
            return updates.size();
        }

        public class ItemHolder extends RecyclerView.ViewHolder {

            TextView title;
            SpoilerRobotoTextView info;
            ImageView imageArea;
            WebView twitterArea;
            View go;


            public ItemHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.title);
                info = itemView.findViewById(R.id.body);
                go = itemView.findViewById(R.id.go);
                imageArea = itemView.findViewById(R.id.image_area);
                twitterArea = itemView.findViewById(R.id.twitter_area);
                twitterArea.setWebChromeClient(new WebChromeClient());
                twitterArea.getSettings().setJavaScriptEnabled(true);
                twitterArea.setBackgroundColor(Color.TRANSPARENT);
                twitterArea.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);

            }

        }
    }

    public void doLiveSidebar() {
        findViewById(R.id.loader).setVisibility(View.GONE);

        final View dialoglayout = findViewById(R.id.sidebarsub);

        dialoglayout.findViewById(R.id.sub_stuff).setVisibility(View.GONE);

        ((TextView) dialoglayout.findViewById(R.id.sub_infotitle)).setText((thread.getState() ? "LIVE: " : "") + thread.getTitle());
        ((TextView) dialoglayout.findViewById(R.id.active_users)).setText(thread.getLocalizedViewerCount() + " viewing");
        ((TextView) dialoglayout.findViewById(R.id.active_users)).setText(thread.getLocalizedViewerCount());

        {
            final String text = thread.getDataNode().get("resources_html").asText();
            final SpoilerRobotoTextView body = (SpoilerRobotoTextView) findViewById(R.id.sidebar_text);
            CommentOverflow overflow = (CommentOverflow) findViewById(R.id.commentOverflow);
            setViews(text, "none", body, overflow);
        }
        {
            final String text = thread.getDataNode().get("description_html").asText();
            final SpoilerRobotoTextView body = (SpoilerRobotoTextView) findViewById(R.id.sub_title);
            CommentOverflow overflow = (CommentOverflow) findViewById(R.id.sub_title_overflow);
            setViews(text, "none", body, overflow);
        }
    }

    private void setViews(String rawHTML, String subreddit, SpoilerRobotoTextView firstTextView, CommentOverflow commentOverflow) {
        if (rawHTML.isEmpty()) {
            return;
        }

        List<String> blocks = SubmissionParser.getBlocks(rawHTML);

        int startIndex = 0;
        // the <div class="md"> case is when the body contains a table or code block first
        if (!blocks.get(0).equals("<div class=\"md\">")) {
            firstTextView.setVisibility(View.VISIBLE);
            firstTextView.setTextHtml(blocks.get(0), subreddit);
            startIndex = 1;
        } else {
            firstTextView.setText("");
            firstTextView.setVisibility(View.GONE);
        }

        if (blocks.size() > 1) {
            if (startIndex == 0) {
                commentOverflow.setViews(blocks, subreddit);
            } else {
                commentOverflow.setViews(blocks.subList(startIndex, blocks.size()), subreddit);
            }
            SidebarLayout sidebar = (SidebarLayout) findViewById(R.id.drawer_layout);
            for (int i = 0; i < commentOverflow.getChildCount(); i++) {
                View maybeScrollable = commentOverflow.getChildAt(i);
                if (maybeScrollable instanceof HorizontalScrollView) {
                    sidebar.addScrollable(maybeScrollable);
                }
            }
        } else {
            commentOverflow.removeAllViews();
        }
    }

}
