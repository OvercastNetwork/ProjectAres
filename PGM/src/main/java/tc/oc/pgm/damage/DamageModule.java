package tc.oc.pgm.damage;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;
import org.jdom2.Document;
import org.jdom2.Element;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.parser.FilterParser;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.map.MapModuleFactory;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModuleFactory;
import tc.oc.pgm.module.ModuleDescription;
import tc.oc.pgm.xml.InvalidXMLException;

@ModuleDescription(name = "damage")
public class DamageModule implements MapModule, MatchModuleFactory<DamageMatchModule> {

    public static class Factory extends MapModuleFactory<DamageModule> {
        @Override
        public DamageModule parse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
            List<Filter> filters = new ArrayList<>();

            for(Element elDamage : doc.getRootElement().getChildren("damage")) {
                for(Element elFilter : elDamage.getChildren()) {
                    filters.add(context.needModule(FilterParser.class).parseElement(elFilter));
                }
            }

            return new DamageModule(ImmutableList.copyOf(filters));
        }
    }

    private final List<Filter> filters;

    public DamageModule(List<Filter> filters) {
        this.filters = filters;
    }

    @Override
    public DamageMatchModule createMatchModule(Match match) {
        return new DamageMatchModule(filters);
    }
}
