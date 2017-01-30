package tc.oc.commons.core.inspect;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringEscapeUtils;
import tc.oc.commons.core.util.Chain;

public class TextInspector implements Inspector<String> {

    @Override
    public String scalar(Object value, Inspection options) {
        if(options.quote()) {
            if(value instanceof Character) {
                final char c = (char) value;
                switch(c) {
                    case '\'': return "'\\''";
                    case '"': return "'\"'";
                    default: return "'" + StringEscapeUtils.escapeJava(String.valueOf(c)) + "'";
                }
            } else if(value instanceof String) {
                return "\"" + StringEscapeUtils.escapeJava((String) value) + "\"";
            }
        }

        if(value instanceof Class) {
            // Short class names are usually enough
            return ((Class) value).getSimpleName();
        }

        // everything else
        return String.valueOf(value);
    }

    @Override
    public <E> String collection(Collection<E> collection, Stream<String> elements, Inspection options) {
        return "[" + elements.collect(Collectors.joining(", ")) + "]";
    }

    @Override
    public <K, V> String map(Map<K, V> map, Stream<Map.Entry<String, String>> entries, Inspection options) {
        return "{" + entries.map(entry -> entry.getKey() + " -> " + entry.getValue())
                            .collect(Collectors.joining(", ")) +
               "}";
    }

    @Override
    public String reference(Inspectable inspectable, Inspection options) {
        return inspectable.identify();
    }

    @Override
    public String inspectable(Inspectable inspectable, Stream<Map.Entry<String, String>> properties, Inspection options) {
        return "{" + Stream.concat(Stream.of(inspectable.identify()),
                                   properties.map(p -> p.getKey() + "=" + p.getValue()))
                           .collect(Collectors.joining(" ")) +
               "}";
    }

    @Override
    public String exception(InspectionException e, Inspection options) {
        return "(" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")";
    }

    @Override
    public String cycle(Object value, Chain<Object> path, Inspection options) {
        return "...";
    }
}
