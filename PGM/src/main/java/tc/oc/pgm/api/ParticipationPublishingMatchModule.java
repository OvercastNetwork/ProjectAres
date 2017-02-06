package tc.oc.pgm.api;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import java.time.Instant;

import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.docs.Participation;
import tc.oc.api.docs.Server;
import tc.oc.api.model.BatchUpdater;
import tc.oc.api.model.BatchUpdaterFactory;
import tc.oc.api.model.IdFactory;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.PlayerPartyChangeEvent;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.Party;
import tc.oc.pgm.teams.Team;

@ListenerScope(MatchScope.LOADED)
public class ParticipationPublishingMatchModule extends MatchModule implements Listener {

    private final Server localServer;
    private final BukkitUserStore userStore;
    private final IdFactory idFactory;
    private final BatchUpdater<Participation.Partial> batchUpdater;

    private final Map<MatchPlayer, String> participationIds = new HashMap<>();

    @Inject ParticipationPublishingMatchModule(Match match, Server localServer, BukkitUserStore userStore, IdFactory idFactory, BatchUpdaterFactory<Participation.Partial> updaterFactory) {
        super(match);
        this.localServer = localServer;
        this.userStore = userStore;
        this.idFactory = idFactory;
        this.batchUpdater = updaterFactory.createBatchUpdater();
    }

    @Override
    public void unload() {
        for(MatchPlayer player : getMatch().getParticipatingPlayers()) {
            finish(player);
        }
        batchUpdater.flush();
        super.unload();
    }

    private void finish(MatchPlayer player) {
        final String oldId = participationIds.remove(player);
        if(oldId != null) {
            batchUpdater.update(new Participation.Finish() {
                @Override
                public String _id() {
                    return oldId;
                }

                @Override
                public Instant end() {
                    return Instant.now();
                }
            });
        }
    }

    private void start(final MatchPlayer player, @Nullable Party party) {
        if(party instanceof Competitor) {
            final String id = idFactory.newId();
            final Team team = party instanceof Team ? ((Team) party) : null;
            participationIds.put(player, id);

            batchUpdater.update(new Participation.Start() {
                @Override
                public String _id() {
                    return id;
                }

                @Override
                public String player_id() {
                    return player.getPlayerId().player_id();
                }

                @Override
                public Instant start() {
                    return Instant.now();
                }

                @Override
                public String team_id() {
                    return team == null ? null : team.slug();
                }

                @Override
                public String league_team_id() {
                    return team == null ? null : team.getLeagueTeamId();
                }

                @Override
                public String family() {
                    return localServer.family();
                }

                @Override
                public String match_id() {
                    return getMatch().getId();
                }

                @Override
                public String server_id() {
                    return localServer._id();
                }

                @Override
                public String session_id() {
                    return userStore.getSession(player.getBukkit())._id();
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPartyChange(final PlayerPartyChangeEvent event) {
        finish(event.getPlayer());
        start(event.getPlayer(), event.getNewParty());
    }
}
