package tc.oc.pgm.modes;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import tc.oc.commons.bukkit.chat.BukkitSound;
import tc.oc.commons.bukkit.chat.TemplateComponent;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.commons.core.util.Comparables;
import tc.oc.commons.core.util.Pair;
import tc.oc.pgm.countdowns.CountdownContext;
import tc.oc.pgm.countdowns.MatchCountdown;
import tc.oc.pgm.countdowns.MultiCountdownContext;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.MatchBeginEvent;
import tc.oc.pgm.events.MatchEndEvent;
import tc.oc.pgm.goals.ModeChangeGoal;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.timelimit.TimeLimitCountdown;

@ListenerScope(MatchScope.LOADED)
public class ObjectiveModeManager implements Listener {

    private static final BukkitSound SOUND = new BukkitSound(Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 0.15f, 1.2f);

    private final Match match;
    private final MultiCountdownContext countdowns;
    private final List<ModeChangeGoal> goals;
    private final Map<ObjectiveMode, ModeChangeCountdown> modes;
    private final String message;
    private boolean started;

    @Inject ObjectiveModeManager(Match match, MultiCountdownContext countdowns, List<ObjectiveMode> modes, List<ModeChangeGoal> goals) {
        this.match = match;
        this.countdowns = countdowns;
        match.registerEvents(countdowns);

        this.goals = goals.stream()
                          .filter(ModeChangeGoal::isAffectedByModeChanges)
                          .collect(Collectors.toImmutableList());

        this.modes = modes.stream()
                          .sorted(Comparator.comparing(ObjectiveMode::after))
                          .collect(Collectors.mappingTo(ModeChangeCountdown::new));

        String message = null;
        for(ModeChangeGoal goal : this.goals) {
            if(message == null) {
                message = goal.getModeChangeMessage();
            } else if(!message.equals(goal.getModeChangeMessage())) {
                message = null;
                break;
            }
        }
        this.message = message != null ? message : "match.objectiveMode.name.generic";
    }

    public BaseComponent name(ObjectiveMode mode) {
        return mode.name()
                   .<BaseComponent>map(TemplateComponent::new)
                   .orElseGet(() -> new TranslatableComponent(message, mode.materialName()));
    }

    public Optional<Pair<ObjectiveMode, Duration>> nextMode() {
        return started
               ? countdowns.countdowns(ModeChangeCountdown.class).findFirst()
                                                      .map(countdown -> Pair.of(countdown.mode, countdowns.getTimeLeft(countdown)))
               : modes.keySet().stream().findFirst().map(mode -> Pair.of(mode, mode.after()));
    }

    public Duration timeUntilMode(ObjectiveMode mode) {
        return started ? Optional.ofNullable(countdowns.getTimeLeft(modes.get(mode)))
                                 .orElse(Duration.ZERO)
                       : mode.after();
    }

    public Map<ObjectiveMode, Duration> modes() {
        return modes.keySet().stream().collect(Collectors.mappingTo(this::timeUntilMode));
    }

    public void advance(ObjectiveMode mode) {
        Set<ObjectiveMode> currentModes = modes().keySet();
        if(currentModes.contains(mode)) {
            goals.forEach(goal -> goal.replaceBlocks(mode.material()));
            countdowns.getAll(ModeChangeCountdown.class).stream().findFirst().ifPresent(countdowns::cancel);
        }
    }

    @EventHandler
    private void start(MatchBeginEvent event) {
        modes.forEach((mode, countdown) -> countdowns.start(countdown, mode.after()));
        started = true;
    }

    @EventHandler
    private void stop(MatchEndEvent event) {
        countdowns.cancelAll();
    }

    private class ModeChangeCountdown extends MatchCountdown {

        final ObjectiveMode mode;

        ModeChangeCountdown(ObjectiveMode mode) {
            super(ObjectiveModeManager.this.match);
            this.mode = mode;
        }

        @Override
        public void onEnd(Duration total) {
            super.onEnd(total);

            match.sendMessage(new Component(ChatColor.DARK_AQUA)
                                             .extra("> > > > ")
                                             .extra(new Component(name(mode), ChatColor.RED))
                                             .extra(" < < < <"));
            match.playSound(SOUND);

            goals.forEach(goal -> goal.replaceBlocks(mode.material()));
        }

        @Override
        public BaseComponent barText(Player viewer) {
            return new Component(new TranslatableComponent("match.objectiveMode.countdown",
                                                           new Component(name(mode), ChatColor.GOLD),
                                                           secondsRemaining(ChatColor.AQUA)),
                                 ChatColor.DARK_AQUA);
        }

        @Override
        protected float barProgress(Duration remaining, Duration total) {
            return super.barProgress(remaining, this.mode.show_before());
        }

        @Override
        public boolean isVisible(Player viewer) {
            if(!super.isVisible(viewer)) return false;

            CountdownContext countdowns = this.getMatch().countdowns();
            Set<TimeLimitCountdown> timeLimitCountdowns = countdowns.getAll(TimeLimitCountdown.class);

            for (TimeLimitCountdown limit : timeLimitCountdowns) {
                // Don't show the countdown if it wont happen before the match ends
                if (Comparables.lessThan(countdowns.getTimeLeft(limit), remaining)) {
                    return false;
                }
            }

            return remaining.getSeconds() < this.mode.show_before().getSeconds();
        }
    }
}
