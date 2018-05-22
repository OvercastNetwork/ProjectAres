package tc.oc.api;

import tc.oc.api.chat.ChatModelManifest;
import tc.oc.api.document.DocumentsManifest;
import tc.oc.api.engagement.EngagementModelManifest;
import tc.oc.api.friendships.FriendshipModelManifest;
import tc.oc.api.games.GameModelManifest;
import tc.oc.api.http.HttpManifest;
import tc.oc.api.maps.MapModelManifest;
import tc.oc.api.match.MatchModelManifest;
import tc.oc.api.message.MessagesManifest;
import tc.oc.api.model.ModelsManifest;
import tc.oc.api.punishments.PunishmentModelManifest;
import tc.oc.api.reports.ReportModelManifest;
import tc.oc.api.serialization.SerializationManifest;
import tc.oc.api.servers.ServerModelManifest;
import tc.oc.api.sessions.SessionModelManifest;
import tc.oc.api.tourney.TournamentModelManifest;
import tc.oc.api.trophies.TrophyModelManifest;
import tc.oc.api.users.UserModelManifest;
import tc.oc.api.whispers.WhisperModelManifest;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.logging.LoggingManifest;

public final class ApiManifest extends HybridManifest {

    @Override
    protected void configure() {
        install(new LoggingManifest()); // Load this right away, so we don't get log spam

        publicBinder().install(new SerializationManifest());
        install(new DocumentsManifest());
        install(new MessagesManifest());
        install(new ModelsManifest());
        install(new HttpManifest());

        install(new ServerModelManifest());
        install(new UserModelManifest());
        install(new SessionModelManifest());
        install(new GameModelManifest());
        install(new ReportModelManifest());
        install(new PunishmentModelManifest());
        install(new MapModelManifest());
        install(new MatchModelManifest());
        install(new EngagementModelManifest());
        install(new WhisperModelManifest());
        install(new TrophyModelManifest());
        install(new TournamentModelManifest());
        install(new FriendshipModelManifest());
        install(new ChatModelManifest());
    }
}
