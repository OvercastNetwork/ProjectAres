package tc.oc.pgm.modules;

import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.StaticFilter;
import tc.oc.pgm.filters.parser.FilterParser;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.map.ProtoVersions;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModuleFactory;
import tc.oc.pgm.module.ModuleDescription;
import tc.oc.pgm.mutation.MutationMapModule;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;

@ModuleDescription(name="Mobs", follows = MutationMapModule.class)
public class MobsModule implements MapModule, MatchModuleFactory<MobsMatchModule> {
    private final Filter mobsFilter;

    public MobsModule(Filter mobsFilter) {
        this.mobsFilter = mobsFilter;
    }

    @Override
    public MobsMatchModule createMatchModule(Match match) {
        return new MobsMatchModule(match, this.mobsFilter);
    }

    // ---------------------
    // ---- XML Parsing ----
    // ---------------------

    public static MobsModule parse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
        FilterParser filterParser = context.needModule(FilterParser.class);
        Element mobsEl = doc.getRootElement().getChild("mobs");
        Filter mobsFilter = StaticFilter.DENY;
        if(mobsEl != null) {
            if(context.getProto().isNoOlderThan(ProtoVersions.FILTER_FEATURES)) {
                mobsFilter = filterParser.parseProperty(mobsEl, "filter");
            } else {
                Element filterEl = XMLUtils.getUniqueChild(mobsEl, "filter");
                if(filterEl != null) {
                    mobsFilter = filterParser.parseElement(filterEl);
                }
            }
        }
        return new MobsModule(mobsFilter);
    }
}
