package me.ccrama.redditslide.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.BuildConfig;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Visuals.Pallete;

/**
 * Created by ccrama on 9/17/2015.
 */
public class Crash extends ActionBarActivity {


    @Override
    public void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        getTheme().applyStyle(new ColorPreferences(this).getThemeSubreddit("", true).getBaseId(), true);

        setContentView(R.layout.activity_crash);

        final String stacktrace  = getIntent().getExtras().getString("stacktrace", "");


        findViewById(R.id.report).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();
                final View dialogLayout = inflater.inflate(R.layout.sendissue, null);
                final AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(Crash.this);

                dialogLayout.findViewById(R.id.title).setBackgroundColor(Pallete.getDefaultColor());
                dialogLayout.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String reportCrashURL = "https://github.com/ccrama/Slide/issues/new?title=" +
                                ((EditText) dialogLayout.findViewById(R.id.tite)).getText().toString() +
                                "&labels=auto%20reported%20crash&body=%23%23%20Info%0A" +
                                ((EditText) dialogLayout.findViewById(R.id.body)).getText().toString() +
                                "%0A%23%23%20Stacktrace%0A```%0A" + stacktrace +
                                "%0A```%0ASlide%20v" + BuildConfig.VERSION_NAME +
                                "%2c%20API%20v" + Build.VERSION.SDK_INT +
                                "%0AReported%20via%20Slide%20Crash%20Capture";

                        if (Authentication.refresh != null)
                            reportCrashURL = reportCrashURL.replace(Authentication.refresh, "[AUTOHRIZATION TOKEN]");
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(reportCrashURL));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        startActivity(intent);
                    }
                });
                final Dialog dialog = builder.setView(dialogLayout).create();
                dialog.show();
            }
        });

        findViewById(R.id.restart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(getBaseContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

    }


}
