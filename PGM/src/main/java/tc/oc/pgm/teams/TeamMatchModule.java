package tc.oc.pgm.teams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.collect.Range;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.apache.commons.lang.math.Fraction;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.api.docs.PlayerId;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.formatting.StringUtils;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.commons.core.util.Comparators;
import tc.oc.commons.core.util.Optionals;
import tc.oc.commons.core.util.Streams;
import tc.oc.pgm.Config;
import tc.oc.commons.bukkit.chat.Links;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.PlayerPartyChangeEvent;
import tc.oc.pgm.features.FeatureDefinitionContext;
import tc.oc.pgm.features.MatchFeatureContext;
import tc.oc.pgm.join.JoinAllowed;
import tc.oc.pgm.join.JoinConfiguration;
import tc.oc.pgm.join.JoinDenied;
import tc.oc.pgm.join.JoinHandler;
import tc.oc.pgm.join.JoinMatchModule;
import tc.oc.pgm.join.JoinMethod;
import tc.oc.pgm.join.JoinQueued;
import tc.oc.pgm.join.JoinRequest;
import tc.oc.pgm.join.JoinResult;
import tc.oc.pgm.join.QueuedParticipants;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.Party;
import tc.oc.pgm.start.StartMatchModule;
import tc.oc.pgm.start.UnreadyReason;
import tc.oc.pgm.teams.events.TeamResizeEvent;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static tc.oc.commons.core.util.Functions.memoize;
import static tc.oc.commons.core.util.Utils.*;

@ListenerScope(MatchScope.LOADED)
public class TeamMatchModule extends MatchModule implements Listener, JoinHandler {

    private static final String CHOOSE_TEAM_PERMISSION = "pgm.join.choose.participating";

    class NeedMorePlayers implements UnreadyReason {
        final @Nullable Team team;
        final int players;

        NeedMorePlayers(@Nullable Team team, int players) {
            this.team = team;
            this.players = players;
        }

        @Override
        public BaseComponent getReason() {
            if(team != null) {
                if(players == 1) {
                    return new TranslatableComponent("start.needMorePlayers.team.singular",
                                                     new Component(String.valueOf(players), ChatColor.AQUA),
                                                     team.getComponentName());
                } else {
                    return new TranslatableComponent("start.needMorePlayers.team.plural",
                                                     new Component(String.valueOf(players), ChatColor.AQUA),
                                                     team.getComponentName());
                }
            } else {
                if(players == 1) {
                    return new TranslatableComponent("start.needMorePlayers.ffa.singular",
                                                     new Component(String.valueOf(players), ChatColor.AQUA));
                } else {
                    return new TranslatableComponent("start.needMorePlayers.ffa.plural",
                                                     new Component(String.valueOf(players), ChatColor.AQUA));
                }
            }
        }

        @Override
        public boolean canForceStart() {
            return true;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{players=" + players + " team=" + team + "}";
        }
    };

    @Inject private TeamConfiguration config;
    @Inject private JoinConfiguration joinConfiguration;
    @Inject private FeatureDefinitionContext definitions;
    @Inject private MatchFeatureContext features;
    @Inject private JoinMatchModule jmm;
    @Inject private StartMatchModule smm;

    private final Optional<Boolean> requireEven;

    // Players who autojoined their current team
    private final Set<MatchPlayer> autoJoins = new HashSet<>();

    // Minimum at any time of the number of additional players needed to start the match
    private int minPlayersNeeded = Integer.MAX_VALUE;

    public TeamMatchModule(Match match, Optional<Boolean> requireEven) {
        super(match);
        this.requireEven = requireEven;
    }

    @Override
    public void load() {
        super.load();

        jmm.registerHandler(this);

        Streams.consume(teams());

        updatePlayerLimits();
        updateReadiness();
    }

    protected void updatePlayerLimits() {
        int min = 0, max = 0;
        for(Team team : getTeams()) {
            min += team.getMinPlayers();
            max += team.getMaxPlayers();
        }
        getMatch().setPlayerLimits(Range.closed(min, max));
    }

    protected void updateReadiness() {
        if(getMatch().hasStarted()) return;

        final int playersQueued = jmm.getQueuedParticipants().getPlayers().size();
        final int playersJoined = getMatch().getParticipatingPlayers().size();

        Team singleTeam = null;
        int teamNeeded = 0;
        for(Team t : getTeams()) {
            int p = t.getMinPlayers() - t.getPlayers().size();
            if(p > 0) {
                singleTeam = teamNeeded == 0 ? t : null;
                teamNeeded += p;
            }
        }
        teamNeeded -= playersQueued;

        int globalNeeded = Config.minimumPlayers() - playersJoined - playersQueued;

        int playersNeeded;
        if(globalNeeded > teamNeeded) {
            playersNeeded = globalNeeded;
            singleTeam = null;
        } else {
            playersNeeded = teamNeeded;
        }

        if(playersNeeded > 0) {
            smm.addUnreadyReason(new NeedMorePlayers(singleTeam, playersNeeded));

            // Whenever playersNeeded reaches a new minimum, reset the unready timeout
            if(playersNeeded < minPlayersNeeded) {
                minPlayersNeeded = playersNeeded;
                smm.restartUnreadyTimeout();
            }
        } else {
            smm.removeUnreadyReason(NeedMorePlayers.class);
        }
    }

    public Stream<Team> teams() {
        return definitions.all(TeamFactory.class)
                          .map(this::team);
    }

    public Set<Team> getTeams() {
        return teams().collect(Collectors.toImmutableSet());
    }

    public Team team(TeamFactory def) {
        return features.get(def);
    }

    public Stream<Team> shuffledTeams() {
        final List<Team> list = new ArrayList<>(getTeams());
        Collections.shuffle(list, match.getRandom());
        return list.stream();
    }

    public @Nullable Team bestFuzzyMatch(String name) {
        return bestFuzzyMatch(name, 0.9);
    }

    public @Nullable Team bestFuzzyMatch(String name, double threshold) {
        Map<String, Team> byName = new HashMap<>();
        for(Team team : getTeams()) byName.put(team.getName(), team);
        return StringUtils.bestFuzzyMatch(name, byName, threshold);
    }

    public Optional<Team> fuzzyMatch(String name) {
        return Optional.ofNullable(bestFuzzyMatch(name));
    }

    protected void setAutoJoin(MatchPlayer player, boolean autoJoined) {
        if(autoJoined) {
            autoJoins.add(player);
        } else {
            autoJoins.remove(player);
        }
    }

    protected boolean isAutoJoin(MatchPlayer player) {
        return autoJoins.contains(player);
    }

    private boolean canSwitchTeams(MatchPlayer joining) {
        return config.allowSwitch() || !getMatch().hasStarted();
    }

    private boolean canChooseTeam(MatchPlayer joining) {
        return config.allowChoose() && joining.getBukkit().hasPermission(CHOOSE_TEAM_PERMISSION);
    }

    public boolean forceJoin(MatchPlayer joining, @Nullable Competitor forcedParty) {
        if(forcedParty instanceof Team) {
            return forceJoin(joining, (Team) forcedParty, false);
        } else if(forcedParty == null) {
            final JoinResult result = queryAutoJoin(joining, false);
            if(result.isAllowed() && result.competitor().isPresent() && result.competitor().get() instanceof Team) {
                return forceJoin(joining, (Team) result.competitor().get(), true);
            }
        }
        return false;
    }

    private boolean forceJoin(MatchPlayer player, Team newTeam, boolean autoJoin) {
        checkNotNull(newTeam);

        if(Optionals.equals(newTeam, player.partyMaybe())) return true;

        if(getMatch().setPlayerParty(player, newTeam, true)) {
            setAutoJoin(player, autoJoin);
            return true;
        } else {
            return false;
        }
    }

    private boolean requireEvenTeams() {
        final boolean requireEven = this.requireEven.orElse(config.requireEven());
        if(!requireEven) return false;

        // If any teams are unequal in size, don't try to even the teams
        // TODO: This could be done, it's just more complicated
        int size = -1;
        for(Team team : getTeams()) {
            if(size == -1) {
                size = team.getMaxOverfill();
            } else if(size != team.getMaxOverfill()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Do all teams have equal fullness ratios?
     */
    public boolean areTeamsEven() {
        return Streams.isUniform(teams().map(team -> team.getFullness(Team::getMaxOverfill)));
    }

    /**
     * Return the most full participating team
     */
    public Team getFullestTeam() {
        return (Team) shuffledTeams()
            .max(Comparator.comparing(team -> team.getFullness(Team::getMaxOverfill)))
            .get();
    }

    /**
     * Return join query results for all teams, sorted by auto-join preference
     * i.e. according to the following chain of criteria:
     *
     *     - Successful joins before failed ones
     *     - Joins that do not require a priority kick before those that do
     *     - Ascending team fullness relative to min-players
     *     - Ascending team fullness relative to max-overfill
     *     - Random order
     *
     * It is assumed that the joining player will leave their current team before
     * joining i.e. they are ignored for all calculations.
     */
    private Stream<JoinResult> autoJoinResults(MatchPlayer joining, boolean priorityKick) {
        return Team.withChange(joining, null, () -> {
            final Function<Team, JoinResult> queryJoin = memoize(team -> team.queryJoin(joining, priorityKick, false));
            return shuffledTeams()
                .sorted(Comparator.<Team, JoinResult>comparing(queryJoin, Comparator.comparing(JoinResult::isAllowed, Comparators.firstIf())
                                                                                    .thenComparing(JoinResult::priorityKickRequired, Comparators.lastIf()))
                                  .<Fraction>thenComparing((Team team) -> team.getFullness(Team::getMinPlayers))
                                  .<Fraction>thenComparing((Team team) -> team.getFullness(Team::getMaxOverfill)))
                .map(queryJoin);
        });
    }

    /**
     * Return the best team for the given player to join, as determined by {@link #autoJoinResults}.
     * If no teams can be joined, the result will be the least bad option.
     */
    private JoinResult queryAutoJoin(MatchPlayer joining, boolean priorityKick) {
        return autoJoinResults(joining, priorityKick)
            .filter(result -> !(result.competitor().isPresent() && joining.inParty(result.competitor().get())))
            .findFirst()
            .get();
    }

    /**
     * Get the given player's last joined {@link Team} in this match,
     * or empty if the player has never joined a team.
     */
    public Optional<Team> lastTeam(PlayerId playerId) {
        return getInstanceOf(getMatch().getLastCompetitor(playerId), Team.class);
    }

    /**
     * What would happen if the given player tried to join the given team right now?
     */
    @Override
    public @Nullable JoinResult queryJoin(MatchPlayer joining, JoinRequest request) {
        if(!request.competitor().isPresent() || request.competitor().get() instanceof Team) {
            return queryJoin(joining, request, false);
        }
        return null;
    }

    private JoinResult queryJoin(MatchPlayer joining, JoinRequest request, boolean queued) {
        final Optional<Team> lastTeam = lastTeam(joining.getPlayerId());
        final Optional<Team> chosenTeam = getInstanceOf(request.competitor(), Team.class);

        if(request.method() == JoinMethod.REMOTE) {
            // If remote joining, force the player onto a team
            return JoinAllowed.force(queryAutoJoin(joining, true));

        } else if(!request.competitor().isPresent()) {
            // If autojoining, and the player is already on a team, the request is satisfied
            if(Optionals.isInstance(joining.partyMaybe(), Competitor.class)) {
                return JoinDenied.error("command.gameplay.join.alreadyOnTeam", joining.getParty().getComponentName());
            }

            // If team choosing is disabled, and the match has not started yet, defer the join.
            // Note that this can only happen with autojoin. Choosing a team always fails if
            // the condition below is true.
            if(!queued && !config.allowChoose() && !getMatch().hasStarted()) {
                return new JoinQueued();
            }

            if(lastTeam.isPresent()) {
                // If the player was previously on a team, try to join that team first
                final JoinResult rejoin = lastTeam.get().queryJoin(joining, true, true);
                if(rejoin.isAllowed() || !canSwitchTeams(joining)) return rejoin;
                // If the join fails, and the player is allowed to switch teams, fall through to the auto-join
            }

            // Try to find a team for the player to join
            final JoinResult auto = queryAutoJoin(joining, true);
            if(auto.isAllowed()) return auto;

            if(jmm.canJoinFull(joining) || !joinConfiguration.overfill()) {
                return JoinDenied.unavailable("autoJoin.teamsFull");
            } else {
                // If the player is not premium, and overfill is enabled, plug the shop
                return JoinDenied.unavailable("autoJoin.teamsFull")
                    .also(Links.shopPlug("shop.plug.joinFull"));
            }

        } else if(chosenTeam.isPresent()) {
            // If the player is already on the chosen team, there is nothing to do
            if(joining.hasParty() && contains(chosenTeam, joining.getParty())) {
                return JoinDenied.error("command.gameplay.join.alreadyOnTeam", joining.getParty().getComponentName());
            }

            // If team switching is disabled and the player is choosing to re-join their
            // last team, don't consider it a "choice" since that's the only team they can
            // join anyway. In any other case, check that they are allowed to choose their team.
            if(config.allowSwitch() || !chosenTeam.equals(lastTeam)) {
                // Team choosing is disabled
                if(!config.allowChoose()) {
                    return JoinDenied.error("command.gameplay.join.choiceDisabled");
                }

                // Player is not allowed to choose their team
                if(!canChooseTeam(joining)) {
                    return JoinDenied.unavailable("command.gameplay.join.choiceDenied")
                        .also(Links.shopPlug("shop.plug.chooseTeam"));
                }
            }

            // If team switching is disabled, check if the player is rejoining their former team
            if(!canSwitchTeams(joining) && lastTeam.isPresent()) {
                if(chosenTeam.equals(lastTeam)) {
                    return chosenTeam.get().queryJoin(joining, true, true);
                } else {
                    return JoinDenied.error("command.gameplay.join.switchDisabled", lastTeam.get().getComponentName());
                }
            }

            return chosenTeam.get().queryJoin(joining, true, false);
        }

        return null;
    }

    @Override
    public boolean join(MatchPlayer joining, JoinRequest request, JoinResult result) {
        if(result.isAllowed() && isInstanceOf(result.competitor(), Team.class)) {
            final Optional<Team> lastTeam = lastTeam(joining.getPlayerId());
            final Team newTeam = (Team) result.competitor().get();

            // FIXME: When a player rejoins their last team, we lose their autojoin status
            if(!forceJoin(joining, newTeam, !lastTeam.isPresent() && !request.competitor().isPresent())) {
                return false;
            }

            if(result.priorityKickRequired()) {
                logger.info("Bumping a player from " + newTeam.getColoredName() + " to make room for " + joining.getDisplayName());
                kickPlayerOffTeam(newTeam, false);
            }

            return true;
        }

        return false;
    }

    @Override
    public void queuedJoin(QueuedParticipants queue) {
        final boolean even = requireEvenTeams();
        final JoinRequest request = JoinRequest.user();

        // First, eliminate any players who cannot join at all, so they do not influence the even teams logic
        List<MatchPlayer> shortList = new ArrayList<>();
        for(MatchPlayer player : queue.getOrderedPlayers()) {
            JoinResult result = queryJoin(player, request, true);
            if(result.isAllowed()) {
                shortList.add(player);
            } else {
                // This will send a failure message
                join(player, request, result);
            }
        }

        for(int i = 0; i < shortList.size(); i++) {
            MatchPlayer player = shortList.get(i);
            if(even && areTeamsEven() && shortList.size() - i < getTeams().size()) {
                // Prevent join if even teams are required, and there aren't enough remaining players to go around
                player.sendWarning(new TranslatableComponent("command.gameplay.join.uneven"));
            } else {
                join(player, request, queryJoin(player, request, true));
            }
        }
    }

    /**
     * Try to balance teams by bumping players to other teams
     */
    public void balanceTeams() {
        if(!config.autoBalance()) return;

        logger.info("Auto-balancing teams");

        for(;;) {
            Team team = this.getFullestTeam();
            if(team == null) break;
            if(!team.isStacked()) break;
            logger.info("Bumping a player from stacked team " + team.getColoredName());
            if(!this.kickPlayerOffTeam(team, true)) break;
        }
    }

    public boolean kickPlayerOffTeam(Team kickFrom, boolean forBalance) {
        checkArgument(kickFrom.getMatch() == getMatch());

        // Find all players who can be bumped
        List<MatchPlayer> kickable = kickFrom.getPlayers().stream()
            .filter(player -> !jmm.canPriorityKick(player) || (forBalance && isAutoJoin(player)))
            .collect(Collectors.toImmutableList());

        // Premium players can be auto-balanced if they auto-joined
        if(kickable.isEmpty()) return false;

        // Choose an unfortunate cheapskate
        MatchPlayer kickMe = kickable.get(getMatch().getRandom().nextInt(kickable.size()));

        // Try to put them on another team
        final Party kickTo;
        final JoinResult kickResult = queryAutoJoin(kickMe, false);
        if(kickResult.isAllowed()) {
            kickTo = kickResult.competitor().get();
        } else {
            // If no teams are available, kick them to observers, if necessary
            if(forBalance) return false;
            kickTo = getMatch().getDefaultParty();
        }

        // Give them the bad news
        if(jmm.canPriorityKick(kickMe)) {
            kickMe.sendMessage(new TranslatableComponent("gameplay.kickedForBalance", kickTo.getComponentName()));
            kickMe.sendMessage(new TranslatableComponent("gameplay.autoJoinSwitch"));
        } else {
            kickMe.playSound(Sound.ENTITY_VILLAGER_HURT, kickMe.getBukkit().getLocation(), 1, 1);
            if(forBalance) {
                kickMe.sendWarning(new TranslatableComponent("gameplay.kickedForBalance", kickTo.getComponentName()), false);
                kickMe.sendMessage(Links.shopPlug("shop.plug.neverSwitched"));
            } else {
                kickMe.sendWarning(new TranslatableComponent("gameplay.kickedForPremium", kickFrom.getComponentName()), false);
                kickMe.sendMessage(Links.shopPlug("shop.plug.neverKicked"));
            }
        }

        logger.info("Bumping " + kickMe.getDisplayName() + " to " + kickTo.getColoredName());

        if(kickTo instanceof Team) {
            return forceJoin(kickMe, (Team) kickTo);
        } else {
            return getMatch().setPlayerParty(kickMe, kickTo, false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPartyChange(PlayerPartyChangeEvent event) {
        if(event.getNewParty() instanceof Team) {
            event.getPlayer().sendMessage(new TranslatableComponent("team.join", event.getNewParty().getComponentName()));
        }
        updateReadiness();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTeamResize(TeamResizeEvent event) {
        updateReadiness();
    }
}
