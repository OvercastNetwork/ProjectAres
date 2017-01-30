package tc.oc.api.games;

import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.Ticket;
import tc.oc.api.message.types.CycleRequest;
import tc.oc.api.message.types.CycleResponse;
import tc.oc.api.message.types.PlayGameRequest;
import tc.oc.api.message.types.Reply;
import tc.oc.api.model.QueryService;

public interface TicketService extends QueryService<Ticket> {

    ListenableFuture<Reply> requestPlay(PlayGameRequest request);

    ListenableFuture<CycleResponse> requestCycle(CycleRequest request);
}
