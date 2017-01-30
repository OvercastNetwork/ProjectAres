package tc.oc.pgm.xml;

import javax.inject.Inject;

import org.jdom2.JDOMFactory;
import org.jdom2.input.sax.SAXHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class BoundedSAXHandler extends SAXHandler {

    @Inject public BoundedSAXHandler(JDOMFactory factory) {
        super(factory);
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        super.startElement(namespaceURI, localName, qName, atts);
        ((BoundedElement) getCurrentElement()).setStartLine(getDocumentLocator().getLineNumber());
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        ((BoundedElement) getCurrentElement()).setEndLine(getDocumentLocator().getLineNumber());
        super.endElement(namespaceURI, localName, qName);
    }
}
