package me.ccrama.redditslide.Views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.drawerlayout.widget.DrawerLayout;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.R;

/**
 * Drawer that allows for horizontal scrolling views.
 * <p/>
 * Required since if the drawer is on the right, swiping right would close
 * the drawer instead of scrolling horizontally.
 * <p/>
 * Only supports R.id.commentOverflow for now, but could be updated to support
 * any view.
 */
public class SidebarLayout extends DrawerLayout {
    private List<View> scrollableViews = new ArrayList<>();

    public SidebarLayout(Context context) {
        super(context);
    }

    public SidebarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SidebarLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void addScrollable(View view) {
        scrollableViews.add(view);
    }

    /**
     * Override to check if the pressed location corresponds to a scrollable
     * view.
     * <p/>
     * Since the sidebar is a ScrollView, the absolute event position is
     * the scroll y position + ev.getY(). The absolute position of the
     * horizontal scrolling views is the View.getHitRect position (relative
     * to the parent commentOverflow) + commentOverflow.getTop().
     * <p/>
     * See activity_overview.xml to get an idea of the view structure.
     *
     * @param ev
     * @return false if the event corresponds to a scrollable, super otherwise.
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        View sidebarScrollView = findViewById(R.id.sidebar_scroll);
        View commentOverflow = findViewById(R.id.commentOverflow);
        int yOffset = sidebarScrollView.getScrollY();
        for (View view : scrollableViews) {
            Rect rect = new Rect();
            view.getHitRect(rect);
            if (rect.contains((int) ev.getX(), (int) ev.getY() - commentOverflow.getTop() + yOffset)) {
                return false;
            }
        }
        try {
            return super.onInterceptTouchEvent(ev);
        } catch(Exception e){
            return false;
        }
    }
}
