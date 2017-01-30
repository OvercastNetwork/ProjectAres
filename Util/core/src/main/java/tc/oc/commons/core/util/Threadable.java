package tc.oc.commons.core.util;

import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public class Threadable<T> extends ThreadLocal<T> {

    private final @Nullable Supplier<T> initialValue;

    public Threadable() {
        this(null);
    }

    public Threadable(@Nullable Supplier<T> initialValue) {
        this.initialValue = initialValue;
    }

    @Override
    protected T initialValue() {
        return initialValue != null ? initialValue.get()
                                    : super.initialValue();
    }

    public Optional<T> value() {
        return Optional.ofNullable(get());
    }

    public T need() {
        final T t = get();
        if(t == null) {
            throw new IllegalStateException("No value present");
        }
        return t;
    }

    public <E extends Throwable> void let(T value, ThrowingRunnable<E> block) throws E {
        if(value == get()) {
            block.runThrows();
        } else {
            try(CheckedCloseable x = let(value)) {
                block.runThrows();
            }
        }
    }

    public <U, E extends Throwable> U let(T value, ThrowingSupplier<U, E> block) throws E {
        if(value == get()) {
            return block.getThrows();
        } else {
            try(CheckedCloseable x = let(value)) {
                return block.getThrows();
            }
        }
    }

    public CheckedCloseable let(T value) {
        final T old = get();
        set(value);
        return () -> set(old);
    }
}
