package tc.oc.parse.xml;

import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.google.inject.Provides;
import tc.oc.commons.core.inject.Manifest;

public class XMLManifest extends Manifest {

    @Provides @Singleton
    DocumentBuilderFactory documentBuilderFactory() {
        return DocumentBuilderFactory.newInstance();
    }

    @Provides
    DocumentBuilder documentBuilder(DocumentBuilderFactory factory) throws ParserConfigurationException {
        return factory.newDocumentBuilder();
    }
}
