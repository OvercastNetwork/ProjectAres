package tc.oc.pgm.xml.validate;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

/**
 * Something that knows how to validate itself
 */
public interface Validatable {
    void validate() throws InvalidXMLException;

    default Validatable offeringNode(@Nullable Node node) {
        if(node == null) return this;

        return () -> {
            try {
                validate();
            } catch(InvalidXMLException e) {
                e.offerNode(node);
                throw e;
            }
        };
    }

    default Validatable offeringNode(Supplier<Node> supplier) {
        return () -> {
            try {
                validate();
            } catch(InvalidXMLException e) {
                e.offerNode(supplier.get());
                throw e;
            }
        };
    }
}
