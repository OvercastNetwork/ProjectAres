package tc.oc.commons.core.reflect;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;

import com.google.inject.TypeLiteral;
import tc.oc.commons.core.ListUtils;
import tc.oc.commons.core.util.Utils;

/**
 * Search a type hiearchy for methods matching a given filter.
 *
 * If matching methods override each other, only the most derived
 * method is included in the result.
 */
public class MethodScanner<D> {

    private class Signature {
        final @Nullable Object context;
        final String name;
        final List<Class<?>> parameters;
        final int hashCode;

        Signature(Method method) {
            name = method.getName();
            parameters = ListUtils.transformedCopyOf(decl.getParameterTypes(method), TypeLiteral::getRawType);

            if(Members.isPrivate(method)) {
                // Private methods are only visible within the same class (i.e. not overridable)
                context = method.getDeclaringClass();
            } else if(Members.isProtected(method) || Members.isPublic(method)) {
                // Protected and public methods are visible throughout the entire hiearchy
                context = null;
            } else {
                // Package-local methods can only override within the same package
                context = method.getDeclaringClass().getPackage();
            }

            hashCode = Objects.hash(context, name, parameters);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            return Utils.equals(Signature.class, this, obj, that ->
                Objects.equals(this.context, that.context) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.parameters, that.parameters)
            );
        }
    }

    private final TypeLiteral<D> decl;
    private final Map<Signature, Method> map = new HashMap<>();
    private final Predicate<? super Method> filter;

    public MethodScanner(Class<D> decl, Predicate<? super Method> filter) {
        this(TypeLiteral.get(decl), filter);
    }

    public MethodScanner(TypeLiteral<D> decl, Predicate<? super Method> filter) {
        this.decl = decl;
        this.filter = filter;

        visitClasses(decl.getRawType());
        visitInterfaces(decl.getRawType());
    }

    public Collection<Method> methods() {
        return map.values();
    }

    private void visitType(Class<?> type) {
        if(!type.isInterface()) {
            visitClasses(type);
        }
        visitInterfaces(type);
    }

    private void visitClasses(@Nullable Class<?> type) {
        if(type != null) {
            visitMethods(type);
            visitClasses(type.getSuperclass());
        }
    }

    private void visitInterfaces(Class<?> type) {
        if(type.isInterface()) {
            visitMethods(type);
        }
        for(Class<?> iface : type.getInterfaces()) {
            visitInterfaces(iface);
        }
    }

    private void visitMethods(Class<?> type) {
        for(Method method : type.getDeclaredMethods()) {
            if(!method.isSynthetic() && !method.isBridge() && filter.test(method)) {
                map.merge(new Signature(method), method, (ma, mb) -> {
                    // Figure out which method overrides the other
                    final Class<?> ta = ma.getDeclaringClass();
                    final Class<?> tb = mb.getDeclaringClass();

                    // If one method is declared in a class and the other is declared
                    // in an interface, the class method always wins.
                    if(!ta.isInterface() && tb.isInterface()) return ma;
                    if(!tb.isInterface() && ta.isInterface()) return mb;

                    // If one method is a default (interface) method, and the other
                    // one isn't, keep the default one (the other method must be
                    // a normal interface method).
                    if(ma.isDefault() && !mb.isDefault()) return ma;
                    if(mb.isDefault() && !ma.isDefault()) return mb;

                    // If one method's owner is a subtype of the other method's owner,
                    // keep the subtype method.
                    if(tb.isAssignableFrom(ta)) return ma;
                    if(ta.isAssignableFrom(tb)) return mb;

                    // If all else fails, keep the method that was encountered first.
                    // I'm pretty sure this can only happen with two abstract methods
                    // in unrelated interfaces, in which case it probably doesn't
                    // matter which one is kept.
                    return ma;
                });
            }
        }
    }
}
