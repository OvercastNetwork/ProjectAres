package tc.oc.pgm.match.inject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.inject.Qualifier;

import com.google.inject.BindingAnnotation;

/**
 * A binding annotation for per-match objects.
 *
 * This is used for generally useful types like Random and CountdownContext,
 * which have a {@link MatchScoped} binding, but may also may have other
 * bindings for other purposes.
 */
@Qualifier
@BindingAnnotation
@Retention(RetentionPolicy.RUNTIME)
public @interface ForMatch {}
