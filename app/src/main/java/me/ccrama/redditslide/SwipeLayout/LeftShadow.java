public class LeftShadow extends Shadowable {
    @Override
    public void setShadow(int edgeFlag, Drawable shadow) {
        if ((edgeFlag & EDGE_LEFT) != 0) {
            mShadowLeft = shadow;
        }
    }

}
