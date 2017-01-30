package tc.oc.commons.core.reflect;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.Invokable;
import com.google.common.reflect.Parameter;
import com.google.common.reflect.TypeToken;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.commons.core.util.Utils;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Equivalent of {@link java.lang.invoke.MethodType} using {@link TypeToken}s
 * in order to support generics.
 */
class GenericMethodType<T> {

    private final TypeToken<T> returnType;
    private final ImmutableList<TypeToken<?>> parameterTypes;

    public GenericMethodType(TypeToken<T> returnType, List<TypeToken<?>> parameterTypes) {
        this.returnType = checkNotNull(returnType);
        this.parameterTypes = ImmutableList.copyOf(parameterTypes);
    }

    public static GenericMethodType<?> of(Method method) {
        return new GenericMethodType<>(TypeToken.of(method.getGenericReturnType()),
                                       Stream.of(method.getGenericParameterTypes())
                                             .map(TypeToken::of)
                                             .collect(Collectors.toImmutableList()));
    }

    public static GenericMethodType<?> of(Invokable<?, ?> invokable) {
        return new GenericMethodType<>(invokable.getReturnType(),
                                       invokable.getParameters()
                                                .stream()
                                                .map(Parameter::getType)
                                                .collect(Collectors.toImmutableList()));
    }

    public TypeToken<T> returnType() {
        return returnType;
    }

    public List<TypeToken<?>> parameterTypes() {
        return parameterTypes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(returnType, parameterTypes);
    }

    @Override
    public boolean equals(Object obj) {
        return Utils.equals(GenericMethodType.class, this, obj, that ->
            this.returnType.equals(that.returnType) && this.parameterTypes.equals(that.parameterTypes)
        );
    }

    @Override
    public String toString() {
        return returnType.toString() +
               "(" +
               parameterTypes.stream()
                             .map(Object::toString)
                             .collect(java.util.stream.Collectors.joining(", ")) +
               ")";
    }

    public GenericMethodType<? extends T> resolveIn(TypeToken<?> context) {
        final TypeToken<? extends T> returnType = (TypeToken<? extends T>) context.resolveType(this.returnType.getType());
        final ImmutableList<TypeToken<?>> parameterTypes = this.parameterTypes.stream()
                                                                              .map(t -> context.resolveType(t.getType()))
                                                                              .collect(Collectors.toImmutableList());
        return returnType.equals(this.returnType) && parameterTypes.equals(this.parameterTypes)
               ? this
               : new GenericMethodType<>(returnType, parameterTypes);
    }

    public boolean canInvoke(GenericMethodType<?> method) {
        if(!returnType.isAssignableFrom(method.returnType)) return false;
        if(parameterTypes.size() != method.parameterTypes.size()) return false;
        for(int i = 0; i < parameterTypes.size(); i++) {
            if(!method.parameterTypes.get(i).isAssignableFrom(parameterTypes.get(i))) return false;
        }
        return true;
    }

    public boolean canInvoke(Invokable<?, ?> invokable) {
        return canInvoke(of(invokable));
    }

    public boolean canInvoke(TypeToken<?> target, Method method) {
        return canInvoke(target.method(method));
    }
}
