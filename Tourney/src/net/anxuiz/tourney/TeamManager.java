package net.anxuiz.tourney;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.anxuiz.tourney.event.EntrantRegisterEvent;
import net.anxuiz.tourney.event.EntrantUnregisterEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventBus;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.docs.Entrant;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.virtual.MatchDoc;
import tc.oc.api.tourney.TeamUtils;
import tc.oc.commons.core.IterableUtils;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.util.TimeUtils;
import tc.oc.pgm.match.Party;
import tc.oc.pgm.match.inject.MatchScoped;
import tc.oc.pgm.teams.Team;

@MatchScoped
public class TeamManager {

    private final Logger logger;
    private final Tourney tourney;
    private final EventBus eventBus;
    private final BukkitUserStore userStore;
    private final Set<Team> teams;
    private final Random random = new Random();
    private final HashBiMap<Team, Entrant> teamMap = HashBiMap.create();

    @Inject TeamManager(Loggers loggers, Tourney tourney, EventBus eventBus, BukkitUserStore userStore, Set<Team> teams) {
        this.logger = loggers.get(getClass());
        this.tourney = tourney;
        this.eventBus = eventBus;
        this.userStore = userStore;
        this.teams = teams;
    }

    /**
     * Maps the specified {@link Team} to the specified {@link Entrant}.
     *
     * @param team          The {@link Team} to map.
     * @param entrant The {@link Entrant} to map the {@link Team} to.
     * @return Whether or not the mapping was successful.
     */
    public boolean mapTeam(Team team, Entrant entrant) {
        logger.info("Mapping entrant '" + entrant.team().name() +
                    "' to map team '" + team.getInfo().getDefaultName() + "'");

        if(tourney.getState() == TourneyState.DISABLED) {
            tourney.setState(TourneyState.ENABLED_WAITING_FOR_TEAMS);
        }

        eventBus.callEvent(new EntrantRegisterEvent(team, entrant), event -> {
            teamMap.put(team, entrant);
            team.setLeagueTeamId(entrant.team()._id());
            team.setName(entrant.team().name());
        });

        if(allTeamsMapped()) {
            tourney.setState(TourneyState.ENABLED_WAITING_FOR_READY);
        }

        return true;
    }

    /**
     * Attempts to retrieve the appropriate {@link Team} for the specified {@link Entrant}.
     *
     * @param entrant The entrant to retrieve a team for.
     * @return The appropriate {@link Team}, or <code>null</code> if none was found.
     */
    public @Nullable Team entrantToTeam(final Entrant entrant) {
        return this.teamMap.inverse().get(entrant);
    }

    /**
     * Retrieves the appropriate {@link Entrant} for the specified {@link Team}.
     *
     * @param team The team to retrieve an entrant for.
     * @return The appropriate {@link Entrant}, or <code>null</code> if none was found.
     */
    public @Nullable Entrant teamToEntrant(final Party team) {
        return team instanceof Team ? this.teamMap.get(team) : null;
    }

    /**
     * Un-maps the specified {@link Team}.
     *
     * @param team The team to un-map.
     * @return Whether or not the team was unregistered.
     */
    public boolean unmap(final Team team) {
        Entrant entrant = teamMap.get(Preconditions.checkNotNull(team, "Team"));
        if (entrant == null) return false;

        if(allTeamsMapped()) {
            tourney.setState(TourneyState.ENABLED_WAITING_FOR_TEAMS);
        }

        eventBus.callEvent(new EntrantUnregisterEvent(team, entrant), event -> {
            teamMap.remove(team);
            team.setName(null);
        });

        if(teamMap.isEmpty()) {
            tourney.setState(TourneyState.DISABLED);
        }

        return true;
    }

    /**
     * Un-maps the specified {@link Entrant}.
     *
     * @param entrant The entrant to un-map.
     * @return Whether or not the team was unregistered.
     */
    public boolean unmap(final Entrant entrant) {
        return this.unmap(this.entrantToTeam(entrant));
    }

    public BiMap<Team, Entrant> mappedTeams() {
        return Maps.unmodifiableBiMap(teamMap);
    }

    /**
     * Gets all mapped entrants.
     *
     * @return All mapped entrants.
     */
    public Set<Entrant> getEntrants() {
        return this.teamMap.values();
    }

    public @Nullable Entrant getEntrant(String teamName) {
        teamName = TeamUtils.normalizeName(teamName);
        for(Entrant entrant : this.teamMap.values()) {
            if(teamName.equals(entrant.team().name_normalized())) return entrant;
        }
        return null;
    }

    public @Nullable Entrant getEntrant(PlayerId playerId) {
        for(Entrant entrant : this.teamMap.values()) {
            if(entrant.members().contains(playerId)) return entrant;
        }
        return null;
    }

    /**
     * @return The team this player is supposed to be on
     */
    public @Nullable Team getTeam(Player player) {
        return getTeam(userStore.getUser(player));
    }

    public @Nullable Team getTeam(PlayerId playerId) {
        for(Map.Entry<Team, Entrant> entry : this.teamMap.entrySet()) {
            if(entry.getValue().members().contains(playerId)) return entry.getKey();
        }
        return null;
    }

    /**
     * Gets all mapped teams.
     *
     * @return All mapped teams.
     */
    public Set<Team> getMappedTeams() {
        return this.teamMap.keySet();
    }

    /**
     * Empties the map entirely. Useful for cycling between matches.
     *
     * @return The number of entries removed.
     */
    public int clearMap() {
        int size = this.teamMap.size();
        this.teamMap.clear();
        return size;
    }

    /**
     * Assigns entrants to teams. Used on map cycle.
     */
    public void assignTeams(Collection<Entrant> entrants) {
        for (Entrant entrant : entrants) {
            this.assignEntrant(entrant, null);
        }
    }

    /**
     * Assigns a team to the specified entrant.
     *
     * @return The assigned team, or <code>null</code> if the assignment failed for one reason or another.
     */
    public @Nullable Team assignEntrant(Entrant entrant, @Nullable Team matchTeam) {
        return this.assignEntrant(entrant, matchTeam, false);
    }

    /**
     * Assigns a team (or returns the team that would be assigned, depending on the <code>soft</code> parameter) to the
     * specified entrant.
     *
     * @param soft  Whether or not to carry out the assignment.
     * @return The assigned (or would-be assigned) team, or <code>null</code> if the assignment failed for one reason or
     * another.
     */
    public @Nullable Team assignEntrant(Entrant entrant, @Nullable Team matchTeam, boolean soft) {
        if(matchTeam == null) {
            // If matchTeam is not specified, choose one of the available unmapped teams
            final Map<ChatColor, Team> available = Maps.uniqueIndex(Sets.filter(teams,
                                                                                team -> !teamMap.keySet().contains(team)),
                                                                    Team::getColor);
            if(available.isEmpty()) return null;

            // Try to find the most recently used color by this team in this tournament
            final String teamId = entrant.team()._id();
            ChatColor bestColor = null;
            Instant bestColorTime = TimeUtils.INF_PAST;
            for(MatchDoc match : entrant.matches()) {
                if(match.start() != null && match.start().isAfter(bestColorTime)) {
                    teamLoop: for(MatchDoc.Team team : match.competitors()) {
                        if(team.color() != null &&
                           available.containsKey(team.color()) &&
                           teamId.equals(team.league_team_id())) {

                            bestColor = team.color();
                            bestColorTime = match.start();
                            break teamLoop;
                        }
                    }
                }
            }

            // Fallback to random choice
            if(bestColor == null) {
                bestColor = IterableUtils.randomElement(available.keySet(), random);
            }

            return assignEntrant(entrant, available.get(bestColor), soft);

        } else {
            // If given a specific matchTeam, try to assign to that team
            if(this.teamMap.keySet().contains(matchTeam)) {
                return null;
            } else if(!soft) {
                return this.mapTeam(matchTeam, entrant) ? matchTeam : null;
            } else {
                return matchTeam;
            }
        }
    }

    public boolean allTeamsMapped() {
        return teamMap.keySet().containsAll(teams);
    }
}
