package tc.oc.api.docs.virtual;

import java.time.Instant;
import javax.annotation.Nullable;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.PlayerId;

public interface SessionDoc {

    interface Partial extends PartialModel {}

    @Serialize
    interface Complete extends Model, Partial {
        String family_id();
        String server_id();
        @Nullable String version();
        PlayerId user();
        @Nullable String nickname();
        @Nullable String nickname_lower();
        String ip();
        Instant start();
        @Nullable Instant end();
    }
}
