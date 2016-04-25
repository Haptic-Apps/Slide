package me.ccrama.redditslide.Activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import me.ccrama.redditslide.BuildConfig;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;


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
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Stacktrace", stacktrace);
                        clipboard.setPrimaryClip(clip);
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
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Version", versionNumber);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(SettingsAbout.this, "Version number copied to clipboard", Toast.LENGTH_SHORT).show();

            }
        });

        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reddit.defaultShare("https://github.com/ccrama/Slide/issues", SettingsAbout.this);
            }
        });
        findViewById(R.id.changelogpost).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new OpenRedditLink(SettingsAbout.this, Reddit.appRestart.getString("url", ""));
            }
        });
        changelog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reddit.defaultShare("https://github.com/ccrama/Slide/blob/master/CHANGELOG.md", SettingsAbout.this);
            }
        });

        //fixme add libs to donottranslate.xml and comment this out
        libs.setVisibility(View.GONE);
        /*libs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsAbout.this);
                builder.setTitle("Libraries used")
                        .setItems(R.array.libs, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        String[] testArray = getResources().getStringArray(R.array.libs_links);
                                        String test = testArray[i];
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(test)));
                                    }
                                }
                        );
                builder.show();
            }
        });*/
    }


}
