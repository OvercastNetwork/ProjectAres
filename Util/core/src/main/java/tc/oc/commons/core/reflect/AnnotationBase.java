package tc.oc.commons.core.reflect;

import java.lang.annotation.Annotation;

/**
 * Generic implementation of {@link Annotation}
 */
public abstract class AnnotationBase implements Annotation {

    @Override
    public Class<? extends Annotation> annotationType() {
        return Annotations.annotationType(getClass());
    }

    @Override
    public int hashCode() {
        return Annotations.hashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return Annotations.equals(this, obj);
    }

    @Override
    public String toString() {
        return Annotations.toString(this);
    }
}
