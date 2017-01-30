package tc.oc.commons.core.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import tc.oc.commons.core.util.Utils;

import static com.google.common.base.Preconditions.checkArgument;

public class TypeParameter<T> extends TypeCapture<T> implements TypeVariable<GenericDeclaration> {

    private final TypeVariable<?> typeVariable;

    public static TypeParameter<?> of(TypeVariable<?> variable) {
        return variable instanceof TypeParameter ? (TypeParameter<?>) variable
                                                 : new TypeParameter<>(variable);
    }

    protected TypeParameter() {
        Type type = capture();
        checkArgument(type instanceof TypeVariable, "%s should be a type variable.", type);
        this.typeVariable = (TypeVariable<?>) type;
    }

    private TypeParameter(TypeVariable<?> variable) {
        this.typeVariable = variable;
    }

    public TypeVariable<?> typeVariable() {
        return typeVariable;
    }

    @Override
    public String toString() {
        return typeVariable.toString();
    }

    @Override
    public int hashCode() {
        return typeVariable.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return Utils.equals(TypeVariable.class, this, obj, that ->
            this.getGenericDeclaration().equals(that.getGenericDeclaration()) &&
            this.getName().equals(that.getName())
        );
    }

    @Override
    public Type[] getBounds() {
        return typeVariable.getBounds();
    }

    @Override
    public GenericDeclaration getGenericDeclaration() {
        return typeVariable.getGenericDeclaration();
    }

    @Override
    public String getName() {
        return typeVariable.getName();
    }

    @Override
    public AnnotatedType[] getAnnotatedBounds() {
        return typeVariable.getAnnotatedBounds();
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return typeVariable.getAnnotation(annotationClass);
    }

    @Override
    public Annotation[] getAnnotations() {
        return typeVariable.getAnnotations();
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return typeVariable.getDeclaredAnnotations();
    }
}
