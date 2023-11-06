public class LeftScrimStrategy implements ScrimStrategy{
    public void drawScrim(Canvas canvas, int mTrackingEdge) {
        if ((mTrackingEdge & EDGE_LEFT) != 0) {
            canvas.clipRect(0, 0, child.getLeft(), getHeight());
        }
    }
}
