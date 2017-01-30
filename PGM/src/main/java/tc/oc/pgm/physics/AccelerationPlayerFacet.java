package tc.oc.pgm.physics;

import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;
import tc.oc.commons.bukkit.event.targeted.TargetedEventHandler;
import tc.oc.pgm.events.PlayerResetEvent;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchPlayerFacet;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.Repeatable;
import tc.oc.pgm.spawns.events.ParticipantDespawnEvent;

public class AccelerationPlayerFacet implements MatchPlayerFacet, Listener {

    private static final double MIN_FORCE = 0.0001;

    private final Player player;
    private final MatchPlayer matchPlayer;
    private final Set<PlayerForce> forces = new HashSet<>();

    @Inject AccelerationPlayerFacet(Player player, MatchPlayer matchPlayer) {
        this.player = player;
        this.matchPlayer = matchPlayer;
    }

    public void clearForces() {
        forces.clear();
    }

    public PlayerForce addForce(Vector force) {
        return addForce(force, false, false);
    }

    public PlayerForce addForce(Vector force, boolean relativeYaw, boolean relativePitch) {
        return addForce(force, RelativeFlags.of(relativeYaw, relativePitch));
    }

    public PlayerForce addForce(Vector acceleration, RelativeFlags relative) {
        final PlayerForce force = new PlayerForce(acceleration, relative);
        addForce(force);
        return force;
    }

    public boolean addForce(PlayerForce force) {
        return forces.add(force);
    }

    public boolean removeForce(PlayerForce force) {
        return forces.remove(force);
    }

    @Repeatable(scope = MatchScope.LOADED)
    public void tickForce() {
        final Vector acceleration = new Vector();
        for(PlayerForce force : forces) {
            acceleration.add(force.acceleration(matchPlayer));
        }
        if(acceleration.lengthSquared() > MIN_FORCE) {
            player.applyImpulse(acceleration);
        }
    }

    @TargetedEventHandler
    public void onReset(PlayerResetEvent event) {
        clearForces();
    }

    @TargetedEventHandler
    public void onReset(ParticipantDespawnEvent event) {
        clearForces();
    }
}
