package tc.oc.commons.bukkit.punishment;

import java.time.Duration;
import java.time.Instant;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.Punishment;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.virtual.MatchDoc;
import tc.oc.api.docs.virtual.PunishmentDoc;
import tc.oc.api.model.IdFactory;
import tc.oc.api.model.ModelService;

@Singleton
public class PunishmentCreator {

    private final ModelService<Punishment, PunishmentDoc.Partial> punishmentService;
    private final IdFactory idFactory;
    private final Server localServer;

    @Inject PunishmentCreator(ModelService<Punishment, PunishmentDoc.Partial> punishmentService, IdFactory idFactory, Server localServer) {
        this.punishmentService = punishmentService;
        this.idFactory = idFactory;
        this.localServer = localServer;
    }

    public boolean offRecord() {
        return localServer.cross_server_profile() == null;
    }

    public ListenableFuture<Punishment> create(@Nullable PlayerId punisher, PlayerId punished, String reason, @Nullable PunishmentDoc.Type type, @Nullable Duration duration, boolean silent, boolean auto, boolean offrecord) {
        final String id = idFactory.newId();
        final Instant time = Instant.now();
        final MatchDoc match = localServer.current_match();
        return punishmentService.update(new PunishmentDoc.Creation() {
            public String punisher_id() { return punisher != null ? punisher._id() : null; }
            public String punished_id() { return punished._id(); }
            public String match_id() { return match != null ? match._id() : null; }
            public String server_id() { return localServer._id(); }
            public String family() { return localServer.family(); }
            public String reason() { return reason; }
            public PunishmentDoc.Type type() { return type; }
            public Instant date() { return time; }
            public Instant expire() { return duration != null ? date().plus(duration) : null; }
            public boolean off_record() { return offRecord() || offrecord; }
            public boolean debatable() { return true; }
            public boolean silent() { return silent; }
            public boolean automatic() { return auto; }
            public boolean active() { return true; }
            public String _id() { return id; }
        });
    }

    public ListenableFuture<Punishment> repeat(Punishment base, PlayerId punished) {
        return create(
            base.punisher(),
            punished,
            base.reason(),
            base.type(),
            base.expire() == null ? null : Duration.between(base.date(), base.expire()),
            base.silent(),
            base.automatic(),
            false // Since off the record punishments are destroyed, this can never be true
        );
    }

}
