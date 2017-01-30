package tc.oc.pgm.regions;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;

import org.jdom2.Element;
import tc.oc.api.docs.SemanticVersion;
import tc.oc.commons.core.util.Streams;
import tc.oc.pgm.features.LegacyFeatureParser;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapProto;
import tc.oc.pgm.map.MapRootParser;
import tc.oc.pgm.map.ProtoVersions;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowFunction;

public class RegionParser extends LegacyFeatureParser<Region> implements MapModule, MapRootParser {

    @Inject protected @MapProto SemanticVersion proto;

    @Override
    public void parse() throws InvalidXMLException {
        defineBuiltInRegions();
    }

    protected void defineBuiltInRegions() throws InvalidXMLException {
        if(!legacy) {
            features.define("everywhere", new EverywhereRegion());
            features.define("nowhere", new EmptyRegion());
        }
    }

    @Override
    protected boolean canIgnore(Element el) throws InvalidXMLException {
        return "apply".equals(el.getName()) || super.canIgnore(el);
    }

    public List<Region> parseReferenceAndChildren(Element el) throws InvalidXMLException {
        return Stream.concat(Streams.compact(Node.tryAttr(el, propertyName())
                                                 .map(rethrowFunction(this::parseReference))),
                             parseChildren(el))
                     .collect(Collectors.toList());
    }

    public Region parseReferenceAndChildUnion(Element el) throws InvalidXMLException {
        return Union.of(parseReferenceAndChildren(el));
    }

    @Override
    public RegionPropertyBuilder property(Element element) {
        return new RegionPropertyBuilder(element, propertyName());
    }

    @Override
    public RegionPropertyBuilder property(Element element, String name) {
        return new RegionPropertyBuilder(element, name);
    }

    public class RegionPropertyBuilder extends PropertyBuilder<RegionPropertyBuilder> {

        private boolean legacy;

        public RegionPropertyBuilder(Element element, String name) {
            super(element, name);
        }

        public RegionPropertyBuilder legacy() {
            legacy = true;
            return this;
        }

        @Override
        protected Optional<List<Region>> parseParent() throws InvalidXMLException {
            // Legacy syntax for several modules with regions directly under the parent element
            if(legacy && proto.isOlderThan(ProtoVersions.MODULE_SUBELEMENT_VERSION)) {
                return Optional.of(parseReferenceAndChildren(element));
            } else {
                return super.parseParent();
            }
        }

        public Region union() throws InvalidXMLException {
            return Union.of(multi());
        }

        public Optional<Region> optionalUnion() throws InvalidXMLException {
            return optionalMulti().map(Union::of);
        }

        public Region optionalUnion(Region def) throws InvalidXMLException {
            return optionalUnion().orElse(def);
        }
    }
}
