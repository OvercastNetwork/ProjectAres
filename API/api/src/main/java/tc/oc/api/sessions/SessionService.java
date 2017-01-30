package tc.oc.api.sessions;

import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.Session;
import tc.oc.api.docs.UserId;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.docs.virtual.SessionDoc;
import tc.oc.api.message.types.FindMultiResponse;
import tc.oc.api.model.ModelService;

public interface SessionService extends ModelService<Session, SessionDoc.Partial> {

    ListenableFuture<Session> start(SessionStartRequest request);

    ListenableFuture<?> finish(Session session);

    ListenableFuture<Session> online(UserId player);

    ListenableFuture<FindMultiResponse<Session>> friends(UserId player);

    ListenableFuture<FindMultiResponse<Session>> staff(ServerDoc.Network network, boolean disguised);
}
