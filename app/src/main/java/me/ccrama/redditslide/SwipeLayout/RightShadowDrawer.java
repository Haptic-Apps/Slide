public class RightShadowDrawer implements ShadowStrategy{
    public void drawShadow(Drawable mShadow, Rect rect, float scrimOpacity, Canvas canvas) {
        if ((mEdgeFlag & EDGE_RIGHT) != 0) {
            mShadow.setBounds(childRect.right, childRect.top,
                    childRect.right + mShadow.getIntrinsicWidth(), childRect.bottom);
            mShadow.setAlpha((int) (mScrimOpacity * FULL_ALPHA));
            mShadow.draw(canvas);
        }
    }
}
