package me.ccrama.redditslide.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;


/**
 * Created by l3d00m on 11/13/2015.
 */
public class SettingsFilter extends BaseActivity {


    EditText title;
    EditText text;
    EditText domain;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_filters);
        setupAppBar(R.id.toolbar, R.string.settings_title_filter, true, true);


        title = (EditText) findViewById(R.id.title);
        text = (EditText) findViewById(R.id.text);
        domain = (EditText) findViewById(R.id.domain);

        title.setText(SettingValues.titleFilters);
        text.setText(SettingValues.textFilters);
        domain.setText(SettingValues.domainFilters);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SettingValues.titleFilters = title.getText().toString();
        SettingValues.domainFilters = domain.getText().toString();
        SettingValues.textFilters = text.getText().toString();

        Reddit.titleFiltersRegex = Reddit.regex(SettingValues.titleFilters);
        Reddit.textFiltersRegex = Reddit.regex(SettingValues.textFilters);
        Reddit.domainFiltersRegex = Reddit.regex(SettingValues.domainFilters);

        SharedPreferences.Editor e = SettingValues.prefs.edit();

        e.putString(SettingValues.PREF_TITLE_FILTERS, SettingValues.titleFilters);
        e.putString(SettingValues.PREF_DOMAIN_FILTERS, SettingValues.domainFilters);
        e.putString(SettingValues.PREF_TEXT_FILTERS, SettingValues.textFilters);
        e.apply();

    }


}