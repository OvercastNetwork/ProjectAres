package tc.oc.commons.core.inject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.inject.Qualifier;

import com.google.inject.BindingAnnotation;

/**
 * Denotes a binding to a dynamic proxy that delegates to the
 * Provider of the same type. The provider's get() method is
 * called for every method call on the proxied interface.
 *
 * This allows you to use a scoped or otherwise dynamic
 * object as if it was constant.
 *
 * Install a {@link ProxiedManifest} or call {@link Binders#bindProxy}
 * to make the proxy available for a given type.
 *
 * TODO: Some way to proxy annotated bindings
 */
@Qualifier
@BindingAnnotation
@Retention(RetentionPolicy.RUNTIME)
public @interface Proxied {}
