package tc.oc.pgm.spawns;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.collect.Lists;
import net.md_5.bungee.api.chat.BaseComponent;
import org.jdom2.Document;
import org.jdom2.Element;
import tc.oc.commons.core.util.Comparables;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.StaticFilter;
import tc.oc.pgm.filters.parser.FilterParser;
import tc.oc.pgm.kits.Kit;
import tc.oc.pgm.kits.KitParser;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.map.MapModuleFactory;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModuleFactory;
import tc.oc.pgm.module.ModuleDescription;
import tc.oc.pgm.teams.TeamModule;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

@ModuleDescription(name = "spawns", follows = { TeamModule.class })
public class SpawnModule implements MapModule, MatchModuleFactory<SpawnMatchModule> {

    public static final Duration MINIMUM_RESPAWN_DELAY = Duration.ofSeconds(1);
    public static final Duration IGNORE_CLICKS_DELAY = Duration.ofMillis(500);
    public static final Duration DEFAULT_RESPAWN_DELAY = Duration.ofSeconds(2);
    public static final Duration DEFAULT_FREEZE_TIME = Duration.ofSeconds(3);

    protected final Spawn defaultSpawn;
    protected final List<Spawn> spawns;
    protected final List<Kit> playerKits;
    protected final List<RespawnOptions> respawnOptions;

    public SpawnModule(Spawn defaultSpawn, List<Spawn> spawns, List<Kit> playerKits, List<RespawnOptions> respawnOptions) {
        this.playerKits = playerKits;
        assert defaultSpawn != null;
        this.defaultSpawn = defaultSpawn;
        this.spawns = spawns;
        this.respawnOptions = respawnOptions;
    }

    @Override
    public SpawnMatchModule createMatchModule(Match match) {
        return new SpawnMatchModule(match, this);
    }

    // ---------------------
    // ---- XML Parsing ----
    // ---------------------

    public static class Factory extends MapModuleFactory<SpawnModule> {

        @Inject private Provider<SpawnParser> spawnParser;
        @Inject private Provider<FilterParser> filterParser;

        @Override
        public SpawnModule parse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
            final SpawnParser parser = spawnParser.get();
            List<Spawn> spawns = Lists.newArrayList();
            List<Kit> playerKits = new ArrayList<>();

            for(Element spawnsEl : doc.getRootElement().getChildren("spawns")) {
                spawns.addAll(parser.parseChildren(spawnsEl, new SpawnAttributes()));
                final Kit playerKit = context.needModule(KitParser.class).property(spawnsEl, "player-kit").optional(null);
                if(playerKit != null) {
                    playerKits.add(playerKit);
                }
            }

            if(parser.getDefaultSpawn() == null) {
                throw new InvalidXMLException("map must have a single default spawn", doc);
            }

            return new SpawnModule(parser.getDefaultSpawn(), spawns, playerKits, parseRespawnOptions(context, logger, doc));
        }

        private List<RespawnOptions> parseRespawnOptions(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
            List<RespawnOptions> respawnOptions = Lists.newArrayList();
            
            for (Element elRespawn : XMLUtils.flattenElements(doc.getRootElement(), "respawns", "respawn")) {
                respawnOptions.add(getRespawnOptions(elRespawn));
            }
            // Parse root children respawn elements, Keeps old syntax and gives a default spawn if all others fail
            respawnOptions.add(getRespawnOptions(doc.getRootElement().getChildren("respawn"),
                    doc.getRootElement().getChild("autorespawn") != null, true));

            return respawnOptions;
        }

        private RespawnOptions getRespawnOptions(Element element) throws InvalidXMLException {
            return getRespawnOptions(Collections.singleton(element), false, false);
        }

        private RespawnOptions getRespawnOptions(Collection<Element> elements, boolean autorespawn, boolean topLevel) throws InvalidXMLException {
            Duration delay = DEFAULT_RESPAWN_DELAY;
            Duration freeze = DEFAULT_FREEZE_TIME;
            boolean auto = autorespawn; //Legacy support
            boolean blackout = false;
            boolean spectate = false;
            boolean bedSpawn = false;
            Filter filter = StaticFilter.ALLOW;
            BaseComponent message = null;

            for (Element element : elements) {
                delay = XMLUtils.parseDuration(element.getAttribute("delay"), delay);
                freeze = XMLUtils.parseDuration(element.getAttribute("freeze"), freeze);
                auto = XMLUtils.parseBoolean(element.getAttribute("auto"), auto); //Legacy support
                blackout = XMLUtils.parseBoolean(element.getAttribute("blackout"), blackout);
                spectate = XMLUtils.parseBoolean(element.getAttribute("spectate"), spectate);
                bedSpawn = XMLUtils.parseBoolean(element.getAttribute("bed"), bedSpawn);
                filter = filterParser.get().property(element).validate((Filter filt, Node node) -> {
                    if (topLevel)
                        throw new InvalidXMLException("Parent respawn elements can't use filters", node);
                }).optional(filter);
                message = XMLUtils.parseFormattedText(element, "message", message);
            }

            if(Comparables.lessThan(delay, MINIMUM_RESPAWN_DELAY)) {
                delay = MINIMUM_RESPAWN_DELAY;
            }

            return new RespawnOptions(delay, freeze, auto, blackout, spectate, bedSpawn, filter, message);
        }

    }

    @Override
    public void postParse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
        // TODO: Make this feasible and remove null checks in the spawn module
        //for(Spawn spawn : spawns) {
        //    if(spawn.pointProvider.canFail()) {
        //        throw new InvalidXMLException("Spawn is not guaranteed to provide a spawning location", context.features().getNode(spawn));
        //    }
        //}
    }
}
