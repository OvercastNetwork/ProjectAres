package tc.oc.pgm.points;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import org.bukkit.util.Vector;
import org.jdom2.Attribute;
import org.jdom2.Element;
import tc.oc.pgm.features.FeatureDefinitionContext;
import tc.oc.pgm.map.inject.MapScoped;
import tc.oc.pgm.regions.PointRegion;
import tc.oc.pgm.regions.RandomPointsValidation;
import tc.oc.pgm.regions.Region;
import tc.oc.pgm.regions.RegionParser;
import tc.oc.pgm.regions.Union;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowConsumer;

/**
 * PointProvider grammar is a bit strange due to backward compatibility. The root element is what
 * the caller passes to {@link #parse}, and it can have any name at all. This element can either
 * be a container of PointProvider sub-elements, or a {@link PointRegion}. Child elements are
 * parsed as {@link Region}s and wrapped in {@link RegionPointProvider}s, EXCEPT for <point>s,
 * which are treated the same as the root element.
 */
@MapScoped
public class PointParser {

    private final FeatureDefinitionContext features;
    private final RegionParser regionParser;

    @Inject private PointParser(FeatureDefinitionContext features, RegionParser regionParser) {
        this.features = features;
        this.regionParser = regionParser;
    }

    private Region validate(Region region, Node node) throws InvalidXMLException {
        features.validate(region, node, (Region region0, Node node0) ->
            Union.expand(region0)
                 .forEach(rethrowConsumer(sub -> RandomPointsValidation.INSTANCE.validate(sub, features.sourceNode(sub))))
        );
        return region;
    }

    /**
     * Parse any number of {@link PointProvider}s in attributes or children of the given names.
     */
    public List<PointProvider> parseMultiProperty(Element el, PointProviderAttributes parentAttributes, String name, String... aliases) throws InvalidXMLException {
        final PointProviderAttributes attributes = parseAttributes(el, parentAttributes);

        List<PointProvider> providers = new ArrayList<>();
        Node.attributes(el, name, aliases).forEach(rethrowConsumer(attr -> {
            providers.add(new RegionPointProvider(validate(regionParser.parseReference(attr), attr), attributes));
        }));
        Node.elements(el, name, aliases).forEach(rethrowConsumer(child -> {
            providers.add(new RegionPointProvider(validate(regionParser.parseChild(child.asElement()), child), parseAttributes(child.asElement(), attributes)));
        }));
        return providers;
    }

    /**
     * Parse the given element as a container for {@link PointProvider}s. The given element is not itself
     * parsed as a PointProvider, but its attributes are inherited by any contained PointProviders.
     */
    public List<PointProvider> parseChildren(Element el, PointProviderAttributes attributes) throws InvalidXMLException {
        return parseChildren(new ArrayList<>(), el, attributes);
    }

    /**
     * Parse the given element as a {@link PointProvider} or container of PointProviders.
     */
    public List<PointProvider> parse(Element el, PointProviderAttributes attributes) throws InvalidXMLException {
        return parsePoint(new ArrayList<>(), el, attributes);
    }

    private List<PointProvider> parsePoint(List<PointProvider> providers, Element el, PointProviderAttributes attributes) throws InvalidXMLException {
        attributes = parseAttributes(el, attributes);
        if(el.getChildren().isEmpty()) {
            // If it has no children, parse it as a Point region (regardless of the element name)
            providers.add(new RegionPointProvider(new PointRegion(XMLUtils.parseVector(Node.of(el))), attributes));
        } else {
            // If it does have children, parse it as a container
            parseChildren(providers, el, attributes);
        }
        return providers;
    }

    private List<PointProvider> parseChildren(List<PointProvider> providers, Element el, PointProviderAttributes attributes) throws InvalidXMLException {
        attributes = parseAttributes(el, attributes);
        for(Element elChild : el.getChildren()) {
            parseChild(providers, elChild, attributes);
        }
        return providers;
    }

    private List<PointProvider> parseChild(List<PointProvider> providers, Element el, PointProviderAttributes attributes) throws InvalidXMLException {
        attributes = parseAttributes(el, attributes);
        if("point".equals(el.getName())) {
            // For legacy compatibility, <point> is treated specially
            parsePoint(providers, el, attributes);
        } else {
            // Anything else is parsed as a region
            parseRegion(providers, el, attributes);
        }
        return providers;
    }

    private void parseRegion(List<PointProvider> providers, Element el, PointProviderAttributes attributes) throws InvalidXMLException {
        providers.add(new RegionPointProvider(validate(regionParser.parseElement(el), new Node(el)), attributes));
    }

    AngleProvider parseStaticAngleProvider(Attribute attr) throws InvalidXMLException {
        Float angle = XMLUtils.parseNumber(attr, Float.class, (Float) null);
        return angle == null ? null : new StaticAngleProvider(angle);
    }

    public PointProviderAttributes parseAttributes(Element el, PointProviderAttributes attributes) throws InvalidXMLException {
        boolean safe = XMLUtils.parseBoolean(el.getAttribute("safe"), attributes.isSafe());
        boolean outdoors = XMLUtils.parseBoolean(el.getAttribute("outdoors"), attributes.isOutdoors());

        Vector target = XMLUtils.parseVector(el.getAttribute("angle"), (Vector) null);
        if(target != null) {
            return new PointProviderAttributes(new DirectedYawProvider(target), new DirectedPitchProvider(target), safe, outdoors);
        }

        AngleProvider yawProvider = parseStaticAngleProvider(el.getAttribute("yaw"));
        AngleProvider pitchProvider = parseStaticAngleProvider(el.getAttribute("pitch"));
        if(yawProvider != null || pitchProvider != null || safe != attributes.isSafe()) {
            return new PointProviderAttributes(yawProvider, pitchProvider, safe, outdoors);
        }

        return attributes;
    }
}
