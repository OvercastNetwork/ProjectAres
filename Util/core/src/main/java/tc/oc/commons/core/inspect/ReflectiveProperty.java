package tc.oc.commons.core.inspect;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.cache.LoadingCache;
import com.google.common.reflect.TypeToken;
import tc.oc.commons.core.reflect.Fields;
import tc.oc.commons.core.reflect.Members;
import tc.oc.commons.core.reflect.MethodHandleUtils;
import tc.oc.commons.core.reflect.Methods;
import tc.oc.commons.core.util.CacheUtils;

public class ReflectiveProperty implements InspectableProperty {

    private static final LoadingCache<Class<? extends Inspectable>, List<ReflectiveProperty>> CACHE = CacheUtils.newCache(
        inspectable -> Stream.concat(Members.annotations(Inspectable.Inspect.class,
                                                         Fields.declaredInAncestors(inspectable))
                                            .merge(ReflectiveProperty::of),
                                     Members.annotations(Inspectable.Inspect.class,
                                                         Methods.declaredMethodsInAncestors(inspectable))
                                            .merge(ReflectiveProperty::of))
                             .collect(Collectors.toList())
    );

    public static <I extends Inspectable> List<ReflectiveProperty> all(Class<I> type) {
        return CACHE.getUnchecked(type);
    }

    public static ReflectiveProperty of(Member member, Inspectable.Inspect annotation) {
        if(member instanceof Field) {
            return of((Field) member, annotation);
        } else if(member instanceof Method) {
            return of((Method) member, annotation);
        } else {
            throw new IllegalArgumentException("Don't know how to inspect a " + member.getClass().getName());
        }
    }

    public static ReflectiveProperty of(Field field, Inspectable.Inspect annotation) {
        field.setAccessible(true);
        return new ReflectiveProperty(
            annotation.name().length() > 0 ? annotation.name()
                                           : field.getName(),
            TypeToken.of(field.getGenericType()),
            new Inspection(annotation),
            MethodHandleUtils.privateUnreflectGetter(field)
        );
    }

    public static ReflectiveProperty of(Method method, Inspectable.Inspect annotation) {
        if(method.getParameterTypes().length > 0) {
            throw new IllegalArgumentException("Can't inspect a method with parameters");
        }

        method.setAccessible(true);
        return new ReflectiveProperty(
            annotation.name().length() > 0 ? annotation.name()
                                           : Methods.removeBeanPrefix(method.getName()),
            TypeToken.of(method.getGenericReturnType()),
            new Inspection(annotation),
            MethodHandleUtils.privateUnreflect(method)
        );
    }

    private final String name;
    private final TypeToken<?> type;
    private final Inspection options;
    private final MethodHandle handle;

    ReflectiveProperty(String name, TypeToken<?> type, Inspection options, MethodHandle handle) {
        this.name = name;
        this.type = type;
        this.options = options;
        this.handle = handle;
    }

    @Override public String name() { return name; }
    @Override public Inspection options() { return options; }

    @Override
    public Object value(Inspectable inspectable) throws Throwable {
        return handle.invoke(inspectable);
    }
}
