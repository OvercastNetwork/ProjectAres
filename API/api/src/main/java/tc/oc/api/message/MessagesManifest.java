package tc.oc.api.message;

import tc.oc.api.engagement.EngagementUpdateRequest;
import tc.oc.api.message.types.CycleRequest;
import tc.oc.api.message.types.CycleResponse;
import tc.oc.api.message.types.FindMultiRequest;
import tc.oc.api.message.types.FindMultiResponse;
import tc.oc.api.message.types.FindRequest;
import tc.oc.api.message.types.ModelDelete;
import tc.oc.api.message.types.ModelUpdate;
import tc.oc.api.message.types.Ping;
import tc.oc.api.message.types.PlayGameRequest;
import tc.oc.api.message.types.PlayerTeleportRequest;
import tc.oc.api.message.types.Reply;
import tc.oc.api.message.types.UpdateMultiResponse;
import tc.oc.api.servers.ServerSearchRequest;
import tc.oc.api.sessions.BadNickname;
import tc.oc.api.sessions.SessionChange;
import tc.oc.commons.core.inject.HybridManifest;

public class MessagesManifest extends HybridManifest {
    @Override
    public void configure() {
        bindAndExpose(MessageRegistry.class);

        publicBinder().forOptional(MessageQueue.class)
                      .setDefault().to(NullMessageQueue.class);

        final MessageBinder messages = new MessageBinder(publicBinder());
        
        messages.register(Reply.class);
        messages.register(BadNickname.class);
        messages.register(Ping.class);

        messages.register(FindRequest.class);
        messages.register(FindMultiRequest.class);
        messages.register(FindMultiResponse.class);

        messages.register(ModelUpdate.class);
        messages.register(ModelDelete.class);
        messages.register(UpdateMultiResponse.class);

        messages.register(ServerSearchRequest.class);
        messages.register(EngagementUpdateRequest.class);
        messages.register(PlayerTeleportRequest.class);
        messages.register(SessionChange.class);
        messages.register(PlayGameRequest.class);
        messages.register(CycleRequest.class);
        messages.register(CycleResponse.class);
    }
}
