package tc.oc.api.games;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.Ticket;
import tc.oc.api.message.types.CycleRequest;
import tc.oc.api.message.types.CycleResponse;
import tc.oc.api.message.types.PlayGameRequest;
import tc.oc.api.message.types.Reply;
import tc.oc.api.model.NullQueryService;

class NullTicketService extends NullQueryService<Ticket> implements TicketService {

    @Override
    public ListenableFuture<Reply> requestPlay(PlayGameRequest request) {
        return Futures.immediateFuture(Reply.FAILURE);
    }

    @Override
    public ListenableFuture<CycleResponse> requestCycle(CycleRequest request) {
        return Futures.immediateFuture(CycleResponse.EMPTY);
    }
}
