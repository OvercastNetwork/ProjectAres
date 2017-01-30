package tc.oc.commons.core.util;

/**
 * AutoCloseable that doesn't throw checked exceptions from {@link #close}
 */
public interface CheckedCloseable extends AutoCloseable {
    @Override
    void close();
}
