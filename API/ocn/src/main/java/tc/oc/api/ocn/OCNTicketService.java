package tc.oc.api.ocn;

import javax.inject.Inject;

import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.Ticket;
import tc.oc.api.games.TicketService;
import tc.oc.api.message.types.CycleRequest;
import tc.oc.api.message.types.CycleResponse;
import tc.oc.api.message.types.PlayGameRequest;
import tc.oc.api.message.types.Reply;
import tc.oc.api.queue.QueueQueryService;
import tc.oc.api.queue.Transaction;

public class OCNTicketService extends QueueQueryService<Ticket> implements TicketService {

    @Inject private Transaction.Factory transactionFactory;

    @Override
    public ListenableFuture<Reply> requestPlay(PlayGameRequest request) {
        return transactionFactory.request(request);
    }

    @Override
    public ListenableFuture<CycleResponse> requestCycle(CycleRequest request) {
        return transactionFactory.request(request, CycleResponse.class);
    }
}
