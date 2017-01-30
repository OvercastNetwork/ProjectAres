package tc.oc.api.match;

import java.time.Instant;
import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.Death;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.message.types.FindRequest;

import javax.annotation.Nullable;

@Serialize
public class DeathSearchRequest extends FindRequest<Death> {

    private final @Nullable String victim;
    private final @Nullable String killer;
    private final @Nullable Instant date;
    private final @Nullable Integer limit;

    public DeathSearchRequest(@Nullable PlayerId victim, @Nullable PlayerId killer, @Nullable Instant date, @Nullable Integer limit) {
        this.victim = victim != null ? victim.player_id() : null;
        this.killer = killer != null ? killer.player_id() : null;
        this.date = date;
        this.limit = limit;
    }

}
