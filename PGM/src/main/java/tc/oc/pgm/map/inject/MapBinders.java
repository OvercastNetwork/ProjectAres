package tc.oc.pgm.map.inject;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.Multibinder;
import org.jdom2.Document;
import org.jdom2.Element;
import tc.oc.commons.core.inject.Binders;
import tc.oc.commons.core.reflect.ResolvableType;
import tc.oc.commons.core.reflect.TypeArgument;
import tc.oc.pgm.map.MapRootParser;
import tc.oc.pgm.map.ProvisionAtParseTime;
import tc.oc.pgm.map.RootElementParsingProvider;
import tc.oc.pgm.xml.parser.ElementParser;
import tc.oc.pgm.xml.parser.ParserBinders;

public interface MapBinders extends Binders, ParserBinders {

    default Multibinder<MapRootParser> rootParsers() {
        return inSet(MapRootParser.class);
    }

    default <T> LinkedBindingBuilder<T> provisionAtParseTime(Key<T> key) {
        install(new ProvisionAtParseTime<>(key));
        return bind(key);
    }
    default <T> LinkedBindingBuilder<T> provisionAtParseTime(Class<T> type) { return provisionAtParseTime(Key.get(type)); }
    default <T> LinkedBindingBuilder<T> provisionAtParseTime(TypeLiteral<T> type) { return provisionAtParseTime(Key.get(type)); }

    /**
     * Bind the given key to an {@link ElementParser<T>}, which will be applied to
     * the root {@link Element} of the {@link Document} at parse-time, and the
     * result bound in {@link MapScoped}.
     */
    default <T> LinkedBindingBuilder<ElementParser<T>> bindRootElementParser(Key<T> key) {
        install(new ProvisionAtParseTime<>(key));
        bind(key).toProvider(new ResolvableType<RootElementParsingProvider<T>>(){}
                                 .with(new TypeArgument<T>(key.getTypeLiteral()){}))
                 .in(MapScoped.class);
        return bindElementParser(key);
    }
    default <T> LinkedBindingBuilder<ElementParser<T>> bindRootElementParser(TypeLiteral<T> type) { return bindRootElementParser(Key.get(type)); }
    default <T> LinkedBindingBuilder<ElementParser<T>> bindRootElementParser(Class<T> type) { return bindRootElementParser(Key.get(type)); }
}
