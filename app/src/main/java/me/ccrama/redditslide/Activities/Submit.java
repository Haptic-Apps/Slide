package me.ccrama.redditslide.Activities;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import me.ccrama.redditslide.Views.ImageInsertEditText;
import net.dean.jraw.ApiException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import gun0912.tedbottompicker.TedBottomPicker;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Constants;
import me.ccrama.redditslide.Drafts;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SecretConstants;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.Views.CommentOverflow;
import me.ccrama.redditslide.Views.DoEditorActions;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.SubmissionParser;
import me.ccrama.redditslide.util.TitleExtractor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;


/**
 * Created by ccrama on 3/5/2015.
 */
public class Submit extends BaseActivity {

    private boolean      sent;
    private String       trying;
    private String       URL;
    private SwitchCompat inboxReplies;
    private View         image;
    private View         link;
    private View         self;
    public static final String EXTRA_SUBREDDIT = "subreddit";
    public static final String EXTRA_BODY = "body";
    public static final String EXTRA_IS_SELF = "is_self";

    AsyncTask<Void, Void, Subreddit> tchange;

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

        subredditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tchange != null) {
                    tchange.cancel(true);
                }
                findViewById(R.id.submittext).setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

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
                                    new AlertDialogWrapper.Builder(Submit.this).setTitle(
                                            R.string.err_submit_restricted)
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
                            new AlertDialogWrapper.Builder(Submit.this).setTitle(
                                    R.string.title_not_found)
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
                                new TedBottomPicker.OnImageSelectedListener() {
                                    @Override
                                    public void onImageSelected(List<Uri> uri) {
                                        handleImageIntent(uri);
                                    }
                                })
                                .setLayoutResource(R.layout.image_sheet_dialog)
                                .setTitle("Choose a photo")
                                .create();

                tedBottomPicker.show(getSupportFragmentManager());
                InputMethodManager imm =
                        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(findViewById(R.id.bodytext).getWindowToken(), 0);
            }
        });

        DoEditorActions.doActions(((EditText) findViewById(R.id.bodytext)),
                findViewById(R.id.selftext), getSupportFragmentManager(), Submit.this, null, null);
        if (intent.hasExtra(Intent.EXTRA_TEXT) && !intent.getExtras()
                .getString(Intent.EXTRA_TEXT, "")
                .isEmpty()
                && !intent.getBooleanExtra(EXTRA_IS_SELF, false)) {
            String data = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (data.contains("\n")) {
                ((EditText) findViewById(R.id.titletext)).setText(
                        data.substring(0, data.indexOf("\n")));
                ((EditText) findViewById(R.id.urltext)).setText(
                        data.substring(data.indexOf("\n"), data.length()));
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
                new UploadImgur(this, uris.get(0));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //Multiple images
            try {
                new UploadImgurAlbum(this, uris.toArray(new Uri[uris.size()]));
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
                        Submission s = new AccountManager(Authentication.reddit).submit(
                                new AccountManager.SubmissionBuilder(
                                        ((EditText) findViewById(R.id.bodytext)).getText()
                                                .toString(), ((AutoCompleteTextView) findViewById(
                                        R.id.subreddittext)).getText().toString(),
                                        ((EditText) findViewById(R.id.titletext)).getText()
                                                .toString()));
                        new AccountManager(Authentication.reddit).sendRepliesToInbox(s,
                                inboxReplies.isChecked());
                        new OpenRedditLink(Submit.this,
                                "reddit.com/r/" + ((AutoCompleteTextView) findViewById(
                                        R.id.subreddittext)).getText().toString() + "/comments/" + s
                                        .getFullName()
                                        .substring(3, s.getFullName().length()));
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
                        new OpenRedditLink(Submit.this,
                                "reddit.com/r/" + ((AutoCompleteTextView) findViewById(
                                        R.id.subreddittext)).getText().toString() + "/comments/" + s
                                        .getFullName()
                                        .substring(3, s.getFullName().length()));

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
                        new OpenRedditLink(Submit.this,
                                "reddit.com/r/" + ((AutoCompleteTextView) findViewById(
                                        R.id.subreddittext)).getText().toString() + "/comments/" + s
                                        .getFullName()
                                        .substring(3, s.getFullName().length()));

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

    private class UploadImgur extends AsyncTask<Uri, Integer, JSONObject> {

        final         Context        c;
        private final MaterialDialog dialog;
        private final Uri            uri;
        public        Bitmap         b;

        public UploadImgur(Context c, Uri u) {
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

        //Following methods sourced from https://github.com/Kennyc1012/Opengur, Code by Kenny Campagna
        public File createFile(Uri uri, @NonNull Context context) {
            InputStream in;
            ContentResolver resolver = context.getContentResolver();
            String type = resolver.getType(uri);
            String extension;

            if ("image/png".equals(type)) {
                extension = ".gif";
            } else if ("image/png".equals(type)) {
                extension = ".png";
            } else {
                extension = ".jpg";
            }

            try {
                in = resolver.openInputStream(uri);
            } catch (FileNotFoundException e) {
                return null;
            }

            // Create files from a uri in our cache directory so they eventually get deleted
            String timeStamp = String.valueOf(System.currentTimeMillis());
            File cacheDir = ((Reddit) context.getApplicationContext()).getImageLoader()
                    .getDiskCache()
                    .getDirectory();
            File tempFile = new File(cacheDir, timeStamp + extension);

            if (writeInputStreamToFile(in, tempFile)) {
                return tempFile;
            } else {
                // If writeInputStreamToFile fails, delete the excess file
                tempFile.delete();
            }

            return null;
        }

        public boolean writeInputStreamToFile(@NonNull InputStream in, @NonNull File file) {
            BufferedOutputStream buffer = null;
            boolean didFinish = false;

            try {
                buffer = new BufferedOutputStream(new FileOutputStream(file));
                byte byt[] = new byte[1024];
                int i;

                for (long l = 0L; (i = in.read(byt)) != -1; l += i) {
                    buffer.write(byt, 0, i);
                }

                buffer.flush();
                didFinish = true;
            } catch (IOException e) {
                didFinish = false;
            } finally {
                closeStream(in);
                closeStream(buffer);
            }

            return didFinish;
        }

        public void closeStream(@Nullable Closeable closeable) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException ex) {
                }
            }
        }

        //End methods sourced from Opengur

        @Override
        protected JSONObject doInBackground(Uri... sub) {
            File bitmap = createFile(sub[0], c);

            final OkHttpClient client = Reddit.client;

            try {
                RequestBody formBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("image", bitmap.getName(),
                                RequestBody.create(MediaType.parse("image/*"), bitmap))
                        .build();

                DoEditorActions.ProgressRequestBody body =
                        new DoEditorActions.ProgressRequestBody(formBody,
                                new DoEditorActions.ProgressRequestBody.Listener() {
                                    @Override
                                    public void onProgress(int progress) {
                                        publishProgress(progress);
                                    }
                                });


                Request request = new Request.Builder().header("Authorization",
                        "Client-ID bef87913eb202e9")
                        .url("https://api.imgur.com/3/image")
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                return new JSONObject(response.body().string());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(final JSONObject result) {
            dialog.dismiss();
            try {
                final String url = result.getJSONObject("data").getString("link");
                setImage(url);

            } catch (Exception e) {
                new AlertDialogWrapper.Builder(c).setTitle(R.string.err_title)
                        .setMessage(R.string.editor_err_msg)
                        .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
                e.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            dialog.setProgress(values[0]);
            LogUtil.v("Progress:" + values[0]);
        }
    }

    private class UploadImgurAlbum extends AsyncTask<Uri, Integer, String> {

        final         Context        c;
        private final MaterialDialog dialog;
        private final Uri[]          uris;
        public        Bitmap         b;

        public UploadImgurAlbum(Context c, Uri... u) {
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

        //Following methods sourced from https://github.com/Kennyc1012/Opengur, Code by Kenny Campagna
        public File createFile(Uri uri, @NonNull Context context) {
            InputStream in;
            ContentResolver resolver = context.getContentResolver();
            String type = resolver.getType(uri);
            String extension;

            if ("image/png".equals(type)) {
                extension = ".gif";
            } else if ("image/png".equals(type)) {
                extension = ".png";
            } else {
                extension = ".jpg";
            }

            try {
                in = resolver.openInputStream(uri);
            } catch (FileNotFoundException e) {
                return null;
            }

            // Create files from a uri in our cache directory so they eventually get deleted
            String timeStamp = String.valueOf(System.currentTimeMillis());
            File cacheDir = ((Reddit) context.getApplicationContext()).getImageLoader()
                    .getDiskCache()
                    .getDirectory();
            File tempFile = new File(cacheDir, timeStamp + extension);

            if (writeInputStreamToFile(in, tempFile)) {
                return tempFile;
            } else {
                // If writeInputStreamToFile fails, delete the excess file
                tempFile.delete();
            }

            return null;
        }

        public boolean writeInputStreamToFile(@NonNull InputStream in, @NonNull File file) {
            BufferedOutputStream buffer = null;
            boolean didFinish = false;

            try {
                buffer = new BufferedOutputStream(new FileOutputStream(file));
                byte byt[] = new byte[1024];
                int i;

                for (long l = 0L; (i = in.read(byt)) != -1; l += i) {
                    buffer.write(byt, 0, i);
                }

                buffer.flush();
                didFinish = true;
            } catch (IOException e) {
                didFinish = false;
            } finally {
                closeStream(in);
                closeStream(buffer);
            }

            return didFinish;
        }

        public void closeStream(@Nullable Closeable closeable) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException ex) {
                }
            }
        }

        //End methods sourced from Opengur

        String finalUrl;

        @Override
        protected String doInBackground(Uri... sub) {
            totalCount = sub.length;
            final OkHttpClient client = Reddit.client;

            String albumurl;
            {
                Request request = new Request.Builder().header("Authorization",
                        "Client-ID bef87913eb202e9")
                        .url("https://api.imgur.com/3/album")
                        .post(new RequestBody() {
                            @Override
                            public MediaType contentType() {
                                return null;
                            }

                            @Override
                            public void writeTo(BufferedSink sink) throws IOException {

                            }
                        })
                        .build();
                Response response = null;
                try {
                    response = client.newCall(request).execute();

                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }
                    JSONObject album = new JSONObject(response.body().string());
                    albumurl = album.getJSONObject("data").getString("deletehash");
                    finalUrl = "http://imgur.com/a/" + album.getJSONObject("data").getString("id");
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }

            }


            try {
                MultipartBody.Builder formBodyBuilder =
                        new MultipartBody.Builder().setType(MultipartBody.FORM);
                for (Uri uri : sub) {
                    File bitmap = createFile(uri, c);
                    formBodyBuilder.addFormDataPart("image", bitmap.getName(),
                            RequestBody.create(MediaType.parse("image/*"), bitmap));
                    formBodyBuilder.addFormDataPart("album", albumurl);
                    MultipartBody formBody = formBodyBuilder.build();

                    DoEditorActions.ProgressRequestBody body =
                            new DoEditorActions.ProgressRequestBody(formBody,
                                    new DoEditorActions.ProgressRequestBody.Listener() {
                                        @Override
                                        public void onProgress(int progress) {
                                            publishProgress(progress);
                                        }
                                    });


                    Request request = new Request.Builder().header("Authorization",
                            "Client-ID bef87913eb202e9")
                            .url("https://api.imgur.com/3/image")
                            .post(body)
                            .build();

                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(final String result) {
            dialog.dismiss();
            try {
                ((RadioButton) findViewById(R.id.linkradio)).setChecked(true);
                link.setVisibility(View.VISIBLE);
                ((EditText) findViewById(R.id.urltext)).setText(finalUrl);
            } catch (Exception e) {
                new AlertDialogWrapper.Builder(c).setTitle(R.string.err_title)
                        .setMessage(R.string.editor_err_msg)
                        .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
                e.printStackTrace();
            }
        }

        int uploadCount;
        int totalCount;

        @Override
        protected void onProgressUpdate(Integer... values) {
            int progress = values[0];
            if (progress < dialog.getCurrentProgress() || uploadCount == 0) {
                uploadCount += 1;
            }
            dialog.setContent("Image " + uploadCount + "/" + totalCount);
            dialog.setProgress(progress);
        }
    }

    private void showErrorRetryDialog(String message) {
        new AlertDialogWrapper.Builder(Submit.this).setTitle(R.string.err_title)
                .setMessage(message)
                .setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ((FloatingActionButton) findViewById(R.id.send)).show();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        ((FloatingActionButton) findViewById(R.id.send)).show();
                    }
                })
                .create()
                .show();
    }

}
