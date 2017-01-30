package tc.oc.pgm.flag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.DyeColor;
import org.bukkit.util.Vector;
import org.jdom2.Document;
import org.jdom2.Element;
import java.time.Duration;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.pgm.features.FeatureParser;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.StaticFilter;
import tc.oc.pgm.filters.parser.FilterParser;
import tc.oc.pgm.goals.ProximityMetric;
import tc.oc.pgm.kits.Kit;
import tc.oc.pgm.kits.KitParser;
import tc.oc.pgm.kits.RemovableValidation;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.map.MapRootParser;
import tc.oc.pgm.points.PointParser;
import tc.oc.pgm.points.PointProvider;
import tc.oc.pgm.points.PointProviderAttributes;
import tc.oc.pgm.regions.Region;
import tc.oc.pgm.regions.RegionParser;
import tc.oc.pgm.teams.TeamFactory;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowFunction;

public class FlagParser implements MapRootParser {

    private final Document document;
    private final MapModuleContext context;
    private final PointParser pointParser;
    private final Logger logger;
    private final FilterParser filterParser;
    private final RegionParser regionParser;
    private final KitParser kitParser;
    private final FeatureParser<TeamFactory> teamParser;
    private final List<FlagDefinition> flags = new ArrayList<>();

    @Inject private FlagParser(Document document, MapModuleContext context, PointParser pointParser, Logger logger, FeatureParser<TeamFactory> teamParser) {
        this.document = document;
        this.context = context;
        this.pointParser = pointParser;
        this.logger = logger;
        this.filterParser = context.needModule(FilterParser.class);
        this.regionParser = context.needModule(RegionParser.class);
        this.kitParser = context.needModule(KitParser.class);
        this.teamParser = teamParser;
    }

    private void checkDeprecatedFilter(Element el) throws InvalidXMLException {
        Node node = Node.fromChildOrAttr(el, "filter");
        if(node != null) {
            throw new InvalidXMLException("'filter' is no longer supported, be more specific e.g. 'pickup-filter'", node);
        }
    }

    public Post parsePost(Element el) throws InvalidXMLException {
        checkDeprecatedFilter(el);

        final Optional<TeamFactory> owner = teamParser.property(el, "owner").optional();
        boolean sequential = XMLUtils.parseBoolean(el.getAttribute("sequential"), false);
        boolean permanent = XMLUtils.parseBoolean(el.getAttribute("permanent"), false);
        double pointsPerSecond = XMLUtils.parseNumber(el.getAttribute("points-rate"), Double.class, 0D);
        Filter pickupFilter = filterParser.property(el, "pickup-filter").optional(StaticFilter.ALLOW);

        Duration recoverTime = XMLUtils.parseDuration(Node.fromAttr(el, "recover-time", "return-time"), Post.DEFAULT_RETURN_TIME);
        Duration respawnTime = XMLUtils.parseDuration(el.getAttribute("respawn-time"), null);
        Double respawnSpeed = XMLUtils.parseNumber(el.getAttribute("respawn-speed"), Double.class, (Double) null);
        ImmutableList<PointProvider> returnPoints = ImmutableList.copyOf(pointParser.parse(el, new PointProviderAttributes()));

        if(respawnTime == null && respawnSpeed == null) {
            respawnSpeed = Post.DEFAULT_RESPAWN_SPEED;
        }

        if(respawnTime != null && respawnSpeed != null) {
            throw new InvalidXMLException("post cannot have both respawn-time and respawn-speed", el);
        }

        if(returnPoints.isEmpty()) {
            throw new InvalidXMLException("post must have at least one point provider", el);
        }

        return context.features().define(el, Post.class, new PostImpl(owner, recoverTime, respawnTime, respawnSpeed, returnPoints, sequential, permanent, pointsPerSecond, pickupFilter));
    }

    public ImmutableSet<FlagDefinition> parseFlagSet(Node node) throws InvalidXMLException {
        return Stream.of(node.getValue().split("\\s"))
                     .map(rethrowFunction(flagId -> context.features().reference(node, flagId, FlagDefinition.class)))
                     .collect(Collectors.toImmutableSet());
    }

    public Net parseNet(Element el, @Nullable FlagDefinition parentFlag) throws InvalidXMLException {
        checkDeprecatedFilter(el);
        Region region = regionParser.property(el).union();
        final Optional<TeamFactory> owner = teamParser.property(el, "owner").optional();
        double pointsPerCapture = XMLUtils.parseNumber(el.getAttribute("points"), Double.class, 0D);
        boolean sticky = XMLUtils.parseBoolean(el.getAttribute("sticky"), true);
        Filter captureFilter = filterParser.property(el, "capture-filter").optional(StaticFilter.ALLOW);
        Filter respawnFilter = filterParser.property(el, "respawn-filter").optional(StaticFilter.ALLOW);
        boolean respawnTogether = XMLUtils.parseBoolean(el.getAttribute("respawn-together"), false);
        BaseComponent respawnMessage = XMLUtils.parseFormattedText(el, "respawn-message");
        BaseComponent denyMessage = XMLUtils.parseFormattedText(el, "deny-message");
        Vector proximityLocation = XMLUtils.parseVector(el.getAttribute("location"), (Vector) null);

        Post returnPost = null;
        Node postAttr = Node.fromAttr(el, "post");
        if(postAttr != null) {
            // Posts are all parsed at this point, so we can do an immediate lookup
            returnPost = context.features().reference(postAttr, Post.class);
            if(returnPost == null) {
                throw new InvalidXMLException("No post with ID '" + postAttr.getValue() + "'", postAttr);
            }
        }

        ImmutableSet<FlagDefinition> capturableFlags;
        Node flagsAttr = Node.fromAttr(el, "flag", "flags");
        if(flagsAttr != null) {
            if(parentFlag != null) {
                throw new InvalidXMLException("Cannot specify flags on a net that is defined inside a flag", flagsAttr);
            }
            capturableFlags = this.parseFlagSet(flagsAttr);
        } else if(parentFlag != null) {
            capturableFlags = ImmutableSet.of(parentFlag);
        } else {
            capturableFlags = ImmutableSet.copyOf(this.flags);
        }

        ImmutableSet<FlagDefinition> returnableFlags;
        Node returnableNode = Node.fromAttr(el, "rescue", "return");
        if(returnableNode != null) {
            returnableFlags = this.parseFlagSet(returnableNode);
        } else {
            returnableFlags = ImmutableSet.of();
        }

        return context.features().define(el, Net.class, new NetImpl(region, captureFilter, respawnFilter, owner, pointsPerCapture, sticky, denyMessage, respawnMessage, returnPost, capturableFlags, returnableFlags, respawnTogether, proximityLocation));
    }

    public FlagDefinition parseFlag(Element el) throws InvalidXMLException {
        checkDeprecatedFilter(el);

        String name = el.getAttributeValue("name");
        boolean visible = XMLUtils.parseBoolean(el.getAttribute("show"), true);
        Boolean required = XMLUtils.parseBoolean(el.getAttribute("required"), null);
        DyeColor color = XMLUtils.parseDyeColor(el.getAttribute("color"), null);
        final Optional<TeamFactory> owner = teamParser.property(el, "owner").optional();
        double pointsPerCapture = XMLUtils.parseNumber(el.getAttribute("points"), Double.class, 0D);
        double pointsPerSecond = XMLUtils.parseNumber(el.getAttribute("points-rate"), Double.class, 0D);
        Filter pickupFilter = filterParser.property(el, "pickup-filter").optional(null);
        if(pickupFilter == null) pickupFilter = filterParser.property(el, "filter").optional(StaticFilter.ALLOW);
        Filter dropFilter = filterParser.property(el, "drop-filter").optional(StaticFilter.ALLOW);
        Filter captureFilter = filterParser.property(el, "capture-filter").optional(StaticFilter.ALLOW);
        Kit pickupKit = kitParser.property(el, "pickup-kit").optional(null);
        Kit dropKit = kitParser.property(el, "drop-kit").optional(null);
        Kit carryKit = kitParser.property(el, "carry-kit")
                                .validate(RemovableValidation.get())
                                .optional(null);
        boolean multiCarrier = XMLUtils.parseBoolean(el.getAttribute("shared"), false);
        BaseComponent carryMessage = XMLUtils.parseFormattedText(el, "carry-message");
        boolean dropOnWater = XMLUtils.parseBoolean(el.getAttribute("drop-on-water"), true);
        boolean showBeam = XMLUtils.parseBoolean(el.getAttribute("beam"), true);
        ProximityMetric flagProximityMetric = ProximityMetric.parse(el, "flag", new ProximityMetric(ProximityMetric.Type.CLOSEST_KILL, false));
        ProximityMetric netProximityMetric = ProximityMetric.parse(el, "net", new ProximityMetric(ProximityMetric.Type.CLOSEST_PLAYER, false));

        Post defaultPost;
        Element elPost = XMLUtils.getUniqueChild(el, "post");
        if(elPost != null) {
            // Parse nested <post>
            defaultPost = this.parsePost(elPost);
        } else {
            Node postAttr = Node.fromRequiredAttr(el, "post");
            defaultPost = context.features().reference(postAttr, Post.class);
            if(defaultPost == null) {
                throw new InvalidXMLException("No post with ID '" + postAttr.getValue() + "'", postAttr);
            }
        }

        FlagDefinition flag = context.features().define(el, FlagDefinition.class, new FlagDefinitionImpl(name, required, visible, color, defaultPost, owner, pointsPerCapture, pointsPerSecond, pickupFilter, dropFilter, captureFilter, pickupKit, dropKit, carryKit, multiCarrier, carryMessage, dropOnWater, showBeam, flagProximityMetric, netProximityMetric));
        flags.add(flag);

        // Parse nested <net>s
        for(Element elNet : el.getChildren("net")) {
            this.parseNet(elNet, flag);
        }

        return flag;
    }

    @Override
    public void parse() throws InvalidXMLException {
        // Order of these is important to avoid the need for forward refs
        for(Element el : XMLUtils.flattenElements(document.getRootElement(), "flags", "post")) {
            this.parsePost(el);
        }

        for(Element el : XMLUtils.flattenElements(document.getRootElement(), "flags", "flag")) {
            this.parseFlag(el);
        }

        for(Element el : XMLUtils.flattenElements(document.getRootElement(), "flags", "net")) {
            this.parseNet(el, null);
        }
    }
}
