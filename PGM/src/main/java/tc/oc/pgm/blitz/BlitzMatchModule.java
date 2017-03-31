package tc.oc.pgm.blitz;

import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

import com.google.api.client.util.Sets;
import com.google.common.collect.ImmutableSet;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.MatchPlayerDeathEvent;
import tc.oc.pgm.events.PartyAddEvent;
import tc.oc.pgm.events.PlayerLeavePartyEvent;
import tc.oc.pgm.join.JoinDenied;
import tc.oc.pgm.join.JoinHandler;
import tc.oc.pgm.join.JoinMatchModule;
import tc.oc.pgm.join.JoinMethod;
import tc.oc.pgm.join.JoinRequest;
import tc.oc.pgm.join.JoinResult;
import tc.oc.pgm.listeners.MatchAnnouncer;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.mutation.Mutation;
import tc.oc.pgm.mutation.MutationMatchModule;
import tc.oc.pgm.spawns.events.ParticipantReleaseEvent;
import tc.oc.pgm.victory.AbstractVictoryCondition;
import tc.oc.pgm.victory.VictoryCondition;
import tc.oc.pgm.victory.VictoryMatchModule;

@ListenerScope(MatchScope.LOADED)
public class BlitzMatchModule extends MatchModule implements Listener, JoinHandler {

    final BlitzConfig config;
    public final LifeManager lifeManager;
    private final Set<UUID> eliminatedPlayers = Sets.newHashSet();
    private int maxCompetitors; // Maximum number of non-empty Competitors that have been in the match at once

    public class BlitzVictoryCondition extends AbstractVictoryCondition {
        public BlitzVictoryCondition() {
            super(Priority.BLITZ, new BlitzMatchResult());
        }

        @Override public boolean isCompleted() {
            // At least one competitor must be eliminated before the match can end.
            // This allows maps to be tested with one or zero competitors present.
            final int count = remainingCompetitors();
            return count <= 1 && count < maxCompetitors;
        }
    }

    final VictoryCondition victoryCondition = new BlitzVictoryCondition();

    public BlitzMatchModule(Match match, BlitzConfig config) {
        super(match);
        this.config = match.module(MutationMatchModule.class).get().enabled(Mutation.BLITZ) ? new BlitzConfig(1, true) : config;
        this.lifeManager = new LifeManager(this.config.getNumLives());
    }

    @Override
    public boolean shouldLoad() {
        return super.shouldLoad() && config.lives != Integer.MAX_VALUE;
    }

    @Override
    public void load() {
        super.load();
        match.needMatchModule(JoinMatchModule.class).registerHandler(this);
        match.needMatchModule(VictoryMatchModule.class).setVictoryCondition(victoryCondition);
    }

    @Override
    public void enable() {
        super.enable();
        updateMaxCompetitors();
    }

    private int remainingCompetitors() {
        return (int) match.getCompetitors()
                          .stream()
                          .filter(c -> !c.getPlayers().isEmpty())
                          .count();
    }

    @EventHandler
    public void onPartyAdd(PartyAddEvent event) {
        if(event.getParty() instanceof Competitor) {
            updateMaxCompetitors();
        }
    }

    private void updateMaxCompetitors() {
        maxCompetitors = Math.max(maxCompetitors, remainingCompetitors());
    }

    public BlitzConfig getConfig() {
        return this.config;
    }

    /** Whether or not the player participated in the match and was eliminated. */
    public boolean isPlayerEliminated(UUID player) {
        return this.eliminatedPlayers.contains(player);
    }

    public int getRemainingPlayers(Competitor competitor) {
        // TODO: this becomes a bit more complex when eliminated players are not forced to observers
        return competitor.getPlayers().size();
    }

    @Override
    public @Nullable JoinResult queryJoin(MatchPlayer joining, JoinRequest request) {
        if(getMatch().hasStarted() && request.method() != JoinMethod.FORCE) {
            // This message should NOT look like an error, because remotely joining players will see it often.
            // It also should not say "Blitz" because not all maps that use this module want to be labelled "Blitz".
            return JoinDenied.friendly("command.gameplay.join.matchStarted");
        }
        return null;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void handleDeath(final MatchPlayerDeathEvent event) {
        MatchPlayer victim = event.getVictim();
        if(victim.getParty() instanceof Competitor) {
            int lives = this.lifeManager.addLives(event.getVictim().getPlayerId(), -1);
            if(lives <= 0) {
                this.handleElimination(victim);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleLeave(final PlayerLeavePartyEvent event) {
        if(!match.isRunning()) return;
        int lives = this.lifeManager.getLives(event.getPlayer().getPlayerId());
        if (event.getOldParty() instanceof Competitor && lives > 0) {
            // Player switching teams, check if match needs to end
            if (event.getNewParty() instanceof Competitor) checkEnd();
            // Player is going to obs, eliminate it
            else handleElimination(event.getPlayer());
        }
    }

    @EventHandler
    public void handleSpawn(final ParticipantReleaseEvent event) {
        if(this.config.broadcastLives) {
            int lives = this.lifeManager.getLives(event.getPlayer().getPlayerId());
            event.getPlayer().showTitle(
                // Fake the "Go!" title at match start
                event.wasFrozen() ? MatchAnnouncer.GO : Components.blank(),
                new Component(
                    new TranslatableComponent(
                        "match.blitz.livesRemaining.message",
                        new Component(
                            new TranslatableComponent(
                                lives == 1 ? "match.blitz.livesRemaining.singularLives"
                                           : "match.blitz.livesRemaining.pluralLives",
                                Integer.toString(lives)
                            ),
                            ChatColor.AQUA
                        )
                    ),
                    ChatColor.RED
                ),
                0, 60, 20
            );
        }
    }

    private void handleElimination(final MatchPlayer player) {
        if (!eliminatedPlayers.add(player.getBukkit().getUniqueId())) return;

        World world = player.getMatch().getWorld();
        Location death = player.getBukkit().getLocation();

        double radius = 0.1;
        int n = 8;
        for(int i = 0; i < 6; i++) {
            double angle = 2 * Math.PI * i / n;
            Location base = death.clone().add(new Vector(radius * Math.cos(angle), 0, radius * Math.sin(angle)));
            for(int j = 0; j <= 8; j++) {
                world.playEffect(base, Effect.SMOKE, j);
            }
        }
        checkEnd();
    }

    private void checkEnd() {
        // Process eliminations within the same tick simultaneously, so that ties are properly detected
        getMatch().getScheduler(MatchScope.RUNNING).debounceTask(() -> {
            ImmutableSet.copyOf(getMatch().getParticipatingPlayers())
                    .stream()
                    .filter(participating -> eliminatedPlayers.contains(participating.getBukkit().getUniqueId()))
                    .forEach(participating -> match.setPlayerParty(participating, match.getDefaultParty()));
            match.needMatchModule(VictoryMatchModule.class).invalidateAndCheckEnd();
        });
    }

}
