public class RightScrimStrategy implements ScrimStrategy{
    public void drawScrim(Canvas canvas, int mTrackingEdge) {
        if ((mTrackingEdge & EDGE_RIGHT) != 0) {
            canvas.clipRect(child.getRight(), 0, getRight(), getHeight());
        }
    }
}
