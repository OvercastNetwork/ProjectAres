package tc.oc.pgm.terrain;

import java.io.IOException;
import java.nio.file.Path;
import javax.inject.Inject;

import org.jdom2.Document;
import org.jdom2.Element;
import tc.oc.pgm.map.MapFolder;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;
import tc.oc.pgm.xml.parser.ElementParser;

public class TerrainParser implements ElementParser<TerrainOptions> {

    private final MapFolder mapFolder;
    private final Document doc;

    @Inject private TerrainParser(MapFolder mapFolder, Document doc) {
        this.mapFolder = mapFolder;
        this.doc = doc;
    }

    @Override
    public TerrainOptions parseElement(Element element) throws InvalidXMLException {
        Path worldFolder = mapFolder.getAbsolutePath();
        try {
            worldFolder = worldFolder.toRealPath();
        } catch(IOException e) {
            throw new InvalidXMLException("Problem accessing map folder " + worldFolder, element);
        }

        boolean vanilla = false;
        Long seed = null;
        boolean initialPhysics = false;

        for(Element elTerrain : doc.getRootElement().getChildren("terrain")) {
            vanilla = XMLUtils.parseBoolean(elTerrain.getAttribute("vanilla"), vanilla);
            worldFolder = XMLUtils.parseRelativeFolder(worldFolder, Node.fromAttr(elTerrain, "world"), worldFolder);
            initialPhysics = XMLUtils.parseBoolean(elTerrain.getAttribute("pre-match-physics"), initialPhysics);

            String seedText = elTerrain.getAttributeValue("seed");
            if(seedText != null) {
                try {
                    seed = Long.parseLong(seedText);
                } catch(NumberFormatException e) {
                    seed = (long) seedText.hashCode();
                }
            }
        }

        return new TerrainOptions(worldFolder, vanilla, seed, initialPhysics);
    }
}
