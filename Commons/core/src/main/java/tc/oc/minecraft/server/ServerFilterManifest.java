package tc.oc.minecraft.server;

import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.commons.core.inject.Manifest;
import tc.oc.parse.EnumParserManifest;
import tc.oc.parse.ParserTypeLiterals;

public class ServerFilterManifest extends Manifest implements ParserTypeLiterals {
    @Override
    protected void configure() {
        install(new EnumParserManifest<>(MapDoc.Gamemode.class));
        install(new EnumParserManifest<>(ServerDoc.Role.class));
        install(new EnumParserManifest<>(ServerDoc.Network.class));

        bind(ElementParser(ServerFilter.class))
            .to(ServerFilterParser.class);
    }
}
