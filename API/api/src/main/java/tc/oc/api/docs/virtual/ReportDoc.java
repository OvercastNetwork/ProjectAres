package tc.oc.api.docs.virtual;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.time.Instant;
import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.PlayerId;

public interface ReportDoc {
    interface Partial extends PartialModel {}

    @Serialize
    interface Base extends Model, Partial {
        @Nonnull String scope();
        boolean automatic();
        @Nullable String family();
        @Nullable String server_id();
        @Nullable String match_id();
        @Nonnull String reason();
        @Nullable List<String> staff_online();
    }

    @Serialize
    interface Creation extends Base {
        @Nullable String reporter_id();
        @Nonnull String reported_id();
    }

    @Serialize
    interface Complete extends Base {
        @Nonnull Instant created_at();
        @Nullable PlayerId reporter();
        @Nullable PlayerId reported();
    }
}
