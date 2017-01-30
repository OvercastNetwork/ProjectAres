package tc.oc.pgm.map;

import javax.inject.Provider;

import tc.oc.commons.core.inject.Injection;
import tc.oc.pgm.xml.InvalidXMLException;

/**
 * A {@link Provider} that can throw {@link InvalidXMLException}s
 */
public interface ParsingProvider<T> extends Provider<T> {

    T parse() throws InvalidXMLException;

    @Override
    default T get() {
        return Injection.wrappingExceptions(this::parse);
    }
}
