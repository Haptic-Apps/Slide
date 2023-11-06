public class TopShadowDrawer implements ShadowStrategy{
    public void drawShadow(Drawable mShadow, Rect rect, float scrimOpacity, Canvas canvas) {
        if ((mEdgeFlag & EDGE_TOP) != 0) {
            mShadow.setBounds(childRect.left, childRect.top - mShadow.getIntrinsicHeight(),
                    childRect.right, childRect.top + getStatusBarHeight());
            mShadow.setAlpha((int) (mScrimOpacity * FULL_ALPHA));
            mShadow.draw(canvas);
        }
    }
}
