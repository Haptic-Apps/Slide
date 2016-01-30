package me.ccrama.redditslide.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import me.ccrama.redditslide.Adapters.SettingsSubAdapter;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.LogUtil;


/**
 * Created by ccrama on 3/5/2015.
 */
public class SettingsSubreddit extends BaseActivity {
    public SettingsSubAdapter mSettingsSubAdapter;
    ArrayList<String> changedSubs = new ArrayList<>();

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_subreddit);

        setupAppBar(R.id.toolbar, R.string.title_subreddit_settings, true, true);
        reloadSubList();

        findViewById(R.id.post_floating_action_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ArrayList<String> subs = SubredditStorage.alphabeticalSubreddits;
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
    }

    public void reloadSubList() {
        Log.v(LogUtil.getTag(), "adapter init");
        changedSubs.clear();
        ArrayList<String> allSubs = SubredditStorage.alphabeticalSubreddits;
        allSubs.remove("all");
        allSubs.remove("frontpage");

        // Check which subreddits are different
        ColorPreferences colorPrefs = new ColorPreferences(SettingsSubreddit.this);
        int defaultFont = colorPrefs.getColor("");

        for (String s : allSubs) {
            if (Palette.getColor(s) != Palette.getDefaultColor()) { //Main color is different
                changedSubs.add(s);
            } else if (SettingValues.prefs.contains(Reddit.PREF_LAYOUT + s)) { //Alternate Layout is set
                changedSubs.add(s);
            } else if (colorPrefs.getColor(s) != defaultFont) { //different accent / font color
                changedSubs.add(s);
            }
        }

        mSettingsSubAdapter = new SettingsSubAdapter(this, changedSubs);
        ((ListView) findViewById(R.id.subslist)).setAdapter(mSettingsSubAdapter);
    }


}