package me.ccrama.redditslide.Services;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.webkit.WebView;

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

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.ccrama.redditslide.Activities.LiveThread;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.HttpUtil;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.TwitterObject;
import okhttp3.OkHttpClient;

public class LiveThreadTask {

    public static class LiveUpdateThread extends AsyncTask<Void, Void, Void> {

        private WeakReference<LiveThread> activity;

        public LiveUpdateThread(@NotNull LiveThread activity) {
            this.activity = new WeakReference<>(activity);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Calling get() method just one time so it wont produce NPEs
            // As subsequent access may produce NPEs
            LiveThread liveThread = this.activity.get();
            if (liveThread != null) {
                try {
                    com.neovisionaries.ws.client.WebSocket ws = new WebSocketFactory().createSocket(liveThread.thread.getWebsocketUrl());
                    ws.addListener(new WebSocketListener() {
                        @Override
                        public void onStateChanged(
                                com.neovisionaries.ws.client.WebSocket websocket,
                                WebSocketState newState) {

                        }

                        @Override
                        public void onConnected(
                                com.neovisionaries.ws.client.WebSocket websocket,
                                Map<String, List<String>> headers) {

                        }

                        @Override
                        public void onConnectError(
                                com.neovisionaries.ws.client.WebSocket websocket,
                                WebSocketException cause) {

                        }

                        @Override
                        public void onDisconnected(
                                com.neovisionaries.ws.client.WebSocket websocket,
                                WebSocketFrame serverCloseFrame,
                                WebSocketFrame clientCloseFrame, boolean closedByServer) {

                        }

                        @Override
                        public void onFrame(com.neovisionaries.ws.client.WebSocket websocket,
                                            WebSocketFrame frame) {

                        }

                        @Override
                        public void onContinuationFrame(
                                com.neovisionaries.ws.client.WebSocket websocket,
                                WebSocketFrame frame) {

                        }

                        @Override
                        public void onTextFrame(
                                com.neovisionaries.ws.client.WebSocket websocket,
                                WebSocketFrame frame) {

                        }

                        @Override
                        public void onBinaryFrame(
                                com.neovisionaries.ws.client.WebSocket websocket,
                                WebSocketFrame frame) {

                        }

                        @Override
                        public void onCloseFrame(
                                com.neovisionaries.ws.client.WebSocket websocket,
                                WebSocketFrame frame) {

                        }

                        @Override
                        public void onPingFrame(
                                com.neovisionaries.ws.client.WebSocket websocket,
                                WebSocketFrame frame) {

                        }

                        @Override
                        public void onPongFrame(
                                com.neovisionaries.ws.client.WebSocket websocket,
                                WebSocketFrame frame) {

                        }

                        @Override
                        public void onTextMessage(
                                com.neovisionaries.ws.client.WebSocket websocket, String s) {
                            liveThread.updateLive(s);
                        }

                        @Override
                        public void onBinaryMessage(
                                com.neovisionaries.ws.client.WebSocket websocket,
                                byte[] binary) {

                        }

                        @Override
                        public void onSendingFrame(
                                com.neovisionaries.ws.client.WebSocket websocket,
                                WebSocketFrame frame) {

                        }

                        @Override
                        public void onFrameSent(
                                com.neovisionaries.ws.client.WebSocket websocket,
                                WebSocketFrame frame) {

                        }

                        @Override
                        public void onFrameUnsent(
                                com.neovisionaries.ws.client.WebSocket websocket,
                                WebSocketFrame frame) {

                        }

                        @Override
                        public void onError(com.neovisionaries.ws.client.WebSocket websocket,
                                            WebSocketException cause) {

                        }

                        @Override
                        public void onFrameError(
                                com.neovisionaries.ws.client.WebSocket websocket,
                                WebSocketException cause, WebSocketFrame frame) {

                        }

                        @Override
                        public void onMessageError(
                                com.neovisionaries.ws.client.WebSocket websocket,
                                WebSocketException cause, List<WebSocketFrame> frames) {

                        }

                        @Override
                        public void onMessageDecompressionError(
                                com.neovisionaries.ws.client.WebSocket websocket,
                                WebSocketException cause, byte[] compressed) {

                        }

                        @Override
                        public void onTextMessageError(
                                com.neovisionaries.ws.client.WebSocket websocket,
                                WebSocketException cause, byte[] data) {

                        }

                        @Override
                        public void onSendError(
                                com.neovisionaries.ws.client.WebSocket websocket,
                                WebSocketException cause, WebSocketFrame frame) {

                        }

                        @Override
                        public void onUnexpectedError(
                                com.neovisionaries.ws.client.WebSocket websocket,
                                WebSocketException cause) {

                        }

                        @Override
                        public void handleCallbackError(
                                com.neovisionaries.ws.client.WebSocket websocket,
                                Throwable cause) {

                        }

                        @Override
                        public void onSendingHandshake(
                                com.neovisionaries.ws.client.WebSocket websocket,
                                String requestLine, List<String[]> headers) {

                        }
                    });
                    ws.connect();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (WebSocketException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }
    }

    public static class DialogThread extends AsyncTask<Void, Void, Void> {
        private String EXTRA_LIVEURL = "liveurl";
        MaterialDialog d;
        private WeakReference<LiveThread> activity;

        public DialogThread(@NotNull LiveThread activity) {
            this.activity = new WeakReference<>(activity);
        }

        @Override
        public void onPreExecute() {
            // Calling get() method just one time so it wont produce NPEs
            // As subsequent access may produce NPEs
            LiveThread liveThread = this.activity.get();
            if (liveThread != null) {
                d = new MaterialDialog.Builder(activity.get())
                        .title(R.string.livethread_loading_title)
                        .content(R.string.misc_please_wait)
                        .progress(true, 100)
                        .cancelable(false)
                        .show();
            }

        }

        @Override
        protected Void doInBackground(Void... params) {
            // Calling get() method just one time so it wont produce NPEs
            // As subsequent access may produce NPEs
            LiveThread liveThread = this.activity.get();
            if (liveThread != null) {
                try {
                    liveThread.thread = new LiveThreadManager(Authentication.reddit).get(activity.get().getIntent().getStringExtra(EXTRA_LIVEURL));
                } catch(Exception e){
                }
            }

            return null;
        }

        @Override
        public void onPostExecute(Void aVoid) {
            // Calling get() method just one time so it wont produce NPEs
            // As subsequent access may produce NPEs
            LiveThread liveThread = this.activity.get();
            if (liveThread.thread == null) {
                liveThread.updateToolBar();
            }
            else {
                d.dismiss();
                liveThread.updateToolBar();
            }
        }
    }

    public static class PaginatorThread extends AsyncTask<Void, Void, Void> {

        private WeakReference<LiveThread> activity;

        public PaginatorThread(@NotNull LiveThread activity) {
            this.activity = new WeakReference<>(activity);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Calling get() method just one time so it wont produce NPEs
            // As subsequent access may produce NPEs
            LiveThread liveThread = this.activity.get();
            if (liveThread != null) {
                liveThread.paginator = new LiveThreadManager(Authentication.reddit).stream(liveThread.thread);
                liveThread.updates = new ArrayList<>(activity.get().paginator.accumulateMerged(5));
            }
            return null;
        }

        @Override
        public void onPostExecute(Void aVoid) {
            // Calling get() method just one time so it wont produce NPEs
            // As subsequent access may produce NPEs
            LiveThread liveThread = this.activity.get();
            if (liveThread != null) {
                liveThread.doLiveThreadUpdates();
            }
        }
    }

    public static class LoadTwitter extends AsyncTask<String, Void, Void> {

        private OkHttpClient client;
        private Gson gson;
        private String url;
        private WeakReference<WebView> view;
        private TwitterObject twitter;

        public LoadTwitter(@NotNull WebView view, @NotNull String url) {
            this.view = new WeakReference<>(view);
            this.url = url;
            client = Reddit.client;
            gson = new Gson();
        }

        private void parseJson() {
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
            // Calling get() method just one time so it wont produce NPEs
            // As subsequent access may produce NPEs
            WebView view = this.view.get();
            if (twitter != null && twitter.getHtml() != null && view != null) {
                view.loadData(twitter.getHtml().replace("//platform.twitter", "https://platform.twitter"), "text/html", "UTF-8");
            }
        }


    }

}
