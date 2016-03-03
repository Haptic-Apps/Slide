package me.ccrama.redditslide.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;

import net.dean.jraw.models.Subreddit;

import java.util.ArrayList;

import me.ccrama.redditslide.Adapters.SettingsSubAdapter;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.Visuals.GetClosestColor;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.LogUtil;


/**
 * Created by ccrama on 3/5/2015.
 */
public class SettingsSubreddit extends BaseActivityAnim {
    public SettingsSubAdapter mSettingsSubAdapter;
    ArrayList<String> changedSubs = new ArrayList<>();

    private RecyclerView recycler;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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

        setupAppBar(R.id.toolbar, R.string.title_subreddit_settings, true, true);

        recycler = ((RecyclerView) findViewById(R.id.subslist));
        recycler.setLayoutManager(new LinearLayoutManager(this));

        reloadSubList();

        findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialogWrapper.Builder(SettingsSubreddit.this)
                        .setTitle("Clear all subreddit themes?")
                        .setMessage("You cannot undo this action.")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (String s : changedSubs) {
                                    Palette.removeColor(s);
                                    // Remove layout settings
                                    SettingValues.prefs.edit().remove(Reddit.PREF_LAYOUT + s).apply();
                                    // Remove accent / font color settings
                                    new ColorPreferences(SettingsSubreddit.this).removeFontStyle(s);

                                    SettingValues.resetPicsEnabled(s);
                                }
                                reloadSubList();

                            }
                        }).setNegativeButton("No", null)
                        .show();
            }
        });
        findViewById(R.id.post_floating_action_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ArrayList<String> subs = SubredditStorage.sort(SubredditStorage.subredditsForHome);
                subs.remove("frontpage");
                subs.remove("all");

                final CharSequence[] subsAsChar = subs.toArray(new CharSequence[subs.size()]);

                MaterialDialog.Builder builder = new MaterialDialog.Builder(SettingsSubreddit.this);
                builder.title(R.string.reorder_add_subreddit)
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
                        .positiveText(R.string.btn_add)
                        .negativeText(R.string.btn_cancel)
                        .show();
            }
        });
        findViewById(R.id.color).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialogWrapper.Builder(SettingsSubreddit.this).setTitle("Color syncing")
                        .setMessage("This will try to retrieve the subreddit's 'key color' set by the moderators. It will not overwrite already colored subreddits.")
                        .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final MaterialDialog d = new MaterialDialog.Builder(SettingsSubreddit.this).title(R.string.general_sub_sync)
                                        .content(R.string.misc_please_wait)
                                        .progress(false, 100)
                                        .cancelable(false).show();

                                new AsyncTask<Void, Void, Void>() {
                                    @Override
                                    protected Void doInBackground(Void... params) {
                                        ArrayList<Subreddit> subColors = SubredditStorage.syncSubredditsGetObject();
                                        d.setMaxProgress(subColors.size());
                                        int i = 0;
                                        for (Subreddit s : subColors) {
                                            if (s.getDataNode().has("key_color") && !s.getDataNode().get("key_color").asText().isEmpty() && Palette.getColor(s.getDisplayName().toLowerCase()) == Palette.getDefaultColor()) {
                                                Palette.setColor(s.getDisplayName().toLowerCase(), GetClosestColor.getClosestColor(s.getDataNode().get("key_color").asText(), SettingsSubreddit.this));
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

                                        new AlertDialogWrapper.Builder(SettingsSubreddit.this)
                                                .setTitle(R.string.color_sync_complete)
                                                .setMessage(done + getString(R.string.color_sync_colored))
                                                .setPositiveButton(getString(R.string.btn_ok), null)
                                                .show();
                                    }
                                }.execute();
                                d.show();
                            }
                        }).setNegativeButton("Cancel", null).show();
            }
        });
    }

    public void reloadSubList() {
        Log.v(LogUtil.getTag(), "adapter init");
        changedSubs.clear();
        ArrayList<String> allSubs = SubredditStorage.sort(SubredditStorage.subredditsForHome);
        allSubs.remove("all");
        allSubs.remove("frontpage");

        // Check which subreddits are different
        ColorPreferences colorPrefs = new ColorPreferences(SettingsSubreddit.this);
        int defaultFont = colorPrefs.getColor("");

        for (String s : allSubs) {
            if (Palette.getColor(s) != Palette.getDefaultColor()
                    || SettingValues.prefs.contains(Reddit.PREF_LAYOUT + s)
                    || colorPrefs.getColor(s) != defaultFont
                    || SettingValues.prefs.contains("picsenabled" + s)) {
                changedSubs.add(s);
            }
        }

        mSettingsSubAdapter = new SettingsSubAdapter(this, changedSubs);
        recycler.setAdapter(mSettingsSubAdapter);
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.post_floating_action_button);
        recycler.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy <= 0 && fab.getId() != 0 && SettingValues.fab) {

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
    }


}