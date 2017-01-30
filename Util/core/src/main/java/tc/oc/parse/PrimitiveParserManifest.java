package tc.oc.parse;

import com.google.inject.TypeLiteral;
import tc.oc.commons.core.inject.KeyedManifest;
import tc.oc.commons.core.inject.Manifest;
import tc.oc.commons.core.util.Pair;
import tc.oc.parse.xml.NodeParser;
import tc.oc.parse.xml.PrimitiveNodeParser;

/**
 * Binds {@link Parser<T>} to the given type, and {@link NodeParser<T>} to {@link PrimitiveNodeParser<T>}
 */
public class PrimitiveParserManifest<T> extends KeyedManifest implements ParserTypeLiterals {

    private final TypeLiteral<T> T;
    private final TypeLiteral<? extends Parser<T>> ParserTImpl;

    public PrimitiveParserManifest(Class<T> type, Class<? extends Parser<T>> ParserTImpl) {
        this(TypeLiteral.get(type), TypeLiteral.get(ParserTImpl));
    }

    public PrimitiveParserManifest(TypeLiteral<T> T, TypeLiteral<? extends Parser<T>> ParserTImpl) {
        this.T = T;
        this.ParserTImpl = ParserTImpl;
    }

    @Override
    protected Object manifestKey() {
        return Pair.of(T, ParserTImpl);
    }

    @Override
    protected void configure() {
        bind(ParserTImpl);
        bind(Parser(T)).to(ParserTImpl);
        bind(NodeParser(T)).to(PrimitiveNodeParser(T));
        bind(ElementParser(T)).to(NodeParser(T));
        bind(DocumentParser(T)).to(NodeParser(T));
    }
}
