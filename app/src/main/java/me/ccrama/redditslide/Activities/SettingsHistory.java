package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.view.ViewGroup;

import me.ccrama.redditslide.Fragments.SettingsHistoryFragment;
import me.ccrama.redditslide.R;

public class SettingsHistory extends BaseActivityAnim {

    private final SettingsHistoryFragment fragment = new SettingsHistoryFragment(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_history);
        setupAppBar(R.id.toolbar, R.string.settings_title_history, true, true);

        ((ViewGroup) findViewById(R.id.settings_history)).addView(
                getLayoutInflater().inflate(R.layout.activity_settings_history_child, null));

        fragment.Bind();
    }

}
