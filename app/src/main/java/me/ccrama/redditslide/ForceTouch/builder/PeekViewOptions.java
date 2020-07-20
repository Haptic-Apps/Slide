package me.ccrama.redditslide.ForceTouch.builder;

import android.graphics.Color;

import androidx.annotation.FloatRange;

public class PeekViewOptions {

    @FloatRange(from=.1,to=.9)
    private float widthPercent = .6f;

    @FloatRange(from=.1,to=.9)
    private float heightPercent = .5f;

    // Values should be in DP
    private int absoluteWidth = 0;
    private int absoluteHeight = 0;

    // 0.0 = fully transparent background dim
    // 1.0 = fully opaque (black) background dim
    @FloatRange(from=0,to=1)
    private float backgroundDim = .6f;

    private boolean useFadeAnimation = true;
    private boolean hapticFeedback = true;
    private boolean fullScreenPeek = false;

    private boolean blurBackground = true;
    private int blurOverlayColor = Color.parseColor("#99000000");

    // region setters
    public PeekViewOptions setWidthPercent(@FloatRange(from=.1,to=.9) float widthPercent) {
        this.widthPercent = widthPercent;
        return this;
    }

    public PeekViewOptions setAbsoluteWidth(int width) {
        this.absoluteWidth = width;
        return this;
    }

    public PeekViewOptions setAbsoluteHeight(int height) {
        this.absoluteHeight = height;
        return this;
    }

    public PeekViewOptions setHeightPercent(@FloatRange(from=.1,to=.9) float heightPercent) {
        this.heightPercent = heightPercent;
        return this;
    }

    public PeekViewOptions setBackgroundDim(@FloatRange(from=0,to=1) float backgroundDim) {
        this.backgroundDim = backgroundDim;
        return this;
    }

    public PeekViewOptions setHapticFeedback(boolean useFeedback) {
        this.hapticFeedback = useFeedback;
        return this;
    }

    public PeekViewOptions setUseFadeAnimation(boolean useFadeAnimation) {
        this.useFadeAnimation = useFadeAnimation;
        return this;
    }

    public PeekViewOptions setFullScreenPeek(boolean fullScreenPeek) {
        this.fullScreenPeek = fullScreenPeek;
        return this;
    }

    public PeekViewOptions setBlurBackground(boolean blur) {
        this.blurBackground = blur;
        return this;
    }

    public PeekViewOptions setBlurOverlayColor(int color) {
        this.blurOverlayColor = color;
        return this;
    }
    //endregion

    // region getters
    public float getWidthPercent() {
        return widthPercent;
    }

    public float getHeightPercent() {
        return heightPercent;
    }

    public int getAbsoluteWidth() {
        return absoluteWidth;
    }

    public int getAbsoluteHeight() {
        return absoluteHeight;
    }

    public float getBackgroundDim() {
        return backgroundDim;
    }

    public boolean getHapticFeedback() {
        return hapticFeedback;
    }

    public boolean useFadeAnimation() {
        return useFadeAnimation;
    }

    public boolean fullScreenPeek() {
        return fullScreenPeek;
    }

    public boolean shouldBlurBackground() {
        return blurBackground;
    }

    public int getBlurOverlayColor() {
        return blurOverlayColor;
    }
    // endregion
}
