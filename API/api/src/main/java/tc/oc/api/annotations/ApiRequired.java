package tc.oc.api.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
/**
 * Generic annotation for something that requires an API connection,
 * and should be ommitted or cause an error if not connected.
 *
 * (maybe we should make that distinction explicit?)
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiRequired {}
