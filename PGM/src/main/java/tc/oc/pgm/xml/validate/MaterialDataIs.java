package tc.oc.pgm.xml.validate;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.bukkit.material.MaterialData;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

public class MaterialDataIs {

    public static class Block implements Validation<MaterialData> {

        private final MaterialIs.Block materialIsBlock;

        @Inject
        Block(MaterialIs.Block materialIsBlock) {
            this.materialIsBlock = materialIsBlock;
        }

        @Override
        public void validate(MaterialData material, @Nullable Node node) throws InvalidXMLException {
            materialIsBlock.validate(material.getItemType(), node);
        }
    }
}
