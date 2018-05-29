package me.ccrama.redditslide.Views;

import android.view.View;
import android.webkit.WebView;

import me.everything.android.ui.overscroll.adapters.IOverScrollDecoratorAdapter;

/**
 * Created by Carlos on 8/19/2016.
 */
public class WebViewOverScrollDecoratorAdapter implements IOverScrollDecoratorAdapter {

    protected final WebView mView;

    public WebViewOverScrollDecoratorAdapter(WebView view) {
        mView = view;
    }

    @Override
    public View getView() {
        return mView;
    }

    @Override
    public boolean isInAbsoluteStart() {
        return !mView.canScrollHorizontally(-1);
    }

    @Override
    public boolean isInAbsoluteEnd() {
        return !mView.canScrollHorizontally(1);
    }

}
