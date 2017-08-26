package tc.oc.pgm.flag.state;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import tc.oc.commons.bukkit.util.NMSHacks;
import tc.oc.minecraft.protocol.MinecraftVersion;
import tc.oc.pgm.flag.Flag;
import tc.oc.pgm.flag.Post;
import tc.oc.pgm.flag.event.FlagCaptureEvent;

/**
 * Base class for flag states in which the banner is physically present
 * somewhere in the map (i.e. most of them).
 */
public abstract class Spawned extends BaseState {

    protected int particleClock;

    public Spawned(Flag flag, Post post) {
        super(flag, post);
    }

    // Location the flag must travel from to respawn
    public abstract Location getLocation();

    /**
     * True if the flag can transition from its current state to a {@link Respawning} state.
     * This is false when the flag is already in that state, or in a {@link Returned} state,
     * in which case there is no reason to respawn it.
     */
    public abstract boolean isRecoverable();

    /**
     * Transition to a {@link Respawning} state, if the flag {@link #isRecoverable()}
     * in its current state.
     *
     * Does nothing unless the match is running.
     */
    protected void recover() {
        if(flag.getMatch().isRunning()) {
            forceRecover();
        }
    }

    /**
     * Transition to a {@link Respawning} state, if the flag {@link #isRecoverable()}
     * in its current state.
     *
     * This method works even if the match is over, so other states use it as a fallback
     * to get out of exceptional situations, e.g. the flag carrier disconnecting.
     */
    protected void forceRecover() {
        if(isRecoverable()) {
            this.flag.transition(new Respawning(this.flag, this.post, this.getLocation(), false, false));
        }
    }

    @Override
    public void onEvent(FlagCaptureEvent event) {
        super.onEvent(event);

        // Not crazy about using an event for game logic, but this is by far the simplest way to do it
        if(event.getNet().getRecoverableFlags().contains(this.flag.getDefinition())) {
            this.recover();
        }
    }

    protected boolean canSeeParticles(Player player) {
        return MinecraftVersion.atLeast(MinecraftVersion.MINECRAFT_1_8, player.getProtocolVersion());
    }

    @Override
    public void tickLoaded() {
        super.tickLoaded();

        this.particleClock++;

        if(this.flag.getDefinition().showBeam()) {
            Object packet = NMSHacks.particlesPacket("ITEM_CRACK", true,
                                                     this.getLocation().clone().add(0, 56, 0).toVector(),
                                                     new Vector(0.15, 24, 0.15), // radius on each axis of the particle ball
                                                     0f, // initial horizontal velocity
                                                     40, // number of particles
                                                     Material.WOOL.getId(), this.flag.getDyeColor().getWoolData());

            for(Player player : this.flag.getMatch().getServer().getOnlinePlayers()) {
                if(this.canSeeParticles(player)) NMSHacks.sendPacket(player, packet);
            }
        }
    }
}
