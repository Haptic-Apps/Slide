package me.ccrama.redditslide.Activities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SubredditStorage;

/**
 * Created by ccrama on 9/17/2015.
 */
public class LoadingData extends ActionBarActivity {

    public TextView loading;
    private  boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        getTheme().applyStyle(new ColorPreferences(this).getThemeSubreddit("", true).getBaseId(), true);

        ((Reddit)getApplication()).active = true;
        ((Reddit)getApplication()).loader = this;
        setContentView(R.layout.activity_loading);
        if(SubredditStorage.alphabeticalSubscriptions != null && isNetworkAvailable()){
            ((Reddit) getApplication()).startMain();

        } else if(!isNetworkAvailable()) {
              AlertDialogWrapper.Builder b = new AlertDialogWrapper.Builder(this);
            b.setTitle(R.string.err_network_title)
            .setMessage(R.string.err_network_msg)
            .create().show();

                }
        loading  = (TextView) findViewById(R.id.loading);
        loading.setText(R.string.info_connecting);

    }


}
