package tc.oc.pgm.xml.parser;

import javax.inject.Inject;

import com.google.common.cache.LoadingCache;
import org.bukkit.util.ImVector;
import tc.oc.commons.core.util.CacheUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

public class VectorParser<T extends Number> extends PrimitiveParser<ImVector> {

    private static final LoadingCache<Class<? extends Number>, VectorParser<?>> byType = CacheUtils.newCache(
        componentType -> new VectorParser<>(NumberParser.get(componentType))
    );

    @Deprecated // @Inject me!
    public static <T extends Number> VectorParser<T> get(Class<T> type) {
        return (VectorParser<T>) byType.getUnchecked(type);
    }

    private final NumberParser<T> componentParser;

    @Inject private VectorParser(NumberParser<T> componentParser) {
        this.componentParser = componentParser;
    }

    @Override
    public ImVector parseInternal(Node node, String text) throws FormatException, InvalidXMLException {
        String[] components = text.trim().split("\\s*,\\s*");
        if(components.length != 3) throw new FormatException();

        try {
            return ImVector.of(componentParser.parse(node, components[0]).doubleValue(),
                               componentParser.parse(node, components[1]).doubleValue(),
                               componentParser.parse(node, components[2]).doubleValue());
        } catch(NumberFormatException e) {
            throw new FormatException();
        }
    }
}
