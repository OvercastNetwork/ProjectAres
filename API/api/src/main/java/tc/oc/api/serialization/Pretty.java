package tc.oc.api.serialization;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.inject.Qualifier;

import com.google.gson.Gson;
import com.google.inject.BindingAnnotation;

/**
 * Qualify a {@link Gson} binding with this to get the pretty-printing version
 */
@Qualifier
@BindingAnnotation
@Retention(RetentionPolicy.RUNTIME)
public @interface Pretty {}
