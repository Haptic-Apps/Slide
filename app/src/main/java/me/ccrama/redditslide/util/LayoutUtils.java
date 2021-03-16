package me.ccrama.redditslide.util;

import android.graphics.Color;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

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
}
