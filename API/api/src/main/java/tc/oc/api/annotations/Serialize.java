package tc.oc.api.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import tc.oc.api.docs.virtual.Document;

/**
 * Anything descended from {@link Document} can use this annotation to indicate that a method,
 * field, or entire class/interface should be included in serialization.
 * Applying it to a class is equivalent to applying it to every method declared in that class.
 *
 * Serialized methods must return a value and take no parameters. The returned value will be
 * serialized using the method name.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Serialize {
    boolean value() default true;
}
