package tc.oc.pgm.utils;

/**
 * A modifier for numeric properties, with constant and linear terms.
 */
public class NumericModifier {
    private final double constant;
    private final double linear;

    public NumericModifier(double constant, double linear) {
        this.constant = constant;
        this.linear = linear;
    }

    public NumericModifier(double constant) {
        this(constant, 0);
    }

    public static final NumericModifier ZERO = new NumericModifier(0);

    public double getConstant() {
        return constant;
    }

    public double getLinear() {
        return linear;
    }

    public double apply(double x) {
        return constant + linear * x;
    }

    public double credit(double x, double scale) {
        return x + scale * apply(x);
    }

    public double credit(double x) {
        return credit(x, 1d);
    }

    public double deduct(double x, double scale) {
        return credit(x, -scale);
    }

    public double deduct(double x) {
        return deduct(x, 1d);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{constant=" + constant + " linear=" + linear + "}";
    }
}
