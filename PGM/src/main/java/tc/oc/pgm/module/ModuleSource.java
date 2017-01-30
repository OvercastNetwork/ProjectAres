package tc.oc.pgm.module;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.inject.Qualifier;

import com.google.inject.BindingAnnotation;

@Qualifier
@BindingAnnotation
@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleSource {}
