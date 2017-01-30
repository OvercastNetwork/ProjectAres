package tc.oc.pgm.kits;

import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;
import tc.oc.pgm.xml.validate.Validation;

import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowConsumer;

public class RemovableValidation implements Validation<Kit> {

    private static final RemovableValidation INSTANCE = new RemovableValidation();
    public static RemovableValidation get() {
        return INSTANCE;
    }

    private RemovableValidation() {}

    @Override
    public void validate(Kit root, Node node) throws InvalidXMLException {
        root.deepDependencies(Kit.class).forEach(rethrowConsumer(kit -> {
            if(!kit.isRemovable()) {
                throw new InvalidXMLException("Kit type " + kit.getDefinitionType().getSimpleName() + " is not removable", node);
            }
        }));
    }
}
