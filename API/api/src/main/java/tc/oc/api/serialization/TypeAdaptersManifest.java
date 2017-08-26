package tc.oc.api.serialization;

import java.net.InetAddress;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import com.google.inject.TypeLiteral;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.SemanticVersion;
import tc.oc.api.docs.UserId;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.api.docs.virtual.MatchDoc;
import tc.oc.commons.core.inject.Manifest;

public class TypeAdaptersManifest extends Manifest {

    @Override
    protected void configure() {
        final GsonBinder gson = new GsonBinder(binder());

        gson.bindFactory().to(StrictEnumTypeAdapter.Factory.class);
        gson.bindFactory().to(InstantTypeAdapter.Factory.class);

        gson.bindAdapter(UUID.class).to(UuidTypeAdapter.class);
        gson.bindAdapter(UserId.class).to(UserIdTypeAdapter.class);
        gson.bindAdapter(PlayerId.class).to(PlayerIdTypeAdapter.class);
        gson.bindAdapter(Duration.class).to(DurationTypeAdapter.class);
        gson.bindAdapter(SemanticVersion.class).to(SemanticVersionTypeAdapter.class);
        gson.bindAdapter(InetAddress.class).to(InetAddressTypeAdapter.class);
        gson.bindAdapter(Path.class).to(PathTypeAdapter.class);

        gson.bindAdapter(new TypeLiteral<Set<MapDoc.Gamemode>>(){})
               .to(new TypeLiteral<LenientEnumSetTypeAdapter<MapDoc.Gamemode>>(){});
    }
}
