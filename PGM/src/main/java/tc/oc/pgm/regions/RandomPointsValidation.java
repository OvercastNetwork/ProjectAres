package tc.oc.pgm.regions;

import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;
import tc.oc.pgm.xml.validate.Validation;

public class RandomPointsValidation implements Validation<Region> {
    public static final RandomPointsValidation INSTANCE = new RandomPointsValidation();

    @Override
    public void validate(Region region, Node node) throws InvalidXMLException {
        if(!region.canGetRandom()) {
            throw new InvalidXMLException("Cannot generate random points in region type " + region.getClass().getSimpleName(), node);
        }
    }
}
