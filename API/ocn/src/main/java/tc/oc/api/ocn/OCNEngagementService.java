package tc.oc.api.ocn;

import java.util.Collection;
import javax.inject.Inject;

import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.virtual.EngagementDoc;
import tc.oc.api.engagement.EngagementService;
import tc.oc.api.engagement.EngagementUpdateRequest;
import tc.oc.api.message.types.Reply;
import tc.oc.api.queue.Transaction;

class OCNEngagementService implements EngagementService {

    @Inject private Transaction.Factory transactionFactory;

    @Override
    public ListenableFuture<Reply> updateMulti(Collection<? extends EngagementDoc> engagements) {
        return transactionFactory.request(new EngagementUpdateRequest(engagements));
    }
}
