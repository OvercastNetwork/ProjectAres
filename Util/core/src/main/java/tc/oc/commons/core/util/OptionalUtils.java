package tc.oc.commons.core.util;

import java.util.Optional;

public final class OptionalUtils {
    private OptionalUtils() {}

    /**
     * Return update if present, otherwise return value.
     */
    public static <T> Optional<T> replaceIfPresent(Optional<T> value, Optional<T> update) {
        return update.isPresent() ? update : value;
    }
}
