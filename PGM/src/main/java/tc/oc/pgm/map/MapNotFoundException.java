package tc.oc.pgm.map;

import java.nio.file.Path;

import tc.oc.pgm.xml.InvalidXMLException;

/**
 * Thrown when the main XML file of a loaded map disappears,
 * or when no maps are found by the map loader.
 */
public class MapNotFoundException extends InvalidXMLException {

    public MapNotFoundException() {
        super("No maps could be loaded");
    }

    public MapNotFoundException(Path path) {
        super("Failed to load map from " + path);
    }
}
