package tc.oc.pgm.fireworks;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.commons.bukkit.util.BukkitUtils;
import tc.oc.commons.core.scheduler.Task;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.MatchEndEvent;
import tc.oc.pgm.fireworks.FireworksConfig.PostMatch;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.victory.VictoryMatchModule;

@ListenerScope(MatchScope.LOADED)
public class PostMatchFireworkListener extends MatchModule implements Listener {

    private Task task;

    @Inject PostMatchFireworkListener(Match match) {
        super(match);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMatchEnd(final MatchEndEvent event) {
        if(!PostMatch.enabled()) return;
        task = match.getScheduler(MatchScope.LOADED).createRepeatingTask(
            PostMatch.delay(),
            PostMatch.frequency(),
            new FireworkRunner(event.getMatch().needMatchModule(VictoryMatchModule.class).winners())
        );
    }

    private void cancelTask() {
        if(this.task != null) {
            this.task.cancel();
            this.task = null;
        }
    }

    public static List<FireworkEffect.Type> AVAILABLE_TYPES = ImmutableList.<FireworkEffect.Type>builder()
            .add(Type.BALL)
            .add(Type.BALL_LARGE)
            .add(Type.BURST)
            .add(Type.STAR)
            .build();

    public class FireworkRunner implements Runnable {
        private final Set<Color> colors;
        private final Set<Competitor> winners;
        private int iterations = 0;

        public FireworkRunner(Set<Competitor> winners) {
            this.winners = winners;
            this.colors = winners.stream()
                                 .map(winner -> BukkitUtils.colorOf(winner.getColor()))
                                 .collect(Collectors.toSet());
        }

        @Override
        public void run() {
            // Build this list fresh every time, because MatchPlayers can unload, but Competitors can't.
            final List<MatchPlayer> players = winners.stream()
                                                     .flatMap(c -> c.getPlayers().stream())
                                                     .collect(Collectors.toList());
            Collections.shuffle(players);

            for(int i = 0; i < players.size() && i < PostMatch.number(); i++) {
                MatchPlayer player = players.get(i);

                Type type = AVAILABLE_TYPES.get(match.getRandom().nextInt(AVAILABLE_TYPES.size()));

                FireworkEffect effect = FireworkEffect.builder().with(type).withFlicker().withColor(this.colors).withFade(Color.BLACK).build();

                FireworkUtil.spawnFirework(player.getBukkit().getLocation(), effect, PostMatch.power());
            }

            this.iterations++;
            if(this.iterations >= PostMatch.iterations()) {
                cancelTask();
            }
        }
    }
}
