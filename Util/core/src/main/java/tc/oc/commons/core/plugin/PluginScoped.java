package tc.oc.commons.core.plugin;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.inject.Scope;

import com.google.inject.ScopeAnnotation;
import tc.oc.commons.core.inject.InjectorScope;

/**
 * One instance per plugin.
 *
 * @see InjectorScope
 */
@Scope
@ScopeAnnotation
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginScoped {}
