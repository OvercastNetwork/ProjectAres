package tc.oc.pgm.raindrops;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import com.google.common.collect.HashMultimap;
import com.google.common.primitives.Ints;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDespawnInVoidEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;
import tc.oc.api.docs.PlayerId;
import tc.oc.commons.bukkit.raindrops.RaindropConstants;
import tc.oc.commons.bukkit.raindrops.RaindropUtil;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.util.Comparables;
import tc.oc.pgm.PGM;
import tc.oc.pgm.destroyable.DestroyableContribution;
import tc.oc.pgm.destroyable.DestroyableDestroyedEvent;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.MatchEndEvent;
import tc.oc.pgm.events.MatchPlayerDeathEvent;
import tc.oc.pgm.goals.Goal;
import tc.oc.pgm.goals.GoalMatchModule;
import tc.oc.pgm.goals.TouchableGoal;
import tc.oc.pgm.goals.events.GoalCompleteEvent;
import tc.oc.pgm.goals.events.GoalTouchEvent;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchPlayerState;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.ParticipantState;
import tc.oc.pgm.teams.Team;
import tc.oc.pgm.teams.TeamMatchModule;
import tc.oc.pgm.victory.VictoryMatchModule;
import tc.oc.pgm.wool.MonumentWool;
import tc.oc.pgm.wool.MonumentWoolFactory;
import tc.oc.pgm.wool.PlayerWoolPlaceEvent;

@ListenerScope(MatchScope.LOADED)
public class RaindropListener implements Listener {

    private final HashMultimap<MonumentWool, PlayerId> touchedWools = HashMultimap.create();
    private final Map<Item, PlayerId> droppedWools = new WeakHashMap<>();
    private final HashMultimap<DyeColor, PlayerId> destroyedWools = HashMultimap.create();
    private final HashMultimap<TouchableGoal, ParticipantState> deferredTouches = HashMultimap.create();

    private double tiedRewardPercent(Match match) {
        return 1.0d / match.needMatchModule(TeamMatchModule.class).getTeams().size();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleItemDrop(final PlayerDropItemEvent event) {
        ParticipantState player = PGM.getMatchManager().getParticipantState(event.getPlayer());
        if(player == null) return;

        Competitor team = player.getParty();
        Item itemDrop = event.getItemDrop();
        ItemStack item = itemDrop.getItemStack();

        if (this.isDestroyableWool(item, team)) {
            this.droppedWools.put(itemDrop, player.getPlayerId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleItemDespawn(final EntityDespawnInVoidEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Item)) return;
        ItemStack stack = ((Item) entity).getItemStack();

        PlayerId playerId = this.droppedWools.remove(entity);
        if (playerId == null) return;

        ParticipantState player = PGM.getMatchManager().getParticipantState(playerId);
        if (player == null) return;

        if(isDestroyableWool(stack, player.getParty())) {
            giveWoolDestroyRaindrops(player, ((Wool) stack.getData()).getColor());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleCraft(final CraftItemEvent event) {
        ParticipantState player = PGM.getMatchManager().getParticipantState(event.getWhoClicked());
        if (player == null) return;

        for (ItemStack ingredient : event.getInventory().getMatrix()) {
            if(this.isDestroyableWool(ingredient, player.getParty())) {
                giveWoolDestroyRaindrops(player, ((Wool) ingredient.getData()).getColor());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleMatchEndEvent(final MatchEndEvent event) {
        final Set<Competitor> winners = event.getMatch().needMatchModule(VictoryMatchModule.class).winners();
        Match match = event.getMatch();

        boolean applyCutoff = Comparables.greaterThan(match.getLength(), RaindropConstants.TEAM_REWARD_CUTOFF);
        for(MatchPlayer player : match.getParticipatingPlayers()) {
            if(player.getParty() instanceof Team) {
                Team team = (Team) player.getParty();
                Duration teamTime = team.getCumulativeParticipation(player.getPlayerId());
                if(!(applyCutoff && Comparables.lessThan(teamTime, RaindropConstants.TEAM_REWARD_CUTOFF))) {
                    Component message = new Component(net.md_5.bungee.api.ChatColor.GRAY);
                    double percent = 1.0;
                    Competitor playerTeam = player.getCompetitor();
                    assert playerTeam != null;

                    if(winners.contains(playerTeam)) {
                        if(winners.size() == 1) {
                            message.extra(new TranslatableComponent("matchend.team.won", playerTeam.getComponentName()));
                        } else {
                            message.extra(new TranslatableComponent("matchend.team.tied", playerTeam.getComponentName()));
                            percent = tiedRewardPercent(match);
                        }
                    } else {
                        message.extra(new TranslatableComponent("matchend.team.loyalty",
                                                                playerTeam.getComponentName(),
                                                                new Component(String.valueOf(teamTime.toMinutes()))));
                        percent = RaindropConstants.LOSING_TEAM_REWARD_PERCENT;
                    }

                    givePercentageRaindrops(player.getParticipantState(), RaindropConstants.TEAM_REWARD, message, true, percent);
                }
            }
        }

        this.touchedWools.clear();
        this.droppedWools.clear();
        this.destroyedWools.clear();
        this.deferredTouches.clear();
    }

    private void giveGoalTouchRaindrops(ParticipantState player, TouchableGoal goal) {
        this.giveRaindrops(player,
                           RaindropConstants.TOUCH_GOAL_REWARD,
                           goal.getTouchMessage(player, true));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleGoalTouch(final GoalTouchEvent event) {
        Goal goal = event.getGoal();

        if (!goal.isVisible() || goal.isCompleted() || event.getPlayer() == null || !event.isFirstForPlayerLife()) {
            return;
        }

        if (goal instanceof MonumentWool) {
            PlayerId playerId = event.getPlayer().getPlayerId();
            if (this.touchedWools.containsEntry(goal, playerId)) {
                return;
            } else {
                this.touchedWools.put((MonumentWool) goal, playerId);
            }
        }

        if(event.getGoal().getDeferTouches()) {
            this.deferredTouches.put(event.getGoal(), event.getPlayer());
        } else {
            this.giveGoalTouchRaindrops(event.getPlayer(), event.getGoal());
            event.setCancelToucherMessage(true);
        }
    }

    @EventHandler
    public void handleGoalComplete(GoalCompleteEvent event) {
        if(event.getGoal() instanceof TouchableGoal) {
            TouchableGoal goal = (TouchableGoal) event.getGoal();
            for(ParticipantState player : this.deferredTouches.get(goal)) {
                this.giveGoalTouchRaindrops(player, goal);
            }
            this.deferredTouches.removeAll(goal);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void woolPlace(final PlayerWoolPlaceEvent event) {
        if (event.getWool().isVisible()) {
            giveRaindrops(event.getPlayer().getPlayerId(), RaindropConstants.WOOL_PLACE_REWARD, new TranslatableComponent("match.wool.place", event.getWool().getComponentName()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void destroyableDestroy(final DestroyableDestroyedEvent event) {
        if (event.getDestroyable().isVisible()) {
            for (DestroyableContribution info : event.getDestroyable().getContributions()) {
                String percentage = ChatColor.GREEN.toString() + ((int) (info.getPercentage() * 100)) + ChatColor.GRAY;
                BaseComponent reason = new TranslatableComponent("match.destroyable.destroy", percentage, new Component(event.getDestroyable().getName(), net.md_5.bungee.api.ChatColor.GREEN));

                giveRaindrops(info.getPlayerState().getPlayerId(), (int) (RaindropConstants.DESTROYABLE_DESTROY_PERCENT_REWARD * info.getPercentage()), reason);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void kill(final MatchPlayerDeathEvent event) {
        if(!event.isChallengeKill()) return;

        // Death raindrops are given by the backend, to reduce API usage

        final int raindrops = calculateRaindrops(
            event.getKiller(),
            RaindropConstants.KILL_REWARD,
            false,
            1
        );

        event.setRaindrops(raindrops);

        final MatchPlayer killer = event.getOnlineKiller();
        if(killer != null) {
            RaindropUtil.showRaindrops(
                killer.getBukkit(),
                raindrops,
                RaindropUtil.calculateMultiplier(event.getKiller().getPlayerId()),
                new TranslatableComponent("match.kill.killed", event.getVictim().getComponentName())
            );
        }
    }

    private void giveWoolDestroyRaindrops(final ParticipantState player, final DyeColor wool) {
        MatchPlayer online = player.getMatchPlayer();
        if(online != null) giveWoolDestroyRaindrops(online, wool);
    }

    private void giveWoolDestroyRaindrops(final MatchPlayer player, final DyeColor wool) {
        if(!this.destroyedWools.containsEntry(wool, player.getPlayerId())) {
            this.destroyedWools.put(wool, player.getPlayerId());
            giveRaindrops(player.getPlayerId(), RaindropConstants.WOOL_DESTROY_REWARD, new TranslatableComponent("match.wool.destroy", MonumentWoolFactory.makeComponentName(wool)));
        }
    }

    private int calculateRaindrops(MatchPlayerState player, int count, boolean scaled, double percent) {
        if(scaled) {
            final Match match = player.getMatch();
            count += (int) ((double) match.getParticipatingPlayers().size() / match.getMaxPlayers() * RaindropConstants.MATCH_FULLNESS_BONUS);

            if(player.getParty() instanceof Team) {
                count += Ints.min((int) (Math.sqrt(((Team) player.getParty()).getCumulativeParticipation(player.getPlayerId()).getSeconds()) / RaindropConstants.PLAY_TIME_BONUS), RaindropConstants.PLAY_TIME_BONUS_CUTOFF);
            }
        }

        return RaindropUtil.calculateRaindrops(player.getPlayerId(), (int) (count * percent), true);
    }

    private void giveRaindrops(final MatchPlayerState player, int count, BaseComponent reason) {
        giveRaindrops(player, count, reason, true);
    }

    private void giveRaindrops(final MatchPlayerState player, int count, final BaseComponent reason, boolean scaled) {
        givePercentageRaindrops(player, count, reason, scaled, 1.0);
    }

    private void giveRaindrops(final PlayerId playerId, int count, BaseComponent reason) {
        RaindropUtil.giveRaindrops(playerId, count, null, reason);
    }

    private void givePercentageRaindrops(final MatchPlayerState player, int count, final BaseComponent reason, boolean scaled, double percent) {
        RaindropUtil.giveRaindrops(
            player.getPlayerId(),
            calculateRaindrops(player, count, scaled, percent),
            RaindropUtil.calculateMultiplier(player.getPlayerId()),
            null,
            reason,
            true
        );
    }

    /**
     * Test if the given ItemStack is strictly an enemy wool i.e. not also
     * a wool that the given team can capture.
     */
    private boolean isDestroyableWool(ItemStack stack, Competitor team) {
        if(stack == null || stack.getType() != Material.WOOL) {
            return false;
        }

        DyeColor color = ((Wool) stack.getData()).getColor();
        boolean enemyOwned = false;

        for(Goal goal : team.getMatch().needMatchModule(GoalMatchModule.class).getGoals()) {
            if(goal instanceof MonumentWool) {
                MonumentWool wool = (MonumentWool) goal;
                if(wool.isVisible() && !wool.isPlaced() && wool.getDyeColor() == color) {
                    if(wool.getOwner() == team) {
                        return false;
                    } else {
                        enemyOwned = true;
                    }
                }
            }
        }

        return enemyOwned;
    }
}
