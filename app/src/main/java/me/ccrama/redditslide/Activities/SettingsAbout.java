package me.ccrama.redditslide.Activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import me.ccrama.redditslide.BuildConfig;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.util.LinkUtil;


/**
 * Created by l3d00m on 11/12/2015.
 */
public class SettingsAbout extends BaseActivityAnim {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_about);
        setupAppBar(R.id.toolbar, R.string.settings_title_about, true, true);

        View report = findViewById(R.id.report);
        View libs = findViewById(R.id.libs);
        View changelog = findViewById(R.id.changelog);
        final TextView version = (TextView) findViewById(R.id.version);

        version.setText("Slide v" + BuildConfig.VERSION_NAME);

        //Copy the latest stacktrace with a long click on the version number
        if (BuildConfig.DEBUG) {
            version.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    SharedPreferences prefs = getSharedPreferences(
                            "STACKTRACE", Context.MODE_PRIVATE);
                    String stacktrace = prefs.getString("stacktrace", null);
                    if (stacktrace != null) {
                        ClipboardManager clipboard = ContextCompat.getSystemService(SettingsAbout.this, ClipboardManager.class);
                        ClipData clip = ClipData.newPlainText("Stacktrace", stacktrace);
                        if (clipboard != null) {
                            clipboard.setPrimaryClip(clip);
                        }
                    }
                    prefs.edit().clear().apply();
                    return true;
                }
            });

        }
        version.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String versionNumber = version.getText().toString();
                ClipboardManager clipboard = ContextCompat.getSystemService(SettingsAbout.this, ClipboardManager.class);
                ClipData clip = ClipData.newPlainText("Version", versionNumber);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                }
                Toast.makeText(SettingsAbout.this, R.string.settings_about_version_copied_toast, Toast.LENGTH_SHORT).show();

            }
        });

        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinkUtil.openExternally("https://github.com/ccrama/Slide/issues");
            }
        });
        findViewById(R.id.sub).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new OpenRedditLink(SettingsAbout.this, "https://reddit.com/r/slideforreddit");
            }
        });
        findViewById(R.id.rate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=me.ccrama.redditslide")));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=me.ccrama.redditslide")));
                }
            }
        });
        changelog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinkUtil.openExternally("https://github.com/ccrama/Slide/blob/master/CHANGELOG.md");
            }
        });

        libs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SettingsAbout.this, SettingsLibs.class);
                startActivity(i);
            }
        });
    }


}
