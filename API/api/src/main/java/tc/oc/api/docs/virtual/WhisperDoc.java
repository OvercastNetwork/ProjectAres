package tc.oc.api.docs.virtual;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.time.Instant;
import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.PlayerId;

public interface WhisperDoc {
    interface Partial extends PartialModel {}

    @Serialize
    interface Complete extends Delivery, Model {
        @Nonnull String family();
        @Nonnull String server_id();
        @Nonnull Instant sent();
        @Nonnull PlayerId sender_uid();
        @Nullable String sender_nickname();
        @Nonnull PlayerId recipient_uid();
        @Nullable String recipient_specified();
        @Nonnull String content();
    }

    @Serialize
    interface Delivery extends Partial, Model {
        boolean delivered();
    }
}
