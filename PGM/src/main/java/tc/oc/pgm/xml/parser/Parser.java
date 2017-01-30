package tc.oc.pgm.xml.parser;

import tc.oc.commons.core.reflect.AutoReified;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;
import tc.oc.pgm.xml.Parseable;

/**
 * Base interface for parsers of any particular type {@link T}, which can be anything
 * from simple primitives to elaborate data structures. Subtypes only have to
 * implement {@link #parseInternal(Node)} to convert a {@link Node} into a {@link T},
 * or throw {@link InvalidXMLException} if that fails.
 *
 * There is no special mechanism for passing context or dependencies to parsers,
 * they simply use injection to get whatever they need.
 *
 * Parsers MAY be scoped, so callers should always inject a {@link javax.inject.Provider}
 * and get a fresh instance every time they want to use the parser.
 *
 * Every parser subtype T should somehow bind itself to {@link Parser<T>}, so
 * that it's simple to inject a parser for any type, even dynamically.
 * {@link ParserBinders} can be helpful for doing that.
 *
 * Reflective parsers, i.e. {@link Parseable}s, expect such bindings to exist for all
 * the types they are composed of. In the future, we may also use this system to
 * implement various higher-order parsers.
 */
public interface Parser<T> extends AutoReified<T> {

    T parseInternal(Node node) throws InvalidXMLException;

    default String readableTypeName() {
        return paramToken().getRawType().getSimpleName().toLowerCase();
    }

    default T parse(Node node) throws InvalidXMLException {
        return InvalidXMLException.offeringNode(node, () -> parseInternal(node));
    }
}
