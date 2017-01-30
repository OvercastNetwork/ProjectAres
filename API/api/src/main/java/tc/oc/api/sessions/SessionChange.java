package tc.oc.api.sessions;

import javax.annotation.Nullable;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.Session;
import tc.oc.api.message.Message;

@Serialize
public interface SessionChange extends Message {
    @Nullable Session old_session();
    @Nullable Session new_session();
}
