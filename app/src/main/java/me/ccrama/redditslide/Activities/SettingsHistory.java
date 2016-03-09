package me.ccrama.redditslide.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;

public class SettingsHistory extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_history);

        SwitchCompat storeHistory = ((SwitchCompat) findViewById(R.id.storehistory));
        storeHistory.setChecked(SettingValues.storeHistory);
        storeHistory.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingValues.storeHistory = isChecked;
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_STORE_HISTORY, isChecked).apply();

                new AlertDialog.Builder(SettingsHistory.this)
                        .setTitle("History status")
                        .setMessage("storeHistory is now: " + Boolean.toString(SettingValues.storeHistory))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

                if (isChecked) {
                    findViewById(R.id.scrollseen).setEnabled(true);
                    findViewById(R.id.storensfw).setEnabled(true);
                }
                else {
                    ((SwitchCompat) findViewById(R.id.storensfw)).setChecked(false);
                    ((SwitchCompat) findViewById(R.id.storensfw)).setEnabled(false);
                    SettingValues.storeNSFWHistory = false;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_STORE_NSFW_HISTORY, false).apply();

                    ((SwitchCompat) findViewById(R.id.scrollseen)).setChecked(false);
                    ((SwitchCompat) findViewById(R.id.scrollseen)).setEnabled(false);
                    SettingValues.scrollSeen = false;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_SCROLL_SEEN, false).apply();
                }
            }
        });

        ((SwitchCompat)findViewById(R.id.storensfw)).setChecked(SettingValues.storeNSFWHistory);

        SwitchCompat single = (SwitchCompat) findViewById(R.id.scrollseen);

        single.setChecked(SettingValues.scrollSeen);
        single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingValues.scrollSeen = isChecked;
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_SCROLL_SEEN, isChecked).apply();

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
