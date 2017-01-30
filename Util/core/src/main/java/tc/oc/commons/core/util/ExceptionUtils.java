package tc.oc.commons.core.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import javax.annotation.Nullable;

import com.google.common.util.concurrent.UncheckedExecutionException;

public class ExceptionUtils {

    public static String formatStackTrace(@Nullable StackTraceElement[] trace) {
        return formatStackTrace(trace, Predicates.alwaysFalse());
    }

    public static String formatStackTrace(@Nullable StackTraceElement[] trace, Predicate<StackTraceElement> skipWhile) {
        if(trace == null || trace.length == 0) return "";

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        int i = 0;
        for(; i < trace.length && skipWhile.test(trace[i]); i++);
        for(; i < trace.length; i++) {
            pw.println("\tat " + trace[i]);
        }
        return sw.toString();
    }

    public static RuntimeException propagate(Throwable e) {
        // If exception is not checked, throw it directly
        if(e instanceof RuntimeException) {
            throw (RuntimeException) e;
        } else if(e instanceof Error) {
            throw (Error) e;
        }

        // Unwrap uninteresting wrappers
        if(e instanceof InvocationTargetException ||
           e instanceof ExecutionException) {
            return propagate(e.getCause());
        }

        // Replace reflection exceptions with their unchecked equivalents
        if(e instanceof InstantiationException) {
            throw new InstantiationError(e.getMessage());
        } else if(e instanceof IllegalAccessException) {
            throw new IllegalAccessError(e.getMessage());
        } else if(e instanceof NoSuchFieldException) {
            throw new NoSuchFieldError(e.getMessage());
        } else if(e instanceof NoSuchMethodException) {
            throw new NoSuchMethodError(e.getMessage());
        } else if(e instanceof ClassNotFoundException) {
            throw new NoClassDefFoundError(e.getMessage());
        }

        // Last resort, use an unchecked wrapper
        throw new UncheckedExecutionException(e);
    }

    public static void propagate(ThrowingRunnable<Throwable> block) {
        propagate(() -> {
            block.runThrows();
            return null;
        });
    }

    public static <T> T propagate(ThrowingSupplier<T, Throwable> block) {
        try {
            return block.getThrows();
        } catch(Throwable e) {
            throw propagate(e);
        }
    }

    public static <E extends Throwable> void propagate(Class<E> ex, ThrowingRunnable<Throwable> block) throws E {
        propagate(ex, () -> {
            block.runThrows();
            return null;
        });
    }

    public static <T, E extends Throwable> T propagate(Class<E> ex, ThrowingSupplier<T, Throwable> block) throws E {
        try {
            return block.getThrows();
        } catch(Throwable e) {
            if(ex.isInstance(e)) {
                throw (E) e;
            } else {
                throw propagate(e);
            }
        }
    }

    public static <E extends Throwable> void collect(Class<E> type, Collection<? super E> errors, ThrowingRunnable<? extends E> block) {
        try {
            block.runThrows();
        } catch(Throwable e) {
            if(type.isInstance(e)) {
                errors.add((E) e);
            } else {
                throw (RuntimeException) e;
            }
        }
    }

    public static <T, E extends Throwable> Optional<T> collect(Class<E> type, Collection<? super E> errors, ThrowingSupplier<T, ? extends E> block) {
        try {
            return Optional.ofNullable(block.getThrows());
        } catch(Throwable e) {
            if(type.isInstance(e)) {
                errors.add((E) e);
                return Optional.empty();
            } else {
                throw (RuntimeException) e;
            }
        }
    }

    public static <T, E extends Throwable> Optional<T> flatCollect(Class<E> type, Collection<? super E> errors, ThrowingSupplier<Optional<T>, ? extends E> block) {
        try {
            return block.getThrows();
        } catch(Throwable e) {
            if(type.isInstance(e)) {
                errors.add((E) e);
                return Optional.empty();
            } else {
                throw (RuntimeException) e;
            }
        }
    }
}
