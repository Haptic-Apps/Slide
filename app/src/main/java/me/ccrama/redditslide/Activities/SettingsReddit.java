package me.ccrama.redditslide.Activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;

import com.afollestad.materialdialogs.MaterialDialog;

import net.dean.jraw.AccountPreferencesEditor;
import net.dean.jraw.managers.AccountManager;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Visuals.Palette;


/**
 * Created by l3d00m on 11/13/2015.
 */
public class SettingsReddit extends BaseActivity {

    AccountPreferencesEditor editor;
    AsyncRedditPrefs mAsyncRedditPrefs;
    Dialog mGettingPrefsDialog;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_reddit);
        setupAppBar(R.id.toolbar, "Reddit settings", true, true);
        mAsyncRedditPrefs = new AsyncRedditPrefs();
        mAsyncRedditPrefs.execute();

        mGettingPrefsDialog = new MaterialDialog.Builder(this)
                .title("Getting preferences")
                .progress(true, 0)
                .content("Please wait...")
                .cancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialogInterface) {
                                        if (mAsyncRedditPrefs != null)
                                            mAsyncRedditPrefs.cancel(true);
                                        finish();
                                    }
                                }
                ).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (editor != null)
                    new AccountManager(Authentication.reddit).updatePreferences(editor);

                return null;
            }
        }.execute();

    }

    private class AsyncRedditPrefs extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            editor = new AccountPreferencesEditor(new AccountManager(Authentication.reddit).getPreferences());

            return null;
        }

        @Override
        protected void onPostExecute(Void voids) {
            {
                if (mGettingPrefsDialog != null)
                    mGettingPrefsDialog.dismiss();

                SwitchCompat nsfw = (SwitchCompat) findViewById(R.id.nsfw);
                final SwitchCompat nsfwprev = (SwitchCompat) findViewById(R.id.nsfwrpev);
                nsfw.setChecked((Boolean) editor.getArgs().get("over_18"));
                SettingValues.prefs.edit().putBoolean("NSFWPostsNew", (Boolean) editor.getArgs().get("over_18")).apply();

                nsfw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        SettingValues.prefs.edit().putBoolean("NSFWPostsNew", isChecked).apply();
                        SettingValues.NSFWPosts = isChecked;

                        nsfwprev.setEnabled(SettingValues.NSFWPosts);
                        editor.over18(isChecked);
                    }
                });

                nsfwprev.setEnabled(nsfw.isChecked());
                nsfwprev.setChecked((Boolean) editor.getArgs().get("no_profanity"));
                SettingValues.prefs.edit().putBoolean("NSFWPreviewsNew", !(Boolean) editor.getArgs().get("no_profanity"));
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
                    if (Reddit.web) {
                        Intent browserIntent = new Intent(SettingsReddit.this, Website.class);
                        browserIntent.putExtra("url", "https://www.reddit.com/prefs/");
                        browserIntent.putExtra("color", Palette.getDefaultColor());
                        startActivity(browserIntent);
                    } else OpenRedditLink.customIntentChooser(
                                "https://www.reddit.com/prefs/", SettingsReddit.this);
                }
            });
        }
    }

}