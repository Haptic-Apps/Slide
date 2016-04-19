package me.ccrama.redditslide.Activities;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.UserSubscriptions;


/**
 * Created by l3d00m on 11/13/2015.
 */
public class ManageHistory extends BaseActivityAnim {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_manage_history);
        setupAppBar(R.id.toolbar, R.string.manage_offline_content, true, true);
        SettingsTheme.changed = true;
        findViewById(R.id.clear_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reddit.cachedData.edit().clear().apply();
                finish();
            }
        });

        updateBackup();
        updateFilters();

        findViewById(R.id.autocache).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialogWrapper.Builder(ManageHistory.this)
                        .setTitle("Auto-caching")
                        .setNeutralButton("Change sync time", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        Reddit.cachedData.edit().putInt("hour", hourOfDay).putInt("minute", minute).apply();
                                        updateBackup();
                                    }
                                };
                                new TimePickerDialog(ManageHistory.this, mTimeSetListener, Reddit.cachedData.getInt("hour", 0), Reddit.cachedData.getInt("minute", 0), false).show();
                            }
                        }).setPositiveButton("Manage subs to backup", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ArrayList<String> sorted = UserSubscriptions.sort(UserSubscriptions.getSubscriptions(ManageHistory.this));
                        final String[] all = new String[sorted.size()];
                        boolean[] checked = new boolean[all.length];

                        int i = 0;
                        ArrayList<String> s2 = new ArrayList<>();
                        Collections.addAll(s2, Reddit.cachedData.getString("toCache", "").split(","));

                        for (String s : sorted) {
                            all[i] = s;
                            if (s2.contains(s)) {
                                checked[i] = true;
                            }
                            i++;
                        }

                        final ArrayList<String> toCheck = new ArrayList<>();
                        toCheck.addAll(s2);
                        new AlertDialogWrapper.Builder(ManageHistory.this).setMultiChoiceItems(all, checked, new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                        if (!isChecked) {
                                            toCheck.remove(all[which]);
                                        } else {
                                            toCheck.add(all[which]);
                                        }
                                    }
                                }

                        ).setTitle(R.string.multireddit_selector).setPositiveButton(getString(R.string.btn_add).toUpperCase(), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Reddit.cachedData.edit().putString("toCache", Reddit.arrayToString(toCheck));
                                        updateBackup();
                                    }
                                }

                        ).show();
                    }

                });
            }

        });

    }

    public void updateBackup() {
        subsToBack = new ArrayList<>();
        Collections.addAll(subsToBack, Reddit.cachedData.getString("toCache", "").split(","));
        TextView text = (TextView) findViewById(R.id.autocache_text);
        if (subsToBack.isEmpty()) {
            text.setText("No subreddits will back up.");
        } else {
            String toSay = "";
            for (String s : subsToBack) {
                toSay = toSay + s + ", ";
            }
            toSay = toSay.substring(0, toSay.length() - 2);
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, Reddit.cachedData.getInt("hour", 0));
            cal.set(Calendar.MINUTE, Reddit.cachedData.getInt("minute", 0));
            toSay += " will be backed up at " + new SimpleDateFormat("hh:mm").format(cal.getTime());
            text.setText(toSay);
        }
    }

    public ArrayList<String> domains = new ArrayList<>();
    ArrayList<String> subsToBack;

    public void updateFilters() {
        domains = new ArrayList<>();

        ((LinearLayout) findViewById(R.id.domainlist)).removeAllViews();
        for (final String s : OfflineSubreddit.getAll()) {
            if (!s.isEmpty()) {

                String[] split = s.split(",");

                final String name = "/r/" + split[0] + " → " + (Long.valueOf(split[1]) == 0 ? "auto backup" : TimeUtils.getTimeAgo(Long.valueOf(split[1]), ManageHistory.this));
                domains.add(name);

                final View t = getLayoutInflater().inflate(R.layout.account_textview, ((LinearLayout) findViewById(R.id.domainlist)), false);

                ((TextView) t.findViewById(R.id.name)).setText(name);
                t.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        domains.remove(name);
                        Reddit.cachedData.edit().remove(s).apply();
                        updateFilters();
                    }
                });
                ((LinearLayout) findViewById(R.id.domainlist)).addView(t);

            }
        }
    }
}