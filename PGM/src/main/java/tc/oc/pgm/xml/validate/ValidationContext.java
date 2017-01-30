package tc.oc.pgm.xml.validate;

import java.util.stream.Stream;
import javax.annotation.Nullable;

import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowConsumer;

/**
 * A context in which validations can be registered to run now or in the future
 */
public interface ValidationContext {

    <T extends Validatable> T validate(T validatable) throws InvalidXMLException;

    default <T> T validate(T value, @Nullable Node node, Validation<? super T>... validations) throws InvalidXMLException {
        return validate(value, node, Stream.of(validations));
    }

    default <T> T validate(T value, @Nullable Node node, Stream<Validation<? super T>> validations) throws InvalidXMLException {
        validate(() -> validations.forEach(rethrowConsumer(validation -> validation.validate(value, node))));
        return value;
    }
}



