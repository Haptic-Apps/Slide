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

import me.ccrama.redditslide.SettingValues;

public class UpgradeUtil {
    // Increment for each needed change
    private static final int VERSION = 1;

    private UpgradeUtil() {
    }

    /**
     * Runs any upgrade actions required between versions in an organised way
     */
    public static void upgrade(Context context) {
        // Exit if this is the first start
        SharedPreferences colors = context.getSharedPreferences("COLOR", 0);
        if (colors != null && !colors.contains("Tutorial")) return;

        SharedPreferences upgradePrefs = context.getSharedPreferences("upgradeUtil", 0);
        final int CURRENT = upgradePrefs.getInt("VERSION", 0);

        // Exit if we're up to date
        if (CURRENT == VERSION) return;

        if (CURRENT < 1) {
            SharedPreferences prefs = context.getSharedPreferences("SETTINGS", 0);
            String domains = prefs.getString(SettingValues.PREF_ALWAYS_EXTERNAL, "");

            domains = domains.replaceFirst("(?<=^|,)youtube.co(?=$|,)", "youtube.com")
                    .replaceFirst("(?<=^|,)play.google.co(?=$|,)", "play.google.com");

            prefs.edit().putString(SettingValues.PREF_ALWAYS_EXTERNAL, domains).apply();
        }

        upgradePrefs.edit().putInt("VERSION", VERSION).apply();
    }
}
