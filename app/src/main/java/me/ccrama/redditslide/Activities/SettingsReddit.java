package me.ccrama.redditslide.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;

import net.dean.jraw.AccountPreferencesEditor;

import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Visuals.Palette;


/**
 * Created by l3d00m on 11/13/2015.
 */
public class SettingsReddit extends BaseActivityAnim {

    AccountPreferencesEditor prefs;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_reddit);
        setupAppBar(R.id.toolbar, R.string.settings_reddit_prefs, true, true);
        {

            final SwitchCompat nsfwprev = (SwitchCompat) findViewById(R.id.nsfwrpev);

            nsfwprev.setChecked(!SettingValues.NSFWPreviews);
            nsfwprev.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.prefs.edit().putBoolean("NSFWPreviewsNew", !isChecked).apply();
                    SettingValues.NSFWPreviews = !isChecked;
                }
            });

        }


      /* TODO  new AsyncTask<Void, Void, Void>() {
            Dialog d;

            @Override
            protected void onPreExecute() {
                d = new MaterialDialog.Builder(SettingsReddit.this).title("Syncing with Reddit")
                        .progress(true, 100).show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                prefs = new AccountPreferencesEditor(new FluentRedditClient(Authentication.reddit).me().accountPreferences());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                {

                    final SwitchCompat thumbnails = (SwitchCompat) findViewById(R.id.nsfwcontent);

                    thumbnails.setChecked(prefs.getArgs.("over_18", boolean.class));
                    thumbnails.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            SettingValues.prefs.edit().putBoolean("NSFWPreviewsNew", !isChecked).apply();
                            SettingValues.NSFWPreviews = !isChecked;
                        }
                    });

                }
                {

                    final SwitchCompat thumbnails = (SwitchCompat) findViewById(R.id.nsfwthumbs);

                    thumbnails.setChecked(prefs.data("no_profanity", boolean.class));
                    thumbnails.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                           prefs.e
                        }
                    });

                }

            }
        }.execute();*/

        findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SettingValues.web) {
                    Intent browserIntent = new Intent(SettingsReddit.this, Website.class);
                    browserIntent.putExtra(Website.EXTRA_URL, "https://www.reddit.com/prefs/");
                    browserIntent.putExtra(Website.EXTRA_COLOR, Palette.getDefaultColor());
                    startActivity(browserIntent);
                } else OpenRedditLink.customIntentChooser(
                        "https://www.reddit.com/prefs/", SettingsReddit.this);
            }
        });
    }


}