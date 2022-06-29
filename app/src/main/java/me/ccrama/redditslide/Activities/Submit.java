package me.ccrama.redditslide.Activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.dean.jraw.ApiException;
import net.dean.jraw.http.HttpRequest;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;

import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import gun0912.tedbottompicker.TedBottomPicker;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Drafts;
import me.ccrama.redditslide.Flair.RichFlair;
import me.ccrama.redditslide.ImgurAlbum.UploadImgur;
import me.ccrama.redditslide.ImgurAlbum.UploadImgurAlbum;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.Views.CommentOverflow;
import me.ccrama.redditslide.Views.DoEditorActions;
import me.ccrama.redditslide.Views.ImageInsertEditText;
import me.ccrama.redditslide.util.HttpUtil;
import me.ccrama.redditslide.util.KeyboardUtil;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.SubmissionParser;
import me.ccrama.redditslide.util.TitleExtractor;
import me.ccrama.redditslide.util.stubs.SimpleTextWatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by ccrama on 3/5/2015.
 */
public class Submit extends BaseActivity {

    private             boolean      sent;
    private             String       trying;
    private             String       URL;
    private             String       selectedFlairID;
    private             SwitchCompat inboxReplies;
    private             View         image;
    private             View         link;
    private             View         self;
    public static final String       EXTRA_SUBREDDIT = "subreddit";
    public static final String       EXTRA_BODY      = "body";
    public static final String       EXTRA_IS_SELF   = "is_self";

    AsyncTask<Void, Void, Subreddit> tchange;
    private OkHttpClient client;
    private Gson         gson;

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            String text = ((EditText) findViewById(R.id.bodytext)).getText().toString();
            if (!text.isEmpty() && sent) {
                Drafts.addDraft(text);
                Toast.makeText(getApplicationContext(), R.string.msg_save_draft, Toast.LENGTH_LONG)
                        .show();
            }
        } catch (Exception e) {

        }
    }

    public void onCreate(Bundle savedInstanceState) {
        disableSwipeBackLayout();
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_submit);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }
        setupAppBar(R.id.toolbar, R.string.title_submit_post, true, true);

        inboxReplies = (SwitchCompat) findViewById(R.id.replies);

        Intent intent = getIntent();
        final String subreddit = intent.getStringExtra(EXTRA_SUBREDDIT);
        final String initialBody = intent.getStringExtra(EXTRA_BODY);

        self = findViewById(R.id.selftext);
        final AutoCompleteTextView subredditText =
                ((AutoCompleteTextView) findViewById(R.id.subreddittext));
        image = findViewById(R.id.image);
        link = findViewById(R.id.url);

        image.setVisibility(View.GONE);
        link.setVisibility(View.GONE);

        if (subreddit != null
                && !subreddit.equals("frontpage")
                && !subreddit.equals("all")
                && !subreddit.equals("friends")
                && !subreddit.equals("mod")
                && !subreddit.contains("/m/")
                && !subreddit.contains("+")) {
            subredditText.setText(subreddit);
        }
        if (initialBody != null) {
            ((ImageInsertEditText) self.findViewById(R.id.bodytext)).setText(initialBody);
        }
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,
                UserSubscriptions.getAllSubreddits(this));

        subredditText.setAdapter(adapter);
        subredditText.setThreshold(2);

        subredditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tchange != null) {
                    tchange.cancel(true);
                }
                findViewById(R.id.submittext).setVisibility(View.GONE);
            }
        });

        subredditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                findViewById(R.id.submittext).setVisibility(View.GONE);
                if (!hasFocus) {
                    tchange = new AsyncTask<Void, Void, Subreddit>() {
                        @Override
                        protected Subreddit doInBackground(Void... params) {
                            try {
                                return Authentication.reddit.getSubreddit(
                                        subredditText.getText().toString());
                            } catch (Exception ignored) {

                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Subreddit s) {

                            if (s != null) {
                                String text = s.getDataNode().get("submit_text_html").asText();
                                if (text != null && !text.isEmpty() && !text.equals("null")) {
                                    findViewById(R.id.submittext).setVisibility(View.VISIBLE);
                                    setViews(text, subredditText.getText().toString(),
                                            (SpoilerRobotoTextView) findViewById(R.id.submittext),
                                            (CommentOverflow) findViewById(R.id.commentOverflow));
                                }
                                if (s.getSubredditType().equals("RESTRICTED")) {
                                    subredditText.setText("");
                                    new AlertDialog.Builder(Submit.this)
                                            .setTitle(R.string.err_submit_restricted)
                                            .setMessage(R.string.err_submit_restricted_text)
                                            .setPositiveButton(R.string.btn_ok, null)
                                            .show();
                                }
                            } else {
                                findViewById(R.id.submittext).setVisibility(View.GONE);
                            }
                        }
                    };
                    tchange.execute();
                }
            }
        });

        findViewById(R.id.selftextradio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                self.setVisibility(View.VISIBLE);

                image.setVisibility(View.GONE);
                link.setVisibility(View.GONE);
            }
        });
        findViewById(R.id.imageradio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                self.setVisibility(View.GONE);
                image.setVisibility(View.VISIBLE);
                link.setVisibility(View.GONE);
            }
        });
        findViewById(R.id.linkradio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                self.setVisibility(View.GONE);
                image.setVisibility(View.GONE);
                link.setVisibility(View.VISIBLE);
            }
        });
        findViewById(R.id.flair).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFlairChooser();
            }
        });

        findViewById(R.id.suggest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncTask<String, Void, String>() {
                    Dialog d;

                    @Override
                    protected String doInBackground(String... params) {
                        try {
                            return TitleExtractor.getPageTitle(params[0]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPreExecute() {
                        d = new MaterialDialog.Builder(Submit.this).progress(true, 100)
                                .title(R.string.editor_finding_title)
                                .content(R.string.misc_please_wait)
                                .show();
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        if (s != null) {
                            ((EditText) findViewById(R.id.titletext)).setText(s);
                            d.dismiss();
                        } else {
                            d.dismiss();
                            new AlertDialog.Builder(Submit.this)
                                    .setTitle(R.string.title_not_found)
                                    .setPositiveButton(R.string.btn_ok, null)
                                    .show();
                        }
                    }
                }.execute(((EditText) findViewById(R.id.urltext)).getText().toString());
            }
        });
        findViewById(R.id.selImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TedBottomPicker tedBottomPicker =
                        new TedBottomPicker.Builder(Submit.this).setOnImageSelectedListener(
                                Submit.this::handleImageIntent)
                                .setLayoutResource(R.layout.image_sheet_dialog)
                                .setTitle("Choose a photo")
                                .create();

                tedBottomPicker.show(getSupportFragmentManager());
                KeyboardUtil.hideKeyboard(Submit.this, findViewById(R.id.bodytext).getWindowToken(), 0);
            }
        });

        DoEditorActions.doActions(((EditText) findViewById(R.id.bodytext)),
                findViewById(R.id.selftext), getSupportFragmentManager(), Submit.this, null, null);
        if (intent.hasExtra(Intent.EXTRA_TEXT) && !intent.getExtras()
                .getString(Intent.EXTRA_TEXT, "")
                .isEmpty() && !intent.getBooleanExtra(EXTRA_IS_SELF, false)) {
            String data = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (data.contains("\n")) {
                ((EditText) findViewById(R.id.titletext)).setText(
                        data.substring(0, data.indexOf("\n")));
                ((EditText) findViewById(R.id.urltext)).setText(data.substring(data.indexOf("\n")));
            } else {
                ((EditText) findViewById(R.id.urltext)).setText(data);
            }
            self.setVisibility(View.GONE);
            image.setVisibility(View.GONE);
            link.setVisibility(View.VISIBLE);
            ((RadioButton) findViewById(R.id.linkradio)).setChecked(true);

        } else if (intent.hasExtra(Intent.EXTRA_STREAM)) {
            final Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (imageUri != null) {
                handleImageIntent(new ArrayList<Uri>() {{
                    add(imageUri);
                }});
                self.setVisibility(View.GONE);
                image.setVisibility(View.VISIBLE);
                link.setVisibility(View.GONE);
                ((RadioButton) findViewById(R.id.imageradio)).setChecked(true);
            }
        }
        if (intent.hasExtra(Intent.EXTRA_SUBJECT) && !intent.getExtras()
                .getString(Intent.EXTRA_SUBJECT, "")
                .isEmpty()) {
            String data = intent.getStringExtra(Intent.EXTRA_SUBJECT);
            ((EditText) findViewById(R.id.titletext)).setText(data);
        }
        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((FloatingActionButton) findViewById(R.id.send)).hide();
                new AsyncDo().execute();
            }
        });

    }

    private void setImage(final String URL) {
        this.URL = URL;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.imagepost).setVisibility(View.VISIBLE);
                ((Reddit) getApplication()).getImageLoader()
                        .displayImage(URL, ((ImageView) findViewById(R.id.imagepost)));
            }
        });
    }

    private void showFlairChooser() {
        client = Reddit.client;
        gson = new Gson();

        String subreddit = ((EditText) findViewById(R.id.subreddittext)).getText().toString();

        final Dialog d =
                new MaterialDialog.Builder(Submit.this).title(R.string.submit_findingflairs)
                        .cancelable(true)
                        .content(R.string.misc_please_wait)
                        .progress(true, 100)
                        .show();
        new AsyncTask<Void, Void, JsonArray>() {
            ArrayList<JsonObject> flairs;

            @Override
            protected JsonArray doInBackground(Void... params) {
                flairs = new ArrayList<>();
                HttpRequest r = Authentication.reddit.request()
                        .path("/r/" + subreddit + "/api/link_flair_v2.json")
                        .get()
                        .build();

                Request request = new Request.Builder()
                        .headers(r.getHeaders().newBuilder().set("User-Agent", "Slide flair search").build())
                        .url(r.getUrl())
                        .build();

                return HttpUtil.getJsonArray(client, gson, request);
            }

            @Override
            protected void onPostExecute(final JsonArray result) {
                if (result == null) {
                    LogUtil.v("Error loading content");
                    d.dismiss();
                } else {
                    try {
                        final HashMap<String, RichFlair> flairs = new HashMap<>();
                        for(JsonElement object : result) {
                            RichFlair choice = new ObjectMapper().disable(
                                    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).readValue(object.toString(), RichFlair.class);
                            String title = choice.getText();
                            flairs.put(title, choice);
                        }
                        d.dismiss();

                        ArrayList<String> allKeys = new ArrayList<>(flairs.keySet());

                        new MaterialDialog.Builder(Submit.this).title(
                                getString(R.string.submit_flairchoices, subreddit))
                                .items(allKeys)
                                .itemsCallback(new MaterialDialog.ListCallback() {
                                    @Override
                                    public void onSelection(MaterialDialog dialog,
                                            View itemView, int which, CharSequence text) {
                                        RichFlair selected = flairs.get(allKeys.get(which));
                                        selectedFlairID = selected.getId();
                                        ((TextView) findViewById(R.id.flair)).setText(getString(R.string.submit_selected_flair, selected.getText()));
                                    }
                                })
                                .show();
                } catch(Exception e) {
                        LogUtil.v(e.toString());
                        d.dismiss();
                        LogUtil.v("Error parsing flairs");
                    }
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void setViews(String rawHTML, String subredditName, SpoilerRobotoTextView firstTextView,
            CommentOverflow commentOverflow) {
        if (rawHTML.isEmpty()) {
            return;
        }

        List<String> blocks = SubmissionParser.getBlocks(rawHTML);

        int startIndex = 0;
        // the <div class="md"> case is when the body contains a table or code block first
        if (!blocks.get(0).equals("<div class=\"md\">")) {
            firstTextView.setVisibility(View.VISIBLE);
            firstTextView.setTextHtml(blocks.get(0) + " ", subredditName);
            startIndex = 1;
        } else {
            firstTextView.setText("");
            firstTextView.setVisibility(View.GONE);
        }

        if (blocks.size() > 1) {
            if (startIndex == 0) {
                commentOverflow.setViews(blocks, subredditName);
            } else {
                commentOverflow.setViews(blocks.subList(startIndex, blocks.size()), subredditName);
            }
        } else {
            commentOverflow.removeAllViews();
        }
    }

    public void handleImageIntent(List<Uri> uris) {
        if (uris.size() == 1) {
            // Get the Image from data (single image)
            try {
                new UploadImgurSubmit(this, uris.get(0));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //Multiple images
            try {
                new UploadImgurAlbumSubmit(this, uris.toArray(new Uri[0]));
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }

    private class AsyncDo extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                if (self.getVisibility() == View.VISIBLE) {
                    final String text =
                            ((EditText) findViewById(R.id.bodytext)).getText().toString();
                    try {
                        AccountManager.SubmissionBuilder builder = new AccountManager.SubmissionBuilder(
                                ((EditText) findViewById(R.id.bodytext)).getText()
                                        .toString(), ((AutoCompleteTextView) findViewById(
                                R.id.subreddittext)).getText().toString(),
                                ((EditText) findViewById(R.id.titletext)).getText()
                                        .toString());

                        if(selectedFlairID != null) {
                            builder.flairID(selectedFlairID);
                        }

                        Submission s = new AccountManager(Authentication.reddit).submit(builder);
                        new AccountManager(Authentication.reddit).sendRepliesToInbox(s,
                                inboxReplies.isChecked());
                       OpenRedditLink.openUrl(Submit.this,
                                "reddit.com/r/" + ((AutoCompleteTextView) findViewById(
                                        R.id.subreddittext)).getText().toString() + "/comments/" + s
                                        .getFullName()
                                        .substring(3), true);
                        Submit.this.finish();
                    } catch (final ApiException e) {
                        Drafts.addDraft(text);
                        e.printStackTrace();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showErrorRetryDialog(getString(R.string.misc_err)
                                        + ": "
                                        + e.getExplanation()
                                        + "\n"
                                        + getString(R.string.misc_retry_draft));
                            }
                        });
                    }
                } else if (link.getVisibility() == View.VISIBLE) {
                    try {
                        Submission s = new AccountManager(Authentication.reddit).submit(
                                new AccountManager.SubmissionBuilder(
                                        new URL(((EditText) findViewById(R.id.urltext)).getText()
                                                .toString()), ((AutoCompleteTextView) findViewById(
                                        R.id.subreddittext)).getText().toString(),
                                        ((EditText) findViewById(R.id.titletext)).getText()
                                                .toString()));
                        new AccountManager(Authentication.reddit).sendRepliesToInbox(s,
                                inboxReplies.isChecked());
                        OpenRedditLink.openUrl(Submit.this,
                                "reddit.com/r/" + ((AutoCompleteTextView) findViewById(
                                        R.id.subreddittext)).getText().toString() + "/comments/" + s
                                        .getFullName()
                                        .substring(3), true);

                        Submit.this.finish();
                    } catch (final ApiException e) {
                        e.printStackTrace();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (e instanceof ApiException) {
                                    showErrorRetryDialog(getString(R.string.misc_err)
                                            + ": "
                                            + e.getExplanation()
                                            + "\n"
                                            + getString(R.string.misc_retry));
                                } else {
                                    showErrorRetryDialog(
                                            getString(R.string.misc_err) + ": " + getString(
                                                    R.string.err_invalid_url) + "\n" + getString(
                                                    R.string.misc_retry));
                                }
                            }
                        });
                    }
                } else if (image.getVisibility() == View.VISIBLE) {
                    try {
                        Submission s = new AccountManager(Authentication.reddit).submit(
                                new AccountManager.SubmissionBuilder(new URL(URL),
                                        ((AutoCompleteTextView) findViewById(
                                                R.id.subreddittext)).getText().toString(),
                                        ((EditText) findViewById(R.id.titletext)).getText()
                                                .toString()));
                        new AccountManager(Authentication.reddit).sendRepliesToInbox(s,
                                inboxReplies.isChecked());
                        OpenRedditLink.openUrl(Submit.this,
                                "reddit.com/r/" + ((AutoCompleteTextView) findViewById(
                                        R.id.subreddittext)).getText().toString() + "/comments/" + s
                                        .getFullName()
                                        .substring(3), true);

                        Submit.this.finish();
                    } catch (final Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (e instanceof ApiException) {
                                    showErrorRetryDialog(
                                            getString(R.string.misc_err) + ": " + ((ApiException) e)
                                                    .getExplanation() + "\n" + getString(
                                                    R.string.misc_retry));
                                } else {
                                    showErrorRetryDialog(
                                            getString(R.string.misc_err) + ": " + getString(
                                                    R.string.err_invalid_url) + "\n" + getString(
                                                    R.string.misc_retry));
                                }
                            }
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showErrorRetryDialog(getString(R.string.misc_retry));
                    }
                });
            }
            return null;
        }
    }

    private class UploadImgurSubmit extends UploadImgur {

        private final Uri uri;

        public UploadImgurSubmit(Context c, Uri u) {
            this.c = c;
            this.uri = u;

            dialog = new MaterialDialog.Builder(c).title(
                    c.getString(R.string.editor_uploading_image))
                    .progress(false, 100)
                    .cancelable(false)
                    .autoDismiss(false)
                    .build();

            new MaterialDialog.Builder(c).title(c.getString(R.string.editor_upload_image_question))
                    .cancelable(false)
                    .autoDismiss(false)
                    .positiveText(c.getString(R.string.btn_upload))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog d, DialogAction w) {
                            d.dismiss();
                            dialog.show();
                            execute(uri);
                        }
                    })
                    .negativeText(c.getString(R.string.btn_cancel))
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog d, DialogAction w) {
                            d.dismiss();
                        }
                    })
                    .show();
        }

        @Override
        protected void onPostExecute(final JSONObject result) {
            dialog.dismiss();
            try {
                final String url = result.getJSONObject("data").getString("link");
                setImage(url);

            } catch (Exception e) {
                new AlertDialog.Builder(c)
                        .setTitle(R.string.err_title)
                        .setMessage(R.string.editor_err_msg)
                        .setPositiveButton(R.string.btn_ok, null)
                        .show();
                e.printStackTrace();
            }
        }
    }

    private class UploadImgurAlbumSubmit extends UploadImgurAlbum {

        private final Uri[] uris;

        public UploadImgurAlbumSubmit(Context c, Uri... u) {
            this.c = c;
            this.uris = u;

            dialog = new MaterialDialog.Builder(c).title(
                    c.getString(R.string.editor_uploading_image))
                    .progress(false, 100)
                    .cancelable(false)
                    .build();

            new MaterialDialog.Builder(c).title(c.getString(R.string.editor_upload_image_question))
                    .cancelable(false)
                    .autoDismiss(false)
                    .positiveText(c.getString(R.string.btn_upload))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog d, DialogAction w) {
                            d.dismiss();
                            dialog.show();
                            execute(uris);
                        }
                    })
                    .negativeText(c.getString(R.string.btn_cancel))
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog d, DialogAction w) {
                            d.dismiss();
                        }
                    })
                    .show();
        }

        @Override
        protected void onPostExecute(final String result) {
            dialog.dismiss();
            try {
                ((RadioButton) findViewById(R.id.linkradio)).setChecked(true);
                link.setVisibility(View.VISIBLE);
                ((EditText) findViewById(R.id.urltext)).setText(finalUrl);
            } catch (Exception e) {
                new AlertDialog.Builder(c)
                        .setTitle(R.string.err_title)
                        .setMessage(R.string.editor_err_msg)
                        .setPositiveButton(R.string.btn_ok, null)
                        .show();
                e.printStackTrace();
            }
        }
    }

    private void showErrorRetryDialog(String message) {
        new AlertDialog.Builder(Submit.this)
                .setTitle(R.string.err_title)
                .setMessage(message)
                .setNegativeButton(R.string.btn_no, (dialogInterface, i) ->
                        finish())
                .setPositiveButton(R.string.btn_yes, (dialogInterface, i) ->
                        ((FloatingActionButton) findViewById(R.id.send)).show())
                .setOnDismissListener(dialog ->
                        ((FloatingActionButton) findViewById(R.id.send)).show())
                .create()
                .show();
    }

}
