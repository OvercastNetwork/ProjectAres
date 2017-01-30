package tc.oc.parse;

import java.nio.file.Path;

import net.md_5.bungee.api.ChatColor;
import java.time.Duration;
import tc.oc.commons.core.inject.Manifest;
import tc.oc.parse.primitive.BooleanParser;
import tc.oc.parse.primitive.DurationParser;
import tc.oc.parse.primitive.PathParser;
import tc.oc.parse.xml.XMLManifest;

/**
 * Configures several generic parser types
 */
public class ParsersManifest extends Manifest {
    @Override
    protected void configure() {
        install(new XMLManifest());

        install(new PrimitiveParserManifest<>(Boolean.class, BooleanParser.class));
        install(new PrimitiveParserManifest<>(Duration.class, DurationParser.class));
        install(new PrimitiveParserManifest<>(Path.class, PathParser.class));

        install(new EnumParserManifest<>(ChatColor.class));
    }
}
