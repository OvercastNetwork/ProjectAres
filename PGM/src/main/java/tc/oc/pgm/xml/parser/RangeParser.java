package tc.oc.pgm.xml.parser;

import javax.inject.Inject;

import com.google.common.collect.Range;
import com.google.inject.TypeLiteral;
import tc.oc.commons.core.reflect.Types;
import tc.oc.commons.core.util.Ranges;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

public class RangeParser<N extends Comparable<N>> extends PrimitiveParser<Range<N>> {

    private final PrimitiveParser<N> domainParser;

    @Inject private RangeParser(TypeLiteral<N> domainType, PrimitiveParser<N> domainParser) {
        super(Ranges.typeOf(Types.toToken(domainType)));
        this.domainParser = domainParser;
    }

    @Override
    protected Range<N> parseInternal(Node node, String text) throws FormatException, InvalidXMLException {
        final String[] parts = text.split("\\.\\.");
        switch(parts.length) {
            case 1: return Range.singleton(domainParser.parse(node, text));
            case 2: return Range.closed(domainParser.parse(node, parts[0]),
                                        domainParser.parse(node, parts[1]));
        }
        throw new FormatException();
    }
}
