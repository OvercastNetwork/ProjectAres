package tc.oc.pgm.xml.parser;

import org.bukkit.Material;
import tc.oc.commons.bukkit.util.NMSHacks;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

public class MaterialParser extends PrimitiveParser<Material> {
    @Override
    protected Material parseInternal(Node node, String text) throws FormatException, InvalidXMLException {
        Material material = NMSHacks.materialByKey(text);
        if(material != null) return material;

        material = Material.matchMaterial(text);
        if(material != null) return material;

        throw new InvalidXMLException("Unknown material '" + text + "'", node);
    }
}
