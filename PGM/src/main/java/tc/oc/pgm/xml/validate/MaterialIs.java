package tc.oc.pgm.xml.validate;

import javax.annotation.Nullable;

import org.bukkit.Material;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

public class MaterialIs {

    public static class Block implements Validation<Material> {
        @Override
        public void validate(Material material, @Nullable Node node) throws InvalidXMLException {
            if(!material.isBlock()) {
                throw new InvalidXMLException("Material " + material.name() + " is not a block", node);
            }
        }
    }
}
