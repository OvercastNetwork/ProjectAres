package tc.oc.pgm.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.inject.Provides;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Difficulty;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.jdom2.Document;
import org.jdom2.Element;
import tc.oc.api.docs.SemanticVersion;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.commons.core.chat.Component;
import tc.oc.pgm.features.GamemodeFeature;
import tc.oc.pgm.map.Contributor;
import tc.oc.pgm.map.MapId;
import tc.oc.pgm.map.MapInfo;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.map.MapModuleFactory;
import tc.oc.pgm.map.ProtoVersions;
import tc.oc.pgm.map.inject.MapScoped;
import tc.oc.pgm.terrain.WorldConfigurator;
import tc.oc.pgm.module.ModuleDescription;
import tc.oc.pgm.terrain.WorldConfiguratorBinder;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

@ModuleDescription(name="Info")
public class InfoModule implements MapModule, WorldConfigurator {

    private final MapInfo info;

    // Use a TreeSet so order is consistent
    private final Set<MapDoc.Gamemode> gamemodes = new TreeSet<>(Collections.singleton(MapDoc.Gamemode.mixed));

    private Range<Integer> playerLimits = Range.singleton(0);

    public InfoModule(MapInfo info) {
        this.info = info;
    }

    public MapInfo getMapInfo() {
        return this.info;
    }

    public Set<MapDoc.Gamemode> getGamemodes() {
        return info.gamemodes.isEmpty() ? gamemodes : info.gamemodes;
    }

    public Range<Integer> getGlobalPlayerLimits() {
        return playerLimits;
    }

    @Override
    public void configureWorld(WorldCreator worldCreator) {
        worldCreator.environment(info.dimension);
    }

    @Override
    public void postParse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
        for(MapModule module : context.loadedModules()) {
            gamemodes.addAll(module.getGamemodes(context));
        }

        context.features()
               .all(GamemodeFeature.class)
               .flatMap(GamemodeFeature::gamemodes)
               .forEach(gamemodes::add);

        int min = 0, max = 0;
        for(MapModule module : context.loadedModules()) {
            final Range<Integer> limits = module.getPlayerLimits();
            min += limits.lowerEndpoint();
            max += limits.upperEndpoint();
        }
        playerLimits = Range.closed(min, max);
    }

    public static class Factory extends MapModuleFactory<InfoModule> {
        @Override
        protected void configure() {
            super.configure();

            new WorldConfiguratorBinder(binder())
                .addBinding().to(InfoModule.class);
        }

        @Provides @MapScoped MapInfo mapInfo(InfoModule module) {
            return module.getMapInfo();
        }

        @Provides @MapScoped MapId mapId(MapInfo info) {
            return info.id;
        }

        @Override
        public InfoModule parse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
            Element root = doc.getRootElement();

            String name = Node.fromRequiredChildOrAttr(root, "name").getValueNormalize();
            SemanticVersion version = XMLUtils.parseSemanticVersion(Node.fromRequiredChildOrAttr(root, "version"));
            MapDoc.Phase phase = XMLUtils.parseEnum(Node.fromLastChildOrAttr(root, "phase"), MapDoc.Phase.class, "phase", MapDoc.Phase.PRODUCTION);
            MapDoc.Edition edition = XMLUtils.parseEnum(Node.fromLastChildOrAttr(root, "edition"), MapDoc.Edition.class, "edition", MapDoc.Edition.STANDARD);

            // Allow multiple <objective> elements, so include files can provide defaults
            final BaseComponent objective = XMLUtils.parseLocalizedText(Node.fromRequiredLastChildOrAttr(root, "objective"));

            String slug = root.getChildTextNormalize("slug");
            BaseComponent game = XMLUtils.parseFormattedText(root, "game");

            MapDoc.Genre genre = XMLUtils.parseEnum(Node.fromNullable(root.getChild("genre")), MapDoc.Genre.class, "genre", MapDoc.Genre.OTHER);

            final TreeSet<MapDoc.Gamemode> gamemodes = new TreeSet<>();
            for(Element elGamemode : root.getChildren("gamemode")) {
                gamemodes.add(XMLUtils.parseEnum(elGamemode, MapDoc.Gamemode.class));
            }

            List<Contributor> authors = readContributorList(root, "authors", "author");

            if(game == null) {
                Element blitz = root.getChild("blitz");
                if(blitz != null) {
                    Element title = blitz.getChild("title");
                    if(title != null) {
                        if(context.getProto().isNoOlderThan(ProtoVersions.REMOVE_BLITZ_TITLE)) {
                            throw new InvalidXMLException("<title> inside <blitz> is no longer supported, use <map game=\"...\">", title);
                        }
                        game = new Component(title.getTextNormalize());
                    }
                }
            }

            List<Contributor> contributors = readContributorList(root, "contributors", "contributor");

            List<String> rules = new ArrayList<String>();
            for(Element parent : root.getChildren("rules")) {
                for(Element rule : parent.getChildren("rule")) {
                    rules.add(rule.getTextNormalize());
                }
            }

            Difficulty difficulty = XMLUtils.parseEnum(Node.fromLastChildOrAttr(root, "difficulty"), Difficulty.class, "difficulty");

            Environment dimension = XMLUtils.parseEnum(Node.fromLastChildOrAttr(root, "dimension"), Environment.class, "dimension", Environment.NORMAL);

            boolean friendlyFire = XMLUtils.parseBoolean(Node.fromLastChildOrAttr(root, "friendly-fire", "friendlyfire"), false);

            return new InfoModule(new MapInfo(context.getProto(), slug, name, version, edition, phase, game, genre, ImmutableSet.copyOf(gamemodes), objective, authors, contributors, rules, difficulty, dimension, friendlyFire));
        }
    }

    private static List<Contributor> readContributorList(Element root, String topLevelTag, String tag) throws InvalidXMLException {
        List<Contributor> contribs = new ArrayList<Contributor>();
        for(Element parent : root.getChildren(topLevelTag)) {
            for(Element child : parent.getChildren(tag)) {
                String name = XMLUtils.getNormalizedNullableText(child);
                UUID uuid = XMLUtils.parseUuid(Node.fromAttr(child, "uuid"));
                String contribution = XMLUtils.getNullableAttribute(child, "contribution", "contrib");

                if(name == null && uuid == null) {
                    throw new InvalidXMLException("Contributor must have either a name or UUID", child);
                }

                contribs.add(new Contributor(uuid, name, contribution));
            }
        }
        return contribs;
    }
}
