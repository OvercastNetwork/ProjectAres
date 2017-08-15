package tc.oc.api.docs.virtual;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.PlayerId;

import javax.annotation.Nullable;
import java.time.Instant;

public interface FriendshipDoc {

    interface Partial extends PartialModel {}

    @Serialize
    interface Complete extends Model, Partial {
        Instant sent_date();
        @Nullable Instant decision_date();
        PlayerId friender();
        PlayerId friended();
        boolean undecided();
        boolean accepted();
        boolean rejected();
    }
}
