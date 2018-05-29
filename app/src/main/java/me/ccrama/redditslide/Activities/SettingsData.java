package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.view.ViewGroup;

import me.ccrama.redditslide.Fragments.SettingsDataFragment;
import me.ccrama.redditslide.R;


/**
 * Created by ccrama on 3/5/2015.
 */
public class SettingsData extends BaseActivityAnim {

    private final SettingsDataFragment fragment = new SettingsDataFragment(this);

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_datasaving);
        setupAppBar(R.id.toolbar, R.string.settings_data, true, true);

        ((ViewGroup) findViewById(R.id.settings_datasaving)).addView(
                getLayoutInflater().inflate(R.layout.activity_settings_datasaving_child, null));

        fragment.Bind();
    }

}