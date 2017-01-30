package tc.oc.commons.core.inject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.inject.Scope;

import com.google.inject.ScopeAnnotation;

/**
 * One instance per Injector.
 *
 * @see InjectorScope
 */
@Scope
@ScopeAnnotation
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectorScoped {}
