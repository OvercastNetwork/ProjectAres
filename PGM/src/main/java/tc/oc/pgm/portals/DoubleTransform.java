package tc.oc.pgm.portals;

import java.util.function.DoubleUnaryOperator;

public interface DoubleTransform extends DoubleUnaryOperator, InvertibleOperator<DoubleTransform> {

    Identity IDENTITY = new Identity();
    class Identity implements DoubleTransform {
        private Identity() {}

        @Override
        public double applyAsDouble(double old) {
            return old;
        }

        @Override
        public boolean invertible() {
            return true;
        }

        @Override
        public DoubleTransform inverse() {
            return this;
        }
    }

    class Translate implements DoubleTransform {
        public static final Translate ZERO = new Translate(0);
        private final double delta;

        public Translate(double delta) {
            this.delta = delta;
        }

        @Override
        public double applyAsDouble(double old) {
            return old + this.delta;
        }

        @Override
        public boolean invertible() {
            return true;
        }

        @Override
        public Translate inverse() {
            if(this.delta == 0) {
                return ZERO;
            } else {
                return new Translate(-this.delta);
            }
        }
    }

    class Constant implements DoubleTransform {
        private final double value;

        public Constant(double value) {
            this.value = value;
        }

        @Override
        public boolean invertible() {
            return false;
        }

        @Override
        public double applyAsDouble(double old) {
            return this.value;
        }
    }
}
