package tc.oc.commons.core.util;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * General-purpose container for a lazily calculated value.
 * Both thread-safe and thread-unsafe variants are available.
 *
 *     Lazy<String> text = Lazy.from(() -> "Expensive string");
 *     text.get();
 *
 * There is also an injectable constructor that injects a Provider<T>,
 * so you can simply self-bind this class to get a cached provider
 * of anything.
 */
public class Lazy<T> implements Supplier<T>, Provider<T> {

    private final Supplier<T> supplier;
    private boolean got;
    private T instance;

    @Inject private Lazy(Provider<T> provider) {
        this.supplier = provider::get;
    }

    private Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        if(!got) {
            instance = supplier.get();
            got = true;
        }
        return instance;
    }

    public void clear() {
        got = false;
        instance = null;
    }

    /**
     * Create a lazy value calculated by the given {@link Supplier}.
     *
     * The lazy container is not thread-safe. If the value is
     * retrieved concurrently from multiple threads, it may be
     * calculated more than once.
     */
    public static <T> Lazy<T> from(Supplier<T> supplier) {
        return new Lazy<>(supplier);
    }

    /**
     * Create a thread-safe lazy value calculated by the given {@link Supplier}.
     */
    public static <T> Lazy<T> sync(Supplier<T> supplier) {
        return new SyncLazy<>(supplier);
    }

    private static class SyncLazy<T> extends Lazy<T> {
        private SyncLazy(Supplier<T> supplier) {
            super(supplier);
        }

        @Override
        synchronized public T get() {
            return super.get();
        }
    }

    public static <T> Lazy<T> expiring(Duration maxAge, Supplier<T> supplier) {
        return new Expiring<T>(maxAge, supplier);
    }

    private static class Expiring<T> extends Lazy<T> {
        private final Duration max;
        private Instant last = TimeUtils.INF_PAST;

        private Expiring(Duration max, Supplier<T> supplier) {
            super(supplier);
            this.max = max;
        }

        @Override
        public T get() {
            if(Comparables.greaterThan(Duration.between(last, Instant.now()), max)) {
                clear();
            }
            return super.get();
        }
    }
}
