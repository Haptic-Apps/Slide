package me.ccrama.redditslide.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.AccountPreferencesEditor;
import net.dean.jraw.managers.AccountManager;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;


/**
 * Created by l3d00m on 11/13/2015.
 */
public class SettingsReddit extends BaseActivity {

    AccountPreferencesEditor editor;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_reddit);
        setupAppBar(R.id.toolbar, "Reddit settings", true, true);
       final Dialog d = new AlertDialogWrapper.Builder(this).setTitle("Getting preferences")
                .setCancelable(false)
                .show();

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                editor = new AccountPreferencesEditor(new AccountManager(Authentication.reddit).getPreferences());

                return null;
            }

            @Override
            protected void onPostExecute(Void voids) {
                {

                    d.dismiss();

                    SwitchCompat nsfw = (SwitchCompat) findViewById(R.id.nsfw);
                    final SwitchCompat nsfwprev = (SwitchCompat) findViewById(R.id.nsfwrpev);
                    nsfw.setChecked(!(Boolean) editor.getArgs().get("over_18"));
                    SettingValues.prefs.edit().putBoolean("NSFWPostsNew", !(Boolean) editor.getArgs().get("over_18")).apply();

                    nsfw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            SettingValues.prefs.edit().putBoolean("NSFWPostsNew", !isChecked).apply();
                            nsfwprev.setEnabled(!SettingValues.NSFWPosts);
                            SettingValues.NSFWPosts = !isChecked;
                            editor.over18(!isChecked);
                        }
                    });

                    nsfwprev.setEnabled(!nsfw.isChecked());
                    nsfwprev.setChecked((Boolean) editor.getArgs().get("no_profanity"));
                    SettingValues.prefs.edit().putBoolean("NSFWPreviewsNew", (Boolean) editor.getArgs().get("no_profanity"));
                    nsfwprev.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            SettingValues.prefs.edit().putBoolean("NSFWPreviewsNew", !isChecked).apply();
                            SettingValues.NSFWPreviews = !isChecked;
                            editor.hideNsfwThumbnails(isChecked);

                        }
                    });

                }

                findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.reddit.com/prefs/"));
                        startActivity(browserIntent);
                    }
                });
            }
        }.execute();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                new AccountManager(Authentication.reddit).updatePreferences(editor);

                return null;
            }
        }.execute();

    }


}