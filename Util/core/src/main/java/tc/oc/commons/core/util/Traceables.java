package tc.oc.commons.core.util;

import java.util.Set;
import java.util.concurrent.Callable;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class Traceables {

    public static @Nullable StackTrace stackTrace(Object obj) {
        return obj instanceof Traceable ? ((Traceable) obj).stackTrace() : null;
    }

    public static StackTrace computeStackTrace(Object obj, Set<Class<?>> skip) {
        return obj instanceof Traceable ? ((Traceable) obj).stackTrace()
                                        : new StackTrace(Sets.union(ImmutableSet.of(Traceables.class), skip));
    }

    public static StackTrace computeStackTrace(Object obj, Class<?> skip) {
        return computeStackTrace(obj, ImmutableSet.of(skip));
    }

    public static StackTrace computeStackTrace(Object obj) {
        return computeStackTrace(obj, ImmutableSet.of());
    }

    public static Runnable wrap(Runnable source, Runnable wrapper) {
        return source instanceof Traceable ? new WrappedRunnable(((Traceable) source).stackTrace(), wrapper) : wrapper;
    }

    public static <E extends Throwable> ThrowingRunnable<E> wrap(ThrowingRunnable<E> source, ThrowingRunnable<E> wrapper) {
        return source instanceof Traceable ? new WrappedThrowingRunnable<>(((Traceable) source).stackTrace(), wrapper) : wrapper;
    }

    public static <V> Callable<V> wrap(Callable<V> source, Callable<V> wrapper) {
        return source instanceof Traceable ? new WrappedCallable<>(((Traceable) source).stackTrace(), wrapper) : wrapper;
    }

    private static class WrappedRunnable extends TraceableWrapper implements Runnable {
        private final Runnable wrapped;
        @Override public void run() { wrapped.run(); }

        private WrappedRunnable(StackTrace trace, Runnable wrapped) {
            super(trace);
            this.wrapped = wrapped;
        }
    }

    private static class WrappedCallable<V> extends TraceableWrapper implements Callable<V> {
        private final Callable<V> wrapped;
        @Override public V call() throws Exception { return wrapped.call(); }

        private WrappedCallable(StackTrace trace, Callable<V> wrapped) {
            super(trace);
            this.wrapped = wrapped;
        }
    }

    private static class WrappedThrowingRunnable<E extends Throwable> extends TraceableWrapper implements ThrowingRunnable<E> {
        private final ThrowingRunnable<E> wrapped;
        @Override public void run() { wrapped.run(); }
        @Override public void runThrows() throws E { wrapped.runThrows(); }

        private WrappedThrowingRunnable(StackTrace trace, ThrowingRunnable<E> wrapped) {
            super(trace);
            this.wrapped = wrapped;
        }
    }
}
