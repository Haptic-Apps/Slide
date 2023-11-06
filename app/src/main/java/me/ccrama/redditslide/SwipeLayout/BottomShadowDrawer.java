public class BottomShadowDrawer implements ShadowStrategy {
    public void drawShadow(Drawable mShadow, Rect rect, float scrimOpacity, Canvas canvas) {
        if ((mEdgeFlag & EDGE_BOTTOM) != 0) {
            mShadow.setBounds(childRect.left, childRect.bottom, childRect.right,
                    childRect.bottom + mShadow.getIntrinsicHeight());
            mShadow.setAlpha((int) (mScrimOpacity * FULL_ALPHA));
            mShadow.draw(canvas);
        }
    }
}
