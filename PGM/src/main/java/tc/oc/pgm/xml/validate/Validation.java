package tc.oc.pgm.xml.validate;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import javax.annotation.Nullable;

import com.google.common.reflect.TypeToken;
import tc.oc.commons.core.reflect.TypeParameterCache;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowConsumer;

/**
 * A validation that can be applied to any {@link T} instance
 */
@FunctionalInterface
public interface Validation<T> {
    TypeParameterCache<Validation, Object> CACHE_T = new TypeParameterCache<>(Validation.class, "T");

    static TypeToken<?> type(Class<? extends Validation> validation) {
        return CACHE_T.resolve(validation);
    }

    default TypeToken<T> type() {
        return (TypeToken<T>) type((Class<Validation<T>>) getClass());
    }

    void validate(T value, @Nullable Node node) throws InvalidXMLException;

    default Validatable bind(T value, @Nullable Node node) {
        return ((Validatable) () -> validate(value, node)).offeringNode(node);
    }

    default LocatedValidation<T> bind(@Nullable Node node) {
        return ((LocatedValidation<T>) value -> validate(value, node)).offeringNode(node);
    }

    default Validation<T> findingNode(Function<T, Node> finder) {
        return (value, node) -> {
            if(node == null) {
                node = finder.apply(value);
            }
            try {
                validate(value, node);
            } catch(InvalidXMLException e) {
                e.offerNode(node);
                throw e;
            }
        };
    }

    static <T> Validation<T> all(Validation<? super T>... validations) {
        return all(Arrays.asList(validations));
    }

    static <T> Validation<T> all(Collection<? extends Validation<? super T>> validations) {
        return (value, node) -> validations.forEach(rethrowConsumer(validation -> validation.validate(value, node)));
    }
}
