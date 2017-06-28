package tc.oc.pgm.spawns.states;

import javax.inject.Inject;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import tc.oc.commons.bukkit.event.CoarsePlayerMoveEvent;
import tc.oc.commons.bukkit.freeze.PlayerFreezer;
import tc.oc.commons.core.util.Comparables;
import tc.oc.pgm.events.MatchBeginEvent;
import tc.oc.pgm.events.MatchEndEvent;
import tc.oc.pgm.events.MatchPlayerDeathEvent;
import tc.oc.pgm.events.ObserverInteractEvent;
import tc.oc.pgm.events.PlayerChangePartyEvent;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.spawns.RespawnOptions;
import tc.oc.pgm.spawns.SpawnMatchModule;
import tc.oc.pgm.start.PreMatchCountdown;

public abstract class State {

    @Inject protected static PlayerFreezer freezer; // HACK

    protected final Match match;
    protected final SpawnMatchModule smm;
    protected final RespawnOptions options;
    protected final MatchPlayer player;
    protected final Player bukkit;

    private boolean entered, exited;

    public State(MatchPlayer player) {
        this.match = player.getMatch();
        this.smm = match.needMatchModule(SpawnMatchModule.class);
        this.player = player;
        this.bukkit = player.getBukkit();
        this.options = smm.getRespawnOptions(player.playerState());
    }

    public boolean isCurrent() {
        return entered && !exited;
    }

    public void enterState() {
        bukkit.setGravity(true);
        if(exited) {
            throw new IllegalStateException("Tried to enter already exited state " + this);
        } else if(entered) {
            throw new IllegalStateException("Tried to enter already entered state " + this);
        }
        entered = true;
    }

    /**
     */
    public void leaveState() {
        bukkit.setGravity(true);
        if(!entered) {
            throw new IllegalStateException("Tried to leave state before entering " + this);
        } else if(exited) {
            throw new IllegalStateException("Tried to leave already exited state " + this);
        }
        exited = true;
    }

    protected void transition(State newState) {
        smm.transition(player, newState);
    }

    protected void forceAlive() {
        // Prevents the default death handling, in case a player manages to die
        // while not in the Alive state, or without generating a PGMPlayerDeathEvent
        // at all. This can happen from e.g. the /slay command.
        //
        // We also need to reset the max health to make sure we don't get an exception
        // when setting the health. We can't just set health to max because max might
        // be zero.
        //
        // TODO: Add a way to enumerate AttributeModifiers to the SportBukkit API
        // and then remove them all here so we can always set max health.

        bukkit.setMaxHealth(20);
        final double maxHealth = bukkit.getMaxHealth();
        if(maxHealth > 0) {
            bukkit.setHealth(maxHealth);
        } else {
            smm.getMatch().getMap().getLogger().severe("Failed to set max health to a non-zero value, custom death screen will fail");
        }
    }

    protected boolean canSpawn() {
        return match.isRunning() || match.countdowns()
                                         .countdown(PreMatchCountdown.class)
                                         .filter(countdown -> Comparables.lessOrEqual(countdown.timeUntilMatchStart(),
                                                                                      smm.getRespawnOptions(player).freeze))
                                         .isPresent();
    }

    public void onEvent(final PlayerDeathEvent event) {
        forceAlive();
        event.getDrops().clear();
    }

    public void tick() {}

    /**
     * Called only when oldParty and newParty are both non-null
     */
    public void onEvent(final PlayerChangePartyEvent event) {}

    public void onEvent(final MatchPlayerDeathEvent event) {}
    public void onEvent(final InventoryClickEvent event) {}
    public void onEvent(final ObserverInteractEvent event) {}
    public void onEvent(final MatchBeginEvent event) {}
    public void onEvent(final MatchEndEvent event) {}
    public void onEvent(final CoarsePlayerMoveEvent event) {}
}
