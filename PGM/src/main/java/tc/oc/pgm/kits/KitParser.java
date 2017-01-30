package tc.oc.pgm.kits;

import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import org.jdom2.Element;
import tc.oc.pgm.features.LegacyFeatureParser;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.xml.InvalidXMLException;

public class KitParser extends LegacyFeatureParser<Kit> implements MapModule {

    protected boolean hasParentsOrChildren(Element el) {
        return el.getAttribute("parent") != null ||
               el.getAttribute("parents") != null ||
               !el.getChildren().isEmpty();
    }

    @Override
    public boolean isReference(Element el) throws InvalidXMLException {
        if(el.getAttribute(idAttributeName()) == null ||
           hasParentsOrChildren(el)) return false;

        if(legacy) {
            // Default conditions are too strict for legacy XML (why?)
            return "kit".equals(el.getName());
        }

        return super.isReference(el);
    }

    @Override
    public Optional<String> parseDefinitionId(Element el, Kit definition) throws InvalidXMLException {
        if(legacy && !"kit".equals(el.getName())) {
            // Only parse the 'name' attribute as an ID on <kit> elements,
            // because item kits use 'name' for something else
            return Optional.empty();
        }
        return super.parseDefinitionId(el, definition);
    }

    @Override
    public Stream<Kit> parseChildren(Element parent) throws InvalidXMLException {
        return super.parseChildren(parent);
    }

    @Override
    protected boolean canIgnore(Element el) throws InvalidXMLException {
        return super.canIgnore(el) || (el.getName().equals("filter") &&
                                       el.getParentElement() != null &&
                                       "kit".equals(el.getParentElement().getName()));
    }

    @Override
    public KitPropertyBuilder property(Element element) {
        return property(element, propertyName());
    }

    @Override
    public KitPropertyBuilder property(Element element, String name) {
        return new KitPropertyBuilder(element, name);
    }

    public class KitPropertyBuilder extends PropertyBuilder<KitPropertyBuilder> {
        public KitPropertyBuilder(Element element, String name) {
            super(element, name);
        }

        @Override
        protected void parseChild(ImmutableList.Builder<Kit> results, Element child) throws InvalidXMLException {
            // Parse property children as KitNodes, so they can have attributes
            results.add(((KitDefinitionParser) definitionParser.get()).kit(child));
        }
    }
}
