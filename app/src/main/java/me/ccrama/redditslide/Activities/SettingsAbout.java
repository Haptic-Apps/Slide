package me.ccrama.redditslide.Activities;

import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import me.ccrama.redditslide.BuildConfig;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Pallete;


/**
 * Created by l3d00m on 11/12/2015.
 */
public class SettingsAbout extends BaseActivityNoAnim {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);
        setContentView(R.layout.activity_settings_about);
        final Toolbar b = (Toolbar) findViewById(R.id.toolbar);
        b.setBackgroundColor(Pallete.getDefaultColor());
        setSupportActionBar(b);
        getSupportActionBar().setTitle(R.string.settings_title_about);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Pallete.getDarkerColor(Pallete.getDefaultColor()));
            window.setNavigationBarColor(Pallete.getDarkerColor(Pallete.getDefaultColor()));
            SettingsAbout.this.setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.settings_title_about),
                    ((BitmapDrawable) ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_launcher)).getBitmap(), Pallete.getDefaultColor()));
        }

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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

}