package me.ccrama.redditslide.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import me.ccrama.redditslide.PostMatch;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;


/**
 * Created by l3d00m on 11/13/2015.
 */
public class SettingsExternalBrowser extends BaseActivityAnim {
    EditText domain;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        Settings.changed = true;
        setContentView(R.layout.activity_settings_openexternal);
        setupAppBar(R.id.toolbar, "Force external browser", true, true);
        domain = (EditText) findViewById(R.id.domain);
        domain.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    SettingValues.alwaysExternal =
                            SettingValues.alwaysExternal + ", " + domain.getText().toString();
                    domain.setText("");
                    updateFilters();
                }
                return false;
            }
        });
        updateFilters();

    }

    public ArrayList<String> domains = new ArrayList<>();

    public void updateFilters() {
        domains = new ArrayList<>();

        ((LinearLayout) findViewById(R.id.domainlist)).removeAllViews();
        for (String s : SettingValues.alwaysExternal.replaceAll("^[,\\s]+", "").split("[,\\s]+")) {
            if (!s.isEmpty()) {
                s = s.trim();
                final String finalS = s;
                domains.add(finalS);
                final View t = getLayoutInflater().inflate(R.layout.account_textview,
                        ((LinearLayout) findViewById(R.id.domainlist)), false);

                ((TextView) t.findViewById(R.id.name)).setText(s);
                t.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        domains.remove(finalS);
                        SettingValues.alwaysExternal = Reddit.arrayToString(domains);
                        updateFilters();
                    }
                });
                ((LinearLayout) findViewById(R.id.domainlist)).addView(t);

            }
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor e = SettingValues.prefs.edit();

        e.putString(SettingValues.PREF_ALWAYS_EXTERNAL, Reddit.arrayToString(domains));
        e.apply();

        PostMatch.externalDomain = null;

        SettingValues.alwaysExternal =
                SettingValues.prefs.getString(SettingValues.PREF_ALWAYS_EXTERNAL, "");

    }


}