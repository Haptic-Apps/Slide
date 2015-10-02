package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.Visuals.StyleView;

/**
 * Created by carlo_000 on 9/17/2015.
 */
public class LoadingData extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        getTheme().applyStyle(new ColorPreferences(this).getThemeSubreddit("ASDF"), true);

        ((Reddit)getApplication()).active = true;
        setContentView(R.layout.activity_slidetabs);
        StyleView.styleActivity(this);
        if(SubredditStorage.alphabeticalSubscriptions != null){
            ((Reddit) getApplication()).startMain();
        }

    }


}
