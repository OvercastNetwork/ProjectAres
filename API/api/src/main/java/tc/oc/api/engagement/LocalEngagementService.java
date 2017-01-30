package tc.oc.api.engagement;

import java.util.Collection;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.virtual.EngagementDoc;
import tc.oc.api.engagement.EngagementService;
import tc.oc.api.message.types.Reply;

public class LocalEngagementService implements EngagementService {

    @Override
    public ListenableFuture<Reply> updateMulti(Collection<? extends EngagementDoc> engagements) {
        return Futures.immediateFuture(Reply.SUCCESS);
    }
}
