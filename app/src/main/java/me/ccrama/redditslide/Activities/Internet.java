package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.view.View;

import me.ccrama.redditslide.R;

/**
 * Created by ccrama on 9/17/2015.
 */
public class Internet extends BaseActivity {


    @Override
    public void onCreate(Bundle savedInstance) {
        disableSwipeBackLayout();
        super.onCreate(savedInstance);
        applyColorTheme();

        setContentView(R.layout.activity_internet);


        findViewById(R.id.restart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }


}
