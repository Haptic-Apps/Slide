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
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.Visuals.Palette;


/**
 * Created by ccrama on 3/5/2015.
 */
public class SettingsSubreddit extends BaseActivity {
    private final static String TAG = "SettingsSubreddit";
    ArrayList<String> done = new ArrayList<>();

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
        final ArrayList<String> subs = SubredditStorage.alphabeticalSubscriptions;
        subs.remove("frontpage");
        subs.remove("all");
        initializeAdapter(subs);

        findViewById(R.id.post_floating_action_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final CharSequence[] subsAsChar = subs.toArray(new CharSequence[subs.size()]);

                MaterialDialog.Builder builder = new MaterialDialog.Builder(SettingsSubreddit.this);
                builder.title("Select a subreddit to add")
                        .items(subsAsChar)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                updateAdapter(subsAsChar[which].toString());
                                Log.v(TAG, "Updated adapter with " + subsAsChar[which]);
                            }
                        }).show();
            }
        });
    }

    private void initializeAdapter(ArrayList<String> subs) {
        ListView l = (ListView) findViewById(R.id.subslist);
        int defaultFont = ColorPreferences.getDefaultFontStyle().getColor();
        for (String s : subs) {
            if (Palette.getColor(s) != Palette.getDefaultColor()) { //Main color is different
                done.add(s);
            } else if (SettingValues.prefs.contains("PRESET" + s)) { //Alternate Layout is set
                done.add(s);
            } else if (new ColorPreferences(SettingsSubreddit.this).getFontStyleSubreddit(s).getColor() !=  defaultFont){ //different accent / font color
                done.add(s);
            }
        }
        final SettingsSubAdapter adapter = new SettingsSubAdapter(this, done);
        l.setAdapter(adapter);
    }

    private void updateAdapter(String subreddit) {
        ListView l = (ListView) findViewById(R.id.subslist);
        done.add(subreddit);
        final SettingsSubAdapter adapter = new SettingsSubAdapter(this, done, subreddit);
        l.setAdapter(adapter);
    }

}