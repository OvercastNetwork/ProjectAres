package tc.oc.pgm.score;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;
import tc.oc.commons.core.util.DefaultMapAdapter;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.MatchPlayerDeathEvent;
import tc.oc.pgm.events.MatchScoreChangeEvent;
import tc.oc.pgm.match.*;
import tc.oc.pgm.victory.VictoryMatchModule;

import static com.google.common.base.Preconditions.checkState;

@ListenerScope(MatchScope.RUNNING)
public class ScoreMatchModule extends MatchModule implements Listener {

    private final ScoreConfig config;
    private final Map<Competitor, Double> scores = new DefaultMapAdapter<>(new HashMap<>(), 0d);

    public ScoreMatchModule(Match match, ScoreConfig config) {
        super(match);
        this.config = config;
    }

    @Override
    public void load() {
        super.load();
        match.needMatchModule(VictoryMatchModule.class).setVictoryCondition(new ScoreVictoryCondition(config.scoreLimit, scores));
    }

    public boolean hasScoreLimit() {
        return this.config.scoreLimit.isPresent();
    }

    public int getScoreLimit() {
        checkState(hasScoreLimit());
        return this.config.scoreLimit.get();
    }

    public double getScore(Competitor competitor) {
        return this.scores.get(competitor);
    }

    /** Gets the score message for the match. */
    public Component getScoreMessage() {
        return new Component(ChatColor.DARK_AQUA)
            .translate("match.scoreboard.scores.title")
            .extra(": ")
            .extra(Components.join(new Component(" "), scores.entrySet()
                                                             .stream()
                                                             .map(entry -> new Component(entry.getValue().intValue(), entry.getKey().getColor()))
                                                             .collect(Collectors.toList())));
    }

    /** Gets the status message for the match. */
    public BaseComponent getStatusMessage() {
        final Component message = getScoreMessage();
        if(config.scoreLimit.isPresent()) {
            message.extra("  [" + config.scoreLimit.get() + "]", ChatColor.GRAY);
        }
        return message;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void incrementDeath(MatchPlayerDeathEvent event) {
        if(!event.getVictim().isParticipating()) return;

        // add +1 to killer's team if it was a kill, otherwise -1 to victim's team
        if(event.isChallengeKill()) {
            this.incrementScore(event.getKiller().getParty(), this.config.killScore);
        } else {
            this.incrementScore(event.getVictim().getCompetitor(), -this.config.deathScore);
        }
    }

    public void incrementScore(Competitor competitor, double amount) {
        this.incrementScore(competitor, amount, null);
    }

    public void incrementScore(Competitor competitor, double amount, Optional<MatchPlayer> player) {
        double oldScore = this.scores.get(competitor);
        double newScore = oldScore  + amount;

        if(this.config.scoreLimit.isPresent() && newScore > this.config.scoreLimit.get()) {
            newScore = this.config.scoreLimit.get();
        }

        MatchScoreChangeEvent event = new MatchScoreChangeEvent(competitor.getMatch(), competitor, player, oldScore, newScore);
        this.match.getServer().getPluginManager().callEvent(event);

        this.scores.put(competitor, event.getNewScore());
        this.match.needMatchModule(VictoryMatchModule.class).invalidateAndCheckEnd();
    }
}
