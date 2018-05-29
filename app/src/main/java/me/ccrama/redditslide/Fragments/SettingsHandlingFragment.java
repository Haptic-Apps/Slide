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
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.common.collect.BiMap;

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
            context.findViewById(R.id.settings_handling_video)
                    .setOnClickListener(new View.OnClickListener() {
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

        setUpBrowserLinkHandling();

        /* activity_settings_handling_child.xml does not load these elements so we need to null check */
        if (context.findViewById(R.id.domain) != null
                & context.findViewById(R.id.domainlist) != null) {
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

    private void setUpBrowserLinkHandling() {
        ((RadioGroup) context.findViewById(R.id.settings_handling_select_browser_type)).check(
                SettingValues.web ? R.id.settings_handling_browser_type_internal_browser
                        : SettingValues.customtabs ? R.id.settings_handling_browser_type_custom_tabs
                                : SettingValues.reader
                                        ? R.id.settings_handling_browser_type_reader_mode
                                        : R.id.settings_handling_browser_type_external_browser);
        ((RadioGroup) context.findViewById(
                R.id.settings_handling_select_browser_type)).setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        SettingValues.web =
                                checkedId == R.id.settings_handling_browser_type_internal_browser;
                        SettingValues.customtabs =
                                checkedId == R.id.settings_handling_browser_type_custom_tabs;
                        SettingValues.reader =
                                checkedId == R.id.settings_handling_browser_type_reader_mode;

                        SettingValues.prefs.edit()
                                .putBoolean(SettingValues.PREFS_WEB, SettingValues.web)
                                .apply();
                        SettingValues.prefs.edit()
                                .putBoolean(SettingValues.PREF_CUSTOMTABS, SettingValues.customtabs)
                                .apply();
                        SettingValues.prefs.edit()
                                .putBoolean(SettingValues.PREF_READER, SettingValues.reader)
                                .apply();
                    }
                });

        final BiMap<String, String> installedBrowsers = Reddit.getInstalledBrowsers();
        if (!installedBrowsers.containsKey(SettingValues.selectedBrowser)) {
            SettingValues.selectedBrowser = "";
            SettingValues.prefs.edit()
                    .putString(SettingValues.PREF_SELECTED_BROWSER, SettingValues.selectedBrowser)
                    .apply();
        }
        ((TextView) context.findViewById(R.id.settings_handling_browser)).setText(
                installedBrowsers.get(SettingValues.selectedBrowser));
        if (installedBrowsers.size() <= 1) {
            context.findViewById(R.id.settings_handling_select_browser).setVisibility(View.GONE);
        } else {
            context.findViewById(R.id.settings_handling_select_browser).setVisibility(View.VISIBLE);
            context.findViewById(R.id.settings_handling_select_browser)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final PopupMenu popupMenu = new PopupMenu(context, v);
                            for (String name : installedBrowsers.values()) {
                                popupMenu.getMenu().add(name);
                            }
                            popupMenu.setOnMenuItemClickListener(
                                    new PopupMenu.OnMenuItemClickListener() {
                                        @Override
                                        public boolean onMenuItemClick(MenuItem item) {
                                            SettingValues.selectedBrowser =
                                                    installedBrowsers.inverse()
                                                            .get(item.getTitle());
                                            SettingValues.prefs.edit()
                                                    .putString(SettingValues.PREF_SELECTED_BROWSER,
                                                            SettingValues.selectedBrowser)
                                                    .apply();
                                            ((TextView) context.findViewById(
                                                    R.id.settings_handling_browser)).setText(
                                                    item.getTitle());
                                            return true;
                                        }
                                    });
                            popupMenu.show();
                        }
                    });
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
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

    private void updateFilters() {
        domains = new ArrayList<>();

        ((LinearLayout) context.findViewById(R.id.domainlist)).removeAllViews();
        for (String s : SettingValues.alwaysExternal.replaceAll("^[,\\s]+", "").split("[,\\s]+")) {
            if (!s.isEmpty() && (!Reddit.videoPlugin || (!s.contains("youtube.co") && !s.contains(
                    "youtu.be")))) {
                s = s.trim();
                final String finalS = s;
                domains.add(finalS);
                final View t = context.getLayoutInflater()
                        .inflate(R.layout.account_textview,
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
