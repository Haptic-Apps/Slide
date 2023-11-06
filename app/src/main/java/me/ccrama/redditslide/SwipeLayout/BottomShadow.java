public class BottomShadow extends Shadow {
    @Override
    public void setShadow(int edgeFlag, Drawable shadow) {
        if ((edgeFlag & EDGE_BOTTOM) != 0) {
            mShadowBottom = shadow;
        }
    }
}
