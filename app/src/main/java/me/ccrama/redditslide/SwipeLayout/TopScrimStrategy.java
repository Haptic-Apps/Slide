public class TopScrimStrategy implements ScrimStrategy{
    public void drawScrim(Canvas canvas, int mTrackingEdge) {
        if ((mTrackingEdge & EDGE_TOP) != 0) {
            canvas.clipRect(child.getLeft(), 0, getRight(), child.getTop() + getStatusBarHeight());
        }
    }
}
