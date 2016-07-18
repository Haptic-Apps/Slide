package me.ccrama.redditslide.Activities;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SwitchCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import net.dean.jraw.AccountPreferencesEditor;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.AccountPreferences;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.LinkUtil;


/**
 * Created by l3d00m on 11/13/2015.
 */
public class SettingsReddit extends BaseActivityAnim {

    AccountPreferences prefs;
    AccountPreferencesEditor editor;

    @Override
    public void onPause() {
        super.onPause();
        if(editor != null)
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    new AccountManager(Authentication.reddit).updatePreferences(editor);

                    return null;
                }
            }.execute();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_reddit);
        setupAppBar(R.id.toolbar, R.string.settings_reddit_prefs, true, true);

        new AsyncTask<Void, Void, Void>() {
            Dialog d;

            @Override
            protected void onPreExecute() {
                d = new MaterialDialog.Builder(SettingsReddit.this)
                        .title("Syncing with Reddit")
                        .content(R.string.misc_please_wait)
                        .progress(true, 100)
                        .show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                prefs = new AccountManager(Authentication.reddit).getPreferences("over_18", "no_profanity", "media");
                editor = new AccountPreferencesEditor(prefs);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                {
                    final SwitchCompat thumbnails = (SwitchCompat) findViewById(R.id.nsfwcontent);
                    thumbnails.setChecked(Boolean.parseBoolean(prefs.data("over_18")));

                    thumbnails.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            editor.setArgs("over_18", String.valueOf(isChecked));
                            Reddit.over18 = isChecked;
                            Settings.changed = true;

                            if (isChecked) {
                                (findViewById(R.id.nsfwrpev)).setEnabled(true);
                                findViewById(R.id.nsfwrpev_text).setAlpha(1f);
                                ((SwitchCompat) findViewById(R.id.nsfwrpev)).setChecked(true);
                            } else {
                                ((SwitchCompat) findViewById(R.id.nsfwrpev)).setChecked(true);
                                (findViewById(R.id.nsfwrpev)).setEnabled(false);
                                findViewById(R.id.nsfwrpev_text).setAlpha(0.25f);
                            }
                        }
                    });
                }
                {
                    final SwitchCompat thumbnails = (SwitchCompat) findViewById(R.id.nsfwrpev);

                    if (!((SwitchCompat) findViewById(R.id.nsfwcontent)).isChecked()) {
                        thumbnails.setChecked(true);
                        thumbnails.setEnabled(false);
                        findViewById(R.id.nsfwrpev_text).setAlpha(0.25f);
                    } else {
                        thumbnails.setChecked(Boolean.parseBoolean(prefs.data("no_profanity")));
                    }

                    thumbnails.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            editor.setArgs("no_profanity", String.valueOf(isChecked));
                            Settings.changed = true;
                        }
                    });
                }

                //Thumbnail type
                String thumbType = String.valueOf(prefs.data("media"));
                ((TextView) findViewById(R.id.thumbtext)).setText(thumbType.equals("on") ? getString(R.string.thumb_type_always) : thumbType.equals("off") ? getString(R.string.thumb_type_off) : getString(R.string.thumb_type_sub));

                findViewById(R.id.thumbmode).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu popup = new PopupMenu(SettingsReddit.this, v);
                        popup.getMenuInflater().inflate(R.menu.thumb_type_settings, popup.getMenu());

                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.on:
                                        editor.setArgs("media", "on");
                                        Settings.changed = true;
                                        break;
                                    case R.id.off:
                                        editor.setArgs("media", "off");
                                        Settings.changed = true;
                                        break;
                                    case R.id.subreddit:
                                        editor.setArgs("media", "subreddit");
                                        Settings.changed = true;
                                        break;
                                }
                                String thumbType = String.valueOf(editor.getArgs().get("media"));
                                ((TextView) findViewById(R.id.thumbtext)).setText(thumbType.equals("on") ? getString(R.string.thumb_type_always) : thumbType.equals("off") ? getString(R.string.thumb_type_off) : getString(R.string.thumb_type_sub));
                                return true;
                            }
                        });
                        popup.show();
                    }
                });
                d.dismiss();
            }
        }.execute();

        findViewById(R.id.viewRedditPrefs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinkUtil.openUrl("https://www.reddit.com/prefs/", Palette.getDefaultColor(), SettingsReddit.this);
            }
        });
    }
}