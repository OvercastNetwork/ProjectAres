package tc.oc.pgm.worldborder;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;
import org.jdom2.Document;
import org.jdom2.Element;
import java.time.Duration;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.StaticFilter;
import tc.oc.pgm.filters.matcher.match.MonostableFilter;
import tc.oc.pgm.filters.parser.FilterParser;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModuleFactory;
import tc.oc.pgm.module.ModuleDescription;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

@ModuleDescription(name = "World Border")
public class WorldBorderModule implements MapModule, MatchModuleFactory<WorldBorderMatchModule> {

    private final List<WorldBorder> borders;

    public WorldBorderModule(List<WorldBorder> borders) {
        this.borders = borders;
    }

    @Override
    public WorldBorderMatchModule createMatchModule(Match match) {
        return new WorldBorderMatchModule(match, borders);
    }

    public static WorldBorderModule parse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
        List<WorldBorder> borders = new ArrayList<>();
        for(Element el : XMLUtils.flattenElements(doc.getRootElement(), "world-borders", "world-border")) {
            Filter filter = context.needModule(FilterParser.class).property(el, "when").optional(StaticFilter.ALLOW);

            Duration after = XMLUtils.parseDuration(Node.fromAttr(el, "after"));
            if(after != null) {
                if(!StaticFilter.ALLOW.equals(filter)) {
                    throw new InvalidXMLException("Cannot combine a filter and an explicit time for a world border", el);
                }
                filter = MonostableFilter.after(context.features(), after);
            }

            WorldBorder border = new WorldBorder(
                filter,
                XMLUtils.parse2DVector(Node.fromRequiredAttr(el, "center")),
                XMLUtils.parseNumber(Node.fromRequiredAttr(el, "size"), Double.class),
                XMLUtils.parseDuration(Node.fromAttr(el, "duration"), Duration.ZERO),
                XMLUtils.parseNumber(Node.fromAttr(el, "damage"), Double.class, 0.2d),
                XMLUtils.parseNumber(Node.fromAttr(el, "buffer"), Double.class, 5d),
                XMLUtils.parseNumber(Node.fromAttr(el, "warning-distance"), Double.class, 5d),
                XMLUtils.parseDuration(Node.fromAttr(el, "warning-time"), Duration.ofSeconds(15))
            );

            borders.add(border);
        }

        if(borders.isEmpty()) {
            return null;
        } else {
            return new WorldBorderModule(ImmutableList.copyOf(borders));
        }
    }
}
