package tc.oc.pgm.flag;

import java.util.Optional;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.util.Vector;
import tc.oc.pgm.features.FeatureDefinition;
import tc.oc.pgm.features.FeatureInfo;
import tc.oc.pgm.features.SluggedFeatureDefinition;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.regions.Region;
import tc.oc.pgm.teams.TeamFactory;

@FeatureInfo(name = "net")
public interface Net extends SluggedFeatureDefinition {

    Region getRegion();

    Filter getCaptureFilter();

    Filter getRespawnFilter();

    @Nullable BaseComponent getRespawnMessage();

    Optional<TeamFactory> owner();

    @Nullable TeamFactory getOwner();

    double getPointsPerCapture();

    boolean isSticky();

    @Nullable BaseComponent getDenyMessage();

    @Nullable Post getReturnPost();

    default Optional<Post> returnPost() { return Optional.ofNullable(getReturnPost()); }

    ImmutableSet<FlagDefinition> getCapturableFlags();

    ImmutableSet<FlagDefinition> getRecoverableFlags();

    boolean isRespawnTogether();

    Vector getProximityLocation();
}

class NetImpl extends FeatureDefinition.Impl implements Net {

    private final @Inspect Region region;                                    // Region flag carrier must enter to capture
    private final @Inspect Filter captureFilter;                             // Carrier must pass this filter to capture
    private final @Inspect Filter respawnFilter;                             // Captured flags will not respawn until they pass this filter
    private final @Inspect Optional<TeamFactory> owner;                      // Team that gets points for captures in this net, null to give points to flag carrier
    private final @Inspect double pointsPerCapture;                          // Points awarded per capture
    private final @Inspect boolean sticky;                                   // If capture is delayed by filter, carrier does not have to stay inside the net
    private final @Inspect @Nullable BaseComponent denyMessage;              // Message to show carrier when capture is prevented by filter
    private final @Inspect @Nullable BaseComponent respawnMessage;           // Message to broadcast when respawn is prevented by filter or respawnTogether
    private final @Inspect @Nullable Post returnPost;                        // Post to send flags after capture, null to send to their current post
    private final @Inspect ImmutableSet<FlagDefinition> capturableFlags;     // Flags that can be captured in this net
    private final @Inspect ImmutableSet<FlagDefinition> recoverableFlags;    // Flags that are force returned on capture, aside from the flag being captured
    private final @Inspect boolean respawnTogether;                          // Delay respawn until all capturableFlags are captured

    private @Nullable Vector proximityLocation;

    public NetImpl(Region region,
                   Filter captureFilter,
                   Filter respawnFilter,
                   Optional<TeamFactory> owner,
                   double pointsPerCapture,
                   boolean sticky,
                   @Nullable BaseComponent denyMessage,
                   @Nullable BaseComponent respawnMessage,
                   @Nullable Post returnPost,
                   ImmutableSet<FlagDefinition> capturableFlags,
                   ImmutableSet<FlagDefinition> recoverableFlags,
                   boolean respawnTogether,
                   @Nullable Vector proximityLocation) {

        this.region = region;
        this.captureFilter = captureFilter;
        this.respawnFilter = respawnFilter;
        this.owner = owner;
        this.pointsPerCapture = pointsPerCapture;
        this.sticky = sticky;
        this.denyMessage = denyMessage;
        this.respawnMessage = respawnMessage;
        this.returnPost = returnPost;
        this.capturableFlags = capturableFlags;
        this.recoverableFlags = recoverableFlags;
        this.respawnTogether = respawnTogether;
        this.proximityLocation = proximityLocation;
    }

    @Override
    public Region getRegion() {
        return this.region;
    }

    @Override
    public Filter getCaptureFilter() {
        return captureFilter;
    }

    @Override
    public Filter getRespawnFilter() {
        return respawnFilter;
    }

    @Override
    public @Nullable BaseComponent getRespawnMessage() {
        return respawnMessage;
    }

    public Optional<TeamFactory> owner() {
        return owner;
    }

    @Override
    public @Nullable TeamFactory getOwner() {
        return owner.orElse(null);
    }

    @Override
    public double getPointsPerCapture() {
        return pointsPerCapture;
    }

    @Override
    public boolean isSticky() {
        return sticky;
    }

    @Override
    public @Nullable BaseComponent getDenyMessage() {
        return denyMessage;
    }

    @Override
    public @Nullable Post getReturnPost() {
        return this.returnPost;
    }

    @Override
    public ImmutableSet<FlagDefinition> getCapturableFlags() {
        return capturableFlags;
    }

    @Override
    public ImmutableSet<FlagDefinition> getRecoverableFlags() {
        return recoverableFlags;
    }

    @Override
    public boolean isRespawnTogether() {
        return respawnTogether;
    }

    @Override
    public Vector getProximityLocation() {
        if(proximityLocation == null) {
            proximityLocation = getRegion().getBounds().center();
        }
        return proximityLocation;
    }
}
