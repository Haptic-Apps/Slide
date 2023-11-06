public class TopShadow extends Shadowable {
    @Override
    public void setShadow(int edgeFlag, Drawable shadow) {
        if ((edgeFlag & EDGE_TOP) != 0) {
            mShadowTop = shadow;
        }
    }
}
