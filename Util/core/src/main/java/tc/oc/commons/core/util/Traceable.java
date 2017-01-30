package tc.oc.commons.core.util;

import javax.annotation.Nullable;

public interface Traceable {
    @Nullable StackTrace stackTrace();
}
