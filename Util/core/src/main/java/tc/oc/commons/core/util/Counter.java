package tc.oc.commons.core.util;

import java.util.function.IntSupplier;

public class Counter implements IntSupplier {

    private int count;

    public Counter() {
        this(0);
    }

    public Counter(int start) {
        count = start;
    }

    public int next() {
        return count++;
    }

    @Override
    public int getAsInt() {
        return next();
    }
}
