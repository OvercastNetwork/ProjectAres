package tc.oc.pgm.filters.parser;

import org.bukkit.entity.LivingEntity;
import org.jdom2.Element;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.QueryTypeFilter;
import tc.oc.pgm.filters.matcher.StaticFilter;
import tc.oc.pgm.filters.matcher.entity.EntityTypeFilter;
import tc.oc.pgm.filters.matcher.entity.LegacyWorldFilter;
import tc.oc.pgm.filters.operator.FallthroughFilter;
import tc.oc.pgm.filters.query.IBlockQuery;
import tc.oc.pgm.filters.query.IEntitySpawnQuery;
import tc.oc.pgm.filters.query.IEntityTypeQuery;
import tc.oc.pgm.filters.query.IPlayerQuery;
import tc.oc.pgm.xml.InvalidXMLException;

/**
 * For proto < 1.4
 */
public class LegacyFilterParser extends FilterParser {

    @Override
    protected void defineBuiltInFilters() throws InvalidXMLException {
        addDefault("allow-all", new StaticFilter(Filter.QueryResponse.ALLOW));
        addDefault("deny-all", new StaticFilter(Filter.QueryResponse.DENY));
        addDefaultPair("players", new QueryTypeFilter(IPlayerQuery.class));
        addDefaultPair("blocks", new QueryTypeFilter(IBlockQuery.class));
        addDefaultPair("world", new LegacyWorldFilter());
        addDefaultPair("spawns", new QueryTypeFilter(IEntitySpawnQuery.class));
        addDefaultPair("entities", new QueryTypeFilter(IEntityTypeQuery.class));
        addDefaultPair("mobs", new EntityTypeFilter(LivingEntity.class));
    }

    private void addDefaultPair(String name, Filter filter) throws InvalidXMLException {
        addDefault("allow-" + name, new FallthroughFilter(Filter.QueryResponse.ALLOW, filter));
        addDefault("deny-" + name, new FallthroughFilter(Filter.QueryResponse.DENY, filter));
    }

    private void addDefault(String name, Filter filter) throws InvalidXMLException {
        features.define(mangleId(name), filter);
    }

    @Override
    protected void parseTopLevelFilters() throws InvalidXMLException {
        // Legacy proto seperates filters and regions. The only reason
        // this matters is that <block> is ambiguous - it's both a region,
        // and a deprecated alias for <material>.
        for(Element el : xml.getRootElement().getChildren("filters")) {
            parseChildren(el).count();
        }
        for(Element el : xml.getRootElement().getChildren("regions")) {
            regionParser.parseChildren(el).count();
        }
    }

    @Override
    public boolean isReference(Element el) {
        // References look different, and are a lot harder to distinguish from other things
        return el.getName().equalsIgnoreCase("filter") &&
               el.getChildren().isEmpty() &&
               el.getAttribute("parents") == null &&
               el.getAttribute("name") != null;
    }
}
