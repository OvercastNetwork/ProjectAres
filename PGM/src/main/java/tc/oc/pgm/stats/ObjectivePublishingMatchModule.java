package tc.oc.pgm.stats;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.time.Instant;

import tc.oc.api.docs.Objective;
import tc.oc.api.docs.Server;
import tc.oc.api.model.IdFactory;
import tc.oc.api.model.UpdateService;
import tc.oc.pgm.core.Core;
import tc.oc.pgm.core.CoreLeakEvent;
import tc.oc.pgm.destroyable.Destroyable;
import tc.oc.pgm.destroyable.DestroyableDestroyedEvent;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.flag.Flag;
import tc.oc.pgm.flag.event.FlagCaptureEvent;
import tc.oc.pgm.goals.Goal;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.wool.MonumentWool;
import tc.oc.pgm.wool.PlayerWoolPlaceEvent;

import javax.annotation.Nullable;
import javax.inject.Inject;

@ListenerScope(MatchScope.LOADED)
public class ObjectivePublishingMatchModule extends MatchModule implements Listener {

    private final UpdateService<Objective> objectiveService;
    private final StatisticsConfiguration statisticsConfiguration;
    private final Server server;
    private final IdFactory idFactory;

    @Inject ObjectivePublishingMatchModule(Match match, UpdateService<Objective> objectiveService, StatisticsConfiguration statisticsConfiguration, Server server, IdFactory idFactory) {
        super(match);
        this.objectiveService = objectiveService;
        this.statisticsConfiguration = statisticsConfiguration;
        this.server = server;
        this.idFactory = idFactory;
    }

    public boolean isActive(Goal goal, @Nullable Competitor completer, @Nullable Competitor owner) {
        final boolean enabled = statisticsConfiguration.deaths(), farming = statisticsConfiguration.farming();
        if(!enabled || !goal.isVisible()) {
            return false;
        } else if(completer != null) {
            return farming || completer.getMatch()
                    .getCompetitors()
                    .stream()
                    .anyMatch(competitor -> !competitor.equals(completer) && !competitor.getPlayers().isEmpty());
        } else if(owner != null) {
            return farming || !owner.getPlayers().isEmpty();
        } else {
            return true;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onWoolPlace(PlayerWoolPlaceEvent event) {
        final MonumentWool wool = event.getWool();
        final Location location = event.getBlock().getLocation();
        if(!isActive(wool, event.getPlayer().getParty(), null)) return;
        objectiveService.update(new Objective.WoolPlace() {
            final String id = idFactory.newId();
            public String color() { return wool.getDyeColor().name(); }
            public String name() { return wool.getName(); }
            public String feature_id() { return wool.getDocument()._id(); }
            public Instant date() { return match.getInstantNow(); }
            public String match_id() { return match.getId(); }
            public String server_id() { return server._id(); }
            public String family() { return server.name(); }
            public Double x() { return location.getX(); }
            public Double y() { return location.getY(); }
            public Double z() { return location.getZ(); }
            public String team() { return event.getPlayer().getParty().getName(); }
            public String player() { return event.getPlayer().getPlayerId().player_id(); }
            public String _id() { return id; }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onFlagCapture(FlagCaptureEvent event) {
        final Flag flag = event.getGoal();
        final Competitor competitor = event.getCarrier().getCompetitor();
        final Location location = event.getGoal().getLocation().orElse(null);
        if(!isActive(flag, competitor, null)) return;
        objectiveService.update(new Objective.FlagCapture() {
            final String id = idFactory.newId();
            public String net_id() { return event.getNet().id().orElse(null); }
            public String color() { return flag.getDyeColor().name(); }
            public String name() { return flag.getName(); }
            public String feature_id() { return flag.getDocument()._id(); }
            public Instant date() { return match.getInstantNow(); }
            public String match_id() { return match.getId(); }
            public String server_id() { return server._id(); }
            public String family() { return server.family(); }
            public Double x() { return location != null ? location.getX() : null; }
            public Double y() { return location != null ? location.getY() : null; }
            public Double z() { return location != null ? location.getZ() : null; }
            public String team() { return competitor != null ? competitor.getName() : null; }
            public String player() { return event.getCarrier().getPlayerId().player_id(); }
            public String _id() { return id; }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onDestroyableDestroy(DestroyableDestroyedEvent event) {
        final Destroyable destroyable = event.getDestroyable();
        final Instant time = match.getInstantNow();
        if(!isActive(destroyable, null, destroyable.getOwner())) return;
        event.getDestroyable()
             .getContributions()
             .forEach(contribution -> objectiveService.update(new Objective.DestroyableDestroy() {
                 final String id = idFactory.newId();
                 final Location location = contribution.getPlayerState().getLocation();
                 public int blocks_broken() { return contribution.getBlocks(); }
                 public double blocks_broken_percentage() { return contribution.getPercentage(); }
                 public String name() { return destroyable.getName(); }
                 public String feature_id() { return destroyable.getDocument()._id(); }
                 public Instant date() { return time; }
                 public String match_id() { return match.getId(); }
                 public String server_id() { return server._id(); }
                 public String family() { return server.family(); }
                 public Double x() { return location.getX(); }
                 public Double y() { return location.getY(); }
                 public Double z() { return location.getZ(); }
                 public String team() { return contribution.getPlayerState().getParty().getName(); }
                 public String player() { return contribution.getPlayerState().getPlayerId().player_id(); }
                 public String _id() { return id; }
             }));
    }

    @EventHandler(ignoreCancelled = true)
    public void onCoreLeak(CoreLeakEvent event) {
        final Core core = event.getCore();
        final Instant time = match.getInstantNow();
        if(!isActive(core, null, core.getOwner())) return;
        core.getContributions()
            .forEach(contribution -> objectiveService.update(new Objective.CoreBreak() {
                final String id = idFactory.newId();
                final Location location = contribution.getPlayerState().getLocation();
                public String material() { return contribution.getMaterial().name(); }
                public String name() { return core.getName(); }
                public String feature_id() { return core.getDocument()._id(); }
                public Instant date() { return time; }
                public String match_id() { return match.getId(); }
                public String server_id() { return server._id(); }
                public String family() { return server.family(); }
                public Double x() { return location.getX(); }
                public Double y() { return location.getY(); }
                public Double z() { return location.getZ(); }
                public String team() { return contribution.getPlayerState().getParty().getName(); }
                public String player() { return contribution.getPlayerState().getPlayerId().player_id(); }
                public String _id() { return id; }
            }));
    }

}
