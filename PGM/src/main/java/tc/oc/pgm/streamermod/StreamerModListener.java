package tc.oc.pgm.streamermod;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import tc.oc.commons.bukkit.tokens.TokenUtil;
import tc.oc.commons.core.chat.Component;
import tc.oc.pgm.destroyable.DestroyableContribution;
import tc.oc.pgm.events.MatchEndEvent;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScheduler;
import tc.oc.pgm.playerstats.StatsUserFacet;

import javax.inject.Inject;

public class StreamerModListener implements Listener {

    private final MatchScheduler scheduler;

    @Inject
    StreamerModListener(MatchScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @EventHandler
    public void matchEnd(MatchEndEvent event) {
        StatsUserFacet bestPlayerStats = null;
        MatchPlayer bestPlayer = null;
        double bestPlayerPoints = 0;

        if (event.getMatch().getParticipatingPlayers().size() < 10) {
            return;
        }

        for (MatchPlayer player : event.getMatch().getParticipatingPlayers()) {
            StatsUserFacet facet = player.getUserContext().facet(StatsUserFacet.class);

            double points = 0;
            points += facet.matchKills();
            points -= facet.deaths();
            for (long wool : facet.getWoolCaptureTimes()) {
                int woolPoints = (int)((wool * 2.5) - 2);
                points += Math.min(Math.max(woolPoints, 0), 120);
            }

            for (long core : facet.getCoreLeakTimes()) {
                int corePoints = (int)((core * 2.5) - 2);
                points += Math.min(Math.max(corePoints, 0), 120);
            }

            for (DestroyableContribution destroyable : facet.getDestroyableDestroyTimes().keySet()) {
                int destroyablePoints = (int)((facet.getDestroyableDestroyTimes().get(destroyable) * 2.5 * destroyable.getPercentage()) - 2);
                points += Math.min(Math.max(destroyablePoints, 0), 120);
            }

            for (long flag : facet.getFlagCaptureTimes()) {
                int flagPoints = (int)(flag / 1.75);
                points += Math.min(Math.max(flagPoints, 0), 120);
            }

            points += (facet.getBlocksBroken() / 20);

            if (bestPlayerStats == null || points > bestPlayerPoints) {
                bestPlayerStats = facet;
                bestPlayer = player;
                bestPlayerPoints = points;
            }
        }

        if (bestPlayer != null) {
            final BaseComponent title = new Component(new TranslatableComponent("broadcast.gameOver.mvp"), ChatColor.AQUA, ChatColor.BOLD);
            Component subtitle;
            if (Math.random() < 0.05) {
                String appendMe;
                if (Math.random() > 0.25) {
                    TokenUtil.giveMutationTokens(TokenUtil.getUser(bestPlayer.getBukkit()), 1);
                    appendMe = ChatColor.YELLOW + ": +1 Mutation Token!";
                } else {
                    TokenUtil.giveMapTokens(TokenUtil.getUser(bestPlayer.getBukkit()), 1);
                    appendMe = ChatColor.YELLOW + ": +1 SetNext Token!";
                }
                subtitle = new Component(bestPlayer.getDisplayName() + appendMe);
            } else {
                subtitle = new Component(bestPlayer.getDisplayName());
            }

            for(MatchPlayer viewer : event.getMatch().getPlayers()) {
                scheduler.createDelayedTask(100L, () -> {
                    viewer.showTitle(title, subtitle, 0, 60, 60);
                });
            }
        }
    }
}
