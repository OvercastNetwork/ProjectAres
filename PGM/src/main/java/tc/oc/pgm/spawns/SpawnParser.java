package tc.oc.pgm.spawns;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.collect.Lists;
import org.jdom2.Element;
import java.time.Duration;
import tc.oc.pgm.features.FeatureParser;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.StaticFilter;
import tc.oc.pgm.filters.matcher.party.TeamFilter;
import tc.oc.pgm.filters.operator.AllFilter;
import tc.oc.pgm.filters.parser.FilterParser;
import tc.oc.pgm.kits.KitParser;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.map.ProtoVersions;
import tc.oc.pgm.map.inject.MapScoped;
import tc.oc.pgm.points.PointParser;
import tc.oc.pgm.points.PointProvider;
import tc.oc.pgm.points.RandomPointProvider;
import tc.oc.pgm.points.SequentialPointProvider;
import tc.oc.pgm.points.SpreadPointProvider;
import tc.oc.pgm.teams.TeamFactory;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;
import tc.oc.pgm.xml.property.DurationProperty;
import tc.oc.pgm.xml.property.PropertyBuilderFactory;

@MapScoped
public class SpawnParser {

    protected final MapModuleContext context;
    protected final PointParser pointParser;
    protected final FeatureParser<TeamFactory> teamParser;
    protected final PropertyBuilderFactory<Boolean, ?> booleans;
    protected final PropertyBuilderFactory<Duration, DurationProperty> durations;
    protected final FilterParser filters;
    protected final KitParser kits;

    protected @Nullable Spawn defaultSpawn;

    @Inject private SpawnParser(MapModuleContext context, PointParser pointParser, FeatureParser<TeamFactory> teamParser, PropertyBuilderFactory<Boolean, ?> booleans, PropertyBuilderFactory<Duration, DurationProperty> durations, FilterParser filters, KitParser kits) {
        this.context = context;
        this.pointParser = pointParser;
        this.teamParser = teamParser;
        this.booleans = booleans;
        this.durations = durations;
        this.filters = filters;
        this.kits = kits;
    }

    public @Nullable Spawn getDefaultSpawn() {
        return defaultSpawn;
    }

    public Spawn parse(Element el, SpawnAttributes attributes) throws InvalidXMLException {
        attributes = this.parseAttributes(el, attributes);
        List<PointProvider> providers;

        if(context.getProto().isOlderThan(ProtoVersions.MODULE_SUBELEMENT_VERSION)) {
            providers = this.pointParser.parse(el, attributes.providerAttributes);
        } else {
            providers = new ArrayList<>(pointParser.parseMultiProperty(el, attributes.providerAttributes, "region"));
            for(Element elRegions : XMLUtils.getChildren(el, "regions")) {
                providers.addAll(this.pointParser.parseChildren(elRegions, attributes.providerAttributes));
            }
        }

        PointProvider provider;
        if(attributes.sequential) {
            provider = new SequentialPointProvider(providers);
        } else if(attributes.spread) {
            provider = new SpreadPointProvider(providers);
        } else {
            provider = new RandomPointProvider(providers);
        }

        return context.features().define(el, Spawn.class, new SpawnImpl(attributes, provider));
    }

    public List<Spawn> parseChildren(Element parent, SpawnAttributes attributes) throws InvalidXMLException {
        attributes = this.parseAttributes(parent, attributes);
        List<Spawn> spawns = Lists.newArrayList();
        for(Element spawnsEl : parent.getChildren("spawns")) {
            spawns.addAll(this.parseChildren(spawnsEl, attributes));
        }
        for(Element spawnEl : parent.getChildren("spawn")) {
            spawns.add(this.parse(spawnEl, attributes));
        }
        for(Element defaultEl : parent.getChildren("default")) {
            if(defaultSpawn != null) {
                throw new InvalidXMLException("Cannot have multiple default spawns", defaultEl);
            }
            this.defaultSpawn = parse(defaultEl, attributes);
        }
        return spawns;
    }

    private SpawnAttributes parseAttributes(Element el, SpawnAttributes parent) throws InvalidXMLException {
        return parent.merge(
            pointParser.parseAttributes(el, parent.providerAttributes),
            parseFilter(el),
            kits.property(el).optional(),
            booleans.property(el, "sequential").optional(),
            booleans.property(el, "spread").optional(),
            booleans.property(el, "exclusive").optional(),
            booleans.property(el, "persistent").optional(),
            booleans.property(el, "use-last-location").optional()
        );
    }

    private Filter parseFilter(Element el) throws InvalidXMLException {
        Filter filter = StaticFilter.ABSTAIN;
        final Node nodeTeam = Node.fromAttr(el, "team");
        if(nodeTeam != null) {
            filter = AllFilter.of(filter, new TeamFilter(teamParser.parseReference(nodeTeam)));
        }
        final Optional<Filter> prop = filters.property(el).optional();
        if(prop.isPresent()) {
            filter = AllFilter.of(filter, prop.get());
        }
        return filter;
    }
}
