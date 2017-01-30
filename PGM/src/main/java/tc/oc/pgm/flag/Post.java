package tc.oc.pgm.flag;

import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import java.time.Duration;
import tc.oc.pgm.features.FeatureDefinition;
import tc.oc.pgm.features.FeatureInfo;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.points.AngleProvider;
import tc.oc.pgm.points.PointProvider;
import tc.oc.pgm.points.PointProviderLocation;
import tc.oc.pgm.teams.TeamFactory;

import static com.google.common.base.Preconditions.checkArgument;

@FeatureInfo(name = "post")
public interface Post extends FeatureDefinition {

    Duration DEFAULT_RETURN_TIME = Duration.ofSeconds(30);
    double DEFAULT_RESPAWN_SPEED = 8;

    Optional<TeamFactory> owner();

    @Nullable TeamFactory getOwner();

    ChatColor getColor();

    Duration getRecoverTime();

    Duration getRespawnTime(double distance);

    ImmutableList<PointProvider> getReturnPoints();

    boolean isSequential();

    Boolean isPermanent();

    double getPointsPerSecond();

    Filter getPickupFilter();

    Location getReturnPoint(Flag flag, AngleProvider yawProvider);
}

class PostImpl extends FeatureDefinition.Impl implements Post {

    private static final int MAX_SPAWN_ATTEMPTS = 100;

    private final @Inspect Optional<TeamFactory> owner;                      // Team that owns the post, affects various things
    private final @Inspect Duration recoverTime;                             // Time between a flag dropping and being recovered, can be infinite
    private final @Inspect @Nullable Duration respawnTime;                   // Fixed time between a flag being recovered and respawning at the post
    private final @Inspect @Nullable Double respawnSpeed;                    // Makes respawn time proportional to distance, flag "moves" back at this m/s
    private final @Inspect ImmutableList<PointProvider> returnPoints;        // Spawn points for the flag
    private final @Inspect boolean sequential;                               // Search for spawn points sequentially, see equivalent field in SpawnInfo
    private final @Inspect boolean permanent;                                // Flag enters Completed state when at this post
    private final @Inspect double pointsPerSecond;                           // Points awarded while any flag is at this post
    private final @Inspect Filter pickupFilter;                              // Filter players who can pickup a flag at this post

    public PostImpl(Optional<TeamFactory> owner,
                    Duration recoverTime,
                    @Nullable Duration respawnTime,
                    @Nullable Double respawnSpeed,
                    ImmutableList<PointProvider> returnPoints,
                    boolean sequential,
                    boolean permanent,
                    double pointsPerSecond,
                    Filter pickupFilter) {

        checkArgument(respawnTime == null || respawnSpeed == null);
        if(respawnSpeed != null) checkArgument(respawnSpeed > 0);

        this.owner = owner;
        this.recoverTime = recoverTime;
        this.respawnTime = respawnTime;
        this.respawnSpeed = respawnSpeed;
        this.returnPoints = returnPoints;
        this.sequential = sequential;
        this.permanent = permanent;
        this.pointsPerSecond = pointsPerSecond;
        this.pickupFilter = pickupFilter;
    }

    @Override
    public Optional<TeamFactory> owner() {
        return owner;
    }

    @Override
    public @Nullable TeamFactory getOwner() {
        return owner.orElse(null);
    }

    @Override
    public ChatColor getColor() {
        return owner.map(TeamFactory::getDefaultColor)
                    .orElse(ChatColor.WHITE);
    }

    @Override
    public Duration getRecoverTime() {
        return this.recoverTime;
    }

    @Override
    public Duration getRespawnTime(double distance) {
        if(respawnTime != null) {
            return respawnTime;
        } else if(respawnSpeed != null) {
            return Duration.ofSeconds(Math.round(distance / respawnSpeed));
        } else {
            return Duration.ZERO;
        }
    }

    @Override
    public ImmutableList<PointProvider> getReturnPoints() {
        return this.returnPoints;
    }

    @Override
    public boolean isSequential() {
        return this.sequential;
    }

    @Override
    public Boolean isPermanent() {
        return this.permanent;
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
    public Location getReturnPoint(Flag flag, AngleProvider yawProvider) {
        Location location = getReturnPoint(flag);
        if(location instanceof PointProviderLocation && !((PointProviderLocation) location).hasYaw()) {
            location.setYaw(yawProvider.getAngle(location.toVector()));
        }
        return location;
    }

    private Location getReturnPoint(Flag flag) {
        if(this.sequential) {
            for(PointProvider provider : this.returnPoints) {
                for(int i = 0; i < MAX_SPAWN_ATTEMPTS; i++) {
                    Location loc = roundToBlock(provider.getPoint(flag.getMatch(), null));
                    if(flag.canDropAt(loc)) {
                        return loc;
                    }
                }
            }

            // could not find a good spot, fallback to the last provider
            return this.returnPoints.get(this.returnPoints.size() - 1).getPoint(flag.getMatch(), null);

        } else {
            Random random = new Random();
            for(int i = 0; i < MAX_SPAWN_ATTEMPTS * this.returnPoints.size(); i++) {
                PointProvider provider = this.returnPoints.get(random.nextInt(this.returnPoints.size()));
                Location loc = roundToBlock(provider.getPoint(flag.getMatch(), null));
                if(flag.canDropAt(loc)) {
                    return loc;
                }
            }

            // could not find a good spot, settle for any spot
            PointProvider provider = this.returnPoints.get(random.nextInt(this.returnPoints.size()));
            return this.returnPoints.get(random.nextInt(this.returnPoints.size())).getPoint(flag.getMatch(), null);
        }
    }

    private Location roundToBlock(Location loc) {
        Location newLoc = loc.clone();

        newLoc.setX(Math.floor(loc.getX()) + 0.5);
        newLoc.setY(Math.floor(loc.getY()));
        newLoc.setZ(Math.floor(loc.getZ()) + 0.5);

        return newLoc;
    }
}
