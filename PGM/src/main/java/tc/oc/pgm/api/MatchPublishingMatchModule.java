package tc.oc.pgm.api;

import javax.inject.Inject;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.api.docs.virtual.MatchDoc;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.minecraft.MinecraftService;
import tc.oc.api.model.UpdateService;
import tc.oc.pgm.blitz.BlitzMatchModule;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.PartyRenameEvent;
import tc.oc.pgm.events.PlayerPartyChangeEvent;
import tc.oc.pgm.events.SetNextMapEvent;
import tc.oc.pgm.ffa.events.MatchResizeEvent;
import tc.oc.pgm.goals.events.GoalCompleteEvent;
import tc.oc.pgm.goals.events.GoalTouchEvent;
import tc.oc.pgm.blitz.BlitzEvent;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchManager;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.teams.events.TeamResizeEvent;

@ListenerScope(MatchScope.LOADED)
public class MatchPublishingMatchModule extends MatchModule implements Listener {
    class Update implements ServerDoc.MatchStatusUpdate {
        @Override
        public int num_observing() {
            return isBlitz() ? getMatch().getPlayers().size() - initialParticipants : getMatch().getObservingPlayers().size();
        }

        @Override
        public int num_participating() {
            return isBlitz() ? initialParticipants : getMatch().getParticipatingPlayers().size();
        }

        @Override
        public int min_players() {
            return match.getPlayerLimits().lowerEndpoint();
        }

        @Override
        public int max_players() {
            return match.getPlayerLimits().upperEndpoint();
        }

        @Override
        public MatchDoc current_match() {
            return matchDocument;
        }

        @Override
        public MapDoc next_map() {
            return mm.getNextMap().getDocument();
        }
    }

    private final MatchManager mm;
    private final MinecraftService minecraftService;
    private final UpdateService<MatchDoc> matchService;
    private final MatchDoc matchDocument;
    private final BlitzMatchModule blitz;

    private int initialParticipants; // Number of participants at match start (for blitz)

    @Inject MatchPublishingMatchModule(Match match, MatchManager mm, MinecraftService minecraftService, UpdateService<MatchDoc> matchService, MatchDoc matchDocument, BlitzMatchModule blitz) {
        super(match);
        this.mm = mm;
        this.minecraftService = minecraftService;
        this.matchService = matchService;
        this.matchDocument = matchDocument;
        this.blitz = blitz;
    }

    public boolean isBlitz() {
        return blitz.activated();
    }

    private void countPlayers() {
        this.initialParticipants = getMatch().getParticipatingPlayers().size();
    }

    private void update() {
        getMatch().getScheduler(MatchScope.LOADED).debounceTask(() -> minecraftService.updateLocalServer(new Update()));
    }

    @Override
    public void load() {
        super.load();
        update();
    }

    @Override
    public void enable() {
        super.enable();
        countPlayers();
        update();
    }

    @Override
    public void disable() {
        update();
        super.disable();
    }

    @Override
    public void unload() {
        // Next match will have already loaded and changed Server.current_match,
        // so we have to update the match directly to set the unload time.
        matchService.update(matchDocument);
        super.unload();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPartyChange(final PlayerPartyChangeEvent event) {
        if(!event.getMatch().hasStarted()) {
            countPlayers();
        }
        update();
    }

    @EventHandler(priority = EventPriority.MONITOR) public void onSetNext(SetNextMapEvent event) { update(); }
    @EventHandler(priority = EventPriority.MONITOR) public void onPartyRename(PartyRenameEvent event) { update(); }
    @EventHandler(priority = EventPriority.MONITOR) public void onMatchResize(MatchResizeEvent event) { update(); }
    @EventHandler(priority = EventPriority.MONITOR) public void onTeamResize(TeamResizeEvent event) { update(); }
    @EventHandler(priority = EventPriority.MONITOR) public void onGoalComplete(GoalCompleteEvent event) { update(); }
    @EventHandler(priority = EventPriority.MONITOR) public void onGoalTouch(GoalTouchEvent event) { update(); }
    @EventHandler(priority = EventPriority.MONITOR) public void onBlitzEnable(BlitzEvent event) { countPlayers(); update(); }
}
