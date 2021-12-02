package me.ccrama.redditslide.util.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import androidx.annotation.StringRes;
import androidx.preference.PreferenceManager;

/**
 * Created by TacoTheDank on 05/12/2021.
 * <p>
 * When adding new settings, please adhere to the preference organization!
 */
public class PreferenceHelper {

    private static SharedPreferences sharedPrefs;
    private static Resources mRes;

    public static void initPreferences(final Context context) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mRes = context.getResources();
    }

    private static String getPrefKey(@StringRes final int prefKey) {
        return mRes.getString(prefKey);
    }

    private static boolean getHelperBoolean(@StringRes final int prefKey, final boolean defValue) {
        return sharedPrefs.getBoolean(getPrefKey(prefKey), defValue);
    }


    //////////////////////////////////////////////////
    // Multi-column settings
    //////////////////////////////////////////////////
    public static boolean singleColumnMultiWindow() {
        return getHelperBoolean(PrefKeys.PREF_SINGLE_COLUMN_MULTI_WINDOW, false);
    }

    public static boolean portraitModeDualColumns() {
        return getHelperBoolean(PrefKeys.PREF_PORTRAIT_MODE_DUAL_COLUMNS, false);
    }

    public static boolean forceFullCommentView() {
        return getHelperBoolean(PrefKeys.PREF_FORCE_FULL_COMMENT_VIEW, false);
    }
}
