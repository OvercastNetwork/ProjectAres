package tc.oc.api.engagement;

import java.util.Collection;

import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.virtual.EngagementDoc;
import tc.oc.api.message.types.Reply;

public interface EngagementService {

    ListenableFuture<Reply> updateMulti(Collection<? extends EngagementDoc> engagements);
}
