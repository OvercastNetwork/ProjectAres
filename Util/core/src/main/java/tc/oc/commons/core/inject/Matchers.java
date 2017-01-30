package tc.oc.commons.core.inject;

import java.util.function.Predicate;

import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matcher;
import tc.oc.commons.core.reflect.Types;
import tc.oc.commons.core.util.FunctionalMatcher;

public class Matchers {

    public static <T> Matcher<T> predicate(Predicate<T> predicate) {
        return (FunctionalMatcher<T>) predicate::test;
    }

    public static Matcher<? extends TypeLiteral<?>> subtypesOf(TypeLiteral<?> type) {
        return (FunctionalMatcher<? extends TypeLiteral<?>>) t -> Types.isAssignable(type, t);
    }

    public static Matcher<? extends Binding<?>> bindingsForKeys(Matcher<? super Key<?>> keys) {
        return (FunctionalMatcher<? extends Binding<?>>) binding -> keys.matches(binding.getKey());
    }

    public static Matcher<? extends Binding<?>> bindingsForTypeLiterals(Matcher<? super TypeLiteral<?>> types) {
        return (FunctionalMatcher<? extends Binding<?>>) binding -> types.matches(binding.getKey().getTypeLiteral());
    }

    public static Matcher<? extends Binding<?>> bindingsForClasses(Matcher<? super Class<?>> types) {
        return (FunctionalMatcher<? extends Binding<?>>) binding -> types.matches(binding.getKey().getTypeLiteral().getRawType());
    }

    public static Matcher<? extends Binding<?>> bindingsForSubtypesOf(TypeLiteral<?> type) {
        return bindingsForTypeLiterals((Matcher<TypeLiteral<?>>) subtypesOf(type));
    }

    public static Matcher<? extends Binding<?>> bindingsForSubtypesOf(Class<?> type) {
        return bindingsForSubtypesOf(TypeLiteral.get(type));
    }
}
