package me.ccrama.redditslide.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketListener;
import com.neovisionaries.ws.client.WebSocketState;

import net.dean.jraw.ApiException;
import net.dean.jraw.http.MultiRedditUpdateRequest;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.managers.LiveThreadManager;
import net.dean.jraw.managers.ModerationManager;
import net.dean.jraw.managers.MultiRedditManager;
import net.dean.jraw.models.FlairTemplate;
import net.dean.jraw.models.LiveUpdate;
import net.dean.jraw.models.MultiReddit;
import net.dean.jraw.models.MultiSubreddit;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.models.UserRecord;
import net.dean.jraw.paginators.LiveThreadPaginator;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;
import net.dean.jraw.paginators.UserRecordPaginator;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.ccrama.redditslide.Adapters.SettingsSubAdapter;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.Fragments.BlankFragment;
import me.ccrama.redditslide.Fragments.CommentPage;
import me.ccrama.redditslide.Fragments.SubmissionsView;
import me.ccrama.redditslide.ImgurAlbum.AlbumImage;
import me.ccrama.redditslide.ImgurAlbum.Image;
import me.ccrama.redditslide.ImgurAlbum.SingleAlbumImage;
import me.ccrama.redditslide.ImgurAlbum.SingleImage;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.PostMatch;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SecretConstants;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.Views.CatchStaggeredGridLayoutManager;
import me.ccrama.redditslide.Views.CommentOverflow;
import me.ccrama.redditslide.Views.PreCachingLayoutManager;
import me.ccrama.redditslide.Views.SidebarLayout;
import me.ccrama.redditslide.Views.ToggleSwipeViewPager;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.HttpUtil;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.OnSingleClickListener;
import me.ccrama.redditslide.util.SubmissionParser;
import me.ccrama.redditslide.util.TwitterObject;
import okhttp3.OkHttpClient;

public class LiveThread extends BaseActivityAnim {

    public static final String EXTRA_LIVEURL = "liveurl";
    public net.dean.jraw.models.LiveThread thread;


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
        new AsyncTask<Void, Void, Void>() {
            MaterialDialog d;

            @Override
            public void onPreExecute() {
                d = new MaterialDialog.Builder(LiveThread.this)
                        .title("Loading live thread...")
                        .progress(true, 100)
                        .cancelable(false)
                        .show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                thread = new LiveThreadManager(Authentication.reddit).get(getIntent().getStringExtra(EXTRA_LIVEURL));
                return null;
            }

            @Override
            public void onPostExecute(Void aVoid) {
                d.dismiss();
                setupAppBar(R.id.toolbar, thread.getTitle(), true, false);
                (findViewById(R.id.toolbar)).setBackgroundResource(R.color.md_red_300);
                (findViewById(R.id.header_sub)).setBackgroundResource(R.color.md_red_300);
                themeSystemBars(Palette.getDarkerColor(getResources().getColor(R.color.md_red_300)));
                setRecentBar("Live thread: " + thread.getTitle(), getResources().getColor(R.color.md_red_300));

                doPaginator();
            }
        }.execute();
    }

    ArrayList<LiveUpdate> updates;
    LiveThreadPaginator paginator;

    public void doPaginator() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                paginator = new LiveThreadManager(Authentication.reddit).stream(thread);
                updates = new ArrayList<>(paginator.accumulateMerged(5));
                return null;
            }

            @Override
            public void onPostExecute(Void aVoid) {

                doLiveThreadUpdates();
            }
        }.execute();
    }

    public void doLiveThreadUpdates() {
        final PaginatorAdapter adapter = new PaginatorAdapter(this);
        baseRecycler.setAdapter(adapter);
        doLiveSidebar();
        if (thread.getWebsocketUrl() != null && !thread.getWebsocketUrl().isEmpty()) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    final ObjectReader o = new ObjectMapper().reader();

                    try {
                        com.neovisionaries.ws.client.WebSocket ws = new WebSocketFactory().createSocket(thread.getWebsocketUrl());
                        ws.addListener(new WebSocketListener() {
                            @Override
                            public void onStateChanged(com.neovisionaries.ws.client.WebSocket websocket, WebSocketState newState) throws Exception {

                            }

                            @Override
                            public void onConnected(com.neovisionaries.ws.client.WebSocket websocket, Map<String, List<String>> headers) throws Exception {

                            }

                            @Override
                            public void onConnectError(com.neovisionaries.ws.client.WebSocket websocket, WebSocketException cause) throws Exception {

                            }

                            @Override
                            public void onDisconnected(com.neovisionaries.ws.client.WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {

                            }

                            @Override
                            public void onFrame(com.neovisionaries.ws.client.WebSocket websocket, WebSocketFrame frame) throws Exception {

                            }

                            @Override
                            public void onContinuationFrame(com.neovisionaries.ws.client.WebSocket websocket, WebSocketFrame frame) throws Exception {

                            }

                            @Override
                            public void onTextFrame(com.neovisionaries.ws.client.WebSocket websocket, WebSocketFrame frame) throws Exception {

                            }

                            @Override
                            public void onBinaryFrame(com.neovisionaries.ws.client.WebSocket websocket, WebSocketFrame frame) throws Exception {

                            }

                            @Override
                            public void onCloseFrame(com.neovisionaries.ws.client.WebSocket websocket, WebSocketFrame frame) throws Exception {

                            }

                            @Override
                            public void onPingFrame(com.neovisionaries.ws.client.WebSocket websocket, WebSocketFrame frame) throws Exception {

                            }

                            @Override
                            public void onPongFrame(com.neovisionaries.ws.client.WebSocket websocket, WebSocketFrame frame) throws Exception {

                            }

                            @Override
                            public void onTextMessage(com.neovisionaries.ws.client.WebSocket websocket, String s) throws Exception {
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
                                    }

                                } /* todoelse if(s.contains("delete")){
                                    updates.remove(0);
                                    adapter.notifyItemRemoved(0);
                                }*/

                            }

                            @Override
                            public void onBinaryMessage(com.neovisionaries.ws.client.WebSocket websocket, byte[] binary) throws Exception {

                            }

                            @Override
                            public void onSendingFrame(com.neovisionaries.ws.client.WebSocket websocket, WebSocketFrame frame) throws Exception {

                            }

                            @Override
                            public void onFrameSent(com.neovisionaries.ws.client.WebSocket websocket, WebSocketFrame frame) throws Exception {

                            }

                            @Override
                            public void onFrameUnsent(com.neovisionaries.ws.client.WebSocket websocket, WebSocketFrame frame) throws Exception {

                            }

                            @Override
                            public void onError(com.neovisionaries.ws.client.WebSocket websocket, WebSocketException cause) throws Exception {

                            }

                            @Override
                            public void onFrameError(com.neovisionaries.ws.client.WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {

                            }

                            @Override
                            public void onMessageError(com.neovisionaries.ws.client.WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames) throws Exception {

                            }

                            @Override
                            public void onMessageDecompressionError(com.neovisionaries.ws.client.WebSocket websocket, WebSocketException cause, byte[] compressed) throws Exception {

                            }

                            @Override
                            public void onTextMessageError(com.neovisionaries.ws.client.WebSocket websocket, WebSocketException cause, byte[] data) throws Exception {

                            }

                            @Override
                            public void onSendError(com.neovisionaries.ws.client.WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {

                            }

                            @Override
                            public void onUnexpectedError(com.neovisionaries.ws.client.WebSocket websocket, WebSocketException cause) throws Exception {

                            }

                            @Override
                            public void handleCallbackError(com.neovisionaries.ws.client.WebSocket websocket, Throwable cause) throws Exception {

                            }

                            @Override
                            public void onSendingHandshake(com.neovisionaries.ws.client.WebSocket websocket, String requestLine, List<String[]> headers) throws Exception {

                            }
                        });
                        ws.connect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (WebSocketException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute();

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
                        i.putExtra(Website.EXTRA_URL, url);
                        startActivity(i);
                    }
                });
                final String host = URI.create(url).getHost().toLowerCase();

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
                    new LoadTwitter(holder.twitterArea, url).execute();
                }
            }

        }

        public class LoadTwitter extends AsyncTask<String, Void, Void> {

            private OkHttpClient client;
            private Gson gson;
            String url;
            private WebView view;
            TwitterObject twitter;

            public LoadTwitter(@NotNull WebView view, @NotNull String url) {
                this.view = view;
                this.url = url;
                client = new OkHttpClient();
                gson = new Gson();
            }

            public void parseJson() {
                try {
                    JsonObject result = HttpUtil.getJsonObject(client, gson, "https://publish.twitter.com/oembed?url=" + url, null);
                   LogUtil.v("Got " + Html.fromHtml(result.toString()));
                    twitter = new ObjectMapper().readValue(result.toString(), TwitterObject.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected Void doInBackground(final String... sub) {
                parseJson();
                return null;
            }

            @Override
            public void onPostExecute(Void aVoid) {
                view.loadData(twitter.getHtml().toString().replace("//platform.twitter", "https://platform.twitter"), "text/html", "UTF-8");
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
                title = (TextView) itemView.findViewById(R.id.title);
                info = (SpoilerRobotoTextView) itemView.findViewById(R.id.body);
                go = itemView.findViewById(R.id.go);
                imageArea = (ImageView) itemView.findViewById(R.id.image_area);
                twitterArea = (WebView) itemView.findViewById(R.id.twitter_area);
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
