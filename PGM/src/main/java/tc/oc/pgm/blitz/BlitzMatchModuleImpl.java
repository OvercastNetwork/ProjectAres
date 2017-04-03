package tc.oc.pgm.blitz;

import com.google.common.collect.ImmutableSet;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.api.docs.PlayerId;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.MatchPlayerDeathEvent;
import tc.oc.pgm.events.PartyAddEvent;
import tc.oc.pgm.events.PlayerChangePartyEvent;
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
import tc.oc.pgm.spawns.events.ParticipantReleaseEvent;
import tc.oc.pgm.teams.TeamMatchModule;
import tc.oc.pgm.victory.VictoryMatchModule;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ListenerScope(MatchScope.LOADED)
public class BlitzMatchModuleImpl extends MatchModule implements BlitzMatchModule, Listener, JoinHandler {

    private final Match match;
    private final World world;
    private final JoinMatchModule join;
    private final VictoryMatchModule victory;
    private final Optional<TeamMatchModule> teams;
    private BlitzProperties properties;

    private final Set<Lives> lives = new HashSet<>();
    private final Set<PlayerId> eliminated = new HashSet<>();
    private boolean activated = false;
    private int competitors = 0;

    @Inject BlitzMatchModuleImpl(Match match, World world, JoinMatchModule join, VictoryMatchModule victory, Optional<TeamMatchModule> teams, BlitzProperties properties) {
        this.match = match;
        this.world = world;
        this.join = join;
        this.victory = victory;
        this.teams = teams;
        this.properties = properties;
    }

    private void preload() {
        if(!properties().empty()) {
            activate();
        }
    }

    protected int competitors() {
        return competitors;
    }

    protected int remainingCompetitors() {
        return (int) match.getCompetitors()
                .stream()
                .filter(c -> !c.getPlayers().isEmpty())
                .count();
    }

    private void updateCompetitors() {
        competitors = Math.max(competitors(), remainingCompetitors());
    }

    private void setup(MatchPlayer player, boolean force) {
        if(force) {
            eliminated.remove(player.getPlayerId());
            lives.removeIf(life -> life.owner(player.getPlayerId()));
        }
        switch(properties().type) {
            case INDIVIDUAL:
                properties().individuals.forEach((filter, count) -> {
                    if(filter.allows(player)) {
                        lives.add(new LivesIndividual(player, count));
                    }
                }); break;
            case TEAM:
                properties().teams.forEach((teamFactory, count) -> {
                    if(teams.get().team(teamFactory).equals(player.getCompetitor())) {
                        lives.add(new LivesTeam(player.getCompetitor(), count));
                    }
                }); break;
        }
    }

    private void livesHotbar(MatchPlayer player) {
        lives(player).map(Lives::remaining).ifPresent(player::sendHotbarMessage);
    }

    private void livesTitle(MatchPlayer player, boolean release, boolean activate) {
        final Optional<Lives> lives = lives(player);
        if(activated() && lives.isPresent()) {
            player.showTitle(
                release ? MatchAnnouncer.GO
                        : activate ? new Component(new TranslatableComponent("blitz.activated"), ChatColor.GREEN)
                                   : Components.blank(),
                lives.get().remaining(),
                0, 60, 20
            );
        }
    }

    private void update() {
        match.callEvent(new BlitzEvent(match, this));
    }

    @Override
    public boolean activated() {
        return activated;
    }

    @Override
    public void activate(@Nullable BlitzProperties newProperties) {
        if(!activated) {
            activated = true;
            if(newProperties != null) {
                properties = newProperties;
            }
            load();
            if(match.hasStarted()) {
                enable();
            }
        }
    }

    @Override
    public void deactivate() {
        activated = false;
        lives.clear();
        eliminated.clear();
        update();
    }

    @Override
    public void load() {
        if(activated()) {
            join.registerHandler(this);
            victory.setVictoryCondition(new BlitzVictoryCondition(this));
        } else {
            preload();
        }
    }

    @Override
    public void enable() {
        if(activated()) {
            updateCompetitors();
            match.participants().forEach(player -> {
                setup(player, false);
                if(match.hasStarted()) {
                    livesTitle(player, false, true);
                }
            });
            update();
        }
    }

    @Override
    public BlitzProperties properties() {
        return properties;
    }

    @Override
    public boolean increment(MatchPlayer player, int lives, boolean notify, boolean immediate) {
        if(!eliminated(player)) {
            return lives(player).map(life -> {
                life.add(player.getPlayerId(), lives);
                if(notify) {
                    player.showTitle(Components.blank(), life.change(lives), 0, 40, 10);
                }
                player.competitor().ifPresent(competitor -> competitor.participants().forEach(this::livesHotbar));
                if(life.empty() && immediate) {
                    eliminate(player);
                    return true;
                }
                return false;
            }).orElse(false);
        }
        return true;
    }

    @Override
    public int livesCount(MatchPlayer player) {
        return lives(player).map(Lives::current)
                            .orElseThrow(() -> new IllegalStateException(player + " has no lives present to count"));
    }

    @Override
    public Optional<Lives> lives(MatchPlayer player) {
        return lives.stream()
                    .filter(lives -> lives.applicableTo(player.getPlayerId()))
                    .findFirst();
    }

    @Override
    public Optional<Lives> lives(Competitor competitor) {
        return lives.stream()
                    .filter(lives -> lives.type().equals(Lives.Type.TEAM) && lives.competitor().equals(competitor))
                    .findFirst();
    }

    @Override
    public boolean eliminated(MatchPlayer player) {
        return eliminated.contains(player.getPlayerId());
    }

    @Override
    public void eliminate(MatchPlayer player) {
        if(activated() && !eliminated(player)) {
            eliminated.add(player.getPlayerId());
            // Process eliminations within the same tick simultaneously, so that ties are properly detected
            match.getScheduler(MatchScope.RUNNING).debounceTask(() -> {
                ImmutableSet.copyOf(getMatch().getParticipatingPlayers())
                            .stream()
                            .filter(this::eliminated)
                            .forEach(eliminated -> {
                                match.setPlayerParty(eliminated, match.getDefaultParty());
                                world.spawnParticle(Particle.SMOKE_LARGE, eliminated.getLocation(), 5);
                            });
                victory.invalidateAndCheckEnd();
            });
        }
    }

    @Override
    public JoinResult queryJoin(MatchPlayer joining, JoinRequest request) {
        if(activated() &&
           match.hasStarted() &&
           !EnumSet.of(JoinMethod.FORCE, JoinMethod.REMOTE).contains(request.method())) {
            // This message should NOT look like an error, because remotely joining players will see it often.
            // It also should not say "Blitz" because not all maps that use this module want to be labelled "Blitz".
            return JoinDenied.friendly("command.gameplay.join.matchStarted");
        }
        return null;
    }

    @EventHandler
    public void onPartyAdd(PartyAddEvent event) {
        if(event.getParty() instanceof Competitor) {
            updateCompetitors();
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPartyChange(PlayerChangePartyEvent event) throws EventException {
        final MatchPlayer player = event.getPlayer();
        if(event.getNewParty() == null) {
            if(event.getOldParty() instanceof Competitor && match.hasStarted() && !increment(player, -1, false, true)) {
                eliminate(player);
            }
        } else if(event.getNewParty() instanceof Competitor) {
            event.yield();
            setup(player, true);
            updateCompetitors();
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDeath(MatchPlayerDeathEvent event) {
        event.getVictim()
             .competitor()
             .ifPresent(competitor -> increment(event.getVictim(), -1, false, true));
    }

    @EventHandler
    public void onRelease(ParticipantReleaseEvent event) {
        livesTitle(event.getPlayer(), event.wasFrozen(), false);
    }

}
