package tc.oc.pgm.module;

import java.util.Optional;

import tc.oc.commons.core.util.ThrowingRunnable;
import tc.oc.commons.core.util.ThrowingSupplier;

public interface ModuleExceptionHandler {

    /**
     * Run the given block.
     *
     * If the block throws a {@link ModuleLoadException}, log it and throw an {@link UpstreamProvisionFailure}.
     * Silently propagate all other exceptions.
     */
    void propagatingFailures(ThrowingRunnable<ModuleLoadException> block);

    /**
     * Run the given block and return its result.
     *
     * If the block throws a {@link ModuleLoadException}, log it and throw an {@link UpstreamProvisionFailure}.
     * Silently propagate all other exceptions.
     */
    <T> T propagatingFailures(ThrowingSupplier<T, ModuleLoadException> block);

    /**
     * Run the given block.
     *
     * If the block throws a {@link ModuleLoadException}, log it and return.
     * If the block throws a {@link UpstreamProvisionFailure}, return.
     * Silently propagate all other exceptions.
     */
    void ignoringFailures(ThrowingRunnable<ModuleLoadException> block);

    /**
     * Run the given block and return its result.
     *
     * If the block throws a {@link ModuleLoadException}, log it and return.
     * If the block throws a {@link UpstreamProvisionFailure}, return.
     * Silently propagate all other exceptions.
     */
    <T> Optional<T> ignoringFailures(ThrowingSupplier<T, ModuleLoadException> block);
}
