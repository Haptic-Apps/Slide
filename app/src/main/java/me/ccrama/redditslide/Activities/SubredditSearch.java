package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import me.ccrama.redditslide.Fragments.SubredditListView;
import me.ccrama.redditslide.R;

/**
 * Created by ccrama on 9/17/2015.
 */
public class SubredditSearch extends BaseActivityAnim {

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        String term = getIntent().getExtras().getString("term");
        applyColorTheme("");
        setContentView(R.layout.activity_fragmentinner);
        setupAppBar(R.id.toolbar, term, true, true);

        Fragment f = new SubredditListView();
        Bundle args = new Bundle();
        args.putString("id", term);
        f.setArguments(args);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction =
                fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentcontent, f);
        fragmentTransaction.commit();
    }


}
