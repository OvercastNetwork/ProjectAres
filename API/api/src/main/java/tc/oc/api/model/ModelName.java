package tc.oc.api.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ModelName {
    String value();
    String singular() default "";
    String plural() default "";
}
