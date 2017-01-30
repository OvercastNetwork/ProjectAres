package tc.oc.pgm.module;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import tc.oc.pgm.map.MapModule;

/**
 * Obsolete way to declare dependencies between modules.
 *
 * The modern way is to just @Inject the dependency, or an Optional of the same,
 * which all modules have a binding for.
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleDescription {
    /** Name of this module. */
    String name() default "";

    /**
     * Try to load all of these modules before this module and throw an exception if any of them fail to load
     */
    Class<? extends MapModule>[] requires() default {};

    /**
     * Silently skip loading this module if any of these modules fail to load
     */
    Class<? extends MapModule>[] depends() default {};

    /**
     * Try to load all of these modules before this module but ignore if any of them fail to load
     */
    Class<? extends MapModule>[] follows() default {};
}
