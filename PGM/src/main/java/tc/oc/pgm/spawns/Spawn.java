package tc.oc.pgm.spawns;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import tc.oc.pgm.features.FeatureDefinition;
import tc.oc.pgm.features.FeatureInfo;
import tc.oc.pgm.kits.Kit;
import tc.oc.pgm.kits.KitPlayerFacet;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.points.PointProvider;
import tc.oc.pgm.tracker.trackers.PlayerLocationTracker;

@FeatureInfo(name = "spawn")
public interface Spawn extends FeatureDefinition {

    SpawnAttributes attributes();

    Optional<Kit> getKit();

    boolean allows(MatchPlayer player);

    // assume the caller has already called .matches()
    Location getSpawn(MatchPlayer player);

    void applyKit(MatchPlayer player);
}

class SpawnImpl extends FeatureDefinition.Impl implements Spawn {
    private final @Inspect SpawnAttributes attributes;
    private final @Inspect PointProvider pointProvider;

    public SpawnImpl(SpawnAttributes attributes, PointProvider pointProvider) {
        this.attributes = attributes;
        this.pointProvider = pointProvider;
    }

    @Override
    public SpawnAttributes attributes() {
        return attributes;
    }

    @Override
    public Optional<Kit> getKit() {
        return attributes.kit;
    }

    @Override
    public boolean allows(MatchPlayer player) {
        return this.attributes.filter.query(player).isAllowed();
    }

    // assume the caller has already called .matches()
    @Override
    public Location getSpawn(MatchPlayer player) {
        Location location = this.pointProvider.getPoint(player.getMatch(), player.getBukkit());
        if (attributes.useLastParticipatingLocation) {
            Location lastParticipatingLocation = PlayerLocationTracker.getLastParticipatingLocation(player);
            if (lastParticipatingLocation != null) {
                location = lastParticipatingLocation;
            }
        }
        if(location == null) {
            player.getMatch().needMatchModule(SpawnMatchModule.class).reportFailedSpawn(this, player);
        }
        return location;
    }

    @Override
    public void applyKit(MatchPlayer player) {
        Optional<Kit> kit = getKit();
        if(kit.isPresent()) {
            player.facet(KitPlayerFacet.class).applyKit(kit.get(), false);
        }
    }
}
