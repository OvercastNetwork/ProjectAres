package tc.oc.pgm.xml.property;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import org.jdom2.Element;
import tc.oc.commons.core.util.NumberFactory;
import tc.oc.pgm.xml.parser.NumberParser;
import tc.oc.pgm.xml.parser.PrimitiveParser;

public class NumberProperty<T extends Number & Comparable<T>> extends TransfiniteProperty<T, NumberProperty<T>> {

    private final NumberFactory<T> numberFactory;

    public NumberProperty(Element parent, String name, Class<T> type) {
        this(parent, name, NumberParser.get(type), NumberFactory.get(type));
    }

    @Inject private NumberProperty(@Assisted Element parent, @Assisted String name, PrimitiveParser<T> parser, NumberFactory<T> factory) {
        super(parent, name, parser);
        this.numberFactory = factory;
    }

    @Override
    protected boolean isFinite(T value) {
        return numberFactory.isFinite(value);
    }
}
