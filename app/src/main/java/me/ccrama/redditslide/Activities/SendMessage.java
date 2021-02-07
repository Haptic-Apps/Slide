package me.ccrama.redditslide.Activities;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.InboxManager;
import net.dean.jraw.models.Captcha;
import net.dean.jraw.models.PrivateMessage;

import java.util.ArrayList;
import java.util.Locale;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.Views.DoEditorActions;
import me.ccrama.redditslide.Visuals.Palette;

/**
 * Created by ccrama on 3/5/2015.
 */
public class SendMessage extends BaseActivity {
    public static final String EXTRA_NAME  = "name";
    public static final String EXTRA_REPLY = "reply";
    public static final String EXTRA_MESSAGE  = "message";
    public static final String EXTRA_SUBJECT  = "subject";

    public String URL;
    private Boolean reply;
    private PrivateMessage previousMessage;
    private EditText subject;
    private EditText to;
    private String bodytext;
    private String subjecttext;
    private String totext;
    private EditText body;

    private String messageSentStatus; //the String to show in the Toast for when the message is sent
    private boolean messageSent = true; //whether or not the message was sent successfully

    String author;

    public void onCreate(Bundle savedInstanceState) {
        disableSwipeBackLayout();
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_sendmessage);

        final Toolbar b = (Toolbar) findViewById(R.id.toolbar);
        final String name;
        reply = getIntent() != null && getIntent().hasExtra(EXTRA_REPLY);
        subject = (EditText) findViewById(R.id.subject);
        to = (EditText) findViewById(R.id.to);
        body = (EditText) findViewById(R.id.body);
        View oldMSG = findViewById(R.id.oldMSG);

        final TextView sendingAs = (TextView) findViewById(R.id.sendas);
        sendingAs.setText("Sending as /u/" + Authentication.name);
        author = Authentication.name;
        sendingAs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> items = new ArrayList<>();
                items.add("/u/" + Authentication.name);
                if(UserSubscriptions.modOf != null && !UserSubscriptions.modOf.isEmpty())
                for(String s : UserSubscriptions.modOf){
                    items.add("/r/" + s);
                }
                new MaterialDialog.Builder(SendMessage.this).title("Send message as")
                        .items(items)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int which,
                                    CharSequence text) {
                                SendMessage.this.author = (String) text;
                                sendingAs.setText("Sending as " + author);
                            }
                        })
                        .negativeText(R.string.btn_cancel)
                        .onNegative(null)
                        .show();
            }
        });

        if (getIntent() != null && getIntent().hasExtra(EXTRA_NAME)) {
            name = getIntent().getExtras().getString(EXTRA_NAME, "");
            to.setText(name);
            to.setInputType(InputType.TYPE_NULL);

            if (reply) {
                b.setTitle(getString(R.string.mail_reply_to, name));
                previousMessage = DataShare.sharedMessage;
                if(previousMessage.getSubject() != null)
                subject.setText(getString(R.string.mail_re, previousMessage.getSubject()));
                subject.setInputType(InputType.TYPE_NULL);

                //Disable if replying to another user, as they are already set
                to.setEnabled(false);
                subject.setEnabled(false);

                body.requestFocus();

                oldMSG.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialogWrapper.Builder b = new AlertDialogWrapper.Builder(SendMessage.this);
                        b.setTitle(getString(R.string.mail_author_wrote, name));
                        b.setMessage(previousMessage.getBody());
                        b.create().show();
                    }
                });
            } else {
                b.setTitle(getString(R.string.mail_send_to, name));
                oldMSG.setVisibility(View.GONE);
            }
        } else {
            name = "";
            oldMSG.setVisibility(View.GONE);
            b.setTitle(R.string.mail_send);
        }

        if(getIntent().hasExtra(EXTRA_MESSAGE)){
            body.setText(getIntent().getStringExtra(EXTRA_MESSAGE));
        }

        if(getIntent().hasExtra(EXTRA_SUBJECT)){
            subject.setText(getIntent().getStringExtra(EXTRA_SUBJECT));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }
        setupUserAppBar(R.id.toolbar, null, true, name);
        setRecentBar(b.getTitle().toString(), Palette.getDefaultColor());

        if(reply || UserSubscriptions.modOf == null || UserSubscriptions.modOf.isEmpty()){
            sendingAs.setVisibility(View.GONE);
        }

        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bodytext = body.getText().toString();
                totext = to.getText().toString();
                subjecttext = subject.getText().toString();
                ((FloatingActionButton)findViewById(R.id.send)).hide();

                new AsyncDo(null, null).execute();
            }
        });
        DoEditorActions.doActions(((EditText) findViewById(R.id.body)), findViewById(R.id.area), getSupportFragmentManager(), SendMessage.this, previousMessage==null?null:previousMessage.getBody(),  null);
    }


    private class AsyncDo extends AsyncTask<Void, Void, Void> {
        String tried;
        Captcha captcha;

        public AsyncDo(Captcha captcha, String tried){
            this.captcha = captcha;
            this.tried = tried;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            sendMessage(captcha, tried);
            return null;
        }

        public void sendMessage(Captcha captcha, String captchaAttempt) {
            if (reply) {
                try {
                    new net.dean.jraw.managers.AccountManager(Authentication.reddit).reply(previousMessage, bodytext);
                } catch (ApiException e) {
                    messageSent = false;
                    e.printStackTrace();
                }
            } else {
                try {
                    if (captcha != null)
                        new InboxManager(Authentication.reddit).compose(totext, subjecttext, bodytext, captcha, captchaAttempt);
                    else {
                        String to = author;
                        if(to.startsWith("/r/")){
                            to = to.substring(3);
                            new InboxManager(Authentication.reddit).compose(to, totext, subjecttext,
                                    bodytext);
                        } else {
                            new InboxManager(Authentication.reddit).compose(totext, subjecttext,
                                    bodytext);

                        }

                    }

                } catch (ApiException e) {
                    messageSent = false;
                    e.printStackTrace();

                    //Display a Toast with an error if the user doesn't exist
                    if (e.getReason().equals("USER_DOESNT_EXIST") || e.getReason().equals("NO_USER")) {
                        messageSentStatus = getString(R.string.msg_send_user_dne);
                    } else if (e.getReason().toLowerCase(Locale.ENGLISH).contains("captcha")) {
                        messageSentStatus = getString(R.string.misc_captcha_incorrect);
                    }

                    //todo show captcha
                }
            }
        }

        @Override
        public void onPostExecute(Void voids) {
            //If the error wasn't that the user doesn't exist, show a generic failure message
            if (messageSentStatus == null) {
                messageSentStatus = getString(R.string.msg_sent_failure);
                ((FloatingActionButton)findViewById(R.id.send)).show();
            }

            final String MESSAGE_SENT = (messageSent)
                    ? getString(R.string.msg_sent_success) : messageSentStatus;

            Toast.makeText(SendMessage.this, MESSAGE_SENT, Toast.LENGTH_SHORT).show();

            //Only finish() this Activity if the message sent successfully
            if (messageSent) {
                finish();
            } else {
                ((FloatingActionButton)findViewById(R.id.send)).show();
                messageSent = true;
            }
        }
    }
}