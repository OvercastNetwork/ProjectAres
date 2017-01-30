package tc.oc.pgm.regions;

import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;
import tc.oc.pgm.xml.validate.Validation;

public class BlockBoundedValidation implements Validation<Region> {
    public static final BlockBoundedValidation INSTANCE = new BlockBoundedValidation();

    @Override
    public void validate(Region region, Node node) throws InvalidXMLException {
        if(!region.isBlockBounded()) {
            throw new InvalidXMLException("Cannot enumerate blocks in region", node);
        }
    }
}
