package tc.oc.pgm.xml;

import tc.oc.api.docs.SemanticVersion;
import tc.oc.pgm.map.PGMMap;

/**
 * Thrown when trying to load a {@link PGMMap} with an unsupported proto version
 */
public class UnsupportedMapProtocolException extends InvalidXMLException {
    private final SemanticVersion proto;

    public UnsupportedMapProtocolException(Node node, SemanticVersion proto) {
        super("Unsupported map protocol version", node);
        this.proto = proto;
    }

    public SemanticVersion getProto() {
        return proto;
    }
}
