package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.view.ViewGroup;

import me.ccrama.redditslide.Fragments.ManageOfflineContentFragment;
import me.ccrama.redditslide.R;


/**
 * Created by l3d00m on 11/13/2015.
 */
public class ManageOfflineContent extends BaseActivityAnim {

    final ManageOfflineContentFragment fragment = new ManageOfflineContentFragment(this);

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_manage_history);
        setupAppBar(R.id.toolbar, R.string.manage_offline_content, true, true);

        ((ViewGroup) findViewById(R.id.manage_history)).addView(
                getLayoutInflater().inflate(R.layout.activity_manage_history_child, null));

        fragment.Bind();
    }

}