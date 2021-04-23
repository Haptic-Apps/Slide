package me.ccrama.redditslide.util;

import android.content.Context;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import me.ccrama.redditslide.R;

/**
 * Created by TacoTheDank on 04/04/2021.
 */
public class ProUtil {
    public static AlertDialog.Builder proUpgradeMsg(final Context context, final @StringRes int titleId) {
        return new AlertDialog.Builder(context)
                .setTitle(titleId)
                .setMessage(R.string.pro_upgrade_msg)
                .setPositiveButton(R.string.btn_yes_exclaim, (dialog, whichButton) ->
                        LinkUtil.launchMarketUri(context, R.string.ui_unlock_package)
                );
    }
}
