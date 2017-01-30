package tc.oc.pgm.xml.validate;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

@FunctionalInterface
public interface LocatedValidation<T> {

    void validate(T value) throws InvalidXMLException;

    default Validatable bind(T value) {
        return () -> validate(value);
    }

    default Validatable compose(Supplier<? extends T> supplier) {
        return () -> validate(supplier.get());
    }

    default LocatedValidation<T> offeringNode(@Nullable Node node) {
        if(node == null) return this;

        return value -> {
            try {
                validate(value);
            } catch(InvalidXMLException e) {
                e.offerNode(node);
                throw e;
            }
        };
    }
}
