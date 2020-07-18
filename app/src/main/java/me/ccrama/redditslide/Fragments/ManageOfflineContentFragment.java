package me.ccrama.redditslide.Fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.google.common.collect.ImmutableList;
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
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.util.NetworkUtil;

public class ManageOfflineContentFragment {

    private Activity context;

    public ManageOfflineContentFragment(Activity context) {
        this.context = context;
    }

    public void Bind() {
        if (!NetworkUtil.isConnected(context)) SettingsThemeFragment.changed = true;
        context.findViewById(R.id.manage_history_clear_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean wifi = Reddit.cachedData.getBoolean("wifiOnly", false);
                String sync = Reddit.cachedData.getString("toCache", "");
                int hour = (Reddit.cachedData.getInt("hour", 0));
                int minute = (Reddit.cachedData.getInt("minute", 0));
                Reddit.cachedData.edit().clear().apply();
                Reddit.cachedData.edit().putBoolean("wifiOnly", wifi).putString(
                        "toCache", sync).putInt("hour", hour).putInt("minute", minute).apply();
                context.finish();
            }
        });
        if (NetworkUtil.isConnectedNoOverride(context)) {
            context.findViewById(R.id.manage_history_sync_now).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new CommentCacheAsync(context, Reddit.cachedData.getString(
                            "toCache", "").split(",")).execute();
                }
            });
        } else {
            context.findViewById(R.id.manage_history_sync_now).setVisibility(View.GONE);
        }
        {
            SwitchCompat single = context.findViewById(R.id.manage_history_wifi);

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
        final List<String> commentDepths = ImmutableList.of("2", "4", "6", "8", "10");
        final String[] commentDepthArray = new String[commentDepths.size()];

        context.findViewById(R.id.manage_history_comments_depth).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String commentDepth = SettingValues.prefs.getString(
                        SettingValues.COMMENT_DEPTH, "2");
                AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(context);
                builder.setTitle(R.string.comments_depth);
                builder.setSingleChoiceItems(
                        commentDepths.toArray(commentDepthArray), commentDepths.indexOf(commentDepth), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SettingValues.prefs.edit().putString(
                                        SettingValues.COMMENT_DEPTH, commentDepths.get(which)).apply();
                            }
                        });
                builder.show();

            }
        });

        final List<String> commentCounts = ImmutableList.of("20", "40", "60", "80", "100");
        final String[] commentCountArray = new String[commentCounts.size()];

        context.findViewById(R.id.manage_history_comments_count).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String commentCount = SettingValues.prefs.getString(SettingValues.COMMENT_COUNT, "2");
                AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(context);
                builder.setTitle(R.string.comments_count);
                builder.setSingleChoiceItems(
                        commentCounts.toArray(commentCountArray), commentCounts.indexOf(commentCount), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SettingValues.prefs.edit().putString(SettingValues.COMMENT_COUNT, commentCounts.get(which)).apply();
                            }
                        });
                builder.show();

            }
        });

        context.findViewById(R.id.manage_history_autocache).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<String> sorted = UserSubscriptions.sort(
                        UserSubscriptions.getSubscriptions(context));
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

                final ArrayList<String> toCheck = new ArrayList<>(s2);
                new AlertDialogWrapper.Builder(context)
                        .alwaysCallMultiChoiceCallback()
                        .setMultiChoiceItems(all, checked, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                if (!isChecked) {
                                    toCheck.remove(all[which]);
                                } else {
                                    toCheck.add(all[which]);
                                }
                            }
                        }

                ).setTitle(R.string.multireddit_selector).setPositiveButton(
                        context.getString(R.string.btn_add).toUpperCase(), new DialogInterface.OnClickListener() {
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
        context.findViewById(R.id.manage_history_autocache_time_touch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final TimePickerDialog d = new TimePickerDialog(context);
                d.hour(Reddit.cachedData.getInt("hour", 0));
                d.minute(Reddit.cachedData.getInt("minute", 0));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    d.applyStyle(new ColorPreferences(context).getFontStyle().getBaseId());
                d.positiveAction("SET");
                TypedValue typedValue = new TypedValue();
                Resources.Theme theme = context.getTheme();
                theme.resolveAttribute(R.attr.activity_background, typedValue, true);
                int color = typedValue.data;
                d.backgroundColor(color);
                d.actionTextColor(context.getResources().getColor(
                        new ColorPreferences(context).getFontStyle().getColor()));
                d.positiveActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Reddit.cachedData.edit()
                                .putInt("hour", d.getHour())
                                .putInt("minute", d.getMinute())
                                .commit();
                        Reddit.autoCache = new AutoCacheScheduler(context);
                        Reddit.autoCache.start(context.getApplicationContext());
                        updateTime();
                        d.dismiss();
                    }
                });
                theme.resolveAttribute(R.attr.fontColor, typedValue, true);
                int color2 = typedValue.data;

                d.setTitle(context.getString(R.string.choose_sync_time));
                d.titleColor(color2);
                d.show();
            }
        });

    }

    public void updateTime() {
        TextView text = context.findViewById(R.id.manage_history_autocache_time);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, Reddit.cachedData.getInt("hour", 0));
        cal.set(Calendar.MINUTE, Reddit.cachedData.getInt("minute", 0));
        if (text != null) {
            text.setText(context.getString(
                    R.string.settings_backup_occurs, new SimpleDateFormat("hh:mm a").format(cal.getTime())));
        }
    }

    public void updateBackup() {
        subsToBack = new ArrayList<>();
        Collections.addAll(subsToBack, Reddit.cachedData.getString("toCache", "").split(","));
        TextView text = context.findViewById(R.id.manage_history_autocache_text);
        if (!Reddit.cachedData.getString("toCache", "").contains(",") || subsToBack.isEmpty()) {
            text.setText(R.string.settings_backup_none);
        } else {
            StringBuilder toSayBuilder = new StringBuilder();
            for (String s : subsToBack) {
                if (!s.isEmpty())
                    toSayBuilder.append(s).append(", ");
            }
            String toSay = toSayBuilder.toString();
            toSay = toSay.substring(0, toSay.length() - 2);
            toSay += context.getString(R.string.settings_backup_will_backup);
            text.setText(toSay);
        }
    }

    public ArrayList<String> domains = new ArrayList<>();
    List<String> subsToBack;

    public void updateFilters() {
        if (context.findViewById(R.id.manage_history_domainlist) != null) {
            Map<String, String> multiNameToSubsMap = UserSubscriptions.getMultiNameToSubs(true);

            domains = new ArrayList<>();
            ((LinearLayout) context.findViewById(R.id.manage_history_domainlist)).removeAllViews();
            for (final String s : OfflineSubreddit.getAll()) {
                if (!s.isEmpty()) {

                    String[] split = s.split(",");
                    String sub = split[0];
                    if (multiNameToSubsMap.containsKey(sub)) {
                        sub = multiNameToSubsMap.get(sub);
                    }
                    final String name = (sub.contains("/m/") ? sub : "/r/" + sub) + " → " + (Long.parseLong(split[1]) == 0 ? context.getString(R.string.settings_backup_submission_only) : TimeUtils.getTimeAgo(Long.parseLong(split[1]), context) + context.getString(R.string.settings_backup_comments));
                    domains.add(name);

                    final View t = context.getLayoutInflater().inflate(
                            R.layout.account_textview,
                            ((LinearLayout) context.findViewById(R.id.manage_history_domainlist)),
                            false);

                    ((TextView) t.findViewById(R.id.name)).setText(name);
                    t.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            domains.remove(name);
                            Reddit.cachedData.edit().remove(s).apply();
                            updateFilters();
                        }
                    });
                    ((LinearLayout) context.findViewById(R.id.manage_history_domainlist)).addView(t);

                }
            }
        }
    }

}
