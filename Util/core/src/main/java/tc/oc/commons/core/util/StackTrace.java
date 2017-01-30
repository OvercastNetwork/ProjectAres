package tc.oc.commons.core.util;

import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.inject.internal.util.StackTraceElements;

public class StackTrace {

    private final StackTraceElements.InMemoryStackTraceElement[] trace;
    private final Set<Class<?>> skip;

    public StackTrace(StackTraceElement[] trace, Set<Class<?>> skip) {
        this.trace = StackTraceElements.convertToInMemoryStackTraceElement(trace);
        this.skip = skip;
    }

    public StackTrace(Set<Class<?>> skip) {
        this(new Throwable().getStackTrace(), Sets.union(ImmutableSet.of(StackTrace.class), skip));
    }

    public StackTrace() {
        this(Collections.emptySet());
    }
    public StackTraceElement[] trace() {
        return StackTraceElements.convertToStackTraceElement(trace);
    }

    public StackTraceElement origin() {
        return ArrayUtils.first(trace(), Predicates.not(skipTest()));
    }

    /**
     * Return a formatted stack trace
     */
    @Override
    public String toString() {
        return ExceptionUtils.formatStackTrace(trace(), skipTest());
    }

    private Set<String> skipNames() {
        return ImmutableSet.<String>builder().addAll(Iterables.transform(skip, Class::getName)).build();
    }

    private Predicate<StackTraceElement> skipTest() {
        final Set<String> skipNames = skipNames();
        return frame ->
            StackTrace.this.getClass().getName().equals(frame.getClassName()) ||
            skipNames.contains(frame.getClassName());
    }
}
