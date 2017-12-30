package tc.oc.pgm.control.point;

import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;
import javax.inject.Inject;

import com.google.common.collect.Range;
import org.bukkit.Material;
import org.jdom2.Element;
import tc.oc.pgm.features.FeatureDefinitionParser;
import tc.oc.pgm.features.FeatureParser;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.block.MaterialFilter;
import tc.oc.pgm.filters.operator.AnyFilter;
import tc.oc.pgm.filters.parser.FilterParser;
import tc.oc.pgm.regions.BlockBoundedValidation;
import tc.oc.pgm.regions.Region;
import tc.oc.pgm.regions.RegionParser;
import tc.oc.pgm.teams.TeamFactory;
import tc.oc.pgm.utils.MaterialPattern;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

public final class ControlPointParser implements FeatureDefinitionParser<ControlPointDefinition> {

    private static final Filter VISUAL_MATERIALS = AnyFilter.of(Stream.of(Material.WOOL,
                                                                          Material.CARPET,
                                                                          Material.STAINED_CLAY,
                                                                          Material.STAINED_GLASS,
                                                                          Material.STAINED_GLASS_PANE)
                                                                      .map(material -> MaterialFilter.of(MaterialPattern.accepting(material))));
    private final RegionParser regionParser;
    private final FilterParser filterParser;
    private final FeatureParser<TeamFactory> teamParser;

    @Inject private ControlPointParser(RegionParser regionParser, FilterParser filterParser, FeatureParser<TeamFactory> teamParser) {
        this.regionParser = regionParser;
        this.filterParser = filterParser;
        this.teamParser = teamParser;
    }

    @Override
    public ControlPointDefinition parseElement(Element elControlPoint) throws InvalidXMLException {
        final boolean koth = "hill".equals(elControlPoint.getName());
        Region captureRegion = regionParser.property(elControlPoint, "capture-region")
                                           .alias("capture")
                                           .union();
        Region progressDisplayRegion = regionParser.property(elControlPoint, "progress-display-region")
                                                   .alias("progress-display", "progress")
                                                   .validate(BlockBoundedValidation.INSTANCE)
                                                   .optionalUnion(null);
        Region ownerDisplayRegion = regionParser.property(elControlPoint, "owner-display-region")
                                                .alias("owner-display", "captured")
                                                .validate(BlockBoundedValidation.INSTANCE)
                                                .optionalUnion(null);
        Filter captureFilter = filterParser.property(elControlPoint, "capture-filter").optional(null);
        Filter playerFilter = filterParser.property(elControlPoint, "player-filter").optional(null);
        Filter visualMaterials = filterParser.property(elControlPoint, "visual-materials")
                                             .optionalMulti()
                                             .<Filter>map(AnyFilter::new)
                                             .orElse(VISUAL_MATERIALS);
        String name = elControlPoint.getAttributeValue("name", "Hill");
        TeamFactory initialOwner = teamParser.property(elControlPoint, "initial-owner").optional(null);
        Duration timeToCapture = XMLUtils.parseDuration(elControlPoint.getAttribute("capture-time"), Duration.ofSeconds(30));
        double timeMultiplier = XMLUtils.parseNumber(elControlPoint.getAttribute("time-multiplier"), Double.class, koth ? 0.1D : 0D);
        final double recoveryRate, decayRate;
        final Node attrIncremental = Node.fromAttr(elControlPoint, "incremental");
        final Node attrRecovery = Node.fromAttr(elControlPoint, "recovery");
        final Node attrDecay = Node.fromAttr(elControlPoint, "decay");
        if(attrIncremental == null) {
            recoveryRate = XMLUtils.parseNumber(attrRecovery, Double.class, Range.atLeast(0D), koth ? 1D : Double.POSITIVE_INFINITY);
            decayRate = XMLUtils.parseNumber(attrDecay, Double.class, Range.atLeast(0D), koth ? 0D : Double.POSITIVE_INFINITY);
        } else {
            if(attrRecovery != null || attrDecay != null) {
                throw new InvalidXMLException("Cannot combine this attribute with 'incremental'", attrRecovery != null ? attrRecovery : attrDecay);
            }
            final boolean incremental = XMLUtils.parseBoolean(attrIncremental, koth);
            recoveryRate = incremental ? 1D : Double.POSITIVE_INFINITY;
            decayRate = incremental ? 0D : Double.POSITIVE_INFINITY;
        }
        boolean neutralState = XMLUtils.parseBoolean(elControlPoint.getAttribute("neutral-state"), koth);
        double neutralRate = XMLUtils.parseNumber(Node.fromAttr(elControlPoint, "rollback"), Double.class, Range.atLeast(0D), 0.0);
        boolean permanent = XMLUtils.parseBoolean(elControlPoint.getAttribute("permanent"), false);
        float pointsOwned = XMLUtils.parseNumber(elControlPoint.getAttribute("owner-points"), Float.class, 0f);
        float pointsPerSecond = XMLUtils.parseNumber(elControlPoint.getAttribute("points"), Float.class, 1f);
        float pointsGrowth = XMLUtils.parseNumber(elControlPoint.getAttribute("points-growth"), Float.class, Float.POSITIVE_INFINITY);
        boolean showProgress = XMLUtils.parseBoolean(elControlPoint.getAttribute("show-progress"), koth);
        boolean visible = XMLUtils.parseBoolean(elControlPoint.getAttribute("show"), true);
        Boolean required = XMLUtils.parseBoolean(elControlPoint.getAttribute("required"), null);
        ControlPointDefinition.CaptureCondition captureCondition =
            XMLUtils.parseEnum(Node.fromAttr(elControlPoint, "capture-rule"),
                               ControlPointDefinition.CaptureCondition.class,
                               "capture rule",
                               ControlPointDefinition.CaptureCondition.EXCLUSIVE);
        return new ControlPointDefinitionImpl(
            name, required, visible, captureFilter, playerFilter,
            timeToCapture, timeMultiplier, recoveryRate, decayRate, neutralRate,
            Optional.ofNullable(initialOwner), captureCondition, neutralState, permanent,
            pointsOwned, pointsPerSecond, pointsGrowth, showProgress,
            captureRegion, progressDisplayRegion, ownerDisplayRegion, visualMaterials
        );
    }

}
