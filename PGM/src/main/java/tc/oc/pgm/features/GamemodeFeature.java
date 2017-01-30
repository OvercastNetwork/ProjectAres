package tc.oc.pgm.features;

import java.util.stream.Stream;

import tc.oc.api.docs.virtual.MapDoc;

/**
 * A feature that implements one or more gamemodes.
 *
 * The results of {@link #gamemodes()} for all loaded instances are
 * automatically included in the map's metadata.
 */
public interface GamemodeFeature extends FeatureDefinition {
    Stream<MapDoc.Gamemode> gamemodes();
}
