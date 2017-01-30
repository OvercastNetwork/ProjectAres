package tc.oc.api.punishments;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.Punishment;
import tc.oc.api.message.types.FindRequest;

import javax.annotation.Nullable;

@Serialize
public class PunishmentSearchRequest extends FindRequest<Punishment> {

    private final @Nullable String punisher;
    private final @Nullable String punished;
    private final @Nullable Boolean active;
    private final @Nullable Integer limit;

    private PunishmentSearchRequest(@Nullable PlayerId punisher, @Nullable PlayerId punished, @Nullable Boolean active, @Nullable Integer limit) {
        this.punisher = punisher == null ? null : punisher._id();
        this.punished = punished == null ? null : punished._id();
        this.active = active;
        this.limit = limit;
    }

    public static PunishmentSearchRequest punisher(PlayerId punisher, @Nullable Integer limit) {
        return new PunishmentSearchRequest(punisher, null, null, limit);
    }

    public static PunishmentSearchRequest punished(PlayerId punished, @Nullable Boolean active, @Nullable Integer limit) {
        return new PunishmentSearchRequest(null, punished, active, limit);
    }

}
