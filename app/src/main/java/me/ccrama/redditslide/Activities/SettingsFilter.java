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
        setupAppBar(R.id.toolbar, "Filters", true, true);


        title = (EditText) findViewById(R.id.title);
        text = (EditText) findViewById(R.id.text);
        domain = (EditText) findViewById(R.id.domain);

        title.setText(Reddit.titleFilters);
        text.setText(Reddit.textFilters);
        domain.setText(Reddit.domainFilters);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Reddit.titleFilters = title.getText().toString();
        Reddit.domainFilters = domain.getText().toString();
        Reddit.textFilters = text.getText().toString();

        Reddit.titleFiltersRegex = Reddit.regex(Reddit.titleFilters);
        Reddit.textFiltersRegex = Reddit.regex(Reddit.textFilters);
        Reddit.domainFiltersRegex = Reddit.regex(Reddit.domainFilters);

        SharedPreferences.Editor e = SettingValues.prefs.edit();

        e.putString("titleFilters", Reddit.titleFilters);
        e.putString("domainFilters", Reddit.domainFilters);
        e.putString("textFilters", Reddit.textFilters);
        e.apply();

    }


}