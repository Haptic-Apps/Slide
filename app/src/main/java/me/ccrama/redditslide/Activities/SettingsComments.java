package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.view.ViewGroup;

import me.ccrama.redditslide.Fragments.SettingsCommentsFragment;
import me.ccrama.redditslide.R;

public class SettingsComments extends BaseActivityAnim {

    private final SettingsCommentsFragment fragment = new SettingsCommentsFragment(this);

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_comments);
        setupAppBar(R.id.toolbar, R.string.settings_title_comments, true, true);

        ((ViewGroup) findViewById(R.id.settings_comments)).addView(
                getLayoutInflater().inflate(R.layout.activity_settings_comments_child, null));

        fragment.Bind();
    }

}