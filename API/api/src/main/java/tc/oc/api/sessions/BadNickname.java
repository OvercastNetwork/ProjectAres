package tc.oc.api.sessions;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.message.types.Reply;

@Serialize
public interface BadNickname extends Reply {
    enum Problem { TAKEN, INVALID, THROTTLE }
    Problem problem();
}
