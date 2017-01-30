package tc.oc.api.engagement;

import java.util.Collection;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.EngagementDoc;
import tc.oc.api.message.Message;
import tc.oc.api.queue.MessageDefaults;

@MessageDefaults.RoutingKey("engagements")
@MessageDefaults.Persistent(true)
public class EngagementUpdateRequest implements Message {

    @Serialize public final Collection<? extends EngagementDoc> engagements;

    public EngagementUpdateRequest(Collection<? extends EngagementDoc> engagements) {
        this.engagements = engagements;
    }
}
