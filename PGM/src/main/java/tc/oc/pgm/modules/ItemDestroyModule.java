package tc.oc.pgm.modules;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableSet;
import org.bukkit.Material;
import org.jdom2.Document;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModuleFactory;
import tc.oc.pgm.module.ModuleDescription;
import tc.oc.pgm.utils.MaterialPattern;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

@ModuleDescription(name="Item Destroy")
public class ItemDestroyModule implements MapModule, MatchModuleFactory<ItemDestroyMatchModule> {
    protected final ImmutableSet<MaterialPattern> patterns;

    public ItemDestroyModule(Set<MaterialPattern> patterns) {
        this.patterns = ImmutableSet.copyOf(patterns);
    }

    @Override
    public ItemDestroyMatchModule createMatchModule(Match match) {
        return new ItemDestroyMatchModule(match, this.patterns);
    }

    // ---------------------
    // ---- XML Parsing ----
    // ---------------------

    public static ItemDestroyModule parse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
        final Set<MaterialPattern> patterns = new HashSet<>();
        for(Node itemRemoveNode : Node.fromChildren(doc.getRootElement(), "item-remove", "itemremove")) {
            for(Node itemNode : Node.fromChildren(itemRemoveNode.asElement(), "item")) {
                final MaterialPattern pattern = XMLUtils.parseMaterialPattern(itemNode);
                patterns.add(pattern);
                if(pattern.matches(Material.POTION)) {
                    // TODO: remove this after we update the maps
                    patterns.add(new MaterialPattern(Material.SPLASH_POTION));
                }
            }
        }
        if(patterns.isEmpty()) {
            return null;
        } else {
            return new ItemDestroyModule(patterns);
        }
    }
}
