package tc.oc.pgm.map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.jdom2.Document;
import org.jdom2.Element;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.parser.ElementParser;

/**
 * Provides {@link T} by parsing the root {@link Element}
 * of the {@link Document}, using an {@link ElementParser<T>}.
 */
public class RootElementParsingProvider<T> implements ParsingProvider<T> {

    private final Provider<ElementParser<T>> parserProvider;
    private final Provider<Document> documentProvider;

    @Inject private RootElementParsingProvider(Provider<ElementParser<T>> parserProvider, Provider<Document> documentProvider) {
        this.parserProvider = parserProvider;
        this.documentProvider = documentProvider;
    }

    @Override
    public T parse() throws InvalidXMLException {
        return parserProvider.get().parseElement(documentProvider.get().getRootElement());
    }
}
