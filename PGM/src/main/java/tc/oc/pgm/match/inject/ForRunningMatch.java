package tc.oc.pgm.match.inject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.inject.Qualifier;

import com.google.inject.BindingAnnotation;

@Qualifier
@BindingAnnotation
@Retention(RetentionPolicy.RUNTIME)
public @interface ForRunningMatch {}
