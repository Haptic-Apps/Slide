package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SubredditStorage;

/**
 * Created by carlo_000 on 9/17/2015.
 */
public class LoadingData extends ActionBarActivity {

    public TextView loading;
    @Override
    public void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        getTheme().applyStyle(new ColorPreferences(this).getThemeSubreddit("", true).getBaseId(), true);

        ((Reddit)getApplication()).active = true;
        ((Reddit)getApplication()).loader = this;
        setContentView(R.layout.activity_loading);
        if(SubredditStorage.alphabeticalSubscriptions != null){
            ((Reddit) getApplication()).startMain();
        }
        loading  = (TextView) findViewById(R.id.loading);
        loading.setText("Connecting to Reddit");

    }


}
