package tc.oc.pgm.xml.parser;

import javax.inject.Inject;

import org.bukkit.Material;
import org.bukkit.material.MaterialData;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

public class MaterialDataParser extends PrimitiveParser<MaterialData> {

    private final MaterialParser materialParser;
    private final NumberParser<Byte> byteParser;

    @Inject MaterialDataParser(MaterialParser materialParser, NumberParser<Byte> byteParser) {
        this.materialParser = materialParser;
        this.byteParser = byteParser;
    }

    @Override
    protected MaterialData parseInternal(Node node, String text) throws FormatException, InvalidXMLException {
        final String[] pieces = text.split(":");
        final Material material = materialParser.parse(node, pieces[0]);
        final byte data;
        if(pieces.length > 1) {
            data = byteParser.parse(node, pieces[1]);
        } else {
            data = 0;
        }
        return material.getNewData(data);
    }
}
