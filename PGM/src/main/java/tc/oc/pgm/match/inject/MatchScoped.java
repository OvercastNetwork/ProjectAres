package tc.oc.pgm.match.inject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.inject.Scope;

import com.google.inject.ScopeAnnotation;
import tc.oc.commons.core.inject.InjectorScope;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchScope;

/**
 * One instance per {@link Match}.
 *
 * @see InjectorScope
 *
 * Not related to {@link MatchScope} in any way.
 */
@Scope
@ScopeAnnotation
@Retention(RetentionPolicy.RUNTIME)
public @interface MatchScoped {}
