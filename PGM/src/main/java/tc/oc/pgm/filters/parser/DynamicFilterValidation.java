package tc.oc.pgm.filters.parser;

import javax.inject.Inject;
import javax.inject.Singleton;

import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;
import tc.oc.pgm.xml.validate.Validation;

@Singleton
public class DynamicFilterValidation implements Validation<Filter> {

    public static final DynamicFilterValidation INSTANCE = new DynamicFilterValidation();

    @Inject private DynamicFilterValidation() {}

    @Override
    public void validate(Filter filter, Node node) throws InvalidXMLException {
        if(!filter.isDynamic()) {
            throw new InvalidXMLException("Filter type " + filter.getDefinitionType().getSimpleName() +
                                          " cannot be used in a dynamic context", node);
        }
    }
}
