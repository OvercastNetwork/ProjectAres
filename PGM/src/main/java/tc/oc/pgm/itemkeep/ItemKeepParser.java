package tc.oc.pgm.itemkeep;

import java.util.Set;

import com.google.common.collect.Sets;
import org.jdom2.Element;
import tc.oc.pgm.filters.matcher.block.MaterialFilter;
import tc.oc.pgm.filters.operator.AnyFilter;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;
import tc.oc.pgm.xml.parser.ElementParser;

public class ItemKeepParser implements ElementParser<ItemKeepRules> {

    @Override
    public ItemKeepRules parseElement(Element element) throws InvalidXMLException {
        final Set<MaterialFilter> itemFilters = Sets.newHashSet();
        for(Node elItemKeep : Node.fromChildren(element, "item-keep", "itemkeep")) {
            for(Node elItem : Node.fromChildren(elItemKeep.asElement(), "item")) {
                itemFilters.add(new MaterialFilter(XMLUtils.parseMaterialPattern(elItem)));
            }
        }

        final Set<MaterialFilter> armorFilters = Sets.newHashSet();
        for(Node elArmorKeep : Node.fromChildren(element, "armor-keep", "armorkeep")) {
            for(Node elItem : Node.fromChildren(elArmorKeep.asElement(), "item")) {
                armorFilters.add(new MaterialFilter(XMLUtils.parseMaterialPattern(elItem)));
            }
        }

        return new ItemKeepRules(AnyFilter.of(itemFilters),
                                 AnyFilter.of(armorFilters));
    }
}
