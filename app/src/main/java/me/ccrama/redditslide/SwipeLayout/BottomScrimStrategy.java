public class BottomScrimStrategy implements ScrimStrategy{
    public void drawScrim(Canvas canvas, int mTrackingEdge) {
        if ((mTrackingEdge & EDGE_BOTTOM) != 0) {
            canvas.clipRect(child.getLeft(), child.getBottom(), getRight(), getHeight());
        }
    }
}
