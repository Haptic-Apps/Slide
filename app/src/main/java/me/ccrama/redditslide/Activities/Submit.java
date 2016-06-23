package me.ccrama.redditslide.Activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.managers.CaptchaHelper;
import net.dean.jraw.models.Captcha;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import me.ccrama.redditslide.Authentication;
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


/**
 * Created by ccrama on 3/5/2015.
 */
public class Submit extends BaseActivity {

    private boolean sent;
    private String trying;
    private String URL;
    private SwitchCompat inboxReplies;
    private View image;
    private View link;
    private View self;
    public static final String EXTRA_SUBREDDIT = "subreddit";

    AsyncTask<Void, Void, Subreddit> tchange;

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            String text = ((EditText) findViewById(R.id.bodytext)).getText().toString();
            if (!text.isEmpty() && sent) {
                Drafts.addDraft(text);
                Toast.makeText(getApplicationContext(), R.string.msg_save_draft, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e){

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

        self = findViewById(R.id.selftext);
        final AutoCompleteTextView subredditText = ((AutoCompleteTextView) findViewById(R.id.subreddittext));
        image = findViewById(R.id.image);
        link = findViewById(R.id.url);

        image.setVisibility(View.GONE);
        link.setVisibility(View.GONE);

        if (subreddit != null && !subreddit.equals("frontpage") && !subreddit.equals("all")
                && !subreddit.equals("friends") && !subreddit.equals("mod")
                && !subreddit.contains("/m/")&& !subreddit.contains("+")) {
            subredditText.setText(subreddit);
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
                                return Authentication.reddit.getSubreddit(subredditText.getText().toString());
                            } catch (Exception ignored) {

                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Subreddit s) {

                            if(s != null) {
                                String text = s.getDataNode().get("submit_text_html").asText();
                                if (text != null && !text.isEmpty() && !text.equals("null")) {
                                    findViewById(R.id.submittext).setVisibility(View.VISIBLE);
                                    setViews(text, subredditText.getText().toString(), (SpoilerRobotoTextView) findViewById(R.id.submittext), (CommentOverflow) findViewById(R.id.commentOverflow));
                                }
                                if (s.getSubredditType().equals("RESTRICTED")) {
                                    subredditText.setText("");
                                    new AlertDialogWrapper.Builder(Submit.this).setTitle("This subreddit is restricted")
                                            .setMessage("You are not allowed to post here. Please choose another subreddit")
                                            .setPositiveButton(R.string.btn_ok, null).show();
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
                        d = new MaterialDialog.Builder(Submit.this)
                                .progress(true, 100)
                                .title("Finding the title")
                                .show();
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        if (s != null) {
                            ((EditText) findViewById(R.id.titletext)).setText(s);
                            d.dismiss();
                        } else {
                            d.dismiss();
                            new AlertDialogWrapper.Builder(Submit.this)
                                    .setTitle(R.string.title_not_found)
                                    .setPositiveButton(R.string.btn_ok, null).show();
                        }
                    }
                }.execute(((EditText) findViewById(R.id.urltext)).getText().toString());
            }
        });
        findViewById(R.id.selImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        getString(R.string.editor_select_img)), 1);
            }
        });

        DoEditorActions.doActions(((EditText) findViewById(R.id.bodytext)), findViewById(R.id.selftext), getSupportFragmentManager(), Submit.this);
        if (intent.hasExtra(Intent.EXTRA_TEXT) && !intent.getExtras().getString(Intent.EXTRA_TEXT, "").isEmpty()) {
            String data = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (data.contains("\n")) {
                ((EditText) findViewById(R.id.titletext)).setText(data.substring(0, data.indexOf("\n")));
                ((EditText) findViewById(R.id.urltext)).setText(data.substring(data.indexOf("\n"), data.length()));
            } else {
                ((EditText) findViewById(R.id.urltext)).setText(data);
            }
            self.setVisibility(View.GONE);
            image.setVisibility(View.GONE);
            link.setVisibility(View.VISIBLE);
            ((RadioButton) findViewById(R.id.linkradio)).setChecked(true);

        } else if (intent.hasExtra(Intent.EXTRA_STREAM)) {
            Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (imageUri != null) {
                try {
                    File f = new File(imageUri.getPath());
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    new UploadImgur(f.getName().contains(".jpg") || f.getName().contains(".jpeg")).execute(bitmap);
                    self.setVisibility(View.GONE);
                    image.setVisibility(View.VISIBLE);
                    link.setVisibility(View.GONE);
                    ((RadioButton) findViewById(R.id.imageradio)).setChecked(true);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (intent.hasExtra(Intent.EXTRA_SUBJECT) && !intent.getExtras().getString(Intent.EXTRA_SUBJECT, "").isEmpty()) {
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
                ((Reddit) getApplication()).getImageLoader().displayImage(URL, ((ImageView) findViewById(R.id.imagepost)));
            }
        });
    }

    public void setViews(String rawHTML, String subredditName, SpoilerRobotoTextView firstTextView, CommentOverflow commentOverflow) {
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

    private String getImageLink(Bitmap b) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, baos); // Not sure whether this should be jpeg or png, try both and see which works best

        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.v("Got result");

        if (resultCode == RESULT_OK && requestCode == 1) {
            Uri selectedImageUri = data.getData();
            try {
                File f = new File(selectedImageUri.getPath());
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                new UploadImgur(f.getName().contains(".jpg") || f.getName().contains(".jpeg")).execute(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private class AsyncDo extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                if (self.getVisibility() == View.VISIBLE) {
                    try {
                        if (new CaptchaHelper(Authentication.reddit).isNecessary()) {
                            //display capacha
                            final Captcha c = new CaptchaHelper(Authentication.reddit).getNew();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    LayoutInflater inflater = getLayoutInflater();

                                    final View dialoglayout = inflater.inflate(R.layout.capatcha, null);
                                    final AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(Submit.this);

                                    ((Reddit) getApplication()).getImageLoader().displayImage(c.getImageUrl().toString(),
                                            (ImageView) dialoglayout.findViewById(R.id.cap));

                                    final Dialog dialog = builder.setView(dialoglayout).create();
                                    dialog.show();
                                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            ((FloatingActionButton) findViewById(R.id.send)).show();
                                        }
                                    });
                                    dialoglayout.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View d) {
                                            trying = ((EditText) dialoglayout.findViewById(R.id.entry)).getText().toString();
                                            dialog.dismiss();
                                            final String text = ((EditText) findViewById(R.id.bodytext)).getText().toString();
                                            new AsyncTask<Void, Void, Boolean>() {
                                                @Override
                                                protected Boolean doInBackground(Void... params) {
                                                    try {
                                                        Submission s = new AccountManager(Authentication.reddit).submit(new AccountManager.SubmissionBuilder(text, ((AutoCompleteTextView) findViewById(R.id.subreddittext)).getText().toString(), ((EditText) findViewById(R.id.titletext)).getText().toString()), c, trying);
                                                        new AccountManager(Authentication.reddit).sendRepliesToInbox(s, inboxReplies.isChecked());
                                                        new OpenRedditLink(Submit.this, "reddit.com/r/" + ((AutoCompleteTextView) findViewById(R.id.subreddittext)).getText().toString() + "/comments/" + s.getFullName().substring(3, s.getFullName().length()));

                                                        Submit.this.finish();
                                                    } catch (ApiException e) {
                                                        Drafts.addDraft(text);
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                new AlertDialogWrapper.Builder(Submit.this)
                                                                        .setTitle(R.string.err_title)
                                                                        .setMessage(R.string.misc_retry_draft)
                                                                        .setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                                finish();
                                                                            }
                                                                        }).setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                                        ((FloatingActionButton) findViewById(R.id.send)).show();
                                                                    }
                                                                }).create().show();
                                                            }
                                                        });
                                                        return false;
                                                    }
                                                    sent = true;
                                                    Submit.this.finish();
                                                    return true;
                                                }


                                            }.execute();
                                        }
                                    });
                                }
                            });
                        } else {
                            Submission s = new AccountManager(Authentication.reddit).submit(new AccountManager.SubmissionBuilder(((EditText) findViewById(R.id.bodytext)).getText().toString(), ((AutoCompleteTextView) findViewById(R.id.subreddittext)).getText().toString(), ((EditText) findViewById(R.id.titletext)).getText().toString()));
                            new AccountManager(Authentication.reddit).sendRepliesToInbox(s, inboxReplies.isChecked());
                            new OpenRedditLink(Submit.this, "reddit.com/r/" + ((AutoCompleteTextView) findViewById(R.id.subreddittext)).getText().toString() + "/comments/" + s.getFullName().substring(3, s.getFullName().length()));
                            Submit.this.finish();
                        }
                    } catch (final ApiException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialogWrapper.Builder(Submit.this)
                                        .setTitle(R.string.err_title)
                                        .setMessage(getString(R.string.misc_err) + ": " + e.getExplanation() + "\n" + getString(R.string.misc_retry))
                                        .setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                finish();
                                            }
                                        }).setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        ((FloatingActionButton) findViewById(R.id.send)).show();
                                    }
                                }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        ((FloatingActionButton) findViewById(R.id.send)).show();
                                    }
                                }).create().show();
                            }
                        });
                    }
                } else if (link.getVisibility() == View.VISIBLE) {
                    try {
                        Submission s = new AccountManager(Authentication.reddit).submit(new AccountManager.SubmissionBuilder(new URL(((EditText) findViewById(R.id.urltext)).getText().toString()), ((AutoCompleteTextView) findViewById(R.id.subreddittext)).getText().toString(), ((EditText) findViewById(R.id.titletext)).getText().toString()));
                        new AccountManager(Authentication.reddit).sendRepliesToInbox(s, inboxReplies.isChecked());
                        new OpenRedditLink(Submit.this, "reddit.com/r/" + ((AutoCompleteTextView) findViewById(R.id.subreddittext)).getText().toString() + "/comments/" + s.getFullName().substring(3, s.getFullName().length()));

                        Submit.this.finish();
                    } catch (final ApiException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (e instanceof ApiException) {
                                    new AlertDialogWrapper.Builder(Submit.this)
                                            .setTitle(R.string.err_title)
                                            .setMessage(R.string.misc_err + ": " + e.getExplanation() + "\n" + R.string.misc_retry)
                                            .setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    finish();
                                                }
                                            }).setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            ((FloatingActionButton) findViewById(R.id.send)).show();
                                        }
                                    }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            ((FloatingActionButton) findViewById(R.id.send)).show();
                                        }
                                    }).create().show();
                                } else {
                                    new AlertDialogWrapper.Builder(Submit.this)
                                            .setTitle(R.string.err_title)
                                            .setMessage(R.string.misc_err + ": " + getString(R.string.err_invalid_url) + "\n" + R.string.misc_retry)
                                            .setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    finish();
                                                }
                                            }).setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            ((FloatingActionButton) findViewById(R.id.send)).show();
                                        }
                                    }).create().show();
                                }
                            }
                        });
                    }
                } else if (image.getVisibility() == View.VISIBLE) {
                    try {
                        Submission s = new AccountManager(Authentication.reddit).submit(new AccountManager.SubmissionBuilder(new URL(URL), ((AutoCompleteTextView) findViewById(R.id.subreddittext)).getText().toString(), ((EditText) findViewById(R.id.titletext)).getText().toString()));
                        new AccountManager(Authentication.reddit).sendRepliesToInbox(s, inboxReplies.isChecked());
                        new OpenRedditLink(Submit.this, "reddit.com/r/" + ((AutoCompleteTextView) findViewById(R.id.subreddittext)).getText().toString() + "/comments/" + s.getFullName().substring(3, s.getFullName().length()));

                        Submit.this.finish();
                    } catch (final Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (e instanceof ApiException) {
                                    new AlertDialogWrapper.Builder(Submit.this)
                                            .setTitle(R.string.err_title)
                                            .setMessage(R.string.misc_err + ": " + ((ApiException) e).getExplanation() + "\n" + R.string.misc_retry)
                                            .setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    finish();
                                                }
                                            }).setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            ((FloatingActionButton) findViewById(R.id.send)).show();
                                        }
                                    }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            ((FloatingActionButton) findViewById(R.id.send)).show();
                                        }
                                    }).create().show();
                                } else {
                                    new AlertDialogWrapper.Builder(Submit.this)
                                            .setTitle(R.string.err_title)
                                            .setMessage(R.string.misc_err + ": " + getString(R.string.err_invalid_url) + "\n" + R.string.misc_retry)
                                            .setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    finish();
                                                }
                                            }).setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            ((FloatingActionButton) findViewById(R.id.send)).show();
                                        }
                                    }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            ((FloatingActionButton) findViewById(R.id.send)).show();
                                        }
                                    }).create().show();
                                }
                            }
                        });
                    }
                }
            } catch (Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialogWrapper.Builder(Submit.this)
                                .setTitle(R.string.err_title)
                                .setMessage(R.string.misc_retry)
                                .setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        finish();
                                    }
                                }).setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ((FloatingActionButton) findViewById(R.id.send)).show();
                            }
                        }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                ((FloatingActionButton) findViewById(R.id.send)).show();
                            }
                        }).create().show();
                    }
                });
            }
            return null;
        }
    }

    private class UploadImgur extends AsyncTask<Bitmap, Void, JSONObject> {


        private final ProgressDialog dialog = new ProgressDialog(Submit.this);
        public Bitmap b;
        boolean jpg;

        public UploadImgur(boolean jpg) {
            this.jpg = jpg;
        }

        @Override
        protected void onPostExecute(final JSONObject result) {
            dialog.dismiss();
            try {
                if (result != null && result.has("data")) {
                    String url = result.getJSONObject("data").getString("link");
                    setImage(url);
                } else {
                    new AlertDialogWrapper.Builder(Submit.this)
                            .setTitle(R.string.err_general)
                            .setMessage("Make sure the image is accessible and try again!")
                            .setPositiveButton(R.string.btn_ok, null)
                            .show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage(getString(R.string.editor_uploading_image));
            dialog.setCancelable(false);
            dialog.show();
        }


        @Override
        protected JSONObject doInBackground(Bitmap... sub) {

            Bitmap bitmap = sub[0];
            b = bitmap;

// Creates Byte Array from picture
            URL url;
            try {

                url = new URL("https://imgur-apiv3.p.mashape.com/3/image");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestMethod("POST");
                conn.addRequestProperty("X-Mashape-Key", SecretConstants.getImgurApiKey(Submit.this));
                conn.addRequestProperty("Authorization", "Client-ID " + "bef87913eb202e9");
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");

                conn.connect();

                OutputStream output = conn.getOutputStream();
                if (jpg)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
                else
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
                output.close();

                StringBuilder stb = new StringBuilder();

                // Get the response
                BufferedReader rd = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    stb.append(line).append("\n");
                }
                rd.close();
                return new JSONObject(stb.toString());

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }


            return null;
        }

    }
}