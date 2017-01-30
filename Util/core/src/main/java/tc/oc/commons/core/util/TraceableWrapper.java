package tc.oc.commons.core.util;

import javax.annotation.Nullable;

public class TraceableWrapper implements Traceable {

    private final @Nullable StackTrace stackTrace;

    public TraceableWrapper(StackTrace stackTrace) {
        this.stackTrace = stackTrace;
    }

    public TraceableWrapper() {
        this(new StackTrace());
    }

    @Override
    public @Nullable StackTrace stackTrace() {
        return stackTrace;
    }
}
