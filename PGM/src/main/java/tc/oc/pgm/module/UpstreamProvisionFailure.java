package tc.oc.pgm.module;

import com.google.inject.ProvisionException;

/**
 * Thrown when trying to provision a module that failed to load due to
 * an error that has *already* been reported. If you catch this exception,
 * you can quietly exit what you are doing, or continue only if you think
 * you can detect other unrelated errors.
 *
 * This will usually be wrapped in a {@link ProvisionException}.
 */
public class UpstreamProvisionFailure extends RuntimeException {}
