package tc.oc.pgm.map;

import java.util.Collections;
import java.util.Set;
import java.util.logging.Logger;
import javax.annotation.Nullable;

import com.google.common.collect.Range;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.WorldCreator;
import org.jdom2.Document;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.pgm.xml.InvalidXMLException;

/**
 * Something that is created at most once for each map, usually by parsing some XML,
 * and stored in the {@link MapModuleContext}.
 *
 * This system is obsolete and may be deprecated soon. An explicit base module type
 * is fairly pointless in an injected environment, and creates a lot of unnecessary
 * dependencies.
 *
 * The preferred approach is to create a plain class, bind it as @MapScoped, and @Inject
 * only direct dependencies.
 */
public interface MapModule {

    /**
     * Get the gamemode implemented by the module, or null if it does not implement a gamemode
     */
    default Set<MapDoc.Gamemode> getGamemodes(MapModuleContext context) {
        return Collections.emptySet();
    }

    /**
     * Get the name of the game implemented by this module, or null if it does not implement a game
     */
    default @Nullable BaseComponent getGameName(MapModuleContext context) {
        return null;
    }

    /**
     * Return the number of players that this module alone can allow to join a match.
     */
    default Range<Integer> getPlayerLimits() {
        return Range.singleton(0);
    }

    /**
     * Called by {@link StaticMethodMapModuleFactory}
     *
     * @deprecated Implement a {@link MapModuleFactory} instead
     */
    @Deprecated
    static MapModule parse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException { return null; }

    /**
     * Called after all modules have finished parsing and all FeatureReferences
     * have been resolved successfully. A module can use this method
     * to replace FeatureReferences with FeatureDefinitions. It can also throw
     * InvalidXMLExceptions from here if needed e.g. for errors that can't be
     * detected until referenced features are available.
     */
    default void postParse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {}
}
