package tc.oc.api.docs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.time.Instant;
import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.Model;
import tc.oc.api.model.ModelName;

public interface Participation {
    interface Partial extends Model {} // Partials always have _id()

    @Serialize
    interface Start extends Partial {
        @Nonnull  Instant start();
        @Nullable String team_id();
        @Nullable String league_team_id();
        @Nonnull String player_id();
        @Nonnull String family();
        @Nonnull String match_id();
        @Nonnull String server_id();
        @Nonnull String session_id();
    }

    @Serialize
    interface Finish extends Partial {
        @Nullable Instant end();
    }

    @ModelName("Participation")
    interface Complete extends Start, Finish {}
}
