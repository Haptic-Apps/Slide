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

import androidx.core.util.Consumer;

import java.util.Locale;
import java.util.Set;

import me.ccrama.redditslide.R;
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

    @Override
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

        title.setOnEditorActionListener(makeOnEditorActionListener(SettingValues.titleFilters::add));
        text.setOnEditorActionListener(makeOnEditorActionListener(SettingValues.textFilters::add));
        domain.setOnEditorActionListener(makeOnEditorActionListener(SettingValues.domainFilters::add));
        subreddit.setOnEditorActionListener(makeOnEditorActionListener(SettingValues.subredditFilters::add));
        user.setOnEditorActionListener(makeOnEditorActionListener(SettingValues.userFilters::add));

        flair.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String text = v.getText().toString().toLowerCase(Locale.ENGLISH).trim();
                if (text.matches(".+:.+")) {
                    SettingValues.flairFilters.add(text);
                    v.setText("");
                    updateFilters();
                }
            }

            return false;
        });

        updateFilters();
    }

    /**
     * Makes an OnEditorActionListener that calls filtersAdd when done is pressed
     *
     * @param filtersAdd called when done is pressed
     * @return The new OnEditorActionListener
     */
    private TextView.OnEditorActionListener makeOnEditorActionListener(Consumer<String> filtersAdd) {
        return new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String text = v.getText().toString().toLowerCase(Locale.ENGLISH).trim();
                    if (!text.isEmpty()) {
                        filtersAdd.accept(text);
                        v.setText("");
                        updateFilters();
                    }
                }
                return false;
            }
        };
    }

    /**
     * Iterate through filters and add an item for each to the layout with id, with a remove button calling filtersRemoved
     *
     * @param id            ID of linearlayout containing items
     * @param filters       Set of filters to iterate through
     * @param filtersRemove Method to call on remove button press
     */
    private void updateList(int id, Set<String> filters, Consumer<String> filtersRemove) {
        ((LinearLayout) findViewById(id)).removeAllViews();
        for (String s : filters) {
            final View t = getLayoutInflater().inflate(R.layout.account_textview, (LinearLayout) findViewById(id), false);
            ((TextView) t.findViewById(R.id.name)).setText(s);
            t.findViewById(R.id.remove).setOnClickListener(v -> {
                filtersRemove.accept(s);
                updateFilters();
            });
            ((LinearLayout) findViewById(id)).addView(t);
        }
    }

    /**
     * Updates the filters shown in the UI
     */
    public void updateFilters() {
        updateList(R.id.domainlist, SettingValues.domainFilters, SettingValues.domainFilters::remove);
        updateList(R.id.subredditlist, SettingValues.subredditFilters, SettingValues.subredditFilters::remove);
        updateList(R.id.userlist, SettingValues.userFilters, SettingValues.userFilters::remove);
        updateList(R.id.selftextlist, SettingValues.textFilters, SettingValues.textFilters::remove);
        updateList(R.id.titlelist, SettingValues.titleFilters, SettingValues.titleFilters::remove);

        ((LinearLayout) findViewById(R.id.flairlist)).removeAllViews();
        for (String s : SettingValues.flairFilters) {
            final View t = getLayoutInflater().inflate(R.layout.account_textview, (LinearLayout) findViewById(R.id.domainlist), false);
            SpannableStringBuilder b = new SpannableStringBuilder();
            String subname = s.split(":")[0];
            SpannableStringBuilder subreddit = new SpannableStringBuilder(" /r/" + subname + " ");
            if ((SettingValues.colorSubName && Palette.getColor(subname) != Palette.getDefaultColor())) {
                subreddit.setSpan(new ForegroundColorSpan(Palette.getColor(subname)), 0, subreddit.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                subreddit.setSpan(new StyleSpan(Typeface.BOLD), 0, subreddit.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            b.append(subreddit).append(s.split(":")[1]);
            ((TextView) t.findViewById(R.id.name)).setText(b);
            t.findViewById(R.id.remove).setOnClickListener(v -> {
                SettingValues.flairFilters.remove(s);
                updateFilters();
            });
            ((LinearLayout) findViewById(R.id.flairlist)).addView(t);
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor e = SettingValues.prefs.edit();
        e.putStringSet(SettingValues.PREF_TITLE_FILTERS, SettingValues.titleFilters);
        e.putStringSet(SettingValues.PREF_DOMAIN_FILTERS, SettingValues.domainFilters);
        e.putStringSet(SettingValues.PREF_TEXT_FILTERS, SettingValues.textFilters);
        e.putStringSet(SettingValues.PREF_SUBREDDIT_FILTERS, SettingValues.subredditFilters);
        e.putStringSet(SettingValues.PREF_FLAIR_FILTERS, SettingValues.flairFilters);
        e.putStringSet(SettingValues.PREF_USER_FILTERS, SettingValues.userFilters);
        e.apply();
    }
}