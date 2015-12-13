package me.ccrama.redditslide.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import me.ccrama.redditslide.BuildConfig;
import me.ccrama.redditslide.R;


/**
 * Created by l3d00m on 11/12/2015.
 */
public class SettingsAbout extends BaseActivity {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_about);
        setupAppBar(R.id.toolbar, R.string.settings_title_about, true);

        View report = findViewById(R.id.report);
        View libs = findViewById(R.id.libs);
        View changelog = findViewById(R.id.changelog);
        TextView version = (TextView) findViewById(R.id.version);

        version.setText("Slide v" + BuildConfig.VERSION_NAME);
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ccrama/Slide/issues")));
            }
        });

        changelog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ccrama/Slide/blob/master/History.md")));
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