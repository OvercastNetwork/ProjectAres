package tc.oc.pgm.xml.property;

import com.google.common.collect.Range;
import org.jdom2.Element;
import tc.oc.commons.core.util.Ranges;
import tc.oc.pgm.xml.parser.PrimitiveParser;
import tc.oc.pgm.xml.InvalidXMLException;

public class ComparableProperty<T extends Comparable, Self extends ComparableProperty<T, Self>> extends PropertyBuilder<T, Self> {

    public ComparableProperty(Element parent, String name, PrimitiveParser<T> parser) {
        super(parent, name, parser);
    }

    public Self range(Range<T> range) {
        validate((value, node) -> {
            if(!range.contains(value)) {
                throw new InvalidXMLException("Value must be " + Ranges.describe(range), node);
            }
        });
        return self();
    }
}
