import java.awt.geom.*;
import java.util.ArrayList;

public class Vector extends Point2D.Float {

    public Vector() {
        super();
    }

    public Vector(float x, float y) {
        super(x, y);
    }

    /**
     * Calculates the magnitude of this vecotr
     * @return magnitude of this vector
     */
    public float magnitude() {
        return (float) Math.sqrt(this.x * this.x + this.y * this.y);
    }

    /**
     * Calculates the dot product of this vector with another vector v
     * @param v the other vector to take the dot product with
     * @return dot product of this vector with the vector v
     */
    public float dot(Vector v) {
        return (this.x * v.x + this.y * v.y);
    }


    /**
     * Finds the angle between this vector and another vector v
     * using the properties of the dot product
     * @param v the vector to find the angle to
     * @return the angle between this vector and the vector v
     */
    public float angleTo(Vector v) {
        return (float) Math.acos(this.dot(v) / (this.magnitude() * v.magnitude()));
    }

    /**
     * Scales the vector by a scalar alpha value
     * @param alpha scalar value to scale vector by
     * @return new vector scaled by alpha
     */
    public Vector scale(float alpha) {
        return new Vector(this.x * alpha, this.y * alpha);
    }

    /**
     * Scales the vector by a scalar alpha value
     * @param alpha scalar value to scale vector by
     * @return new vector scaled by alpha
     */
    public Vector scale(double alpha) {
        return new Vector(this.x * (float) alpha, this.y * (float) alpha);
    }

    /**
     * Scales the vector by a scalar alpha value
     * @param alpha scalar value to scale vector by
     * @return new vector scaled by alpha
     */
    public Vector scale(int alpha) {
        return new Vector(this.x * alpha, this.y * alpha);
    }

    /**
     * Shifts this vector over by a given vector v
     * @param v vector to shift this vector by
     * @return new vector shifted by v
     */
    public Vector shift(Vector v) {
        return new Vector(this.x + v.x, this.y + v.y);
    }

    /**
     * Finds the closest point of intersect given a point, direction vector, and path defined shape
     * @param pos point to start dir vector from
     * @param dir direction to search for intersect
     * @param path path of points defining object
     * @return closest point of intersect of path to position point, (-1, -1) if no point found
     */
    public static Vector closestIntersect(Vector pos, Vector dir, Outline path) {
        PathIterator pi = path.getPathIterator(null);
        Vector intersect = new Vector(-1, -1);
        ArrayList<Vector> coordList = new ArrayList<>();
        float[] tempCoords = new float[2];
        while (!pi.isDone()) {
            if (pi.currentSegment(tempCoords) != 4) {
                coordList.add(new Vector(tempCoords[0], tempCoords[1]));
            }
            pi.next();
        }

        int coordPairIndex;
        Vector a;
        Vector b;
        for (int i = 0; i < coordList.size(); i++) {
            a = coordList.get(i).shift(pos.scale(-1));
            if (i == coordList.size() - 1) {
                coordPairIndex = 0;
            } else {
                coordPairIndex = i + 1;
            }
            b = coordList.get(coordPairIndex).shift(pos.scale(-1));
            
            double thetaOne = a.angleTo(dir);
            double thetaTwo = dir.angleTo(b);

            if (thetaOne + thetaTwo <= a.angleTo(b) * 1.01) {
                Vector aToB = b.shift(a.scale(-1));
                double sinThetaA = Math.sin(Math.PI - aToB.angleTo(a));
                double sinThetaB = Math.sin(aToB.angleTo(b));
                double sinThetaOne = Math.sin(thetaOne);
                double sinThetaTwo = Math.sin(thetaTwo);
                float ratio = (float) (sinThetaOne * sinThetaB / (sinThetaOne * sinThetaB + sinThetaTwo * sinThetaA));
                Vector newIntersect = a.shift(aToB.scale(ratio));
                if (intersect.x == -1 || newIntersect.magnitude() < intersect.magnitude()) {
                    intersect = newIntersect;
                }
            }
        }
        if (intersect.x != -1) {
            intersect = intersect.shift(pos);
        }
        return intersect;
    }


    public static ArrayList<Vector> spreadPath(Vector pos, Vector dir1, Vector dir2, ArrayList<Outline> paths) {
        for (Outline path : paths) {
            
        }
        return null;
    }
}
