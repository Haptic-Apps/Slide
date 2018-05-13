package me.ccrama.redditslide.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SwitchCompat;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;


public class SettingsHandlingFragment implements CompoundButton.OnCheckedChangeListener {

    private Activity context;

    public ArrayList<String> domains = new ArrayList<>();
    EditText domain;

    public SettingsHandlingFragment(Activity context) {
        this.context = context;
    }

    public void Bind() {
        TextView web = context.findViewById(R.id.settings_handling_browser);

        //todo web stuff
        SwitchCompat image = context.findViewById(R.id.settings_handling_image);
        SwitchCompat gif = context.findViewById(R.id.settings_handling_gif);
        SwitchCompat album = context.findViewById(R.id.settings_handling_album);
        SwitchCompat peek = context.findViewById(R.id.settings_handling_peek);
        SwitchCompat shortlink = context.findViewById(R.id.settings_handling_shortlink);

        image.setChecked(SettingValues.image);
        gif.setChecked(SettingValues.gif);
        album.setChecked(SettingValues.album);
        peek.setChecked(SettingValues.peek);
        shortlink.setChecked(!SettingValues.shareLongLink);

        image.setOnCheckedChangeListener(this);
        gif.setOnCheckedChangeListener(this);
        album.setOnCheckedChangeListener(this);
        peek.setOnCheckedChangeListener(this);
        shortlink.setOnCheckedChangeListener(this);

        if (!Reddit.videoPlugin) {
            context.findViewById(R.id.settings_handling_video).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                                "market://details?id=" + context.getString(
                                        R.string.youtube_plugin_package))));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                                "http://play.google.com/store/apps/details?id=ccrama.me.slideyoutubeplugin")));
                    }
                }
            });
        } else {
            context.findViewById(R.id.settings_handling_video).setVisibility(View.GONE);
        }
        ((TextView) context.findViewById(R.id.settings_handling_browser)).setText(SettingValues.firefox ? context.getString(R.string.firefox) :
                SettingValues.web ? SettingValues.reader ? context.getString(
                        R.string.handling_reader_mode)
                        : (SettingValues.customtabs ? context.getString(
                                R.string.settings_link_chrome)
                                : context.getString(R.string.handling_internal_browser))
                        : context.getString(R.string.handling_external_browser));


        final SwitchCompat readernight = context.findViewById(R.id.settings_handling_readernight);
        readernight.setEnabled(
                SettingValues.nightMode && SettingValues.web && SettingValues.reader);
        readernight.setChecked(SettingValues.readerNight);
        readernight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingValues.readerNight = isChecked;
                SettingValues.prefs.edit()
                        .putBoolean(SettingValues.PREF_READER_NIGHT, isChecked)
                        .apply();
            }
        });
        context.findViewById(R.id.settings_handling_select_browser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(context, v);

                if(Reddit.firefox) {
                    popup.getMenuInflater().inflate(R.menu.browser_type_firefox, popup.getMenu());
                } else {
                    popup.getMenuInflater().inflate(R.menu.browser_type, popup.getMenu());
                }

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.firefox:
                                SettingValues.customtabs = false;
                                SettingValues.web = true;
                                SettingValues.reader = false;
                                SettingValues.firefox = true;
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREFS_WEB, true)
                                        .apply();
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREF_READER, false)
                                        .apply();
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREF_CUSTOMTABS, false)
                                        .apply();
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREF_FIREFOX, true)
                                        .apply();
                                break;

                            case R.id.chrome:
                                SettingValues.customtabs = true;
                                SettingValues.web = true;
                                SettingValues.reader = false;
                                SettingValues.firefox = false;
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREFS_WEB, true)
                                        .apply();
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREF_READER, false)
                                        .apply();
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREF_CUSTOMTABS, true)
                                        .apply();
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREF_FIREFOX, false)
                                        .apply();
                                break;
                            case R.id.internal:
                                SettingValues.customtabs = false;
                                SettingValues.web = true;
                                SettingValues.reader = false;
                                SettingValues.firefox = false;
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREFS_WEB, true)
                                        .apply();
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREF_READER, false)
                                        .apply();
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREF_CUSTOMTABS, false)
                                        .apply();
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREF_FIREFOX, false)
                                        .apply();
                                break;
                            case R.id.reader:
                                SettingValues.customtabs = false;
                                SettingValues.web = true;
                                SettingValues.reader = true;
                                SettingValues.firefox = false;
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREFS_WEB, true)
                                        .apply();
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREF_READER, true)
                                        .apply();
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREF_CUSTOMTABS, false)
                                        .apply();
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREF_FIREFOX, false)
                                        .apply();
                                break;
                            case R.id.external:
                                SettingValues.web = false;
                                SettingValues.reader = false;
                                SettingValues.firefox = false;
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREFS_WEB, false)
                                        .apply();
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREF_READER, false)
                                        .apply();
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREF_FIREFOX, false)
                                        .apply();
                                break;
                        }
                        ((TextView) context.findViewById(R.id.settings_handling_browser)).setText(SettingValues.firefox ? context.getString(R.string.firefox) :
                                SettingValues.web ? SettingValues.reader ? context.getString(
                                        R.string.handling_reader_mode)
                                        : (SettingValues.customtabs ? context.getString(
                                                R.string.settings_link_chrome)
                                                : context.getString(R.string.handling_internal_browser))
                                        : context.getString(R.string.handling_external_browser));
                        readernight.setEnabled(SettingValues.nightMode
                                && SettingValues.web
                                && SettingValues.reader);

                        return true;
                    }
                });
                popup.show();
            }
        });

        /* activity_settings_handling_child.xml does not load these elements so we need to null check */
        if (context.findViewById(R.id.domain) != null & context.findViewById(R.id.domainlist) != null) {
            domain = context.findViewById(R.id.domain);
            domain.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        SettingValues.alwaysExternal =
                                SettingValues.alwaysExternal + ", " + domain.getText().toString();
                        domain.setText("");
                        updateFilters();
                    }
                    return false;
                }
            });
            updateFilters();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.web:
                SettingValues.web = isChecked;
                (((SwitchCompat) context.findViewById(R.id.chrome))).setEnabled(isChecked);
                SettingValues.prefs.edit().putBoolean(SettingValues.PREFS_WEB, isChecked).apply();
                break;
            case R.id.settings_handling_image:
                SettingValues.image = isChecked;
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_IMAGE, isChecked).apply();
                break;
            case R.id.settings_handling_gif:
                SettingValues.gif = isChecked;
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_GIF, isChecked).apply();
                break;
            case R.id.settings_handling_album:
                SettingValues.album = isChecked;
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_ALBUM, isChecked).apply();
                break;
            case R.id.settings_handling_shortlink:
                SettingValues.shareLongLink = !isChecked;
                SettingValues.prefs.edit()
                        .putBoolean(SettingValues.PREF_LONG_LINK, !isChecked)
                        .apply();
                break;
            case R.id.settings_handling_peek:
                SettingValues.peek = isChecked;
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_PEEK, isChecked).apply();
                break;
        }

    }

    public void updateFilters() {
        domains = new ArrayList<>();

        ((LinearLayout) context.findViewById(R.id.domainlist)).removeAllViews();
        for (String s : SettingValues.alwaysExternal.replaceAll("^[,\\s]+", "").split("[,\\s]+")) {
            if (!s.isEmpty() && (!Reddit.videoPlugin || (!s.contains("youtube.co") && !s.contains(
                    "youtu.be")))) {
                s = s.trim();
                final String finalS = s;
                domains.add(finalS);
                final View t = context.getLayoutInflater().inflate(R.layout.account_textview,
                        ((LinearLayout) context.findViewById(R.id.domainlist)), false);

                ((TextView) t.findViewById(R.id.name)).setText(s);
                t.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        domains.remove(finalS);
                        SettingValues.alwaysExternal = Reddit.arrayToString(domains);
                        updateFilters();
                    }
                });
                ((LinearLayout) context.findViewById(R.id.domainlist)).addView(t);

            }
        }
    }

}
