package tc.oc.commons.core.util;

import com.google.common.base.Throwables;

public interface ThrowingRunnable<E extends Throwable> extends Runnable {

    void runThrows() throws E;

    @Override
    default void run() {
        try {
            runThrows();
        } catch(Throwable throwable) {
            throw Throwables.propagate(throwable);
        }
    }
}
