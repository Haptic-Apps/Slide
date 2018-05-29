package me.ccrama.redditslide.ForceTouch.util;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import java.util.Locale;

/**
 * Gen
 */
public class NavigationUtils {
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId =
                context.getResources().getIdentifier("status_bar_height", "dimen", "android");

        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }

        return result;
    }

    public static int getNavBarHeight(Context context) {
        int result = 0;
        int resourceId =
                context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        } else if (hasNavBar(context)) {
            DensityUtils.toDp(context, 48);
        }

        return result;
    }

    public static boolean hasNavBar(Context context) {
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        boolean hasHomeKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME);

        if (hasBackKey && hasHomeKey) {
            if (Build.MANUFACTURER.toLowerCase(Locale.ENGLISH).contains("samsung")
                    && !Build.MODEL.toLowerCase(Locale.ENGLISH).contains("nexus")) {
                return false;
            }

            Resources resources = context.getResources();
            int id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
            if (id > 0) {
                return resources.getBoolean(id);
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
}
