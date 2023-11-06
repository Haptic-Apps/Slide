public class LeftShadowDrawer implements ShadowStrategy{
    public void drawShadow(Drawable mShadow, Rect rect, float scrimOpacity, Canvas canvas) {
        if ((mEdgeFlag & EDGE_LEFT) != 0) {
            mShadow.setBounds(childRect.left - mShadow.getIntrinsicWidth(), childRect.top,
                    childRect.left, childRect.bottom);
            mShadow.setAlpha((int) (mScrimOpacity * FULL_ALPHA));
            mShadow.draw(canvas);
        }
    }
}
