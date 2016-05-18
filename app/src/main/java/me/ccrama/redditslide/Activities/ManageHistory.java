package me.ccrama.redditslide.Activities;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.TypedValue;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.rey.material.app.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import me.ccrama.redditslide.Autocache.AutoCacheScheduler;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.CommentCacheAsync;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.util.NetworkUtil;


/**
 * Created by l3d00m on 11/13/2015.
 */
public class ManageHistory extends BaseActivityAnim {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_manage_history);
        setupAppBar(R.id.toolbar, R.string.manage_offline_content, true, true);
        if (!NetworkUtil.isConnected(this)) SettingsTheme.changed = true;
        findViewById(R.id.clear_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reddit.cachedData.edit().clear().apply();
                finish();
            }
        });
        if (NetworkUtil.isConnectedNoOverride(this)) {
            findViewById(R.id.sync_now).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new CommentCacheAsync(ManageHistory.this, Reddit.cachedData.getString("toCache", "").split(","), true).execute();
                }
            });
        } else {
            findViewById(R.id.sync_now).setVisibility(View.GONE);
        }
        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.wifi);

            single.setChecked(Reddit.cachedData.getBoolean("wifiOnly", false));
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Reddit.cachedData.edit().putBoolean("wifiOnly", isChecked).apply();
                }
            });
        }
        updateBackup();
        updateFilters();

        findViewById(R.id.autocache).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<String> sorted = UserSubscriptions.sort(UserSubscriptions.getSubscriptions(ManageHistory.this));
                final String[] all = new String[sorted.size()];
                boolean[] checked = new boolean[all.length];

                int i = 0;
                List<String> s2 = new ArrayList<>();
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
                new AlertDialogWrapper.Builder(ManageHistory.this).alwaysCallMultiChoiceCallback().setMultiChoiceItems(all, checked, new DialogInterface.OnMultiChoiceClickListener() {
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
                                Reddit.cachedData.edit().putString("toCache", Reddit.arrayToString(toCheck)).apply();
                                updateBackup();
                            }
                        }

                ).show();
            }


        });
        updateTime();
        findViewById(R.id.autocache_time_touch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final TimePickerDialog d = new TimePickerDialog(ManageHistory.this);
                d.hour(Reddit.cachedData.getInt("hour", 0));
                d.minute(Reddit.cachedData.getInt("minute", 0));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    d.applyStyle(new ColorPreferences(ManageHistory.this).getFontStyle().getBaseId());
                d.positiveAction("SET");
                TypedValue typedValue = new TypedValue();
                Resources.Theme theme = getTheme();
                theme.resolveAttribute(R.attr.activity_background, typedValue, true);
                int color = typedValue.data;
                d.backgroundColor(color);
                d.actionTextColor(getResources().getColor(new ColorPreferences(ManageHistory.this).getFontStyle().getColor()));
                d.positiveActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Reddit.cachedData.edit().putInt("hour", d.getHour()).putInt("minute", d.getMinute()).commit();
                        Reddit.autoCache = new AutoCacheScheduler(ManageHistory.this);
                        Reddit.autoCache.start(getApplicationContext());
                        updateTime();
                        d.dismiss();
                    }
                });
                theme.resolveAttribute(R.attr.font, typedValue, true);
                int color2 = typedValue.data;

                d.setTitle(getString(R.string.choose_sync_time));
                d.titleColor(color2);
                d.show();
            }
        });

    }


    public void updateTime() {
        TextView text = (TextView) findViewById(R.id.autocache_time);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, Reddit.cachedData.getInt("hour", 0));
        cal.set(Calendar.MINUTE, Reddit.cachedData.getInt("minute", 0));
        if (text != null) {
            text.setText("Backup will occur at " + new SimpleDateFormat("hh:mm a").format(cal.getTime()));
        }
    }

    public void updateBackup() {
        subsToBack = new ArrayList<>();
        Collections.addAll(subsToBack, Reddit.cachedData.getString("toCache", "").split(","));
        TextView text = (TextView) findViewById(R.id.autocache_text);
        if (!Reddit.cachedData.getString("toCache", "").contains(",") || subsToBack.isEmpty()) {
            text.setText("No subreddits will back up.");
        } else {
            String toSay = "";
            for (String s : subsToBack) {
                if (!s.isEmpty())
                    toSay = toSay + s + ", ";
            }
            toSay = toSay.substring(0, toSay.length() - 2);
            toSay += " will back up";
            text.setText(toSay);
        }
    }

    public ArrayList<String> domains = new ArrayList<>();
    List<String> subsToBack;

    public void updateFilters() {
        Map<String, String> multiNameToSubsMap = UserSubscriptions.getMultiNameToSubs(true);

        domains = new ArrayList<>();
        ((LinearLayout) findViewById(R.id.domainlist)).removeAllViews();
        for (final String s : OfflineSubreddit.getAll()) {
            if (!s.isEmpty()) {

                String[] split = s.split(",");
                String sub = split[0];
                if (multiNameToSubsMap.containsKey(sub)) {
                    sub = multiNameToSubsMap.get(sub);
                }
                final String name = (sub.contains("/m/") ? sub : "/r/" + sub) + " → " + (Long.valueOf(split[1]) == 0 ? "submission only" : TimeUtils.getTimeAgo(Long.valueOf(split[1]), ManageHistory.this) + " (comments)");
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