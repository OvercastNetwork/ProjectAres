package tc.oc.commons.bukkit.geometry;

import java.util.Optional;
import java.util.function.UnaryOperator;

import org.bukkit.Location;
import org.bukkit.util.ImVector;
import org.bukkit.util.Vector;

/**
 * A 4x3 matrix representing an affine transform in homogenous coordinates.
 *
 * May contain slight copypasta from http://geom-java.sourceforge.net/index.html
 */
public class AffineTransform implements UnaryOperator<Vector> {
    private final double
        m00, m01, m02, m03,
        m10, m11, m12, m13,
        m20, m21, m22, m23;

    private AffineTransform(double m00, double m01, double m02, double m03,
                            double m10, double m11, double m12, double m13,
                            double m20, double m21, double m22, double m23) {

        this.m00 = m00; this.m01 = m01; this.m02 = m02; this.m03 = m03;
        this.m10 = m10; this.m11 = m11; this.m12 = m12; this.m13 = m13;
        this.m20 = m20; this.m21 = m21; this.m22 = m22; this.m23 = m23;
    }

    @Override
    public ImVector apply(Vector v) {
        return ImVector.of(m00 * v.getX() + m01 * v.getY() + m02 * v.getZ() + m03,
                                  m10 * v.getX() + m11 * v.getY() + m12 * v.getZ() + m13,
                                  m20 * v.getX() + m21 * v.getY() + m22 * v.getZ() + m23);
    }

    public Direction apply(Direction dir) {
        final ImVector v = dir.toVector();
        return Direction.fromVector(m00 * v.getX() + m01 * v.getY() + m02 * v.getZ(),
                                       m10 * v.getX() + m11 * v.getY() + m12 * v.getZ(),
                                       m20 * v.getX() + m21 * v.getY() + m22 * v.getZ());
    }

    public Location apply(Location location) {
        final Direction dir = apply(Direction.fromLocation(location));
        return apply(location.toVector()).toLocation(location.getWorld(),
                                                     (float) dir.yawDegrees(),
                                                     (float) dir.pitchDegrees());
    }

    public boolean isIdentity() {
        return m00 == 1 && m01 == 0 && m02 == 0 && m03 == 0 &&
               m10 == 0 && m11 == 1 && m12 == 0 && m13 == 0 &&
               m20 == 0 && m21 == 0 && m22 == 1 && m23 == 0;
    }

    private double determinant() {
        return   m00 * (m11 * m22 - m12 * m21)
               - m01 * (m10 * m22 - m20 * m12)
               + m02 * (m10 * m21 - m20 * m11);
    }

    public boolean isInvertible() {
        return determinant() != 0;
    }

    public Optional<AffineTransform> inverse() {
        final double det = this.determinant();
        if(det == 0) return Optional.empty();

        return Optional.of(new AffineTransform(
            (m11 * m22 - m21 * m12) / det,
            (m21 * m01 - m01 * m22) / det,
            (m01 * m12 - m11 * m02) / det,
            (  m01 * (m22 * m13 - m12 * m23)
             + m02 * (m11 * m23 - m21 * m13)
             - m03 * (m11 * m22 - m21 * m12)) / det,

            (m20 * m12 - m10 * m22) / det,
            (m00 * m22 - m20 * m02) / det,
            (m10 * m02 - m00 * m12) / det,
            (  m00 * (m12 * m23 - m22 * m13)
             - m02 * (m10 * m23 - m20 * m13)
             + m03 * (m10 * m22 - m20 * m12)) / det,

            (m10 * m21 - m20 * m11) / det,
            (m20 * m01 - m00 * m21) / det,
            (m00 * m11 - m10 * m01) / det,
            (  m00 * (m21 * m13 - m11 * m23)
             + m01 * (m10 * m23 - m20 * m13)
             - m03 * (m10 * m21 - m20 * m11)) / det
        ));
    }

    public AffineTransform append(AffineTransform o) {
        return new AffineTransform(m00 * o.m00 + m01 * o.m10 + m02 * o.m20,
                                   m00 * o.m01 + m01 * o.m11 + m02 * o.m21,
                                   m00 * o.m02 + m01 * o.m12 + m02 * o.m22,
                                   m00 * o.m03 + m01 * o.m13 + m02 * o.m23 + m03,

                                   m10 * o.m00 + m11 * o.m10 + m12 * o.m20,
                                   m10 * o.m01 + m11 * o.m11 + m12 * o.m21,
                                   m10 * o.m02 + m11 * o.m12 + m12 * o.m22,
                                   m10 * o.m03 + m11 * o.m13 + m12 * o.m23 + m13,

                                   m20 * o.m00 + m21 * o.m10 + m22 * o.m20,
                                   m20 * o.m01 + m21 * o.m11 + m22 * o.m21,
                                   m20 * o.m02 + m21 * o.m12 + m22 * o.m22,
                                   m20 * o.m03 + m21 * o.m13 + m22 * o.m23 + m23);
    }

    public AffineTransform prepend(AffineTransform o) {
        return o.append(this);
    }

    private static final AffineTransform IDENTITY = of(1, 0, 0,
                                                       0, 1, 0,
                                                       0, 0, 1);
    public static AffineTransform identity() {
        return IDENTITY;
    }

    public static AffineTransform of(double m00, double m01, double m02, double m03,
                                     double m10, double m11, double m12, double m13,
                                     double m20, double m21, double m22, double m23) {
        return new AffineTransform(m00, m01, m02, m03,
                                   m10, m11, m12, m13,
                                   m20, m21, m22, m23);
    }

    public static AffineTransform of(double m00, double m01, double m02,
                                     double m10, double m11, double m12,
                                     double m20, double m21, double m22) {
        return of(m00, m01, m02, 0,
                  m10, m11, m12, 0,
                  m20, m21, m22, 0);
    }

    public static AffineTransform of(LinearFunction x,
                                     LinearFunction y,
                                     LinearFunction z) {
        return new AffineTransform(x.linear(), 0, 0, x.constant(),
                                   0, y.linear(), 0, y.constant(),
                                   0, 0, z.linear(), z.constant());
    }

    public static AffineTransform translate(double x, double y, double z) {
        return new AffineTransform(1, 0, 0, x,
                                   0, 1, 0, y,
                                   0, 0, 1, z);
    }

    public static AffineTransform translate(Vector v) {
        return translate(v.getX(), v.getY(), v.getZ());
    }

    public static AffineTransform rotateX(double degrees) {
        double sin = sin(degrees);
        double cos = cos(degrees);
        return of(1, 0,   0,
                  0, cos, -sin,
                  0, sin, cos);
    }

    public static AffineTransform rotateY(double degrees) {
        double sin = sin(degrees);
        double cos = cos(degrees);
        return of(cos,  0,  sin,
                  0,    1,  0,
                  -sin, 0,  cos);
    }

    public static AffineTransform rotateZ(double degrees) {
        double sin = sin(degrees);
        double cos = cos(degrees);
        return of(cos, -sin, 0,
                  sin, cos,  0,
                  0,   0,    1);
    }

    public static AffineTransform forYaw(double degrees) {
        return rotateY(-degrees);
    }

    public static AffineTransform forPitch(double degrees) {
        return rotateX(degrees);
    }

    public static AffineTransform forDirection(double yaw, double pitch) {
        return forYaw(yaw).append(forPitch(pitch));
    }

    public static AffineTransform forDirection(Location location) {
        return forDirection(location.getYaw(), location.getPitch());
    }

    public static AffineTransform mirror(boolean x, boolean y, boolean z) {
        return of(x ? 1 : -1, 0, 0,
                  0, y ? 1 : -1, 0,
                  0, 0, z ? 1 : -1);
    }

    public static AffineTransform mirrorX() {
        return mirror(true, false, false);
    }

    public static AffineTransform mirrorY() {
        return mirror(false, true, false);
    }

    public static AffineTransform mirrorZ() {
        return mirror(false, false, true);
    }

    protected static double sin(double degrees) {
        int t = (int) degrees;
        if(t == degrees && t % 90 == 0) {
            t %= 360;
            if(t < 0) t += 360;

            switch(t) {
                case 0:
                case 180:
                    return 0;
                case 90:
                    return 1;
                case 270:
                    return -1;
            }
        }
        return Math.sin(Math.toRadians(degrees));
    }

    protected static double cos(double degrees) {
        int t = (int) degrees;
        if(t == degrees && t % 90 == 0) {
            t %= 360;
            if(t < 0) t += 360;

            switch(t) {
                case 90:
                case 270:
                    return 0;
                case 0:
                    return 1;
                case 180:
                    return -1;
            }
        }
        return Math.cos(Math.toRadians(degrees));
    }
}
