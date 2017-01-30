package tc.oc.pgm.xml.property;

import org.jdom2.Element;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;
import tc.oc.pgm.xml.parser.PrimitiveParser;

public abstract class TransfiniteProperty<T extends Comparable, Self extends TransfiniteProperty<T, Self>> extends ComparableProperty<T, Self> {

    boolean infinity;

    public TransfiniteProperty(Element parent, String name, PrimitiveParser<T> parser) {
        super(parent, name, parser);
    }

    public Self infinity(boolean allow) {
        infinity = allow;
        return self();
    }

    @Override
    protected T parseAndValidate(Node node, String text) throws InvalidXMLException {
        final T value = super.parseAndValidate(node, text);
        if(!infinity && !isFinite(value)) {
            throw new InvalidXMLException("Value must be finite", node);
        }
        return value;
    }

    protected abstract boolean isFinite(T value);
}
