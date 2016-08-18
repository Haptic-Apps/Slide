package me.ccrama.redditslide.Activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.CaptchaHelper;
import net.dean.jraw.managers.InboxManager;
import net.dean.jraw.models.Captcha;
import net.dean.jraw.models.PrivateMessage;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Views.DoEditorActions;
import me.ccrama.redditslide.Visuals.Palette;

/**
 * Created by ccrama on 3/5/2015.
 */
public class Sendmessage extends BaseActivity {
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_REPLY = "reply";

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

        if (getIntent() != null && getIntent().hasExtra(EXTRA_NAME)) {
            name = getIntent().getExtras().getString(EXTRA_NAME, "");
            to.setText(name);
            to.setInputType(InputType.TYPE_NULL);

            if (reply) {
                b.setTitle(getString(R.string.mail_reply_to, name));
                previousMessage = DataShare.sharedMessage;
                subject.setText(getString(R.string.mail_re, previousMessage.getSubject()));
                subject.setInputType(InputType.TYPE_NULL);

                //Disable if replying to another user, as they are already set
                to.setEnabled(false);
                subject.setEnabled(false);

                body.requestFocus();

                oldMSG.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialogWrapper.Builder b = new AlertDialogWrapper.Builder(Sendmessage.this);
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }
        setupUserAppBar(R.id.toolbar, null, true, name);
        setRecentBar(b.getTitle().toString(), Palette.getDefaultColor());

        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bodytext = body.getText().toString();
                totext = to.getText().toString();
                subjecttext = subject.getText().toString();
                ((FloatingActionButton)findViewById(R.id.send)).hide();

                new AsyncDo().execute();
            }
        });
        DoEditorActions.doActions(((EditText) findViewById(R.id.body)), findViewById(R.id.area), getSupportFragmentManager(), Sendmessage.this, previousMessage==null?null:previousMessage.getBody());
    }


    private class AsyncDo extends AsyncTask<Void, Void, Void> {
        String trying;

        @Override
        protected Void doInBackground(Void... voids) {
            if (new CaptchaHelper(Authentication.reddit).isNecessary()) {
                //display capacha
                final Captcha c;
                try {
                    c = new CaptchaHelper(Authentication.reddit).getNew();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LayoutInflater inflater = getLayoutInflater();

                            final View dialoglayout = inflater.inflate(R.layout.capatcha, null);
                            final AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(Sendmessage.this);

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
                                            sendMessage(c, trying);
                                            return true;
                                        }


                                    }.execute();
                                }
                            });
                        }
                    });
                } catch (ApiException e) {
                    e.printStackTrace();
                    //todo fail
                    sendMessage(null, null);
                }
            } else {
                sendMessage(null, null);
            }

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
                    else
                        new InboxManager(Authentication.reddit).compose(totext, subjecttext, bodytext);

                } catch (ApiException e) {
                    messageSent = false;
                    e.printStackTrace();

                    //Display a Toast with an error if the user doesn't exist
                    if (e.getReason().equals("USER_DOESNT_EXIST") || e.getReason().equals("NO_USER")) {
                        messageSentStatus = getString(R.string.msg_send_user_dne);
                    } else if (e.getReason().toLowerCase().contains("captcha")) {
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

            Toast.makeText(Sendmessage.this, MESSAGE_SENT, Toast.LENGTH_SHORT).show();

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