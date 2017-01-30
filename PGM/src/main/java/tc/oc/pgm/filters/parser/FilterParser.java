package tc.oc.pgm.filters.parser;

import javax.inject.Inject;

import org.jdom2.Document;
import org.jdom2.Element;
import tc.oc.pgm.features.LegacyFeatureParser;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.StaticFilter;
import tc.oc.pgm.filters.query.IQuery;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapRootParser;
import tc.oc.pgm.regions.RegionParser;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;
import tc.oc.pgm.xml.UnrecognizedXMLException;
import tc.oc.pgm.xml.validate.Validation;

import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowConsumer;

public class FilterParser extends LegacyFeatureParser<Filter> implements MapModule, MapRootParser {

    @Inject protected Document xml;
    @Inject protected RegionParser regionParser;
    @Inject protected DynamicFilterValidation dynamicFilterValidation;

    @Override
    public void parse() throws InvalidXMLException {
        defineBuiltInFilters();
        parseTopLevelFilters();
    }

    protected void defineBuiltInFilters() throws InvalidXMLException {
        features.define("always", new StaticFilter(Filter.QueryResponse.ALLOW));
        features.define("never", new StaticFilter(Filter.QueryResponse.DENY));
    }

    protected void parseTopLevelFilters() throws InvalidXMLException {
        // Modern proto treats <filters> and <regions> the same
        for(Element el : XMLUtils.getChildren(xml.getRootElement(), "filters", "regions")) {
            parseChildren(el).count();
        }
    }

    @Override
    protected boolean canIgnore(Element el) throws InvalidXMLException {
        return "apply".equals(el.getName()) || super.canIgnore(el);
    }

    @Override
    public boolean isParseable(Element el) throws InvalidXMLException {
        return super.isParseable(el) || regionParser.isParseable(el);
    }

    @Override
    public Filter parseElement(Element el) throws InvalidXMLException {
        // If we find something unparseable, try parsing it as a region before giving up
        if(super.isParseable(el)) {
            return super.parseElement(el);
        } else if(regionParser.isParseable(el)) {
            return regionParser.parseElement(el);
        } else {
            throw new UnrecognizedXMLException(propertyName(), el);
        }
    }

    @Override
    public FilterPropertyBuilder property(Element element) {
        return property(element, propertyName());
    }

    @Override
    public FilterPropertyBuilder property(Element el, String name) {
        return new FilterPropertyBuilder(el, name);
    }

    public class FilterPropertyBuilder extends PropertyBuilder<FilterPropertyBuilder> {
        public FilterPropertyBuilder(Element element, String name) {
            super(element, name);
        }

        public FilterPropertyBuilder respondsTo(Class<? extends IQuery> queryType) {
            validate(RespondsToQueryValidation.get(queryType));
            return this;
        }

        public FilterPropertyBuilder dynamic() {
            validateTree(dynamicFilterValidation);
            return this;
        }

        public FilterPropertyBuilder validateTree(Validation<? super Filter> validation) {
            validate((filter, node) -> applyValidationToTree(filter, node, validation));
            return this;
        }
    }

    private static void applyValidationToTree(Filter filter, Node node, Validation<? super Filter> validation) throws InvalidXMLException {
        validation.validate(filter, node);
        filter.dependencies(Filter.class).forEach(rethrowConsumer(dep -> applyValidationToTree(dep, node, validation)));
    }
}
