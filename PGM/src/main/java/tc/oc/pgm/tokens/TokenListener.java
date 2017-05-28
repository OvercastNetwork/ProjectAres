package tc.oc.pgm.tokens;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import tc.oc.commons.bukkit.event.ObserverKitApplyEvent;
import tc.oc.commons.bukkit.raindrops.RaindropConstants;
import tc.oc.commons.bukkit.tokens.TokenUtil;
import tc.oc.commons.core.util.Comparables;
import tc.oc.pgm.events.MatchEndEvent;
import tc.oc.pgm.events.ObserverInteractEvent;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.teams.Team;
import tc.oc.pgm.tokens.gui.MainTokenButton;
import java.time.Duration;

public class TokenListener implements Listener {

    @EventHandler
    public void onObserverInteract(ObserverInteractEvent event) {
        if (event.getClickType() == ClickType.RIGHT) {
            ItemStack main = MainTokenButton.getInstance().getIcon().create();
            //isSimilar so that stacks of the item will still open the menu
            if (event.getPlayer().getBukkit().getItemInHand().isSimilar(main)) {
                MainTokenButton.getInstance().function(event.getPlayer().getBukkit());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleMatchEndEvent(final MatchEndEvent event) {
        Match match = event.getMatch();
        //use the same playtime rules as raindrops
        boolean applyCutoff = Comparables.greaterThan(match.getLength(), RaindropConstants.TEAM_REWARD_CUTOFF);
        for(MatchPlayer player : match.getParticipatingPlayers()) {
            if(player.getParty() instanceof Team) {
                Team team = (Team) player.getParty();
                Duration teamTime = team.getCumulativeParticipation(player.getPlayerId());
                if(!(applyCutoff && Comparables.lessThan(teamTime, RaindropConstants.TEAM_REWARD_CUTOFF))) {
                    if (Math.random() < 0.005) {
                        if (Math.random() > 0.25) {
                            Bukkit.broadcastMessage(player.getDisplayName() + ChatColor.AQUA + " has won a Mutation Token!");
                            TokenUtil.giveMutationTokens(TokenUtil.getUser(player.getBukkit()), 1);
                        } else {
                            Bukkit.broadcastMessage(player.getDisplayName() + ChatColor.AQUA + " has won a SetNext Token!");
                            TokenUtil.giveMapTokens(TokenUtil.getUser(player.getBukkit()), 1);
                        }
                    }
                }
            }
        }
    }
}
