package me.ccrama.redditslide.Activities;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.koushikdutta.ion.Ion;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.InboxManager;
import net.dean.jraw.models.PrivateMessage;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Pallete;


/**
 * Created by carlo_000 on 3/5/2015.
 */
public class Sendmessage extends ActionBarActivity {
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
    Boolean reply;
    public PrivateMessage previousMessage;
    EditText subject;
    EditText to;
    String bodytext;
    String subjecttext;
    String totext;
    EditText body;
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getTheme().applyStyle(new ColorPreferences(this).getThemeSubreddit("", true).getBaseId(), true);

        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        setContentView(R.layout.activity_sendmessage);

        final Toolbar b = (Toolbar) findViewById(R.id.toolbar);
        final String name;
       reply = false;
        if(getIntent() != null && getIntent().hasExtra("reply")) {
            reply = true;
        }
        subject= (EditText) findViewById(R.id.subject);
        to = (EditText) findViewById(R.id.to);
        to.requestFocus();
         body = (EditText) findViewById(R.id.body);
        View oldMSG = findViewById(R.id.oldMSG);

        if(getIntent() != null && getIntent().hasExtra("name")) {
            name = getIntent().getExtras().getString("name", "");
            to.setText(name);
            to.setInputType(InputType.TYPE_NULL);
            if(reply) {
                b.setTitle("Reply to " + name);
                previousMessage = DataShare.sharedMessage;
                subject.setText("re: " + previousMessage.getSubject() );

                subject.setInputType(InputType.TYPE_NULL);

                body.requestFocus();
                oldMSG.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder b = new AlertDialog.Builder(Sendmessage.this);
                        b.setTitle(name + " said...");
                        b.setMessage(previousMessage.getBody());
                        b.create().show();

                    }
                });

            } else {
                b.setTitle("Send a message to " + name);
                oldMSG.setVisibility(View.GONE);
            }

        } else {
            name = "";
            oldMSG.setVisibility(View.GONE);
            b.setTitle("Send a message");

        }

        b.setBackgroundColor(Pallete.getColorUser(name));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Pallete.getDarkerColor(Pallete.getColorUser(name)));
        }
        setSupportActionBar(b);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);






        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bodytext = body.getText().toString();
                totext = to.getText().toString();
                subjecttext = subject.getText().toString();

                new AsyncDo().execute();
                findViewById(R.id.send).setVisibility(View.GONE);
            }
        });

    }
    public String URL;
    public void setImage(String URL){
        this.URL = URL;
        Ion.with(this).load(URL).intoImageView(((ImageView) findViewById(R.id.imagepost)));
    }

    private class AsyncDo extends AsyncTask<Void, Void,Void>{

        @Override
        public void onPostExecute(Void voids){
            finish();
        }
        @Override
        protected Void doInBackground(Void... voids) {
            if(reply) {
                try {
                    new net.dean.jraw.managers.AccountManager(Authentication.reddit).reply(previousMessage, bodytext);
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    new InboxManager(Authentication.reddit).compose(totext, subjecttext, bodytext);
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

  }