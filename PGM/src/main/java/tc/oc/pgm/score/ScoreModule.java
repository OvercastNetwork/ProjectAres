package tc.oc.pgm.score;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.jdom2.Document;
import org.jdom2.Element;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.commons.core.util.Optionals;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.StaticFilter;
import tc.oc.pgm.filters.parser.FilterParser;
import tc.oc.pgm.goals.GoalModule;
import tc.oc.pgm.blitz.BlitzModule;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.map.ProtoVersions;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModuleFactory;
import tc.oc.pgm.module.ModuleDescription;
import tc.oc.pgm.regions.Region;
import tc.oc.pgm.regions.RegionParser;
import tc.oc.pgm.utils.MaterialPattern;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

@ModuleDescription(name = "Score", follows = { GoalModule.class, BlitzModule.class })
public class ScoreModule implements MapModule, MatchModuleFactory<ScoreMatchModule> {

    private final ScoreConfig config;

    public ScoreModule(@Nonnull ScoreConfig config) {
        Preconditions.checkNotNull(config, "score config");
        this.config = config;
    }

    private static final BaseComponent GAME = new TranslatableComponent("match.scoreboard.scores.title");
    @Override
    public BaseComponent getGameName(MapModuleContext context) {
        return GAME;
    }

    @Override
    public Set<MapDoc.Gamemode> getGamemodes(MapModuleContext context) {
        ImmutableSet.Builder<MapDoc.Gamemode> gamemodes = ImmutableSet.builder();
        if(getConfig().killScore > 0) {
            gamemodes.add(MapDoc.Gamemode.tdm);
        }
        if(context.features().containsAny(ScoreBoxFactory.class)) {
            gamemodes.add(MapDoc.Gamemode.scorebox);
        }
        return gamemodes.build();
    }

    @Override
    public ScoreMatchModule createMatchModule(Match match) {
        return new ScoreMatchModule(match, config);
    }

    @Nonnull
    public ScoreConfig getConfig() {
        return config;
    }

    // ---------------------
    // ---- XML Parsing ----
    // ---------------------

    public static ScoreModule parse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
        final boolean defaultScoresToZero = context.getProto().isNoOlderThan(ProtoVersions.DEFAULT_SCORES_TO_ZERO);
        final int defaultBoxScore = defaultScoresToZero ? 0 : 1;

        final FilterParser filterParser = context.needModule(FilterParser.class);
        final RegionParser regionParser = context.needModule(RegionParser.class);

        final List<Element> scoreElements = doc.getRootElement().getChildren("score");
        if(scoreElements.isEmpty()) {
            return null;
        }

        Optional<Integer> scoreLimit = Optional.empty();
        Optional<Integer> deathScore = Optional.empty();
        Optional<Integer> killScore = Optional.empty();
        boolean kingOfTheHill = false;

        for(Element scoreEl : scoreElements) {
            if(scoreEl.getChild("king") != null) {
                kingOfTheHill = true;
            }

            scoreLimit = Optionals.first(XMLUtils.parseNumber(scoreEl, "limit", Integer.class).range(Range.atLeast(1)).optional(), scoreLimit);
            deathScore = Optionals.first(XMLUtils.parseNumber(scoreEl, "deaths", Integer.class).optional(), deathScore);
            killScore = Optionals.first(XMLUtils.parseNumber(scoreEl, "kills", Integer.class).optional(), killScore);

            for(Element scoreBoxEl : scoreEl.getChildren("box")) {
                boolean silent = XMLUtils.parseBoolean(Node.fromAttr(scoreBoxEl, "silent"), false);
                int points = XMLUtils.parseNumber(Node.fromAttr(scoreBoxEl, "value", "points"),
                                                  Integer.class,
                                                  defaultBoxScore);

                final Filter filter = filterParser.property(scoreBoxEl, "filter")
                                                  .optional(StaticFilter.ALLOW);

                final Optional<Region> region = regionParser.property(scoreBoxEl)
                                                            .legacy()
                                                            .optionalUnion();
                final Filter trigger = region.isPresent() ? region.get()
                                                          : filterParser.property(scoreBoxEl, "trigger")
                                                                        .dynamic()
                                                                        .required();

                final Map<MaterialPattern, Double> redeemables = new HashMap<>();
                final Element elItems = scoreBoxEl.getChild("redeemables");
                if(elItems != null) {
                    for(Element elItem : elItems.getChildren("item")) {
                        redeemables.put(XMLUtils.parseMaterialPattern(elItem),
                                        XMLUtils.parseNumber(Node.fromAttr(elItem, "points"), Double.class, 1D));
                    }
                }

                context.features().define(scoreBoxEl, new ScoreBoxFactoryImpl(trigger, points, filter, silent, ImmutableMap.copyOf(redeemables)));
            }
        }

        // For backwards compatibility, default kill/death points to 1 if proto is old and <king/> tag is not present
        final int defaultKillScore = defaultScoresToZero || kingOfTheHill ? 0 : 1;

        return new ScoreModule(new ScoreConfig(scoreLimit,
                                               deathScore.orElse(defaultKillScore),
                                               killScore.orElse(defaultKillScore)));
    }
}
