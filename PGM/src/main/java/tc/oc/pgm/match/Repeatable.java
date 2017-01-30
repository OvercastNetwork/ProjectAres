package tc.oc.pgm.match;

import java.lang.annotation.*;

import tc.oc.time.Time;

/**
 * Represents a method that is automatically repeated over an interval during a {@link Match}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Repeatable {

    /**
     * Get the match scope that this repeatable will be active.
     */
    MatchScope scope() default MatchScope.RUNNING;

    /**
     * Get the interval, in ticks, that this method will be invoked.
     */
    Time interval() default @Time(ticks = 1);

}
