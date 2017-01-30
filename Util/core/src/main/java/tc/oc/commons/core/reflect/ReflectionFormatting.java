package tc.oc.commons.core.reflect;

import java.lang.reflect.Member;

import static tc.oc.commons.core.reflect.Members.enclosingMember;

/**
 * Helper methods to generate readable descriptions from reflective objects
 */
public final class ReflectionFormatting {
    private ReflectionFormatting() {}

    public static String multilineDescription(Member member) {
        return member.getClass().getSimpleName().toLowerCase() +
               " " + member.getName() +
               " of " + multilineDescription(member.getDeclaringClass());
    }

    public static String multilineDescription(Class<?> cls) {
        final String text = cls.getName();

        final Member member = enclosingMember(cls);
        if(member != null) {
            return text + "\n  in " + multilineDescription(member);
        }

        final Class<?> outer = cls.getEnclosingClass();
        if(outer != null) {
            return text + "\n  in " + multilineDescription(outer);
        }

        return text;
    }

}
