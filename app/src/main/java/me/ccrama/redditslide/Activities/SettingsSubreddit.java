package me.ccrama.redditslide.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import net.dean.jraw.models.Subreddit;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.ccrama.redditslide.Adapters.SettingsSubAdapter;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Fragments.SettingsThemeFragment;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.Visuals.GetClosestColor;
import me.ccrama.redditslide.Visuals.Palette;


/**
 * Created by ccrama on 3/5/2015.
 */
public class SettingsSubreddit extends BaseActivityAnim {
    public SettingsSubAdapter mSettingsSubAdapter;
    ArrayList<String> changedSubs = new ArrayList<>();

    private RecyclerView recycler;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            Intent i = new Intent(SettingsSubreddit.this, SettingsSubreddit.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(i);
            overridePendingTransition(0, 0);

            finish();
            overridePendingTransition(0, 0);
        }
    }

    int done;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_subreddit);
        SettingsThemeFragment.changed = true;

        setupAppBar(R.id.toolbar, R.string.title_subreddit_settings, true, true);

        recycler = ((RecyclerView) findViewById(R.id.subslist));
        recycler.setLayoutManager(new LinearLayoutManager(this));

        reloadSubList();

        findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialogWrapper.Builder(SettingsSubreddit.this)
                        .setTitle(R.string.clear_all_sub_themes)
                        .setMessage(R.string.clear_all_sub_themes_msg)
                        .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (String s : changedSubs) {
                                    Palette.removeColor(s);
                                    SettingValues.prefs.edit().remove(Reddit.PREF_LAYOUT + s).apply();
                                    new ColorPreferences(SettingsSubreddit.this).removeFontStyle(s);
                                    SettingValues.resetPicsEnabled(s);
                                }
                                reloadSubList();

                            }
                        }).setNegativeButton(R.string.btn_no, null)
                        .show();
            }
        });
        findViewById(R.id.post_floating_action_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ArrayList<String> subs = UserSubscriptions.sort(UserSubscriptions.getSubscriptions(SettingsSubreddit.this));
                final CharSequence[] subsAsChar = subs.toArray(new CharSequence[0]);

                MaterialDialog.Builder builder = new MaterialDialog.Builder(SettingsSubreddit.this);
                builder.title(R.string.dialog_choose_subreddits_to_edit)
                        .items(subsAsChar)
                        .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMultiChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                                ArrayList<String> selectedSubs = new ArrayList<>();
                                for (int i : which) {
                                    selectedSubs.add(subsAsChar[i].toString());
                                }
                                if (mSettingsSubAdapter != null)
                                    mSettingsSubAdapter.prepareAndShowSubEditor(selectedSubs);
                                return true;
                            }
                        })
                        .positiveText(R.string.btn_select)
                        .negativeText(R.string.btn_cancel)
                        .show();
            }
        });
        findViewById(R.id.color).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Authentication.isLoggedIn) {
                    new AlertDialogWrapper.Builder(SettingsSubreddit.this).setTitle(R.string.dialog_color_sync_title)
                            .setMessage(R.string.dialog_color_sync_message)
                            .setPositiveButton(R.string.misc_continue, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final MaterialDialog d = new MaterialDialog.Builder(SettingsSubreddit.this).title(R.string.general_sub_sync)
                                            .content(R.string.misc_please_wait)
                                            .progress(false, 100)
                                            .cancelable(false).show();

                                    new AsyncTask<Void, Void, Void>() {
                                        @Override
                                        protected Void doInBackground(Void... params) {
                                            ArrayList<Subreddit> subColors = UserSubscriptions.syncSubredditsGetObject();
                                            d.setMaxProgress(subColors.size());
                                            int i = 0;
                                            done = 0;
                                            for (Subreddit s : subColors) {
                                                if (s.getDataNode().has("key_color") && !s.getDataNode().get("key_color").asText().isEmpty() && Palette.getColor(s.getDisplayName().toLowerCase(Locale.ENGLISH)) == Palette.getDefaultColor()) {
                                                    Palette.setColor(s.getDisplayName().toLowerCase(Locale.ENGLISH), GetClosestColor.getClosestColor(s.getDataNode().get("key_color").asText(), SettingsSubreddit.this));
                                                    done++;
                                                }
                                                d.setProgress(i);

                                                i++;
                                                if (i == d.getMaxProgress()) {
                                                    d.dismiss();

                                                }

                                            }
                                            return null;
                                        }

                                        @Override
                                        protected void onPostExecute(Void aVoid) {

                                            reloadSubList();
                                            Resources res = getResources();

                                            new AlertDialogWrapper.Builder(SettingsSubreddit.this)
                                                    .setTitle(R.string.color_sync_complete)
                                                    .setMessage(res.getQuantityString(R.plurals.color_sync_colored, done, done))
                                                    .setPositiveButton(getString(R.string.btn_ok), null)
                                                    .show();
                                        }
                                    }.execute();
                                    d.show();
                                }
                            }).setNegativeButton(R.string.btn_cancel, null).show();
                } else {
                    Snackbar s = Snackbar.make(mToolbar, R.string.err_color_sync_login, Snackbar.LENGTH_SHORT);
                    View view = s.getView();
                    TextView tv = (TextView) view.findViewById(com.google.android.material.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    s.show();
                }
            }
        });
    }

    public void reloadSubList() {
        changedSubs.clear();
        List<String> allSubs = UserSubscriptions.sort(UserSubscriptions.getAllUserSubreddits(this));

        // Check which subreddits are different
        ColorPreferences colorPrefs = new ColorPreferences(SettingsSubreddit.this);
        int defaultFont = colorPrefs.getFontStyle().getColor();

        for (String s : allSubs) {
            if (Palette.getColor(s) != Palette.getDefaultColor()
                    || SettingValues.prefs.contains(Reddit.PREF_LAYOUT + s)
                    || colorPrefs.getFontStyleSubreddit(s).getColor() != defaultFont
                    || SettingValues.prefs.contains("picsenabled" + s.toLowerCase(Locale.ENGLISH))) {
                changedSubs.add(s);
            }
        }

        mSettingsSubAdapter = new SettingsSubAdapter(this, changedSubs);
        recycler.setAdapter(mSettingsSubAdapter);
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.post_floating_action_button);
        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy <= 0 && fab.getId() != 0) {
                    fab.show();
                } else {
                    fab.hide();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        fab.show();
    }


}