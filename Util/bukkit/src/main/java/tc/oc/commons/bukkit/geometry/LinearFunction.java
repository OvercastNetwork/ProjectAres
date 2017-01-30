package tc.oc.commons.bukkit.geometry;

import java.util.Optional;
import java.util.function.DoubleUnaryOperator;

public class LinearFunction implements DoubleUnaryOperator {

    private final double linear, constant;

    private LinearFunction(double linear, double constant) {
        this.linear = linear;
        this.constant = constant;
    }

    private static final LinearFunction IDENTITY = new LinearFunction(1, 0);
    public static LinearFunction identity() {
        return IDENTITY;
    }

    public static LinearFunction of(double linear, double constant) {
        return new LinearFunction(linear, constant);
    }

    public static LinearFunction scale(double linear) {
        return new LinearFunction(linear, 0);
    }

    public static LinearFunction translate(double constant) {
        return new LinearFunction(1, constant);
    }

    public static LinearFunction fixed(double fixed) {
        return new LinearFunction(0, fixed);
    }

    public double linear() {
        return linear;
    }

    public double constant() {
        return constant;
    }

    public double apply(double n) {
        return linear * n + constant;
    }

    @Override
    public double applyAsDouble(double operand) {
        return apply(operand);
    }

    public boolean isIdentity() {
        return linear == 1 && constant == 0;
    }

    public boolean isInvertible() {
        return linear != 0;
    }

    public Optional<LinearFunction> inverse() {
        if(linear == 0) {
            return Optional.empty();
        } else {
            return Optional.of(new LinearFunction(1 / linear, -constant / linear));
        }
    }

    public LinearFunction append(LinearFunction that) {
        return new LinearFunction(this.linear * that.linear,
                                  this.linear * that.constant + this.constant);
    }

    public LinearFunction prepend(LinearFunction that) {
        return that.append(this);
    }
}
