package me.ccrama.redditslide.Activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;

/**
 * Created by ccrama on 9/17/2015.
 */
public class Crash extends AppCompatActivity {


    @Override
    public void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        getTheme().applyStyle(new ColorPreferences(this).getThemeSubreddit(""), true);

        setContentView(R.layout.activity_crash);

        final String stacktrace = getIntent().getExtras().getString("stacktrace", "");


        findViewById(R.id.report).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             /*old   LayoutInflater inflater = getLayoutInflater();
                final View dialogLayout = inflater.inflate(R.layout.sendissue, null);
                final AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(Crash.this);

                dialogLayout.findViewById(R.id.title).setBackgroundColor(Palette.getDefaultColor());
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
                            reportCrashURL = reportCrashURL.replace(Authentication.refresh, "[AUTHORIZATION TOKEN]");
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(reportCrashURL));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        startActivity(intent);
                    }
                });
                final Dialog dialog = builder.setView(dialogLayout).create();
                dialog.show();*/
            //    Intent i = new Intent(Crash.this, GitReporter.class);
             //   i.putExtra("stacktrace", stacktrace);
             //   startActivity(i);
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

        findViewById(R.id.restart).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Stacktrace", stacktrace);
                clipboard.setPrimaryClip(clip);
                return true;
            }
        });

    }


}
