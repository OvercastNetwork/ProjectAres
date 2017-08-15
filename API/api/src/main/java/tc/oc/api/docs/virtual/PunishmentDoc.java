package tc.oc.api.docs.virtual;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.PlayerId;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;

public interface PunishmentDoc {
    interface Partial extends PartialModel {}

    @Serialize
    interface Base extends Model, Partial {
        @Nullable String match_id();
        @Nullable String server_id();
        @Nullable Instant expire();
        @Nullable String family();
        @Nonnull String reason();
        @Nonnull Instant date();
        boolean debatable();
        boolean silent();
        boolean automatic();
        boolean active();
        boolean off_record();
    }

    @Serialize
    interface Creation extends Base {
        @Nullable String punisher_id();
        @Nullable String punished_id();
        @Nullable Type type();
    }

    @Serialize
    interface Complete extends Base, Enforce, Evidence {
        @Nullable PlayerId punisher();
        @Nullable PlayerId punished();
        @Nonnull Type type();
        boolean stale();
    }

    @Serialize
    interface Enforce extends Partial {
        boolean enforced();
    }

    @Serialize
    interface Evidence extends Partial {
        @Nullable String evidence();
    }

    enum Type {

        WARN,
        KICK,
        BAN,
        FORUM_WARN,
        FORUM_BAN,
        TOURNEY_BAN,
        UNKNOWN;

        public String permission() {
            return name().toLowerCase();
        }
    }

}
