package tc.oc.api.docs.virtual;

import java.time.Instant;
import javax.annotation.Nullable;

import tc.oc.api.annotations.Serialize;

public interface DeletableModel extends Model {
    @Serialize @Nullable Instant died_at();
    boolean dead();
    boolean alive();
}
