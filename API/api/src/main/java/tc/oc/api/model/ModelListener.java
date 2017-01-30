package tc.oc.api.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface ModelListener {
    @Retention(RetentionPolicy.RUNTIME)
    @interface HandleModel {}
}
