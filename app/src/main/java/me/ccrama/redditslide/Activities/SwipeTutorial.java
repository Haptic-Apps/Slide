package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.widget.TextView;

import me.ccrama.redditslide.R;


/**
 * Created by ccrama on 3/5/2015.
 */

public class SwipeTutorial extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.swipe_tutorial);
        if (getIntent().hasExtra("subtitle")) {
            ((TextView) findViewById(R.id.subtitle)).setText(
                    getIntent().getStringExtra("subtitle"));
        }
    }
}