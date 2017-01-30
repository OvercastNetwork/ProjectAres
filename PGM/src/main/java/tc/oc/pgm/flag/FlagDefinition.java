package tc.oc.pgm.flag;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.DyeColor;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.commons.bukkit.util.BukkitUtils;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.pgm.features.FeatureInfo;
import tc.oc.pgm.features.GamemodeFeature;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.query.IQuery;
import tc.oc.pgm.goals.ProximityGoalDefinition;
import tc.oc.pgm.goals.ProximityGoalDefinitionImpl;
import tc.oc.pgm.goals.ProximityMetric;
import tc.oc.pgm.kits.Kit;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.module.ModuleLoadException;
import tc.oc.pgm.teams.TeamFactory;

@FeatureInfo(name = "flag")
public interface FlagDefinition extends ProximityGoalDefinition<Flag>, GamemodeFeature {

    @Nullable DyeColor getColor();

    @Override
    String getColoredName();

    Post getDefaultPost();

    double getPointsPerCapture();

    double getPointsPerSecond();

    Filter getPickupFilter();

    Filter getDropFilter();

    Filter getCaptureFilter();

    @Nullable Kit getPickupKit();

    @Nullable Kit getDropKit();

    @Nullable Kit getCarryKit();

    boolean hasMultipleCarriers();

    @Nullable BaseComponent getCarryMessage();

    boolean canDropOnWater();

    boolean showBeam();

    boolean canPickup(IQuery query);

    boolean canCapture(IQuery query, Collection<Net> nets);
}

class FlagDefinitionImpl extends ProximityGoalDefinitionImpl<Flag> implements FlagDefinition {

    private static String makeName(@Nullable String name, @Nullable DyeColor color) {
        if(name != null) return name;
        if(color != null) return color.name().charAt(0) + color.name().substring(1).toLowerCase() + " Flag";
        return "Flag";
    }

    private final @Inspect @Nullable DyeColor color;                         // Flag color, null detects color from the banner at match load time
    private final @Inspect Post defaultPost;                                 // Flag starts the match at this post
    private final @Inspect double pointsPerCapture;                          // Points awarded for capturing this flag, in addition to points from the Net
    private final @Inspect double pointsPerSecond;                           // Points awarded while carrying this flag
    private final @Inspect Filter pickupFilter;                              // Filter players who can pickup this flag
    private final @Inspect Filter dropFilter;                                // Filter players who can drop the flag
    private final @Inspect Filter captureFilter;                             // Filter players who can capture this flag
    private final @Inspect @Nullable Kit pickupKit;                          // Kit to give on flag pickup
    private final @Inspect @Nullable Kit dropKit;                            // Kit to give carrier when they drop the flag
    private final @Inspect @Nullable Kit carryKit;                           // Kit to give to/take from the flag carrier
    private final @Inspect boolean multiCarrier;                             // Affects how the flag appears in the scoreboard
    private final @Inspect @Nullable BaseComponent carryMessage;             // Custom message to show flag carrier
    private final @Inspect boolean dropOnWater;                              // Flag can freeze water to drop on it
    private final @Inspect boolean showBeam;

    public FlagDefinitionImpl(@Nullable String name,
                              @Nullable Boolean required,
                              boolean visible,
                              @Nullable DyeColor color,
                              Post defaultPost,
                              Optional<TeamFactory> owner,
                              double pointsPerCapture,
                              double pointsPerSecond,
                              Filter pickupFilter,
                              Filter dropFilter,
                              Filter captureFilter,
                              @Nullable Kit pickupKit,
                              @Nullable Kit dropKit,
                              @Nullable Kit carryKit,
                              boolean multiCarrier,
                              @Nullable BaseComponent carryMessage,
                              boolean dropOnWater,
                              boolean showBeam,
                              ProximityMetric flagProximityMetric,
                              ProximityMetric netProximityMetric) {

        // We can't use the owner field in OwnedGoal because our owner
        // is a reference that can't be resolved until after parsing.
        super(makeName(name, color), required, visible, owner, flagProximityMetric, netProximityMetric);

        this.color = color;
        this.defaultPost = defaultPost;
        this.pointsPerCapture = pointsPerCapture;
        this.pointsPerSecond = pointsPerSecond;
        this.pickupFilter = pickupFilter;
        this.dropFilter = dropFilter;
        this.captureFilter = captureFilter;
        this.pickupKit = pickupKit;
        this.dropKit = dropKit;
        this.carryKit = carryKit;
        this.multiCarrier = multiCarrier;
        this.carryMessage = carryMessage;
        this.dropOnWater = dropOnWater;
        this.showBeam = showBeam;
    }

    @Override
    public Stream<MapDoc.Gamemode> gamemodes() {
        return Stream.of(MapDoc.Gamemode.ctf);
    }

    @Override
    public boolean isShared() {
        return true;
    }

    @Override
    public @Nullable DyeColor getColor() {
        return this.color;
    }

    @Override
    public String getColoredName() {
        if(this.getColor() != null) {
            return BukkitUtils.dyeColorToChatColor(this.getColor()) + this.getName();
        } else {
            return super.getColoredName();
        }
    }

    @Override
    public Post getDefaultPost() {
        return this.defaultPost;
    }

    @Override
    public double getPointsPerCapture() {
        return this.pointsPerCapture;
    }

    @Override
    public double getPointsPerSecond() {
        return this.pointsPerSecond;
    }

    @Override
    public Filter getPickupFilter() {
        return this.pickupFilter;
    }

    @Override
    public Filter getDropFilter() {
        return dropFilter;
    }

    @Override
    public Filter getCaptureFilter() {
        return captureFilter;
    }

    @Override
    public @Nullable Kit getPickupKit() {
        return pickupKit;
    }

    @Override
    public @Nullable Kit getDropKit() {
        return dropKit;
    }

    @Override
    public @Nullable Kit getCarryKit() {
        return carryKit;
    }

    @Override
    public boolean hasMultipleCarriers() {
        return multiCarrier;
    }

    @Override
    public @Nullable BaseComponent getCarryMessage() {
        return carryMessage;
    }

    @Override
    public boolean canDropOnWater() {
        return dropOnWater;
    }

    @Override
    public boolean showBeam() {
        return showBeam;
    }

    @Override
    public Flag createFeature(Match match) throws ModuleLoadException {
        return new Flag(match, this, match.featureDefinitions()
                                          .all(Net.class)
                                          .filter(net -> net.getCapturableFlags().contains(this))
                                          .collect(Collectors.toImmutableSet()));
    }

    @Override
    public boolean canPickup(IQuery query) {
        return getPickupFilter().query(query).isAllowed() &&
               getDefaultPost().getPickupFilter().query(query).isAllowed();
    }

    @Override
    public boolean canCapture(IQuery query, Collection<Net> nets) {
        if(getCaptureFilter().query(query).isDenied()) return false;
        for(Net net : nets) {
            if(net.getCaptureFilter().query(query).isAllowed()) return true;
        }
        return false;
    }
}
