package tc.oc.commons.core.inject;

import tc.oc.inject.ProtectedModule;

/**
 * A {@link ProtectedModule} with {@link ProtectedBinders} mixed in.
 *
 * TODO: This should be called ProtectedManifest
 */
public class HybridManifest extends ProtectedModule implements ProtectedBinders {}
