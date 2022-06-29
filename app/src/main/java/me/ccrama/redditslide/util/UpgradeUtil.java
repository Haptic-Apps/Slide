/*
 * Copyright (c) 2016. ccrama
 *
 * Slide is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.ccrama.redditslide.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import me.ccrama.redditslide.SettingValues;

public class UpgradeUtil {
    // Increment for each needed change
    private static final int VERSION = 2;

    private UpgradeUtil() {
    }

    /**
     * Runs any upgrade actions required between versions in an organised way
     */
    public static void upgrade(Context context) {
        SharedPreferences colors = context.getSharedPreferences("COLOR", 0);
        SharedPreferences upgradePrefs = context.getSharedPreferences("upgradeUtil", 0);

        // Exit if this is the first start
        if (colors != null && !colors.contains("Tutorial")) {
            upgradePrefs.edit().putInt("VERSION", VERSION).apply();
            return;
        }

        final int CURRENT = upgradePrefs.getInt("VERSION", 0);

        // Exit if we're up to date
        if (CURRENT == VERSION) return;

        if (CURRENT < 1) {
            SharedPreferences prefs = context.getSharedPreferences("SETTINGS", 0);
            String domains = prefs.getString(SettingValues.PREF_ALWAYS_EXTERNAL, "");

            domains = domains
                    .replaceFirst("(?<=^|,)youtube.co(?=$|,)", "youtube.com")
                    .replaceFirst("(?<=^|,)play.google.co(?=$|,)", "play.google.com");

            prefs.edit().putString(SettingValues.PREF_ALWAYS_EXTERNAL, domains).apply();
        }

        // migrate old filters
        if (CURRENT < 2) {
            SharedPreferences prefs = context.getSharedPreferences("SETTINGS", 0);
            SharedPreferences.Editor prefsEditor = prefs.edit();
            String titleFilterStr = prefs.getString(SettingValues.PREF_TITLE_FILTERS, "");
            String textFilterStr = prefs.getString(SettingValues.PREF_TEXT_FILTERS, "");
            String flairFilterStr = prefs.getString(SettingValues.PREF_FLAIR_FILTERS, "");
            String subredditFilterStr = prefs.getString(SettingValues.PREF_SUBREDDIT_FILTERS, "");
            String domainFilterStr = prefs.getString(SettingValues.PREF_DOMAIN_FILTERS, "");
            String usersFilterStr = prefs.getString(SettingValues.PREF_USER_FILTERS, "");
            String alwaysExternalStr = prefs.getString(SettingValues.PREF_ALWAYS_EXTERNAL, "");

            prefsEditor.remove(SettingValues.PREF_TITLE_FILTERS);
            prefsEditor.remove(SettingValues.PREF_TEXT_FILTERS);
            prefsEditor.remove(SettingValues.PREF_FLAIR_FILTERS);
            prefsEditor.remove(SettingValues.PREF_SUBREDDIT_FILTERS);
            prefsEditor.remove(SettingValues.PREF_DOMAIN_FILTERS);
            prefsEditor.remove(SettingValues.PREF_USER_FILTERS);
            prefsEditor.remove(SettingValues.PREF_ALWAYS_EXTERNAL);

            Set<String> titleFilters = titleFilterStr.isEmpty() ? new HashSet<>() :
                    new HashSet<>(Arrays.asList(titleFilterStr.replaceAll("^[,\\s]+", "")
                            .toLowerCase(Locale.ENGLISH).split("[,\\s]+")));

            Set<String> textFilters = textFilterStr.isEmpty() ? new HashSet<>() :
                    new HashSet<>(Arrays.asList(textFilterStr.replaceAll("^[,\\s]+", "")
                            .toLowerCase(Locale.ENGLISH).split("[,\\s]+")));

            Set<String> flairFilters = flairFilterStr.isEmpty() ? new HashSet<>() :
                    new HashSet<>(Arrays.asList(flairFilterStr.replaceAll("^[,]+", "")
                            .toLowerCase(Locale.ENGLISH).split("[,]+")));
            // verify flairs filters are valid
            HashSet<String> invalid = new HashSet<>();
            for (String s : flairFilters) {
                if (!s.contains(":")) {
                    invalid.add(s);
                }
            }
            flairFilters.removeAll(invalid);

            Set<String> subredditFilters = subredditFilterStr.isEmpty() ? new HashSet<>() :
                    new HashSet<>(Arrays.asList(subredditFilterStr.replaceAll("^[,\\s]+", "")
                            .toLowerCase(Locale.ENGLISH).split("[,\\s]+")));

            Set<String> domainFilters = domainFilterStr.isEmpty() ? new HashSet<>() :
                    new HashSet<>(Arrays.asList(domainFilterStr.replaceAll("^[,\\s]+", "")
                            .toLowerCase(Locale.ENGLISH).split("[,\\s]+")));

            Set<String> usersFilters = usersFilterStr.isEmpty() ? new HashSet<>() :
                    new HashSet<>(Arrays.asList(usersFilterStr.replaceAll("^[,\\s]+", "")
                            .toLowerCase(Locale.ENGLISH).split("[,\\s]+")));

            Set<String> alwaysExternal = alwaysExternalStr.isEmpty() ? new HashSet<>() :
                    new HashSet<>(Arrays.asList(alwaysExternalStr.replaceAll("^[,\\s]+", "")
                            .toLowerCase(Locale.ENGLISH).split("[,\\s]+")));

            prefsEditor.putStringSet(SettingValues.PREF_TITLE_FILTERS, titleFilters);
            prefsEditor.putStringSet(SettingValues.PREF_TEXT_FILTERS, textFilters);
            prefsEditor.putStringSet(SettingValues.PREF_FLAIR_FILTERS, flairFilters);
            prefsEditor.putStringSet(SettingValues.PREF_SUBREDDIT_FILTERS, subredditFilters);
            prefsEditor.putStringSet(SettingValues.PREF_DOMAIN_FILTERS, domainFilters);
            prefsEditor.putStringSet(SettingValues.PREF_USER_FILTERS, usersFilters);
            prefsEditor.putStringSet(SettingValues.PREF_ALWAYS_EXTERNAL, alwaysExternal);

            prefsEditor.apply();
        }

        upgradePrefs.edit().putInt("VERSION", VERSION).apply();
    }
}
