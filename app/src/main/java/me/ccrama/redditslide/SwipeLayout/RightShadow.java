public class RightShadow extends Shadowable {
    @Override
    public void setShadow(int edgeFlag, Drawable shadow) {
        if ((edgeFlag & EDGE_RIGHT) != 0) {
            mShadowRight = shadow;
        }
    }
}
