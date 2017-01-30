package tc.oc.pgm.map.inject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.inject.Scope;

import com.google.inject.ScopeAnnotation;

@Scope
@ScopeAnnotation
@Retention(RetentionPolicy.RUNTIME)
public @interface MapScoped {}
