package tc.oc.commons.core.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import tc.oc.commons.core.stream.BiStream;

public final class Members {
    private Members() {}

    private static final int ACCESS_MODIFIERS = Modifier.PRIVATE | Modifier.PROTECTED | Modifier.PUBLIC;
    private static final int NON_INHERITABLE_MODIFIERS = Modifier.PRIVATE | Modifier.STATIC;

    public static String qualifiedName(Class<?> decl, String name) {
        return decl.getName() + "#" + name;
    }

    public static String qualifiedName(Member member) {
        return qualifiedName(member.getDeclaringClass(), member.getName());
    }

    public static boolean isPrivate(Member member) {
        return Modifier.isPrivate(member.getModifiers());
    }

    public static boolean isProtected(Member member) {
        return Modifier.isProtected(member.getModifiers());
    }

    public static boolean isPublic(Member member) {
        return Modifier.isPublic(member.getModifiers());
    }

    public static boolean isPackagePrivate(Member member) {
        return (member.getModifiers() & ACCESS_MODIFIERS) == 0;
    }

    public static boolean isAbstract(Member member) {
        return Modifier.isAbstract(member.getModifiers());
    }

    public static boolean isStatic(Member member) {
        return Modifier.isStatic(member.getModifiers());
    }

    public static boolean isInheritable(Member member) {
        return (member.getModifiers() & NON_INHERITABLE_MODIFIERS) == 0;
    }

    public static void error(Member member, String description) {
        throw new IllegalArgumentException(member.getDeclaringClass().getName() + "#" + member.getName() + " " + description);
    }

    public static void assertPublic(Member member) {
        if(!Modifier.isPublic(member.getModifiers())) {
            error(member, "is not public");
        }
    }

    public static Predicate<? super Member> withModifiers(final int modifiers, final int mask) {
        return member -> (member.getModifiers() & mask) == modifiers;
    }

    public static Predicate<? super Member> withAllModifiers(int modifiers) {
        return withModifiers(modifiers, modifiers);
    }

    public static Predicate<? super Member> withoutAnyModifiers(int modifiers) {
        return withModifiers(0, modifiers);
    }

    public static Predicate<? super Member> staticMembers() {
        return withAllModifiers(Modifier.STATIC);
    }

    public static Predicate<? super Member> instanceMembers() {
        return withoutAnyModifiers(Modifier.STATIC);
    }

    public static @Nullable Member enclosingMember(Class<?> cls) {
        final Member member = cls.getEnclosingMethod();
        return member != null ? member : cls.getEnclosingConstructor();
    }

    public static <A extends Annotation> Optional<A> annotation(Class<A> type, AnnotatedElement element) {
        return Optional.ofNullable(element.getAnnotation(type));
    }

    public static <E extends AnnotatedElement, A extends Annotation> BiStream<E, A> annotations(Class<A> type, Stream<E> elements) {
        return BiStream.fromKeys(elements, element -> element.getAnnotation(type))
                       .filter((element, annotation) -> annotation != null);
    }
}
