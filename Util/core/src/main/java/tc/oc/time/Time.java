package tc.oc.time;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import java.time.Duration;

/**
 * An annotation that can represent time periods.
 *
 * Useful for nesting in other annotations e.g.
 * <p>
 * {@code       @Delay(interval = @Time(seconds = 5))       }
 * <p>
 * Use the inner {@link convertTo} class to convert the annotation to other types:
 * <p>
 * {@code       Time.to.duration(annotation)                }
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Time {
    long ticks()default 0;
    long milliseconds()default 0;
    int seconds()default 0;
    int minutes()default 0;
    int hours()default 0;
    int days()default 0;

    class convertTo {
        public static long milliseconds(Time span) {
            return span.milliseconds() + 50 * (
                span.ticks() + 20 * (
                    span.seconds() + 60 * (
                        span.minutes() + 60 * (
                            span.hours() + 24 * span.days()
                        )
                    )
                )
            );
        }

        public static Duration duration(Time span) {
            return Duration.ofMillis(milliseconds(span));
        }
    }
}
