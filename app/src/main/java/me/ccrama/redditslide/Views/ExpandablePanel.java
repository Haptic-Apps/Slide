package me.ccrama.redditslide.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;

import me.ccrama.redditslide.R;

public class ExpandablePanel extends RelativeLayout {

    private static final int DEFAULT_ANIM_DURATION = 500;

    private final int mHandleId;
    private final int mContentContainerId;
    private final int mContentId;

    private View mHandle;
    private View mContentContainer;
    private View mContent;

    private boolean mExpanded  = false;
    private boolean mFirstOpen = true;

    private int mCollapsedHeight;
    private int mContentHeight;
    private int mContentWidth;
    private int mAnimationDuration = 0;

    private OnExpandListener mListener;

    public ExpandablePanel(Context context) {
        this(context, null);
    }

    public ExpandablePanel(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExpandablePanel, 0, 0);

        mAnimationDuration =
                a.getInteger(R.styleable.ExpandablePanel_animationDuration, DEFAULT_ANIM_DURATION);

        int handleId = a.getResourceId(R.styleable.ExpandablePanel_handle, 0);
        if (handleId == 0) {
            throw new IllegalArgumentException(
                    "The handle attribute is required and must refer to a valid child.");
        }

        int contentContainerId = a.getResourceId(R.styleable.ExpandablePanel_contentContainer, 0);
        if (contentContainerId == 0) {
            throw new IllegalArgumentException(
                    "The content attribute is required and must refer to a valid child.");
        }

        int contentId = a.getResourceId(R.styleable.ExpandablePanel_content, 0);
        if (contentId == 0) {
            throw new IllegalArgumentException(
                    "The content attribute is required and must refer to a valid child.");
        }

        mHandleId = handleId;
        mContentContainerId = contentContainerId;
        mContentId = contentId;

        a.recycle();
    }

    public void setOnExpandListener(OnExpandListener listener) {
        mListener = listener;
    }

    public void setAnimationDuration(int animationDuration) {
        mAnimationDuration = animationDuration;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mHandle = findViewById(mHandleId);
        if (mHandle == null) {
            throw new IllegalArgumentException(
                    "The handle attribute is must refer to an existing child.");
        }

        mContentContainer = findViewById(mContentContainerId);
        if (mContentContainer == null) {
            throw new IllegalArgumentException(
                    "The content container attribute must refer to an existing child.");
        }

        mContent = findViewById(mContentId);
        if (mContentContainer == null) {
            throw new IllegalArgumentException(
                    "The content attribute must refer to an existing child.");
        }

        mContent.setVisibility(View.INVISIBLE);

        mHandle.setOnClickListener(new PanelToggler());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mContentContainer.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        mHandle.measure(MeasureSpec.UNSPECIFIED, heightMeasureSpec);
        mCollapsedHeight = mHandle.getMeasuredHeight();
        mContentWidth = mContentContainer.getMeasuredWidth();
        mContentHeight = mContentContainer.getMeasuredHeight();

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mFirstOpen) {
            mContentContainer.getLayoutParams().width = 0;
            mContentContainer.getLayoutParams().height = mCollapsedHeight;
            mFirstOpen = false;
        }

        int width = mHandle.getMeasuredWidth()
                + mContentContainer.getMeasuredWidth()
                + mContentContainer.getPaddingRight();
        int height = mContentContainer.getMeasuredHeight() + mContentContainer.getPaddingBottom();

        setMeasuredDimension(width, height);
    }

    public interface OnExpandListener {

        void onExpand(View handle, View content);

        void onCollapse(View handle, View content);

    }

    private class PanelToggler implements OnClickListener {
        @Override
        public void onClick(View v) {
            Animation animation;

            if (mExpanded) {
                mContent.setVisibility(View.INVISIBLE);
                animation = new ExpandAnimation(mContentWidth, 0, mContentHeight, mCollapsedHeight);
                if (mListener != null) {
                    mListener.onCollapse(mHandle, mContentContainer);
                }
            } else {
                ExpandablePanel.this.invalidate();
                animation = new ExpandAnimation(0, mContentWidth, mCollapsedHeight, mContentHeight);
                if (mListener != null) {
                    mListener.onExpand(mHandle, mContentContainer);
                }
            }

            animation.setDuration(mAnimationDuration);
            animation.setAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mExpanded = !mExpanded;
                    if (mExpanded) {
                        mContent.setVisibility(View.VISIBLE);
                    }
                }
            });

            mContentContainer.startAnimation(animation);
        }
    }

    private class ExpandAnimation extends Animation {

        private final int mStartWidth;
        private final int mDeltaWidth;
        private final int mStartHeight;
        private final int mDeltaHeight;

        public ExpandAnimation(int startWidth, int endWidth, int startHeight, int endHeight) {
            mStartWidth = startWidth;
            mDeltaWidth = endWidth - startWidth;
            mStartHeight = startHeight;
            mDeltaHeight = endHeight - startHeight;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            android.view.ViewGroup.LayoutParams lp = mContentContainer.getLayoutParams();
            lp.width = (int) (mStartWidth + mDeltaWidth * interpolatedTime);
            lp.height = (int) (mStartHeight + mDeltaHeight * interpolatedTime);
            mContentContainer.setLayoutParams(lp);
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }
}