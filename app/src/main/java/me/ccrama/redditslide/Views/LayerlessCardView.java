package me.ccrama.redditslide.Views;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;

/**
 * Created by seizonsenryaku on 13/06/16.
 */
public class LayerlessCardView extends CardView {

    public LayerlessCardView(Context context) {
        super(context);
        removeLayer();
    }


    public LayerlessCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        removeLayer();
    }

    public LayerlessCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        removeLayer();
    }

    void removeLayer() {
        setLayerType(LAYER_TYPE_NONE, null);
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }
}
