package tc.oc.api.docs.virtual;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.PlayerId;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;

public interface ChatDoc {
    interface Partial extends PartialModel {}

    @Serialize
    interface Base extends Model, Partial {
        @Nonnull String message();
        @Nonnull String server_id();
        @Nullable String match_id();
        @Nonnull Type type();
        @Nonnull Instant sent_at();
        @Nullable Broadcast broadcast();
    }

    @Serialize
    interface Broadcast extends Partial {
        @Nonnull Destination destination();
        @Nullable String id();
    }

    @Serialize
    interface Creation extends Base {
        @Nullable String sender_id();
    }

    @Serialize
    interface Complete extends Base {
        @Nullable PlayerId sender();
    }

    enum Type {
        TEAM(true), SERVER(true), ADMIN(false), BROADCAST(false);

        public boolean batchUpdate;

        Type(boolean batchUpdate) {
            this.batchUpdate = batchUpdate;
        }
    }

    enum Destination {
        SERVER, FAMILY, GAME, NETWORK, GLOBAL
    }
}
