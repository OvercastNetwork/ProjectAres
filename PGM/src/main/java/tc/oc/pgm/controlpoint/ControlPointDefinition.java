package tc.oc.pgm.controlpoint;

import java.time.Duration;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import org.bukkit.util.BlockVector;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.pgm.features.FeatureInfo;
import tc.oc.pgm.features.GamemodeFeature;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.goals.GoalDefinition;
import tc.oc.pgm.goals.GoalDefinitionImpl;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.regions.Region;
import tc.oc.pgm.teams.TeamFactory;

@FeatureInfo(name = "control-point",
             plural = {"control-points", "hills"},
             singular = {"control-point", "hill"})
public interface ControlPointDefinition extends GoalDefinition, GamemodeFeature {

    @Override ControlPoint getGoal(Match match);

    Region getCaptureRegion();

    Filter getCaptureFilter();

    Filter getPlayerFilter();

    Region getProgressDisplayRegion();

    Region getControllerDisplayRegion();

    Filter getVisualMaterials();

    BlockVector getCapturableDisplayBeacon();

    Duration getTimeToCapture();

    double decayRate();

    double recoveryRate();

    double getTimeMultiplier();

    TeamFactory getInitialOwner();

    CaptureCondition getCaptureCondition();

    boolean hasNeutralState();

    boolean isPermanent();

    boolean affectsScore();

    float getPointsOwned();

    float getPointsPerSecond();

    float getPointsGrowth();

    boolean getShowProgress();

    // Conditions required for a team to capture:
    enum CaptureCondition {
        EXCLUSIVE, // Team owns all players on the point
        MAJORITY,  // Team owns more than half the players on the point
        LEAD       // Team owns more players on the point than any other single team
    }
}

class ControlPointDefinitionImpl extends GoalDefinitionImpl implements ControlPointDefinition {
    // Players in this region are considered "on" the point
    private final Region captureRegion;

    // Which players can capture the point
    private final Filter captureFilter;

    // Which players can prevent other teams from capturing the point
    private final Filter playerFilter;

    // Blocks in this region are used to show capturing progress
    private final Region progressDisplayRegion;

    // Blocks in this region are used to show the team that owns the point
    private final Region ownerDisplayRegion;

    // Block types used for the regions above (currently fixed to wool and stained clay)
    private final Filter visualMaterials;

    // Location of a beacon used to indicate to players that they can capture this point
    private final BlockVector capturableDisplayBeacon;

    // Base time for the point to transition between states
    private final Duration timeToCapture;

    // Capture time multiplier for increasing or decreasing capture time based on the number of players on the point
    private final double timeMultiplier;

    // Relative rate at which progress reverts from players dominating the point
    private final double recoveryRate;

    // Relative rate at which progress reverts while nobody is dominating the point
    private final double decayRate;

    // The team that owns the point when the match starts, null for no owner (neutral state)
    private final TeamFactory initialOwner;
    private final CaptureCondition captureCondition;

    // true: point must transition through unowned state to change owners
    // false: point transitions directly from one owner to the next
    // NOTE: points always start in an unowned state, regardless of this value
    private final boolean neutralState;

    // If true, the point can only be captured once in the match
    private final boolean permanent;

    // Amount of points given to the team that owns the point, they are removed when the team looses the point
    private final float pointsOwned;

    // Rate that the owner's score increases, or 0 if the CP does not affect score
    private final float pointsPerSecond;

    // If this is less than +inf, the effective pointsPerSecond will increase over time
    // at an exponential rate, such that it doubles every time this many seconds elapses.
    private final float pointsGrowth;

    // If true, capturing progress is displayed on the scoreboard
    private final boolean showProgress;

    public ControlPointDefinitionImpl(String name,
                                      @Nullable Boolean required,
                                      boolean visible,
                                      Region captureRegion,
                                      Filter captureFilter,
                                      Filter playerFilter,
                                      Region progressDisplayRegion,
                                      Region ownerDisplayRegion,
                                      Filter visualMaterials,
                                      BlockVector capturableDisplayBeacon,
                                      Duration timeToCapture,
                                      double timeMultiplier,
                                      double recoveryRate,
                                      double decayRate,
                                      TeamFactory initialOwner,
                                      CaptureCondition captureCondition,
                                      boolean neutralState,
                                      boolean permanent,
                                      float pointsOwned,
                                      float pointsPerSecond,
                                      float pointsGrowth,
                                      boolean progress) {

        super(name, required, visible);
        this.captureRegion = captureRegion;
        this.captureFilter = captureFilter;
        this.playerFilter = playerFilter;
        this.progressDisplayRegion = progressDisplayRegion;
        this.ownerDisplayRegion = ownerDisplayRegion;
        this.visualMaterials = visualMaterials;
        this.capturableDisplayBeacon = capturableDisplayBeacon;
        this.timeToCapture = timeToCapture;
        this.timeMultiplier = timeMultiplier;
        this.recoveryRate = recoveryRate;
        this.decayRate = decayRate;
        this.initialOwner = initialOwner;
        this.captureCondition = captureCondition;
        this.neutralState = neutralState;
        this.permanent = permanent;
        this.pointsOwned = pointsOwned;
        this.pointsPerSecond = pointsPerSecond;
        this.pointsGrowth = pointsGrowth;
        this.showProgress = progress;
    }

    @Override
    public String toString() {
        return "ControlPointDefinition {name=" + this.getName() +
               " timeToCapture=" + this.getTimeToCapture() +
               " timeMultiplier=" + this.getTimeMultiplier() +
               " initialOwner=" + this.getInitialOwner() +
               " captureCondition=" + this.getCaptureCondition() +
               " neutralState=" + this.hasNeutralState() +
               " permanent=" + this.isPermanent() +
               " captureRegion=" + this.getCaptureRegion() +
               " captureFilter=" + this.getCaptureFilter() +
               " playerFilter=" + this.getPlayerFilter() +
               " progressDisplay=" + this.getProgressDisplayRegion() +
               " ownerDisplay=" + this.getControllerDisplayRegion() +
               " beacon=" + this.getCapturableDisplayBeacon() +
               " visible=" + this.isVisible();
    }

    @Override
    public Stream<MapDoc.Gamemode> gamemodes() {
        return Stream.of(MapDoc.Gamemode.koth);
    }

    @Override
    public ControlPoint getGoal(Match match) {
        return (ControlPoint) super.getGoal(match);
    }

    @Override
    public ControlPoint createFeature(Match match) {
        final ControlPoint cp = new ControlPoint(match, this);
        cp.registerEvents();
        return cp;
    }

    @Override
    public boolean isShared() {
        return true;
    }

    @Override
    public Region getCaptureRegion() {
        return this.captureRegion;
    }

    @Override
    public Filter getCaptureFilter() {
        return this.captureFilter;
    }

    @Override
    public Filter getPlayerFilter() {
        return this.playerFilter;
    }

    @Override
    public Region getProgressDisplayRegion() {
        return this.progressDisplayRegion;
    }

    @Override
    public Region getControllerDisplayRegion() {
        return this.ownerDisplayRegion;
    }

    @Override
    public Filter getVisualMaterials() {
        return this.visualMaterials;
    }

    @Override
    public BlockVector getCapturableDisplayBeacon() {
        return this.capturableDisplayBeacon;
    }

    @Override
    public Duration getTimeToCapture() {
        return this.timeToCapture;
    }

    @Override
    public double getTimeMultiplier() {
        return this.timeMultiplier;
    }

    @Override
    public double recoveryRate() {
        return this.recoveryRate;
    }

    @Override
    public double decayRate() {
        return this.decayRate;
    }

    @Override
    public TeamFactory getInitialOwner() {
        return this.initialOwner;
    }

    @Override
    public CaptureCondition getCaptureCondition() {
        return this.captureCondition;
    }

    @Override
    public boolean hasNeutralState() {
        return this.neutralState;
    }

    @Override
    public boolean isPermanent() {
        return this.permanent;
    }

    @Override
    public boolean affectsScore() {
        return this.pointsPerSecond > 0;
    }

    @Override
    public float getPointsOwned() {
        return this.pointsOwned;
    }

    @Override
    public float getPointsPerSecond() {
        return this.pointsPerSecond;
    }

    @Override
    public float getPointsGrowth() {
        return this.pointsGrowth;
    }

    @Override
    public boolean getShowProgress() {
        return this.showProgress;
    }
}
