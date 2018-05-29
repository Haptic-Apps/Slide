package me.ccrama.redditslide.Activities;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import me.ccrama.redditslide.PostMatch;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Visuals.Palette;


/**
 * Created by l3d00m on 11/13/2015.
 */
public class SettingsFilter extends BaseActivityAnim {


    EditText title;
    EditText text;
    EditText domain;
    EditText subreddit;
    EditText flair;
    EditText user;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_filters);
        setupAppBar(R.id.toolbar, R.string.settings_title_filter, true, true);


        title = (EditText) findViewById(R.id.title);
        text = (EditText) findViewById(R.id.text);
        domain = (EditText) findViewById(R.id.domain);
        subreddit = (EditText) findViewById(R.id.subreddit);
        flair = (EditText) findViewById(R.id.flair);
        user = (EditText) findViewById(R.id.user);

        title.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    SettingValues.titleFilters =
                            SettingValues.titleFilters + ", " + title.getText().toString();
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
                    SettingValues.textFilters =
                            SettingValues.textFilters + ", " + text.getText().toString();
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
                    SettingValues.domainFilters =
                            SettingValues.domainFilters + ", " + domain.getText().toString();
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
                    SettingValues.subredditFilters =
                            SettingValues.subredditFilters + ", " + subreddit.getText().toString();
                    subreddit.setText("");
                    updateFilters();
                }
                return false;
            }
        });
        user.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    SettingValues.userFilters =
                            SettingValues.userFilters + ", " + user.getText().toString();
                    user.setText("");
                    updateFilters();
                }
                return false;
            }
        });
        flair.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (flair.getText().toString().contains(":")) {
                        SettingValues.flairFilters =
                                SettingValues.flairFilters + ", " + flair.getText().toString();
                        flair.setText("");
                        updateFilters();
                    } else {
                        Toast.makeText(SettingsFilter.this, R.string.settings_filter_flair_error,
                                Toast.LENGTH_LONG).show();
                    }
                }
                return false;
            }
        });
        updateFilters();

    }

    public ArrayList<String> subs      = new ArrayList<>();
    public ArrayList<String> domains   = new ArrayList<>();
    public ArrayList<String> textlist  = new ArrayList<>();
    public ArrayList<String> titlelist = new ArrayList<>();
    public ArrayList<String> flairs    = new ArrayList<>();
    public ArrayList<String> users     = new ArrayList<>();


    public void updateFilters() {
        domains = new ArrayList<>();

        ((LinearLayout) findViewById(R.id.domainlist)).removeAllViews();
        for (String s : SettingValues.domainFilters.replaceAll("^[,\\s]+", "").split("[,\\s]+")) {
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
                        SettingValues.domainFilters = Reddit.arrayToString(domains);
                        updateFilters();
                    }
                });
                ((LinearLayout) findViewById(R.id.domainlist)).addView(t);

            }
        }

        subs = new ArrayList<>();
        ((LinearLayout) findViewById(R.id.subredditlist)).removeAllViews();

        for (String s : SettingValues.subredditFilters.replaceAll("^[,\\s]+", "")
                .split("[,\\s]+")) {
            if (!s.isEmpty()) {
                s = s.trim();
                final String finalS = s;
                subs.add(finalS);
                final View t = getLayoutInflater().inflate(R.layout.account_textview,
                        ((LinearLayout) findViewById(R.id.subredditlist)), false);

                ((TextView) t.findViewById(R.id.name)).setText(s);
                t.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        subs.remove(finalS);
                        SettingValues.subredditFilters = Reddit.arrayToString(subs);
                        updateFilters();

                    }
                });
                ((LinearLayout) findViewById(R.id.subredditlist)).addView(t);

            }
        }

        users = new ArrayList<>();
        ((LinearLayout) findViewById(R.id.userlist)).removeAllViews();

        for (String s : SettingValues.userFilters.replaceAll("^[,\\s]+", "").split("[,\\s]+")) {
            if (!s.isEmpty()) {
                s = s.trim();
                final String finalS = s;
                users.add(finalS);
                final View t = getLayoutInflater().inflate(R.layout.account_textview,
                        ((LinearLayout) findViewById(R.id.subredditlist)), false);

                ((TextView) t.findViewById(R.id.name)).setText(s);
                t.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        users.remove(finalS);
                        SettingValues.userFilters = Reddit.arrayToString(users);
                        updateFilters();

                    }
                });
                ((LinearLayout) findViewById(R.id.userlist)).addView(t);

            }
        }

        textlist = new ArrayList<>();
        ((LinearLayout) findViewById(R.id.selftextlist)).removeAllViews();

        for (String s : SettingValues.textFilters.replaceAll("^[,\\s]+", "").split("[,\\s]+")) {
            if (!s.isEmpty()) {
                s = s.trim();
                final String finalS = s;
                textlist.add(finalS);
                final View t = getLayoutInflater().inflate(R.layout.account_textview,
                        ((LinearLayout) findViewById(R.id.selftextlist)), false);

                ((TextView) t.findViewById(R.id.name)).setText(s);
                t.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        textlist.remove(finalS);
                        SettingValues.textFilters = Reddit.arrayToString(textlist);
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
                final View t = getLayoutInflater().inflate(R.layout.account_textview,
                        ((LinearLayout) findViewById(R.id.titlelist)), false);

                ((TextView) t.findViewById(R.id.name)).setText(s);
                t.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        titlelist.remove(finalS);
                        SettingValues.titleFilters = Reddit.arrayToString(titlelist);
                        updateFilters();

                    }
                });

                ((LinearLayout) findViewById(R.id.titlelist)).addView(t);

            }
        }

        flairs = new ArrayList<>();

        ((LinearLayout) findViewById(R.id.flairlist)).removeAllViews();
        for (String s : SettingValues.flairFilters.replaceAll("^[,]+", "").split("[,]+")) {
            if (!s.isEmpty()) {
                s = s.trim();
                final String finalS = s;
                flairs.add(finalS);
                final View t = getLayoutInflater().inflate(R.layout.account_textview,
                        ((LinearLayout) findViewById(R.id.domainlist)), false);

                SpannableStringBuilder b = new SpannableStringBuilder();

                String subname = s.split(":")[0];
                SpannableStringBuilder subreddit =
                        new SpannableStringBuilder(" /r/" + subname + " ");

                if ((SettingValues.colorSubName
                        && Palette.getColor(subname) != Palette.getDefaultColor())) {
                    subreddit.setSpan(new ForegroundColorSpan(Palette.getColor(subname)), 0,
                            subreddit.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    subreddit.setSpan(new StyleSpan(Typeface.BOLD), 0, subreddit.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                b.append(subreddit);
                b.append(s.split(":")[1]);
                ((TextView) t.findViewById(R.id.name)).setText(b);
                t.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        flairs.remove(finalS);
                        SettingValues.flairFilters = Reddit.arrayToString(flairs);
                        updateFilters();
                    }
                });
                ((LinearLayout) findViewById(R.id.flairlist)).addView(t);

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
        e.putString(SettingValues.PREF_FLAIR_FILTERS, Reddit.arrayToString(flairs));
        e.putString(SettingValues.PREF_USER_FILTERS, Reddit.arrayToString(users));
        e.apply();

        PostMatch.subreddits = null;
        PostMatch.domains = null;
        PostMatch.titles = null;
        PostMatch.externalDomain = null;
        PostMatch.flairs = null;
        PostMatch.texts = null;
        PostMatch.users = null;

        SettingValues.titleFilters =
                SettingValues.prefs.getString(SettingValues.PREF_TITLE_FILTERS, "");
        SettingValues.textFilters =
                SettingValues.prefs.getString(SettingValues.PREF_TEXT_FILTERS, "");
        SettingValues.domainFilters =
                SettingValues.prefs.getString(SettingValues.PREF_DOMAIN_FILTERS, "");
        SettingValues.flairFilters =
                SettingValues.prefs.getString(SettingValues.PREF_FLAIR_FILTERS, "");
        SettingValues.subredditFilters =
                SettingValues.prefs.getString(SettingValues.PREF_SUBREDDIT_FILTERS, "");
        SettingValues.userFilters =
                SettingValues.prefs.getString(SettingValues.PREF_USER_FILTERS, "");

    }


}