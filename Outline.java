import java.awt.geom.*;
import java.util.ArrayList;

public class Outline extends Path2D.Float {
    public Outline() {
        super();
    }

    public ArrayList<Vector> coordList() {
        PathIterator pi = this.getPathIterator(null);
        ArrayList<Vector> coordList = new ArrayList<>();
        float[] tempCoords = new float[2];
        while (!pi.isDone()) {
            if (pi.currentSegment(tempCoords) != 4) {
                coordList.add(new Vector(tempCoords[0], tempCoords[1]));
            }
            pi.next();
        }
        return coordList;
    }

    public Vector center() {
        ArrayList<Vector> coordList = this.coordList();
        Vector center = new Vector();
        for (Vector v : coordList) {
            center = center.shift(v);
        }
        center = center.scale(1f / (float) coordList.size());
        return center;
    }
}
