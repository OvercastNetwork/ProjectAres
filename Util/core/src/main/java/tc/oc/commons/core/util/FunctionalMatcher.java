package tc.oc.commons.core.util;

import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;

@FunctionalInterface
public interface FunctionalMatcher<T> extends Matcher<T> {
    @Override
    default Matcher<T> and(com.google.inject.matcher.Matcher<? super T> other) {
        return new AbstractMatcher<T>() {
            @Override public boolean matches(T t) {
                return FunctionalMatcher.this.matches(t) && other.matches(t);
            }
        };
    }

    @Override
    default Matcher<T> or(com.google.inject.matcher.Matcher<? super T> other) {
        return new AbstractMatcher<T>() {
            @Override public boolean matches(T t) {
                return FunctionalMatcher.this.matches(t) || other.matches(t);
            }
        };
    }
}
