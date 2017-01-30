package tc.oc.pgm.regions;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import com.google.common.collect.Range;
import org.bukkit.util.ImVector;
import org.bukkit.util.Vector;
import org.jdom2.Attribute;
import org.jdom2.Element;
import tc.oc.pgm.features.FeatureDefinitionParser;
import tc.oc.pgm.features.MagicMethodFeatureParser;
import tc.oc.pgm.utils.MethodParser;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowFunction;
import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowSupplier;

public class RegionDefinitionParser extends MagicMethodFeatureParser<Region> implements FeatureDefinitionParser<Region> {

    @Inject private RegionParser regionParser;

    private Region parseHalves(Element el, double dir) throws InvalidXMLException {
        final List<HalfspaceRegion> halves = new ArrayList<>();

        XMLUtils.parseNumber(el, "x", Double.class).infinity(true).optional().ifPresent(x ->
            halves.add(new HalfspaceRegion(new Vector(x, 0, 0), new Vector(dir, 0, 0)))
        );

        XMLUtils.parseNumber(el, "y", Double.class).infinity(true).optional().ifPresent(y ->
            halves.add(new HalfspaceRegion(new Vector(0, y, 0), new Vector(0, dir, 0)))
        );

        XMLUtils.parseNumber(el, "z", Double.class).infinity(true).optional().ifPresent(z ->
            halves.add(new HalfspaceRegion(new Vector(0, 0, z), new Vector(0, 0, dir)))
        );

        if(halves.isEmpty()) {
            throw new InvalidXMLException("Expected at least one of x, y, or z attributes", el);
        }

        return new Intersection(halves);
    }

    @MethodParser
    public HalfspaceRegion half(Element el) throws InvalidXMLException {
        Vector normal = XMLUtils.parseVector(XMLUtils.getRequiredAttribute(el, "normal"));
        if(normal.lengthSquared() == 0) {
            throw new InvalidXMLException("normal must have a non-zero length", el);
        }

        Vector origin = XMLUtils.parseVector(el.getAttribute("origin"), new Vector());

        return new HalfspaceRegion(origin, normal);
    }

    @MethodParser
    public Region below(Element el) throws InvalidXMLException {
        return parseHalves(el, -1);
    }

    @MethodParser
    public Region above(Element el) throws InvalidXMLException {
        return parseHalves(el, 1);
    }

    @MethodParser
    public PointRegion point(Element el) throws InvalidXMLException {
        return new PointRegion(XMLUtils.parseVector(new Node(el)));
    }

    @MethodParser
    public CuboidRegion cuboid(Element el) throws InvalidXMLException {
        Vector min = XMLUtils.parseVector(el.getAttribute("min"));
        Vector max = XMLUtils.parseVector(el.getAttribute("max"));
        Vector size = XMLUtils.parseVector(el.getAttribute("size"));

        if(min != null && max != null && size == null) {
            return new CuboidRegion(min, max);
        } else if(min != null && max == null && size != null) {
            return new CuboidRegion(min, min.plus(size));
        } else if(min == null && max != null && size != null) {
            return new CuboidRegion(max.clone().subtract(size), max);
        } else {
            throw new InvalidXMLException("cuboid must specify exactly two of 'min', 'max', and 'size' attributes", el);
        }
    }

    @MethodParser
    public CylindricalRegion cylinder(Element el) throws InvalidXMLException {
        Vector base = XMLUtils.parseVector(el.getAttribute("base"));
        if(base == null) {
            throw new InvalidXMLException("Cylindrical region must specify valid base vector.", el);
        }
        try {
            final double radius = XMLUtils.parseNumber(Node.fromRequiredAttr(el, "radius"), Double.class, true);
            final double top = Node.tryAttr(el, "top")
                                   .map(rethrowFunction(node -> XMLUtils.parseNumber(node, Double.class, true)))
                                   .orElseGet(rethrowSupplier(() -> base.getY() + XMLUtils.parseNumber(Node.fromRequiredAttr(el, "height"), Double.class, true)));
            return new CylindricalRegion(base, radius, top);
        } catch (NumberFormatException e) {
            throw new InvalidXMLException("Cylindrical region must specify valid radius and height.", el);
        }
    }

    @MethodParser
    public Region rectangle(Element el) throws InvalidXMLException {
        Vector min = XMLUtils.parse2DVector(Node.fromRequiredAttr(el, "min"));
        Vector max = XMLUtils.parse2DVector(Node.fromRequiredAttr(el, "max"));
        return new CuboidRegion(ImVector.of(min.getX(), Double.NEGATIVE_INFINITY, min.getZ()),
                                ImVector.of(max.getX(), Double.POSITIVE_INFINITY, max.getZ()));
    }

    @MethodParser
    public BlockRegion block(Element el) throws InvalidXMLException {
        // TODO: remove "location" backwards compatibility with next major map proto bump
        Vector loc = XMLUtils.parseVector(Node.fromAttr(el, "location"));
        if(loc == null) {
            loc = XMLUtils.parseVector(new Node(el));
            if(loc == null) {
                throw new InvalidXMLException("Block region must have valid location vector.", el);
            }
        }
        return new BlockRegion(loc);
    }

    @MethodParser
    public Region union(Element el) throws InvalidXMLException {
        return new Union(regionParser.parseChildList(el));
    }

    @MethodParser
    public Region intersect(Element el) throws InvalidXMLException {
        return new Intersection(regionParser.parseChildList(el, Range.atLeast(1)));
    }

    @MethodParser
    public Region complement(Element el) throws InvalidXMLException {
        final List<Region> regions = regionParser.parseChildList(el, Range.atLeast(2));
        return new Complement(regions.get(0), Union.of(regions.subList(1, regions.size())));
    }

    @MethodParser
    public Region negative(Element el) throws InvalidXMLException {
        return new NegativeRegion(regionParser.parseReferenceAndChildUnion(el));
    }

    @MethodParser
    public Region circle(Element el) throws InvalidXMLException {
        Vector center = XMLUtils.parse2DVector(Node.fromRequiredAttr(el, "center"));
        double radius = XMLUtils.parseNumber(Node.fromRequiredAttr(el, "radius"), Double.class, true);
        return new CylindricalRegion(ImVector.of(center.getX(),
                                                 Double.NEGATIVE_INFINITY,
                                                 center.getZ()),
                                     radius,
                                     Double.POSITIVE_INFINITY);
    }

    @MethodParser
    public SphereRegion sphere(Element el) throws InvalidXMLException {
        return new SphereRegion(XMLUtils.parseVector(Node.fromRequiredAttr(el, "center", "origin")),
                                XMLUtils.parseNumber(Node.fromRequiredAttr(el, "radius"), Double.class, true));
    }

    @MethodParser
    public TranslatedRegion translate(Element el) throws InvalidXMLException {
        Attribute offsetAttribute = el.getAttribute("offset");
        if(offsetAttribute == null) {
            throw new InvalidXMLException("Translate region must have an offset", el);
        }
        Vector offset = XMLUtils.parseVector(offsetAttribute);
        return new TranslatedRegion(regionParser.parseReferenceAndChildUnion(el), offset);
    }

    @MethodParser
    public MirroredRegion mirror(Element el) throws InvalidXMLException {
        Vector normal = XMLUtils.parseVector(XMLUtils.getRequiredAttribute(el, "normal"));
        if(normal.lengthSquared() == 0) {
            throw new InvalidXMLException("normal must have a non-zero length", el);
        }

        Vector origin = XMLUtils.parseVector(el.getAttribute("origin"), new Vector());
        return new MirroredRegion(regionParser.parseReferenceAndChildUnion(el), origin, normal);
    }

    @MethodParser
    public EverywhereRegion everywhere(Element el) throws InvalidXMLException {
        return new EverywhereRegion();
    }

    @MethodParser({"empty", "nowhere"})
    public EmptyRegion empty(Element el) throws InvalidXMLException {
        return new EmptyRegion();
    }
}
