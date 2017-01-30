package tc.oc.api.docs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.time.Instant;
import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.Model;

@Serialize
public interface Ticket extends Model {
    @Nonnull PlayerId user();
    @Nonnull String arena_id();
    @Nullable String server_id();
    @Nullable Instant dispatched_at();

    @Override @Serialize(false)
    default String toShortString() {
        return user().toShortString();
    }
}
