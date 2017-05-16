package tc.oc.pgm.score;

import java.util.Map;
import java.util.Optional;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.bukkit.inventory.Slot;
import tc.oc.commons.bukkit.item.ItemUtils;
import tc.oc.commons.core.chat.Component;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.PlayerItemTransferEvent;
import tc.oc.pgm.features.FeatureDefinition;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.FilterMatchModule;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.teams.Team;
import tc.oc.pgm.utils.MaterialPattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public interface ScoreBoxFactory extends FeatureDefinition {}

class ScoreBoxFactoryImpl extends FeatureDefinition.Impl implements ScoreBoxFactory {
    private final Filter trigger;
    private final int score;
    private final Filter filter;
    private final boolean silent;
    private final ImmutableMap<MaterialPattern, Double> redeemables;

    public ScoreBoxFactoryImpl(Filter trigger, int score, Filter filter, boolean silent, ImmutableMap<MaterialPattern, Double> redeemables) {
        Preconditions.checkNotNull(trigger, "region");
        Preconditions.checkNotNull(filter, "filter");

        this.trigger = trigger;
        this.score = score;
        this.filter = filter;
        this.silent = silent;
        this.redeemables = redeemables;
    }

    @Override
    public void load(Match match) {
        new ScoreBox(match);
    }

    @ListenerScope(MatchScope.RUNNING)
    public class ScoreBox implements Listener {

        private final Match match;
        private final ScoreMatchModule smm;

        public ScoreBox(Match match) {
            this.match = checkNotNull(match);
            this.smm = match.needMatchModule(ScoreMatchModule.class);

            match.registerEvents(this);
            match.needMatchModule(FilterMatchModule.class)
                 .onRise(MatchPlayer.class, trigger, player -> {
                     if(canScore(player)) {
                         score(player, score + redeemItems(player));
                     }
                 });
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onPlayerAcquireItem(final PlayerItemTransferEvent event) {
            match.interactor(event.getPlayer()).ifPresent(player -> {
                if(event.isAcquiring() &&
                   canScore(player) &&
                   trigger.query(player).isAllowed()) {

                    score(player, redeemItems(player));
                }
            });
        }

        private boolean canScore(MatchPlayer player) {
            return player.canInteract() &&
                   filter.query(player).isAllowed();
        }

        /**
         * If given item is redeemed, change its amount and return the points awarded (which may be 0 or negative)
         *
         * Return NaN if the item is not redeemed.
         */
        private double redeemItem(ItemStack stack) {
            if(stack != null) {
                for(Map.Entry<MaterialPattern, Double> entry : ((Map<MaterialPattern, Double>) redeemables).entrySet()) {
                    if(entry.getKey().matches(stack.getData())) {
                        final double points = entry.getValue() * stack.getAmount();
                        stack.setAmount(0);
                        return points;
                    }
                }
            }
            return Double.NaN;
        }

        private double redeemItems(MatchPlayer player) {
            if(redeemables.isEmpty()) return 0;

            final PlayerInventory inventory = player.getInventory();
            return Slot.Player.player()
                              .map(slot -> {
                                  final ItemStack item = slot.getItem(inventory);
                                  final double points = redeemItem(item);
                                  if(Double.isNaN(points)) {
                                      return 0D;
                                  } else {
                                      // Note that redeeming a zero-amount stack will remove the item, but give no points
                                      slot.putItem(inventory, ItemUtils.something(item).orElse(null));
                                      return points;
                                  }
                              })
                              .reduce(0D, (a, b) -> a + b);
        }

        private void score(MatchPlayer player, double points) {
            checkState(player.isParticipating());

            if(points == 0) return;

            final int integerPoints = (int) points; // can be negative
            if(!silent && integerPoints != 0) {
                player.playSound(Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                player.getMatch().sendMessage(
                    new Component(
                        new TranslatableComponent(
                            player.getCompetitor() instanceof Team ? "match.score.scorebox.team"
                                                                   : "match.score.scorebox.individual",
                            player.getStyledName(NameStyle.VERBOSE),
                            new TranslatableComponent(
                                integerPoints == 1 ? "points.singularCompound"
                                                   : "points.pluralCompound",
                                new Component(integerPoints, ChatColor.DARK_AQUA)
                            ),
                            new Component(player.getParty().getComponentName())
                        ), ChatColor.GRAY
                    )
                );
            }

            smm.incrementScore(player.getCompetitor(), points, Optional.of(player));
        }
    }
}
