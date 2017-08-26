package tc.oc.pgm.tokens;

import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.bukkit.event.ObserverKitApplyEvent;
import tc.oc.commons.bukkit.raindrops.RaindropConstants;
import tc.oc.commons.bukkit.tokens.TokenUtil;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.util.Comparables;
import tc.oc.pgm.Config;
import tc.oc.pgm.events.MatchEndEvent;
import tc.oc.pgm.events.ObserverInteractEvent;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.teams.Team;
import tc.oc.pgm.tokens.gui.MainTokenButton;
import tc.oc.pgm.victory.VictoryMatchModule;

import java.time.Duration;
import java.util.Set;

public class TokenListener implements Listener {

    @EventHandler
    public void onObserverInteract(ObserverInteractEvent event) {
        if (event.getClickType() == ClickType.RIGHT) {
            MainTokenButton button = new MainTokenButton();
            ItemStack main = button.getIcon().create();
            //isSimilar so that stacks of the item will still open the menu
            if (event.getPlayer().getBukkit().getItemInHand().isSimilar(main)) {
                button.function(event.getPlayer().getBukkit());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleMatchEndEvent(final MatchEndEvent event) {
        if (Config.Token.enabled()) {
            Match match = event.getMatch();
            //use the same playtime rules as raindrops
            boolean applyCutoff = Comparables.greaterThan(match.getLength(), RaindropConstants.TEAM_REWARD_CUTOFF);
            final Set<Competitor> winners = event.getMatch().needMatchModule(VictoryMatchModule.class).winners();
            for (MatchPlayer player : match.getParticipatingPlayers()) {
                if (player.getParty() instanceof Team) {
                    Team team = (Team) player.getParty();
                    Duration teamTime = team.getCumulativeParticipation(player.getPlayerId());
                    if (!(applyCutoff && Comparables.lessThan(teamTime, RaindropConstants.TEAM_REWARD_CUTOFF))) {
                        Competitor playerTeam = player.getCompetitor();
                        assert playerTeam != null;
                        double chance;
                        if (winners.contains(playerTeam)) {
                            chance = Config.Token.winningChance();
                        } else {
                            chance = Config.Token.losingChance();
                        }
                        if (Math.random() < chance) {
                            if (Math.random() > Config.Token.setNextTokenChange()) {
                                event.getMatch().sendMessage(new TranslatableComponent("tokens.mutation.find", player.getStyledName(NameStyle.COLOR)));
                                TokenUtil.giveMutationTokens(TokenUtil.getUser(player.getBukkit()), 1);
                            } else {
                                event.getMatch().sendMessage(new TranslatableComponent("tokens.map.find", player.getStyledName(NameStyle.COLOR)));
                                TokenUtil.giveMapTokens(TokenUtil.getUser(player.getBukkit()), 1);
                            }
                        }

                    }
                }
            }
        }
    }
}
