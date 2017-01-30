package tc.oc.parse;

import tc.oc.commons.core.reflect.AutoReified;

/**
 * Creates {@link T}s from {@link String}s
 *
 * Not coupled to any particular document type
 */
public interface Parser<T> extends AutoReified<T> {

    T parse(String text) throws ParseException;

    default String readableTypeName() {
        return paramToken().getRawType().getSimpleName().toLowerCase();
    }
}
