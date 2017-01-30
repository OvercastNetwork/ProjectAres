package tc.oc.commons.core.reflect;

import java.lang.reflect.TypeVariable;
import javax.annotation.Nullable;

import com.google.common.reflect.TypeToken;

/**
 * Represents a type parameter of {@link D} that extends {@link T}, and allows the parameter to
 * be resolved in the context of a given {@link D} subtype.
 *
 * @param <T> Upper bound of the type parameter
 * @param <D> Class that the type parameter belongs to
 *
 *     class Woot<T extends Number> {}
 *
 *     class IntWoot extends Woot<Integer> {}
 *
 *     new ResolvableTypeParameter<Number, Woot>("T"){}
 *         .resolveIn(IntWoot.class)
 *
 *      => Integer.class
 *
 * Note: TypeToken assumes that its type is the first type parameter of the immediate superclass.
 * It doesn't actually check if the superclass is TypeToken, so instead it will capture our first
 * type parameter, which therefor must be the one passed to TypeToken.
 */
public abstract class ResolvableTypeParameter<T, D> extends TypeToken<T> {

    private final TypeVariable<Class<D>> typeVariable;

    protected ResolvableTypeParameter(String name) {
        this(null, name);
    }

    protected ResolvableTypeParameter(@Nullable Class<D> decl, String name) {
        if(decl == null) {
            decl = (Class<D>) new TypeToken<D>(getClass()){}.getRawType();
        }
        this.typeVariable = Types.typeVariable(decl, name);

        if(!isAssignableFrom(typeVariable)) {
            throw new IllegalArgumentException("Type variable " + typeVariable + " is not assignable to " + this);
        }
    }

    public TypeVariable<Class<D>> typeVariable() {
        return typeVariable;
    }

    public TypeToken<? extends T> resolveIn(TypeToken<? extends D> context) {
        return (TypeToken<? extends T>) context.resolveType(typeVariable);
    }
}
