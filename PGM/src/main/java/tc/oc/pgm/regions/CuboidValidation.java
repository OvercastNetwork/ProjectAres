package tc.oc.pgm.regions;

import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;
import tc.oc.pgm.xml.validate.Validation;

public class CuboidValidation implements Validation<Region> {
    public static final CuboidValidation INSTANCE = new CuboidValidation();

    @Override
    public void validate(Region region, Node node) throws InvalidXMLException {
        if(!(region instanceof CuboidRegion)) {
            throw new InvalidXMLException("region must be a cuboid", node);
        }
    }
}
