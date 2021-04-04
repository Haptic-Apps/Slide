package me.ccrama.redditslide.util;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;

/**
 * Created by TacoTheDank on 12/31/2020.
 */
public class LayoutUtils {

    /**
     * Method to scroll the TabLayout to a specific index
     *
     * @param tabLayout   the tab layout
     * @param tabPosition index to scroll to
     */
    public static void scrollToTabAfterLayout(final TabLayout tabLayout, final int tabPosition) {
        //from http://stackoverflow.com/a/34780589/3697225
        if (tabLayout != null) {
            final ViewTreeObserver observer = tabLayout.getViewTreeObserver();

            if (observer.isAlive()) {
                observer.dispatchOnGlobalLayout(); // In case a previous call is waiting when this call is made
                observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        observer.removeOnGlobalLayoutListener(this);
                        tabLayout.getTabAt(tabPosition).select();
                    }
                });
            }
        }
    }

    public static void showSnackbar(final Snackbar s) {
        final View view = s.getView();
        final TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        s.show();
    }

    // Should this go here in this class??? I don't think it should but idk where else to put it
    public static int getNumColumns(final int orientation, final Activity activity) {
        final int numColumns;
        boolean singleColumnMultiWindow = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            singleColumnMultiWindow = activity.isInMultiWindowMode() && SettingValues.singleColumnMultiWindow;
        }
        if (orientation == Configuration.ORIENTATION_LANDSCAPE && SettingValues.isPro && !singleColumnMultiWindow) {
            numColumns = Reddit.dpWidth;
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT && SettingValues.dualPortrait) {
            numColumns = 2;
        } else {
            numColumns = 1;
        }
        return numColumns;
    }
}
