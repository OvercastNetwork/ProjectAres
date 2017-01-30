package tc.oc.commons.core.exception;

import java.util.concurrent.ThreadFactory;

public interface NamedThreadFactory {

    Thread newThread(String name, Runnable code);

    default ThreadFactory newThreadFactory(String name) {
        return code -> newThread(name, code);
    }
}
