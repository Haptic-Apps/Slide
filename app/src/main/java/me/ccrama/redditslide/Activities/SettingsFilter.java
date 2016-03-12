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

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;


/**
 * Created by l3d00m on 11/13/2015.
 */
public class SettingsFilter extends BaseActivityAnim {


    EditText title;
    EditText text;
    EditText domain;
    EditText subreddit;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_filters);
        setupAppBar(R.id.toolbar, R.string.settings_title_filter, true, true);


        title = (EditText) findViewById(R.id.title);
        text = (EditText) findViewById(R.id.text);
        domain = (EditText) findViewById(R.id.domain);
        subreddit = (EditText) findViewById(R.id.subreddit);

        title.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    SettingValues.titleFilters = SettingValues.titleFilters + ", " + title.getText().toString();
                    title.setText("");
                    updateFilters();
                }
                return false;
            }
        });
        text.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    SettingValues.textFilters = SettingValues.textFilters + ", " + text.getText().toString();
                    text.setText("");
                    updateFilters();
                }
                return false;
            }
        });
        domain.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    SettingValues.domainFilters = SettingValues.domainFilters + ", " + domain.getText().toString();
                    domain.setText("");
                    updateFilters();
                }
                return false;
            }
        });
        subreddit.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    SettingValues.subredditFilters = SettingValues.subredditFilters + ", " + subreddit.getText().toString();
                    subreddit.setText("");
                    updateFilters();
                }
                return false;
            }
        });
        updateFilters();

    }

    public ArrayList<String> subs = new ArrayList<>();
    public ArrayList<String> domains = new ArrayList<>();
    public ArrayList<String> textlist = new ArrayList<>();
    public ArrayList<String> titlelist = new ArrayList<>();


    public void updateFilters() {
        domains = new ArrayList<>();

        ((LinearLayout) findViewById(R.id.domainlist)).removeAllViews();
        for (String s : SettingValues.domainFilters.replaceAll("^[,\\s]+", "").split("[,\\s]+")) {
            if (!s.isEmpty()) {
                s = s.trim();
                final String finalS = s;
                domains.add(finalS);
                final View t = getLayoutInflater().inflate(R.layout.account_textview, ((LinearLayout) findViewById(R.id.domainlist)), false);

                ((TextView) t.findViewById(R.id.name)).setText(s);
                t.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SettingValues.domainFilters = SettingValues.domainFilters.replace(finalS, "");
                        updateFilters();
                    }
                });
                ((LinearLayout) findViewById(R.id.domainlist)).addView(t);

            }
        }

        subs = new ArrayList<>();
        ((LinearLayout) findViewById(R.id.subredditlist)).removeAllViews();

        for (String s : SettingValues.subredditFilters.replaceAll("^[,\\s]+", "").split("[,\\s]+")) {
            if (!s.isEmpty()) {
                s = s.trim();
                final String finalS = s;
                subs.add(finalS);
                final View t = getLayoutInflater().inflate(R.layout.account_textview, ((LinearLayout) findViewById(R.id.subredditlist)), false);

                ((TextView) t.findViewById(R.id.name)).setText(s);
                t.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SettingValues.subredditFilters = SettingValues.subredditFilters.replace(finalS, "");
                        updateFilters();

                    }
                });
                ((LinearLayout) findViewById(R.id.subredditlist)).addView(t);

            }
        }

        textlist = new ArrayList<>();
        ((LinearLayout) findViewById(R.id.selftextlist)).removeAllViews();

        for (String s : SettingValues.textFilters.replaceAll("^[,\\s]+", "").split("[,\\s]+")) {
            if (!s.isEmpty()) {
                s = s.trim();
                final String finalS = s;
                textlist.add(finalS);
                final View t = getLayoutInflater().inflate(R.layout.account_textview, ((LinearLayout) findViewById(R.id.selftextlist)), false);

                ((TextView) t.findViewById(R.id.name)).setText(s);
                t.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SettingValues.textFilters = SettingValues.textFilters.replace(finalS, "");
                        updateFilters();

                    }
                });
                ((LinearLayout) findViewById(R.id.selftextlist)).addView(t);

            }
        }

        titlelist = new ArrayList<>();
        ((LinearLayout) findViewById(R.id.titlelist)).removeAllViews();

        for (String s : SettingValues.titleFilters.replaceAll("^[,\\s]+", "").split("[,\\s]+")) {
            if (!s.isEmpty()) {
                s = s.trim();
                final String finalS = s;
                titlelist.add(finalS);
                final View t = getLayoutInflater().inflate(R.layout.account_textview, ((LinearLayout) findViewById(R.id.titlelist)), false);

                ((TextView) t.findViewById(R.id.name)).setText(s);
                t.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SettingValues.titleFilters = SettingValues.titleFilters.replace(finalS, "");
                        updateFilters();

                    }
                });

                ((LinearLayout) findViewById(R.id.titlelist)).addView(t);

            }
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor e = SettingValues.prefs.edit();

        e.putString(SettingValues.PREF_TITLE_FILTERS, Reddit.arrayToString(titlelist));
        e.putString(SettingValues.PREF_DOMAIN_FILTERS, Reddit.arrayToString(domains));
        e.putString(SettingValues.PREF_TEXT_FILTERS, Reddit.arrayToString(textlist));
        e.putString(SettingValues.PREF_SUBREDDIT_FILTERS, Reddit.arrayToString(subs));
        e.apply();

        SettingValues.titleFilters = SettingValues.prefs.getString(SettingValues.PREF_TITLE_FILTERS, "");
        SettingValues.textFilters = SettingValues.prefs.getString(SettingValues.PREF_TEXT_FILTERS, "");
        SettingValues.domainFilters = SettingValues.prefs.getString(SettingValues.PREF_DOMAIN_FILTERS, "");
        SettingValues.subredditFilters = SettingValues.prefs.getString(SettingValues.PREF_SUBREDDIT_FILTERS, "");

    }


}