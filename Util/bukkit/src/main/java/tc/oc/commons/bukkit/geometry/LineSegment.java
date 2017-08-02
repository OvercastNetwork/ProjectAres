package tc.oc.commons.bukkit.geometry;

import org.bukkit.util.ImVector;
import org.bukkit.util.Vector;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Line segment defined by two points
 */
public class LineSegment {

    private final ImVector start, finish, delta;

    private LineSegment(ImVector start, ImVector finish, ImVector delta) {
        this.start = checkNotNull(start);
        this.finish = checkNotNull(finish);
        this.delta = delta;
    }

    public static LineSegment of(double x1, double y1, double z1, double x2, double y2, double z2) {
        return new LineSegment(ImVector.of(x1, y1, z1),
                               ImVector.of(x2, y2, z2),
                               ImVector.of(x2 - x1, y2 - y1, z2 - z1));
    }

    public static LineSegment between(Vector start, Vector finish) {
        return new LineSegment(ImVector.copyOf(start),
                               ImVector.copyOf(finish),
                               ImVector.copyOf(finish.minus(start)));
    }

    public static LineSegment from(Vector start, Vector delta) {
        return new LineSegment(ImVector.copyOf(start),
                               ImVector.copyOf(start.plus(delta)),
                               ImVector.copyOf(delta));
    }

    /**
     * The "start" point of the line segment
     */
    public ImVector start() {
        return start;
    }

    /**
     * The "finish" point of the line segment
     */
    public ImVector finish() {
        return finish;
    }

    /**
     * The vector from {@link #start()} to {@link #finish()}
     */
    public ImVector delta() {
        return delta;
    }

    /**
     * Return an interpolated point on the line (but not necessarily on the segment)
     * based on the given parameter, such that:
     *
     *      t < 0       before start
     *      t == 0      start
     *      0 < t < 1   between start and finish
     *      t == 1      finish
     *      t > 1       after finish
     */
    public ImVector parametricPoint(double t) {
        return ImVector.interpolate(start, finish, t);
    }

    /**
     * Find the perpendicular projection of the given point onto this line,
     * as a parameter that can be passed to {@link #parametricPoint(double)}.
     *
     * The projection may be outside the segment.
     */
    public double perpendicularProjectionParameter(Vector point) {
        return point.minus(start).dot(delta) / delta.lengthSquared();
    }

    /**
     * Return the minimal distance of the given point from this line segment.
     *
     * This will either be the perpendicular distance to the line, or the
     * cartesian distance from one of the endpoints.
     */
    public double distance(Vector point) {
        return Math.sqrt(distanceSquared(point));
    }

    public double distanceSquared(Vector point) {
        if(delta.isZero()) {
            return point.distanceSquared(start);
        } else {
            final double t = perpendicularProjectionParameter(point);
            if(t <= 0) {
                return point.distanceSquared(start);
            } else if(t >= 1) {
                return point.distanceSquared(finish);
            } else {
                return point.distanceSquared(parametricPoint(t));
            }
        }
    }
}
