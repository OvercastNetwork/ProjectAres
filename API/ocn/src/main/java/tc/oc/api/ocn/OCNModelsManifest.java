package tc.oc.api.ocn;

import tc.oc.api.docs.Arena;
import tc.oc.api.docs.Chat;
import tc.oc.api.docs.Death;
import tc.oc.api.docs.Game;
import tc.oc.api.docs.Objective;
import tc.oc.api.docs.Participation;
import tc.oc.api.docs.Punishment;
import tc.oc.api.docs.Report;
import tc.oc.api.docs.Trophy;
import tc.oc.api.docs.virtual.ChatDoc;
import tc.oc.api.docs.virtual.DeathDoc;
import tc.oc.api.docs.virtual.MatchDoc;
import tc.oc.api.docs.virtual.PunishmentDoc;
import tc.oc.api.docs.virtual.ReportDoc;
import tc.oc.api.engagement.EngagementService;
import tc.oc.api.friendships.FriendshipService;
import tc.oc.api.games.TicketService;
import tc.oc.api.maps.MapService;
import tc.oc.api.model.ModelBinders;
import tc.oc.api.servers.ServerService;
import tc.oc.api.sessions.SessionService;
import tc.oc.api.tourney.TournamentService;
import tc.oc.api.users.UserService;
import tc.oc.api.whispers.WhisperService;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.inject.Manifest;

public class OCNModelsManifest extends HybridManifest implements ModelBinders {

    @Override
    protected void configure() {
        // Generic AMQP services
        bindModel(Game.class, model -> {
            model.queryService().setBinding().to(model.queueQueryService());
        });
        bindModel(Arena.class, model -> {
            model.queryService().setBinding().to(model.queueQueryService());
        });
        bindModel(Trophy.class, model -> {
            model.queryService().setBinding().to(model.queueQueryService());
        });

        // Generic HTTP services
        bindModel(Report.class, ReportDoc.Partial.class, model -> {
            model.bindService().to(model.httpService());
        });
        bindModel(Punishment.class, PunishmentDoc.Partial.class, model -> {
            model.bindService().to(model.httpService());
        });
        bindModel(Chat.class, ChatDoc.Partial.class, model -> {
            model.bindService().to(model.httpService());
        });
        bindModel(MatchDoc.class, model -> {
            model.bindService().to(model.httpService());
        });
        bindModel(Participation.Complete.class, Participation.Partial.class, model -> {
            model.bindService().to(model.httpService());
        });
        bindModel(Death.class, DeathDoc.Partial.class, model -> {
            model.bindService().to(model.httpService());
        });
        bindModel(Objective.class, model -> {
            model.bindService().to(model.httpService());
        });
        publicBinder().install(new Manifest() {
            @Override protected void configure() {
                // Specialized AMQP services
                forOptional(EngagementService.class).setBinding().to(OCNEngagementService.class);
                forOptional(TicketService.class).setBinding().to(OCNTicketService.class);

                // Specialized HTTP services
                forOptional(MapService.class).setBinding().to(OCNMapService.class);
                forOptional(ServerService.class).setBinding().to(OCNServerService.class);
                forOptional(SessionService.class).setBinding().to(OCNSessionService.class);
                forOptional(TournamentService.class).setBinding().to(OCNTournamentService.class);
                forOptional(UserService.class).setBinding().to(OCNUserService.class);
                forOptional(WhisperService.class).setBinding().to(OCNWhisperService.class);
                forOptional(FriendshipService.class).setBinding().to(OCNFriendshipService.class);
            }
        });
    }
}
