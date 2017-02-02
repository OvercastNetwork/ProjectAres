package tc.oc.api.ocn;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.UserDoc;
import tc.oc.api.message.types.UpdateMultiResponse;

import static com.google.common.base.Preconditions.checkNotNull;

@Serialize
public class MapUpdateMultiResponse extends UpdateMultiResponse {

    public @Nonnull Map<UUID, UserDoc.Identity> users_by_uuid;

    /** Used by serializer */
    protected MapUpdateMultiResponse() {}

    public MapUpdateMultiResponse(Map<UUID, UserDoc.Identity> users_by_uuid) {
        super(0, 0, 0, 0, Collections.emptyMap());
        this.users_by_uuid = checkNotNull(users_by_uuid);
    }
}
