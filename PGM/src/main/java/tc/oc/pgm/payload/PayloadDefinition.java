package tc.oc.pgm.payload;

import org.bukkit.util.Vector;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.pgm.features.FeatureInfo;
import tc.oc.pgm.features.GamemodeFeature;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.goals.GoalDefinition;
import tc.oc.pgm.goals.GoalDefinitionImpl;
import tc.oc.pgm.goals.OwnableGoalDefinition;
import tc.oc.pgm.goals.OwnableGoalDefinitionImpl;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.regions.Region;
import tc.oc.pgm.teams.TeamFactory;
import tc.oc.pgm.utils.MaterialPattern;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

@FeatureInfo(name = "payload",
             plural = {"payloads"},
             singular = {"payload"})
public interface PayloadDefinition extends OwnableGoalDefinition<Payload>, GamemodeFeature {

    @Override
    Payload getGoal(Match match);

    Vector getStartingLocation();

    Vector getSpawnLocation();

    float getYaw();

    Filter getCaptureFilter();

    Filter getPlayerFilter();

    Duration getTimeToCapture();

    double decayRate();

    double emptyDecayRate();

    double recoveryRate();

    double getTimeMultiplier();

    TeamFactory getInitialOwner();

    CaptureCondition getCaptureCondition();

    boolean hasNeutralState();

    boolean hasFriendlyCheckpoints();

    float getRadius();

    float getHeight();

    MaterialPattern getCheckpointMaterial();

    float getFriendlySpeed();

    float getEnemySpeed();

    float getPoints();

    float getFriendlyPoints();

    boolean getShowProgress();

    // Conditions required for a team to capture:
    enum CaptureCondition {
        EXCLUSIVE, // Team owns all players on the point
        MAJORITY,  // Team owns more than half the players on the point
        LEAD       // Team owns more players on the point than any other single team
    }
}

class PayloadDefinitionImpl extends OwnableGoalDefinitionImpl<Payload> implements PayloadDefinition {
    //Where the rail starts
    private final Vector startingLocation;

    //Where the payload starts
    private final Vector spawnLocation;

    //The direction the Payload spawns
    private final float yaw;

    // Which players can capture the point
    private final Filter captureFilter;

    // Which players can prevent other teams from capturing the point
    private final Filter playerFilter;

    // Base time for the point to transition between states
    private final Duration timeToCapture;

    // Capture time multiplier for increasing or decreasing capture time based on the number of players on the point
    private final double timeMultiplier;

    // Relative rate at which progress reverts from players dominating the point
    private final double recoveryRate;

    // Relative rate at which progress reverts while nobody is dominating the point
    private final double decayRate;

    // Relative rate at which progress reverts while nobody is standing on the point
    private final double emptyDecayRate;

    // The team that owns the point when the match starts, null for no currentOwner (neutral state)
    private final TeamFactory initialOwner;
    private final CaptureCondition captureCondition;

    // true: point must transition through unowned state to change owners
    // false: point transitions directly from one currentOwner to the next
    // NOTE: points always start in an unowned state, regardless of this value
    private final boolean neutralState;

    private final boolean friendlyCheckpoints;

    //The radius of the control point of the payload
    private final float radius;

    //The height of the control point of the payload
    private final float height;

    //The material of the checkpoint blocks
    private final MaterialPattern checkpointMaterial;

    //The speed of the payload when under control of the owning team
    private final float friendlySpeed;

    //The speed of the payload when not under control of the owning team
    private final float enemySpeed;

    // Amount of points given to the team that captures the payload
    private final float points;

    // Amount of points given to the team that owns and captures the payload at the initial location
    private final float friendlyPoints;

    // If true, capturing progress is displayed on the scoreboard
    private final boolean showProgress;

    public PayloadDefinitionImpl(String name,
                                 @Nullable Boolean required,
                                 boolean visible,
                                 Vector location,
                                 Vector spawnLocation,
                                 float yaw,
                                 Filter captureFilter,
                                 Filter playerFilter,
                                 Duration timeToCapture,
                                 double timeMultiplier,
                                 double recoveryRate,
                                 double decayRate,
                                 double emptyDecayRate,
                                 TeamFactory initialOwner,
                                 TeamFactory owner,
                                 CaptureCondition captureCondition,
                                 boolean neutralState,
                                 boolean friendlyCheckpoints,
                                 float radius,
                                 float height,
                                 MaterialPattern checkpointMaterial,
                                 float friendlySpeed,
                                 float enemySpeed,
                                 float points,
                                 float friendlyPoints,
                                 boolean progress) {

        super(name, required, visible, Optional.of(owner));
        this.startingLocation = location;
        this.spawnLocation = spawnLocation;
        this.yaw = yaw;
        this.captureFilter = captureFilter;
        this.playerFilter = playerFilter;
        this.timeToCapture = timeToCapture;
        this.timeMultiplier = timeMultiplier;
        this.recoveryRate = recoveryRate;
        this.decayRate = decayRate;
        this.emptyDecayRate = emptyDecayRate;
        this.initialOwner = initialOwner;
        this.captureCondition = captureCondition;
        this.neutralState = neutralState;
        this.friendlyCheckpoints = friendlyCheckpoints;
        this.radius = radius;
        this.height = height;
        this.checkpointMaterial = checkpointMaterial;
        this.friendlySpeed = friendlySpeed;
        this.enemySpeed = enemySpeed;
        this.points = points;
        this.friendlyPoints = friendlyPoints;
        this.showProgress = progress;
    }

    @Override
    public String toString() {
        return "PayloadDefinition {name=" + this.getName() +
               " timeToCapture=" + this.getTimeToCapture() +
               " timeMultiplier=" + this.getTimeMultiplier() +
               " initialOwner=" + this.getInitialOwner() +
               " captureCondition=" + this.getCaptureCondition() +
               " neutralState=" + this.hasNeutralState() +
               " captureFilter=" + this.getCaptureFilter() +
               " playerFilter=" + this.getPlayerFilter() +
               " visible=" + this.isVisible();
    }

    @Override
    public Stream<MapDoc.Gamemode> gamemodes() {
        return Stream.of(MapDoc.Gamemode.payload);
    }

    @Override
    public Payload getGoal(Match match) {
        return (Payload) super.getGoal(match);
    }

    @Override
    public Payload createFeature(Match match) {
        final Payload cp = new Payload(match, this);
        cp.registerEvents();
        return cp;
    }

    @Override
    public boolean isShared() {
        return true;
    }

    @Override
    public float getYaw() {
        return this.yaw;
    }

    @Override
    public Vector getStartingLocation() {
        return this.startingLocation;
    }

    @Override
    public Vector getSpawnLocation() {
        return this.spawnLocation;
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
    public double emptyDecayRate() {
        return this.emptyDecayRate;
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
    public boolean hasFriendlyCheckpoints() {
        return this.friendlyCheckpoints;
    }

    @Override
    public float getRadius() {
        return this.radius;
    }

    @Override
    public float getHeight() {
        return this.height;
    }

    @Override
    public MaterialPattern getCheckpointMaterial() {
        return checkpointMaterial;
    }

    @Override
    public float getFriendlySpeed() {
        return this.friendlySpeed;
    }

    @Override
    public float getEnemySpeed() {
        return this.enemySpeed;
    }

    @Override
    public float getFriendlyPoints() {
        return this.friendlyPoints;
    }

    @Override
    public float getPoints() {
        return this.points;
    }

    @Override
    public boolean getShowProgress() {
        return this.showProgress;
    }
}
