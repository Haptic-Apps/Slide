package me.ccrama.redditslide.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by ccrama on 9/28/2015.
 */
public class Slide extends Activity {

    public static boolean hasStarted;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        if (!hasStarted) {
            hasStarted = true;
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }
        finish();
    }
}
