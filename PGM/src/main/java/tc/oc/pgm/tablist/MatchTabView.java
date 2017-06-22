package tc.oc.pgm.tablist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import tc.oc.commons.bukkit.nick.PlayerIdentityChangeEvent;
import tc.oc.commons.bukkit.tablist.TabEntry;
import tc.oc.commons.bukkit.tablist.TabManager;
import tc.oc.commons.bukkit.tablist.TabView;
import tc.oc.commons.bukkit.util.PermissionUtils;
import tc.oc.commons.core.util.DefaultProvider;
import tc.oc.commons.core.util.Numbers;
import tc.oc.pgm.Config;
import tc.oc.pgm.events.PlayerJoinMatchEvent;
import tc.oc.pgm.events.PlayerPartyChangeEvent;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.Party;
import tc.oc.pgm.teams.Team;
import tc.oc.pgm.teams.TeamMatchModule;

public class MatchTabView extends TabView implements Listener {

    public static class Factory implements DefaultProvider<Player, MatchTabView> {
        @Override
        public MatchTabView get(Player key) {
            return new MatchTabView(key);
        }
    }

    private final MatchPlayerOrder playerOrder;

    private final ListMultimap<Party.Type, MatchPlayer> players = ArrayListMultimap.create();
    private final ListMultimap<Team, MatchPlayer> teamPlayers = ArrayListMultimap.create();

    private Match match;
    private @Nullable TeamMatchModule tmm;
    private MatchPlayer matchPlayer;
    private TeamOrder teamOrder;

    public MatchTabView(Player viewer) {
        super(viewer);
        this.playerOrder = new MatchPlayerOrder(viewer);
    }

    @Override
    public void enable(TabManager manager) {
        super.enable(manager);
        manager.getPlugin().eventRegistry().registerListener(this);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        super.disable();
    }

    protected MatchTabManager getManager() {
        return (MatchTabManager) this.manager;
    }

    @Override
    public void render() {
        if(this.manager == null) return;

        if(this.match != null && this.isLayoutDirty()) {
            this.setHeader(this.getManager().getMapEntry(this.match));
            this.setFooter(this.getManager().getFooterEntry(this.match));

            Set<MatchPlayer> observers = this.match.getObservingPlayers();

            // Number of players/staff on observers
            int observingPlayers = 0;
            int observingStaff = 0;
            if(Config.PlayerList.playersSeeObservers() || matchPlayer.isObservingType()) {
                observingPlayers = observers.size();
                for(MatchPlayer player : observers) {
                    if(PermissionUtils.isStaff(player.getBukkit())) observingStaff++;
                }
            }

            int availableRows = this.getHeight();

            // Minimum rows required to show all staff observers
            int observerRows = Math.min(availableRows, Numbers.divideRoundingUp(observingStaff, this.getWidth()));

            if(tmm != null) {
                // Render participating teams
                Iterator<Team> iter = tmm.teams().sorted(this.teamOrder).iterator();

                // Current row to print teams in, will increase by 1 (head) + team player size + 1 (space row after teams)
                int currentTeamRow = 0;
                do { // Always render the first team row, even if observers could fill up the whole list
                    // Store all the teams that fit in in a row
                    List<Team> row = new ArrayList<>(this.getWidth());
                    for (int i = 0; i < this.getWidth() && iter.hasNext(); i++) row.add(iter.next());

                    // Max rows allowed: max rows - (current row + reserved rows for obs + 1 (reserved for blank space)
                    currentTeamRow += renderTeams(currentTeamRow, availableRows - (currentTeamRow + observerRows + 1), row);
                    renderBlank(currentTeamRow++);
                    // Need 3 rows for team headers, show someone in the teams, and the extra space row,
                    // so stop if we don't have that space left, or we ran out of teams
                } while (iter.hasNext() && currentTeamRow + observerRows + 3 <= availableRows);

                // Expand observer rows until all observers are showing
                observerRows = Math.min(availableRows - currentTeamRow, Numbers.divideRoundingUp(observingPlayers, this.getWidth()));

                renderBlank(currentTeamRow, availableRows - observerRows);
            } else {
                List<MatchPlayer> participants = players.get(Party.Type.Participating);
                // Minimum rows required by participating players
                int participantRows = Math.min(availableRows - addRowIfAny(observerRows), 1 + Numbers.divideRoundingUp(participants.size(), this.getWidth()));

                // Expand observer rows until all observers are showing
                observerRows = Math.min(availableRows - participantRows, Numbers.divideRoundingUp(observingPlayers, this.getWidth()));

                // Expand participant rows to fill whatever if left
                participantRows = availableRows - addRowIfAny(observerRows);

                this.renderTeam(participants, getManager().getFreeForAllEntry(match), true, 0, this.getWidth(), 0, participantRows);

                // Render 1 line space before observers
                if (observerRows > 0) renderBlank(availableRows - observerRows + 1);
            }

            if(observerRows > 0) {
                // Render observers
                this.renderTeam(players.get(Party.Type.Observing), null, false, 0, this.getWidth(), this.getHeight() - observerRows, this.getHeight());
            }
        }

        super.render();
    }
    /* --- Render utils --- */
    // Add a blank row, but only if there is any obs to show
    private int addRowIfAny(int rows) {
        return rows <= 0 ? 0 : rows + 1;
    }

    // Render a blank row at y
    private void renderBlank(int y) {
        renderBlank(0, this.getWidth(), y, y + 1);
    }

    // Render blank rows from y1 to y2
    private void renderBlank(int y1, int y2) {
        renderBlank(0, this.getWidth(), y1, y2);
    }

    // Fill with blank spaces from x1,x2 to y1,y2
    private void renderBlank(int x1, int x2, int y1, int y2) {
        for(int y = y1; y < y2; y++) {
            for(int x = x1; x < x2; x++) {
                this.setSlot(x, y, null);
            }
        }
    }

    // Will render a row of teams, max is tab list width, will return the number of rows used
    private int renderTeams(int y, int maxHeight, List<Team> teams) {
        if (maxHeight <= 1) return 0;

        int columnsPerTeam = Math.max(1, this.getWidth() / Math.max(1, teams.size()));
        int maxRows = Numbers.divideRoundingUp(teams.stream().mapToInt(Team::getSize).max().orElse(0), columnsPerTeam);
        maxRows = Math.min(maxRows + 1, maxHeight);

        int x1 = 0;
        for (Team team : teams) {
            int x2 = Math.min(x1 + columnsPerTeam, this.getWidth());
            this.renderTeam(teamPlayers.get(team), getManager().getTeamEntry(team), true, x1, x2, y, y + maxRows);
            x1 = x2;
        }
        // Render remaining spaces to the side of the teams, for example if the row has only 3 teams
        renderBlank(x1, this.getWidth(), y, y + maxRows);
        return maxRows;
    }

    // Will render a team and fill all the remaining spaces
    private void renderTeam(List<MatchPlayer> players, @Nullable TabEntry header, boolean vertical, final int x1, final int x2, int y1, final int y2) {
        if(header != null) {
            // Render the header row
            for(int x = x1; x < x2; x++) {
                this.setSlot(x, y1, x == x1 ? header : null);
            }
            y1++;
        }

        // Re-sort team members and render them
        Collections.sort(players, this.playerOrder);
        Iterator<MatchPlayer> iter = players.iterator();

        if(vertical) {
            // Fill columns first
            for(int x = x1; x < x2; x++) {
                for(int y = y1; y < y2; y++) {
                    this.setSlot(x, y, iter.hasNext() ? this.getManager().getPlayerEntry(iter.next()) : null);
                }
            }
        } else {
            // Fill rows first
            for(int y = y1; y < y2; y++) {
                for(int x = x1; x < x2; x++) {
                    this.setSlot(x, y, iter.hasNext() ? this.getManager().getPlayerEntry(iter.next()) : null);
                }
            }
        }
    }

    /* --- End rendering utils --- */

    public void onViewerJoinMatch(PlayerJoinMatchEvent event) {
        if(this.getViewer() == event.getPlayer().getBukkit()) {
            this.match = event.getMatch();
            this.matchPlayer = event.getPlayer();

            this.teamOrder = new TeamOrder(this.matchPlayer);

            this.players.replaceValues(Party.Type.Observing, this.match.getObservingPlayers());
            this.players.replaceValues(Party.Type.Participating, this.match.getParticipatingPlayers());

            this.tmm = this.match.getMatchModule(TeamMatchModule.class);
            if(this.tmm != null) {
                for(Team team : this.tmm.getTeams()) {
                    this.teamPlayers.replaceValues(team, team.getPlayers());
                }
            }

            this.invalidateLayout();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeamChange(PlayerPartyChangeEvent event) {
        if(this.match != event.getMatch()) return;

        if(event.getOldParty() != null) {
            this.players.get(event.getOldParty().getType()).removeAll(Collections.singleton(event.getPlayer()));
        }
        if(event.getNewParty() != null && !this.players.containsEntry(event.getNewParty().getType(), event.getPlayer())) {
            this.players.put(event.getNewParty().getType(), event.getPlayer());
        }

        if(event.getOldParty() instanceof Team) {
            this.teamPlayers.get((Team) event.getOldParty()).removeAll(Collections.singleton(event.getPlayer()));
        }

        if(event.getNewParty() instanceof Team && !this.teamPlayers.containsEntry(event.getNewParty(), event.getPlayer())) {
            this.teamPlayers.put((Team) event.getNewParty(), event.getPlayer());
        }

        this.invalidateLayout();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNickChange(PlayerIdentityChangeEvent event) {
        this.invalidateLayout();
    }
}
