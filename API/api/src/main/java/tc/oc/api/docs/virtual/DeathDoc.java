package tc.oc.api.docs.virtual;

import java.time.Instant;
import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.PlayerId;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface DeathDoc {

    interface Partial extends PartialModel {}

    @Serialize
    interface Complete extends Base {
        @Nonnull PlayerId victim();
        @Nullable PlayerId killer();
    }

    @Serialize
    interface Creation extends Base {
        @Nonnull String victim();
        @Nullable String killer();
        int raindrops();
    }

    @Serialize
    interface Base extends Model, Partial {
        double x();
        double y();
        double z();
        @Nonnull String server_id();
        @Nonnull String match_id();
        @Nonnull String family();
        @Nonnull Instant date();
        @Nullable String entity_killer();
        @Nullable String block_killer();
        @Nullable Boolean player_killer();
        @Nullable Boolean teamkill();
        @Nullable String victim_class();
        @Nullable String killer_class();
        @Nullable Double distance();
        @Nullable Boolean enchanted();
        @Nullable String weapon();
        @Nullable String from();
        @Nullable String action();
        @Nullable String cause();
    }

}
