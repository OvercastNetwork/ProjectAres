package tc.oc.pgm.payload;

import com.google.common.collect.Range;
import org.bukkit.util.ImVector;
import org.bukkit.util.Vector;
import org.jdom2.Element;
import tc.oc.pgm.features.FeatureDefinitionParser;
import tc.oc.pgm.features.FeatureParser;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.parser.FilterParser;
import tc.oc.pgm.goals.ProximityMetric;
import tc.oc.pgm.regions.Region;
import tc.oc.pgm.regions.RegionParser;
import tc.oc.pgm.teams.TeamFactory;
import tc.oc.pgm.utils.MaterialPattern;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;
import tc.oc.pgm.xml.property.PropertyBuilderFactory;

import javax.inject.Inject;
import java.time.Duration;

public final class PayloadParser implements FeatureDefinitionParser<PayloadDefinition> {

    private final FilterParser filterParser;
    private final FeatureParser<TeamFactory> teamParser;
    private final PropertyBuilderFactory<ImVector, ?> vectors;

    @Inject
    private PayloadParser(FilterParser filterParser, FeatureParser<TeamFactory> teamParser, PropertyBuilderFactory<ImVector, ?> vectors) {
        this.filterParser = filterParser;
        this.teamParser = teamParser;
        this.vectors = vectors;
    }

    @Override
    public PayloadDefinition parseElement(Element elPayload) throws InvalidXMLException {
        final Vector location = vectors.property(elPayload, "location").required();
        final Vector spawnLocation = vectors.property(elPayload, "spawn-location").optional((ImVector)location);
        final float yaw = XMLUtils.parseNumber(elPayload.getAttribute("yaw"), Float.class, (Float) null);

        Filter captureFilter = filterParser.property(elPayload, "capture-filter").optional(null);
        Filter playerFilter = filterParser.property(elPayload, "player-filter").optional(null);

        String name = elPayload.getAttributeValue("name", "Payload");
        TeamFactory initialOwner = teamParser.property(elPayload, "initial-owner").optional(null);
        TeamFactory owner = teamParser.property(elPayload, "owner").required();
        Duration timeToCapture = XMLUtils.parseDuration(elPayload.getAttribute("capture-time"), Duration.ofSeconds(30));

        double timeMultiplier = XMLUtils.parseNumber(elPayload.getAttribute("time-multiplier"), Double.class,0D);

        final double recoveryRate, decayRate;
        final Node attrIncremental = Node.fromAttr(elPayload, "incremental");
        final Node attrRecovery = Node.fromAttr(elPayload, "recovery");
        final Node attrDecay = Node.fromAttr(elPayload, "decay");

        double emptyDecayRate = XMLUtils.parseNumber(elPayload.getAttribute("empty-decay"), Double.class, 0D);

        if(attrIncremental == null) {
            recoveryRate = XMLUtils.parseNumber(attrRecovery, Double.class, Range.atLeast(0D), 1D);
            decayRate = XMLUtils.parseNumber(attrDecay, Double.class, Range.atLeast(0D), 0D);
        } else {
            if(attrRecovery != null || attrDecay != null) {
                throw new InvalidXMLException("Cannot combine this attribute with 'incremental'", attrRecovery != null ? attrRecovery : attrDecay);
            }
            final boolean incremental = XMLUtils.parseBoolean(attrIncremental, true);
            recoveryRate = incremental ? 1D : Double.POSITIVE_INFINITY;
            decayRate = incremental ? 0D : Double.POSITIVE_INFINITY;
        }

        boolean neutralState = XMLUtils.parseBoolean(elPayload.getAttribute("neutral-state"), true);
        float radius = XMLUtils.parseNumber(elPayload.getAttribute("radius"), Float.class, 5f);
        float height = XMLUtils.parseNumber(elPayload.getAttribute("height"), Float.class, 3f);
        MaterialPattern checkpointMaterial = XMLUtils.parseMaterialPattern(Node.fromAttr(elPayload, "checkpoint-material"));
        float friendlySpeed = XMLUtils.parseNumber(elPayload.getAttribute("friendly-speed"), Float.class, 0f);
        float enemySpeed = XMLUtils.parseNumber(elPayload.getAttribute("enemy-speed"), Float.class, 1f);
        float points = XMLUtils.parseNumber(elPayload.getAttribute("points"), Float.class, 1f);
        float friendlyPoints = XMLUtils.parseNumber(elPayload.getAttribute("friendly-points"), Float.class, 0f);
        boolean showProgress = XMLUtils.parseBoolean(elPayload.getAttribute("show-progress"), true);
        boolean visible = XMLUtils.parseBoolean(elPayload.getAttribute("show"), true);
        Boolean required = XMLUtils.parseBoolean(elPayload.getAttribute("required"), null);

        PayloadDefinition.CaptureCondition captureCondition =
            XMLUtils.parseEnum(Node.fromAttr(elPayload, "capture-rule"),
                               PayloadDefinition.CaptureCondition.class,
                               "capture rule",
                               PayloadDefinition.CaptureCondition.EXCLUSIVE);

        return new PayloadDefinitionImpl(
            name, required, visible,
            location, spawnLocation, yaw, captureFilter, playerFilter,
            timeToCapture, timeMultiplier, recoveryRate, decayRate, emptyDecayRate, initialOwner, owner, captureCondition,
            neutralState, radius, height, checkpointMaterial, friendlySpeed, enemySpeed, points, friendlyPoints, showProgress
        );
    }
}
