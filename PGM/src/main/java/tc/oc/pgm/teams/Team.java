package tc.oc.pgm.teams;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.collect.Sets;
import com.google.inject.assistedinject.Assisted;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.apache.commons.lang.math.Fraction;
import org.bukkit.command.CommandSender;
import java.time.Duration;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.virtual.MatchDoc;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.core.chat.BlankComponent;
import tc.oc.commons.core.chat.ChatUtils;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.util.Comparables;
import tc.oc.commons.core.util.Numbers;
import tc.oc.commons.core.util.PunchClock;
import tc.oc.pgm.events.PartyRenameEvent;
import tc.oc.pgm.features.SluggedFeature;
import tc.oc.pgm.join.JoinConfiguration;
import tc.oc.pgm.join.JoinDenied;
import tc.oc.pgm.join.JoinHandler;
import tc.oc.pgm.join.JoinMatchModule;
import tc.oc.pgm.join.JoinResult;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MultiPlayerParty;
import tc.oc.pgm.teams.events.TeamResizeEvent;

/** Mutable class to represent a team created from a TeamInfo instance that is
 * tied to a specific match and will only live as long as the match lives.
 * Teams support custom names and colors that differ from the defaults
 * specified by the map creator.
 */
public class Team extends MultiPlayerParty implements Competitor, SluggedFeature<TeamFactory> {

    // The maximum allowed ratio between the "fullness" of any two teams in a match,
    // as measured by the Team.getFullness method. An imbalance of one player is
    // always allowed, even if it exceeds this ratio.
    public static final Fraction MAX_IMBALANCE = Fraction.getFraction(6, 5);

    public interface Factory {
        Team create(TeamFactory definition);
    }

    private final TeamFactory info;
    private final JoinConfiguration joinConfiguration;
    private final TeamConfiguration teamConfiguration;

    private TeamMatchModule tmm;
    protected @Nullable String name = null;
    protected @Nullable Component componentName;
    protected BaseComponent chatPrefix;
    protected Optional<Integer> minPlayers = Optional.empty(),
                                maxPlayers = Optional.empty(),
                                maxOverfill = Optional.empty();
    protected final Document document = new Document();
    protected final Set<PlayerId> pastPlayers = new HashSet<>();

    // Recorded in the match document, Tourney plugin sets this
    protected @Nullable String leagueTeamId;

    // Players who have ever been on this team and their participation/absence times
    protected final PunchClock<PlayerId> participationClock = new PunchClock<>(getMatch()::runningTime);

    /** Construct a Team instance with the necessary information.
     * @param info Defaults to use for name and color.
     * @param match Match this team is in.
     * @param joinConfiguration
     * @param teamConfiguration
     */
    @Inject Team(@Assisted TeamFactory info, Match match, JoinConfiguration joinConfiguration, TeamConfiguration teamConfiguration) {
        super(match);
        this.info = info;
        this.joinConfiguration = joinConfiguration;
        this.teamConfiguration = teamConfiguration;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{match=" + getMatch() + ", name=" + getName() + "}";
    }

    @Override
    public boolean equals(Object that) {
        return this == that || (that instanceof Team &&
                                getDefinition().equals(((Team) that).getDefinition()) &&
                                getMatch().equals(((Team) that).getMatch()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDefinition(), getMatch());
    }

    protected TeamMatchModule module() {
        if(tmm == null) {
            tmm = getMatch().needMatchModule(TeamMatchModule.class);
        }
        return tmm;
    }

    @Override
    public String getId() {
        return slug();
    }

    @Override
    public String slug() {
        return match.featureDefinitions().slug(getDefinition());
    }

    /** Gets map specified information about this team.
     * @return Map-specific information about the team.
     */
    public TeamFactory getInfo() {
        return this.info;
    }

    public @Nullable String getLeagueTeamId() {
        return leagueTeamId;
    }

    public void setLeagueTeamId(@Nullable String leagueTeamId) {
        this.leagueTeamId = leagueTeamId;
    }

    @Override
    public MatchDoc.Team getDocument() {
        return document;
    }

    @Override
    public TeamFactory getDefinition() {
        return this.info;
    }

    @Override
    public Type getType() {
        return Type.Participating;
    }

    @Override
    public boolean isParticipatingType() {
        return true;
    }

    @Override
    public boolean isParticipating() {
        return match.isRunning();
    }

    @Override
    public boolean isObservingType() {
        return false;
    }

    @Override
    public boolean isObserving() {
        return !match.isRunning();
    }

    @Override
    public String getDefaultName() {
        return info.getDefaultName();
    }

    /** Gets the name of this team that can be modified using setTeam.  If no
     * custom name is set then this will return the default team name as
     * specified in the team info.
     * @return Name of the team without colors.
     */
    @Override
    public String getName() {
        return name != null ? name : getDefaultName();
    }

    public String getShortName() {
        String lower = getName().toLowerCase();
        if(lower.endsWith(" team")) {
            return getName().substring(0, lower.length() - " team".length());
        } else if(lower.startsWith("team ")) {
            return getName().substring("team ".length());
        } else {
            return getName();
        }
    }

    @Override
    public String getName(@Nullable CommandSender viewer) {
        return getName();
    }

    @Override
    public boolean isNamePlural() {
        // Assume custom names are singular
        return this.name == null && this.info.isDefaultNamePlural();
    }

    /** Gets the combination of the team color with the team name.
     * @return Colored version of the team name.
     */
    @Override
    public String getColoredName() {
        return getColor() + getName();
    }

    @Override
    public String getColoredName(@Nullable CommandSender viewer) {
        return getColor() + getName(viewer);
    }

    /** Sets a custom name for this team that should be unique in the match.
     * Note that setting the name to null will reset it to the default name as
     * specified in the team info.
     * @param newName New name for this team.  Should not include colors.
     */
    public void setName(@Nullable String newName) {
        if(Objects.equals(this.name, newName) || this.getName().equals(newName)) return;
        String oldName = this.getName();
        this.name = newName;
        this.componentName = null;
        this.match.callEvent(new PartyRenameEvent(this, oldName, this.getName()));
    }

    @Override
    public ChatColor getColor() {
        return this.info.getDefaultColor();
    }

    @Override
    public BaseComponent getComponentName() {
        if(componentName == null) {
            this.componentName = new Component(getName(), ChatUtils.convert(getColor()));
        }
        return componentName;
    }

    @Override
    public BaseComponent getStyledName(NameStyle style) {
        return getComponentName();
    }

    @Override
    public BaseComponent getChatPrefix() {
        if(chatPrefix == null) {
            this.chatPrefix = new Component("(Team) ", ChatColor.GRAY);
        }
        return chatPrefix;
    }

    @Override
    public org.bukkit.scoreboard.Team.OptionStatus getNameTagVisibility() {
        return info.getNameTagVisibility();
    }

    public int getMinPlayers() {
        return minPlayers.orElse(info.getMinPlayers().orElse(teamConfiguration.minimumPlayers()));
    }

    public int getMaxPlayers() {
        return maxPlayers.orElse(info.getMaxPlayers());
    }

    public int getMaxOverfill() {
        return maxOverfill.orElse(info.getMaxOverfill().orElse(joinConfiguration.overfillFromMax(getMaxPlayers())));
    }

    public void setMinSize(@Nullable Integer minPlayers) {
        this.minPlayers = Optional.ofNullable(minPlayers);

        if(getMaxPlayers() < getMinPlayers()) {
            this.maxPlayers = Optional.of(getMinPlayers());
        }
        if(getMaxOverfill() < getMaxPlayers()) {
            this.maxOverfill = Optional.of(getMaxPlayers());
        }

        getMatch().callEvent(new TeamResizeEvent(this));
        module().updatePlayerLimits();
    }

    public void resetMinSize() {
        setMinSize(null);
    }

    public void setMaxSize(@Nullable Integer maxPlayers, @Nullable Integer maxOverfill) {
        this.maxPlayers = Optional.ofNullable(maxPlayers);
        this.maxOverfill = Optional.ofNullable(maxOverfill);

        if(getMinPlayers() > getMaxPlayers()) {
            this.minPlayers = Optional.of(getMaxPlayers());
        }
        if(getMaxOverfill() < getMaxPlayers()) {
            this.maxOverfill = Optional.of(getMaxPlayers());
        }

        getMatch().callEvent(new TeamResizeEvent(this));
        module().updatePlayerLimits();
    }

    public void resetMaxSize() {
        setMaxSize(null, null);
    }

    public PunchClock<PlayerId> getParticipationClock() {
        return participationClock;
    }

    public Duration getCumulativeParticipation(PlayerId playerId) {
        return getParticipationClock().getCumulativePresence(playerId);
    }

    @Override
    public Set<PlayerId> getPastPlayers() {
        return pastPlayers;
    }

    @Override
    public boolean addPlayerInternal(MatchPlayer player) {
        if(super.addPlayerInternal(player)) {
            participationClock.punchIn(player.getPlayerId());
            if(getMatch().isCommitted()) {
                pastPlayers.add(player.getPlayerId());
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean removePlayerInternal(MatchPlayer player) {
        if(super.removePlayerInternal(player)) {
            participationClock.punchOut(player.getPlayerId());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void commit() {
        for(MatchPlayer player : getPlayers()) {
            pastPlayers.add(player.getPlayerId());
        }
    }

    @Override
    public boolean isAutomatic() {
        return false;
    }

    protected @Nullable MatchPlayer joiningPlayer() {
        final Change change = CHANGE.get();
        return change != null && equals(change.newTeam) ? change.player : null;
    }

    @Override
    public Set<MatchPlayer> getPlayers() {
        final Change change = CHANGE.get();
        if(change != null) {
            if(equals(change.oldTeam)) {
                return Sets.difference(super.getPlayers(), Collections.singleton(change.player));
            } else if(equals(change.newTeam)) {
                return Sets.union(super.getPlayers(), Collections.singleton(change.player));
            }
        }
        return super.getPlayers();
    }

    /**
     * Return the number of players on this team.
     * If priority is true, exclude players who can be bumped off the team.
     */
    public int getSize() {
        final int realSize = super.getPlayers().size();
        final Change change = CHANGE.get();
        if(change != null) {
            if(equals(change.oldTeam)) {
                return realSize - 1;
            } else if(equals(change.newTeam)) {
                return realSize + 1;
            }
        }
        return realSize;
    }

    /**
     * Get the "fullness" of this team, relative to some capacity returned by
     * the given function. The return value is always in the range 0 to 1.
     */
    public Fraction getFullness(ToIntFunction<? super Team> maxFunction) {
        final int max = maxFunction.applyAsInt(this);
        return max == 0 ? Fraction.ONE
                        : Fraction.getReducedFraction(getSize(), max);
    }

    /**
     * Get the maximum number of players currently allowed on this team without
     * exceeding any limits.
     */
    public int getMaxBalancedSize() {
        // Find the minimum fullness among other teams
        final Fraction minFullness = (Fraction) module().getTeams()
                                                        .stream()
                                                        .filter(team -> !equals(team))
                                                        .map(team -> team.getFullness(Team::getMaxOverfill))
                                                        .min(Comparator.naturalOrder())
                                                        .orElse(Fraction.ONE);

        // Calculate the dynamic limit to maintain balance with other teams (this can be zero)
        int slots = Numbers.ceil(Comparables.min(Fraction.ONE, minFullness.multiplyBy(MAX_IMBALANCE))
                                            .multiplyBy(Fraction.getFraction(getMaxOverfill(), 1)));

        // Clamp to the static limit defined for this team (cannot be zero unless the static limit is zero)
        return Math.min(getMaxOverfill(), Math.max(1, slots));
    }

    public boolean isStacked() {
        return this.getSize() > this.getMaxBalancedSize();
    }

    public int getTotalSlots(MatchPlayer joining) {
        return getMatch().needMatchModule(JoinMatchModule.class).canJoinFull(joining) ? getMaxOverfill() : getMaxPlayers();
    }

    /**
     * Return the number of available slots for the given player. If priority is true,
     * and the joining player has priority kick privileges, assume that non-privileged
     * players can be kicked off the team to make room.
     */
    public int getOpenSlots(MatchPlayer joining, boolean priorityKick) {
        // Can always join obs
        if(this.getType() == Type.Observing) return 1;

        // Count existing team members with and without join privileges
        int normal = 0, privileged = 0;

        final JoinMatchModule jmm = getMatch().needMatchModule(JoinMatchModule.class);

        for(MatchPlayer player : this.getPlayers()) {
            if(jmm.canPriorityKick(player)) privileged++;
            else normal++;
        }

        // Get the total slots available to the joining player
        // Deduct slots in use by privileged players, who cannot be kicked
        int slots = getTotalSlots(joining) - privileged;

        // If normal players cannot be bumped, deduct them as well
        if(!priorityKick || !jmm.canPriorityKick(joining)) {
            slots -= normal;
        }

        return Math.max(0, slots);
    }

    /**
     * @return if there is a free slot available for the given player to join this team.
     *         If the player is already on this team, the test behaves as if they are not.
     */
    public boolean hasOpenSlots(MatchPlayer joining, boolean priorityKick) {
        return this.getOpenSlots(joining, priorityKick) > 0;
    }

    /**
     * Perform a join query specific to this team, similar to {@link JoinHandler#queryJoin}.
     *
     * @param joining         Player who is joining
     * @param priorityKick    If false, priority kicking is not considered at all.
     *                        If true, priority kicking is considered if the player has that privilege.
     * @param rejoin          If true, assume the player was previously on this team and is rejoining.
     *
     * @return The result of the hypothetical join
     */
    public JoinResult queryJoin(MatchPlayer joining, boolean priorityKick, boolean rejoin) {
        if(hasOpenSlots(joining, false)) {
            return new JoinTeam(this, rejoin, false);
        }

        if(priorityKick && hasOpenSlots(joining, true)) {
            return new JoinTeam(this, rejoin, true);
        }

        return JoinDenied.unavailable("command.gameplay.join.completelyFull", getComponentName());
    }

    // TODO: send an update to the API when any of these values change
    class Document implements MatchDoc.Team {
        @Override
        public String _id() {
            return slug();
        }

        @Override
        public String name() {
            return getName();
        }

        @Override
        public @Nullable Integer min_players() {
            return getMinPlayers();
        }

        @Override
        public @Nullable Integer max_players() {
            return getMaxPlayers();
        }

        @Override
        public @Nullable net.md_5.bungee.api.ChatColor color() {
            return ChatUtils.convert(getColor());
        }

        @Override
        public @Nullable Integer size() {
            return getParticipationClock().getAllWithPresence().size();
        }

        @Override
        public String league_team_id() {
            return leagueTeamId;
        }
    }

    /**
     * Run the given block with a temporary team change in effect for all team queries and calculations.
     * All {@link Team}s will behave as if the given player has left their current team, if any, and
     * joined the given team, if its non-null.
     *
     * If the given player is null, or the specified change is not actually a change, the block is
     * run without altering any behavior. Whatever the block returns is returned by this method.
     *
     * This mechanism affects {@link #getPlayers()}, {@link #getSize()}, and any other method derived
     * from those, which includes all the methods involved in joining and balancing teams. This is
     * useful for performing calculations about some hypothetical state, without actually changing
     * the current state.
     *
     * Internally, this works by storing the change in a static {@link ThreadLocal}, which is checked
     * by various methods. If a change is present, they adjust their behavior to reflect the change.
     * The effects of this method are only visible while it is executing, and only to the current thread.
     *
     * Only one change can be simulated at a time on any particular thread. Calling this method again
     * from within the block will throw a {@link IllegalStateException}.
     */
    public static <R> R withChange(@Nullable MatchPlayer player, @Nullable Team newTeam, Supplier<R> block) {
        if(player == null || Objects.equals(player.partyMaybe().orElse(null), newTeam)) {
            return block.get();
        } else {
            if(CHANGE.get() != null) {
                throw new IllegalStateException("Nested call to Team.withChange");
            }
            try {
                CHANGE.set(new Change(player, Teams.get(player), newTeam));
                return block.get();
            } finally {
                CHANGE.remove();
            }
        }
    }

    private static final ThreadLocal<Change> CHANGE = new ThreadLocal<>();

    private static class Change {
        final MatchPlayer player;
        final @Nullable Team oldTeam;
        final @Nullable Team newTeam;

        private Change(MatchPlayer player, Team oldTeam, Team newTeam) {
            this.player = player;
            this.oldTeam = oldTeam;
            this.newTeam = newTeam;
        }
    }
}
