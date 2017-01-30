package net.anxuiz.tourney.vote;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.sk89q.minecraft.util.commands.ChatColor;
import net.anxuiz.tourney.Config;
import net.anxuiz.tourney.MapClassification;
import net.anxuiz.tourney.TeamManager;
import net.anxuiz.tourney.Tourney;
import net.anxuiz.tourney.TourneyState;
import net.anxuiz.tourney.event.mapselect.MapSelectionBeginEvent;
import net.anxuiz.tourney.event.mapselect.MapSelectionClassificationSelectEvent;
import net.anxuiz.tourney.event.mapselect.MapSelectionClassificationVetoEvent;
import net.anxuiz.tourney.event.mapselect.MapSelectionMapSelectEvent;
import net.anxuiz.tourney.event.mapselect.MapSelectionMapVetoEvent;
import net.anxuiz.tourney.event.mapselect.MapSelectionTurnCycleEvent;
import net.anxuiz.tourney.util.EntrantUtils;
import org.apache.commons.collections.CollectionUtils;
import org.bukkit.Server;
import org.bukkit.event.EventBus;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.api.docs.Entrant;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.formatting.StringUtils;
import tc.oc.pgm.cycle.CycleMatchModule;
import tc.oc.pgm.events.CycleEvent;
import tc.oc.pgm.map.PGMMap;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.inject.MatchScoped;

@MatchScoped
public class VoteContext {

    private final Tourney tourney;
    private final EntrantUtils entrantUtils;
    private final TeamManager teamManager;
    private final Match match;
    private final EventBus eventBus;
    private final Server bukkit;

    private VetoVote vote;
    private VoteListener listener;

    @Inject VoteContext(Tourney tourney, EntrantUtils entrantUtils, TeamManager teamManager, Match match, EventBus eventBus, Server bukkit) {
        this.tourney = tourney;
        this.entrantUtils = entrantUtils;
        this.teamManager = teamManager;
        this.match = match;
        this.eventBus = eventBus;
        this.bukkit = bukkit;
        this.vote = null;
    }

    public Optional<VetoVote> currentVote() {
        return Optional.ofNullable(vote);
    }

    public boolean voteInProgress() {
        return vote != null && (vote.getSelectedClassification() == null || vote.getSelectedMap() == null);
    }

    public void clearCurrentVote() {
        if(listener != null) {
            match.unregisterEvents(listener);
            listener = null;
            vote = null;
        }
    }

    public void startVote(Collection<MapClassification> classifications) {
        vote = new VetoVoteImpl(classifications);
        listener = new VoteListener();
        match.registerEvents(listener);
        eventBus.callEvent(new MapSelectionBeginEvent(vote));
        vote.cycleTurn();
    }

    private class VoteListener implements Listener {

        @EventHandler
        public void onMapSelectionBegin(MapSelectionBeginEvent event) {
            bukkit.broadcastMessage(ChatColor.YELLOW + "Beginning map selection vote...");
            tourney.setState(TourneyState.ENABLED_MAP_SELECTION);
        }

        @EventHandler
        public void onClassificationVeto(MapSelectionClassificationVetoEvent event) {
            bukkit.broadcastMessage(
                    event.getTeam().getColoredName() +
                            ChatColor.RESET +
                            ChatColor.YELLOW +
                            " vetoed a classification");
        }

        @EventHandler
        public void onMapVeto(MapSelectionMapVetoEvent event) {
            bukkit.broadcastMessage(
                    event.getTeam().getColoredName() +
                            ChatColor.RESET +
                            ChatColor.YELLOW +
                            " vetoed a map");
        }

        @EventHandler
        public void onMapSelect(MapSelectionMapSelectEvent event) {
            bukkit.broadcastMessage(ChatColor.YELLOW + "The winning map has been selected: " + ChatColor.AQUA + event.getMap().getName());
            match.needMatchModule(CycleMatchModule.class)
                 .startCountdown(Duration.ofSeconds(15), event.getMap());
        }

        @EventHandler
        public void onClassificationSelect(MapSelectionClassificationSelectEvent event) {
            bukkit.broadcastMessage(ChatColor.YELLOW + "The winning classification has been selected: " + ChatColor.AQUA + event.getClassification());
        }

        @EventHandler
        public void onTurnCycle(MapSelectionTurnCycleEvent event) {
            VetoVote vote = event.getVote();
            boolean classificationSelected = vote.getSelectedClassification() != null;

            // broadcast to match
            bukkit.broadcastMessage(ChatColor.YELLOW + "Waiting for both teams to veto a " +
                    (classificationSelected ? "map" : "classification") +
                    "..."
            );

            // broadcast to team
            if (classificationSelected) {
                for (Entrant participation : vote.getParticipatingTeams()) {
                    final Audience team = teamManager.entrantToTeam(participation).audience();
                    team.sendMessage(StringUtils.dashedChatMessage(ChatColor.GRAY + " Veto Information", "-", ChatColor.RED + "" + ChatColor.STRIKETHROUGH));
                    team.sendMessage(ChatColor.GRAY + "Your team may now veto another map.");
                    team.sendMessage(ChatColor.GRAY + "Note that any team member may veto. Please consult with your teammates and choose wisely.");
                    team.sendMessage(ChatColor.GRAY + "Execute " + ChatColor.RESET + ChatColor.YELLOW + ChatColor.ITALIC + "/tourney map options [page]" + ChatColor.RESET + ChatColor.GRAY + " to display remaining maps.");
                    team.sendMessage(ChatColor.GRAY + "Execute " + ChatColor.RESET + ChatColor.YELLOW + ChatColor.ITALIC + "/tourney map veto <map...>" + ChatColor.RESET + ChatColor.GRAY + " to cast your team's veto.");
                    team.sendMessage(StringUtils.dashedChatMessage(ChatColor.RED + "" + ChatColor.STRIKETHROUGH + "----------------", "-", ChatColor.RED + "" + ChatColor.STRIKETHROUGH));
                }
            } else {
                for (Entrant participation : vote.getParticipatingTeams()) {
                    final Audience team = teamManager.entrantToTeam(participation).audience();
                    team.sendMessage(StringUtils.dashedChatMessage(ChatColor.GRAY + " Veto Information", "-", ChatColor.RED + "" + ChatColor.STRIKETHROUGH));
                    team.sendMessage(ChatColor.GRAY + "Your team may now veto another classification.");
                    team.sendMessage(ChatColor.GRAY + "Note that any team member may veto. Please consult with your teammates and choose wisely.");
                    team.sendMessage(ChatColor.GRAY + "Execute " + ChatColor.RESET + ChatColor.YELLOW + ChatColor.ITALIC + "/tourney map options [page]" + ChatColor.RESET + ChatColor.GRAY + " to display remaining classifications.");
                    team.sendMessage(ChatColor.GRAY + "Execute " + ChatColor.RESET + ChatColor.YELLOW + ChatColor.ITALIC + "/tourney map veto <classification...>" + ChatColor.RESET + ChatColor.GRAY + " to cast your team's veto.");
                    team.sendMessage(StringUtils.dashedChatMessage(ChatColor.RED + "" + ChatColor.STRIKETHROUGH + "----------------", "-", ChatColor.RED + "" + ChatColor.STRIKETHROUGH));
                }
            }
        }

        @EventHandler(priority = EventPriority.HIGH)
        public void onMatchCycle(CycleEvent event) {
            tourney.setState(TourneyState.ENABLED_WAITING_FOR_TEAMS);
            teamManager.assignTeams(vote.getParticipatingTeams());

            clearCurrentVote();
        }
    }

    /** Represents a veto vote. */
    private class VetoVoteImpl implements VetoVote {
        
        private final Collection<Entrant> participatingTeams;
        private final HashSet<MapClassification> remainingClassifications = new HashSet<>();
        private final HashSet<PGMMap> remainingMaps = new HashSet<>();
        
        private Collection<Entrant> currentTurnRemainingTeams;
        private Collection<MapClassification> currentTurnVetoedClassifications;
        private Collection<PGMMap> currentTurnVetoedMaps;

        public VetoVoteImpl(Collection<MapClassification> classifications) {
            this.participatingTeams = teamManager.getEntrants();

            HashSet<MapClassification> excludedClassifications = new HashSet<>();
            HashSet<PGMMap> excludedMaps = new HashSet<>();
            for (Entrant entrant : participatingTeams) {
                HashMap<MapClassification, Integer> classificationCounts = new HashMap<>();
                for (MapClassification classification : classifications) {
                    classificationCounts.put(classification, entrantUtils.getClassificationPlayCount(entrant, classification));
                }

                // remove classifications that have been played X times more than any other classification
                int maxClassificationPlays = Config.maxClassificationPlays();
                for (Map.Entry<MapClassification, Integer> entry : classificationCounts.entrySet()) {
                    int value = entry.getValue();
                    if (value > 0) {
                        for (int count : classificationCounts.values()) {
                            if ((count > 0 && count * maxClassificationPlays <= value) || (count == 0 && value >= maxClassificationPlays)) {
                                MapClassification classification = entry.getKey();
                                excludedClassifications.add(classification);
                                excludedMaps.addAll(classification.maps());
                            }
                        }
                    }
                }

                HashMap<PGMMap, Integer> mapCounts = new HashMap<>();
                for (MapClassification classification : classifications) {
                    for (PGMMap map : classification.maps()) {
                        mapCounts.put(map, entrantUtils.getMapPlayCount(entrant, map));
                    }
                }

                // exclude maps that have been played X times more than any other map
                int maxMapPlays = Config.maxMapPlays();
                for (Map.Entry<PGMMap, Integer> entry : mapCounts.entrySet()) {
                    int value = entry.getValue();
                    if (value > 0) {
                        for (int count : mapCounts.values()) {
                            if ((count > 0 && count * maxMapPlays <= value) || (count == 0 && value >= maxMapPlays)) {
                                excludedMaps.add(entry.getKey());
                            }
                        }
                    }
                }
            }

            // remove excluded classifications, and reset filtering if no classifications remain
            this.remainingClassifications.addAll(CollectionUtils.subtract(classifications, excludedClassifications));
            if (this.remainingClassifications.isEmpty()) {
                this.remainingClassifications.addAll(classifications);
            }

            // remove excluded maps, and reset filtering if no maps remain
            for (MapClassification classification : classifications) {
                this.remainingMaps.addAll(CollectionUtils.subtract(classification.maps(), excludedMaps));
            }
            if (this.remainingMaps.isEmpty()) {
                for (MapClassification classification : classifications) {
                    this.remainingMaps.addAll(classification.maps());
                }
            }

            // remove maps that have no classifications
            mapLoop:
            for (PGMMap map : new HashSet<>(this.remainingMaps)) {
                for (MapClassification classification : this.remainingClassifications) {
                    /* We can safely ignore maps in the classification that aren't included, because they will already have
                     ~ been excluded by this point. */
                    if (classification.maps().contains(map)) break mapLoop;
                }

                this.remainingMaps.remove(map);
            }

            // remove classifications that have no maps
            for (MapClassification classification : new HashSet<>(this.remainingClassifications)) {
                if (Collections.disjoint(classification.maps(), this.remainingMaps)) {
                    this.remainingClassifications.remove(classification);
                }
            }
        }

        @Override
        public Collection<Entrant> getCurrentTurnRemainingTeams() {
            return this.currentTurnRemainingTeams;
        }

        @Override
        public ImmutableSet<MapClassification> getRemainingClassifications() {
            return ImmutableSet.copyOf(this.remainingClassifications);
        }

        @Override
        public ImmutableSet<PGMMap> getRemainingMaps() {
            return ImmutableSet.copyOf(this.remainingMaps);
        }

        @Override
        public Collection<Entrant> getParticipatingTeams() {
            return participatingTeams;
        }

        @Override
        public @Nullable MapClassification getSelectedClassification() {
            int remaining = this.remainingClassifications.size();
            Preconditions.checkState(remaining > 0, "Remaining classification count was not greater than 0");

            if (remaining < 3) {
                // return least played
                if (remaining == 2) {
                    MapClassification winningClassification = null;
                    int winningCount = Integer.MAX_VALUE;
                    for (MapClassification classification : this.remainingClassifications) {
                        int count = 0;
                        for (Entrant entrant : this.participatingTeams) {
                            count += entrantUtils.getClassificationPlayCount(entrant, classification);
                        }
                        if (count <= winningCount) {
                            winningClassification = classification;
                            winningCount = count;
                        }
                    }

                    return winningClassification;
                } else /* remaining == 1 */ {
                    return this.remainingClassifications.iterator().next();
                }
            }

            return null;
        }

        @Override
        public @Nullable PGMMap getSelectedMap() {
            int remaining = this.remainingMaps.size();
            Preconditions.checkState(remaining > 0, "Remaining map count was not greater than 0");

            if (remaining < 3) {
                // return least played
                if (remaining == 2) {
                    PGMMap winningMap = null;
                    int winningCount = 0;
                    for (PGMMap map : this.remainingMaps) {
                        int count = 0;
                        for (Entrant entrant : this.participatingTeams) {
                            count += entrantUtils.getMapPlayCount(entrant, map);
                        }
                        if (count <= winningCount) {
                            winningMap = map;
                            winningCount = count;
                        }
                    }

                    return winningMap;
                } else /* remaining == 1 */ {
                    return this.remainingMaps.iterator().next();
                }
            }

            return null;
        }

        @Override
        public void registerVeto(Entrant entrant, PGMMap map) {
            Preconditions.checkNotNull(entrant, "Entrant");
            Preconditions.checkArgument(this.remainingMaps.contains(Preconditions.checkNotNull(map, "Map")), "Map specified to veto was not remaining");
            this.currentTurnVetoedMaps.add(map);
            this.currentTurnRemainingTeams.remove(entrant);
            eventBus.callEvent(new MapSelectionMapVetoEvent(this, teamManager.entrantToTeam(entrant), entrant));

            if (this.currentTurnRemainingTeams.isEmpty()) {
                this.remainingMaps.removeAll(this.currentTurnVetoedMaps);

                PGMMap winnerMap = this.getSelectedMap();
                if (winnerMap != null) {
                    this.remainingMaps.clear();
                    this.remainingMaps.add(winnerMap);
                    eventBus.callEvent(new MapSelectionMapSelectEvent(this, winnerMap));
                } else {
                    this.cycleTurn();
                }
            }
        }

        @Override
        public void registerVeto(Entrant entrant, MapClassification classification) {
            Preconditions.checkNotNull(entrant, "Entrant");
            Preconditions.checkArgument(this.remainingClassifications.contains(Preconditions.checkNotNull(classification, "Classification")), "Classification specified to veto was not remaining");
            this.currentTurnVetoedClassifications.add(classification);
            this.currentTurnRemainingTeams.remove(entrant);
            eventBus.callEvent(new MapSelectionClassificationVetoEvent(this, teamManager.entrantToTeam(entrant), entrant));

            if (this.currentTurnRemainingTeams.isEmpty()) {
                this.remainingClassifications.removeAll(this.currentTurnVetoedClassifications);
                for (MapClassification cl : this.currentTurnVetoedClassifications) {
                    this.remainingMaps.removeAll(cl.maps());
                }
                MapClassification winnerClassification = this.getSelectedClassification();
                if (winnerClassification != null) {
                    this.remainingClassifications.clear();
                    this.remainingClassifications.add(winnerClassification);

                    for (PGMMap map : new HashSet<>(this.remainingMaps)) {
                        if (!winnerClassification.maps().contains(map)) {
                            this.remainingMaps.remove(map);
                        }
                    }
                    eventBus.callEvent(new MapSelectionClassificationSelectEvent(this, winnerClassification));
                }

                PGMMap winnerMap = this.getSelectedMap();
                if (winnerMap != null) {
                    this.remainingMaps.clear();
                    this.remainingMaps.add(winnerMap);
                    eventBus.callEvent(new MapSelectionMapSelectEvent(this, winnerMap));
                } else {
                    this.cycleTurn();
                }
            }
        }

        @Override
        public void cycleTurn() {
            eventBus.callEvent(new MapSelectionTurnCycleEvent(this));

            this.currentTurnVetoedClassifications = null;
            this.currentTurnVetoedMaps = null;

            this.currentTurnRemainingTeams = new HashSet<>(this.participatingTeams);
            if (this.getSelectedClassification() != null) {
                this.currentTurnVetoedMaps = new HashSet<>();
            } else {
                this.currentTurnVetoedClassifications = new HashSet<>();
            }
        }
    }
}
