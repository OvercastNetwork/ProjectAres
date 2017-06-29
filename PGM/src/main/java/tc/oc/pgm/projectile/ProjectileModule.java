package tc.oc.pgm.projectile;

import java.util.List;
import java.util.logging.Logger;
import javax.annotation.Nullable;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.potion.PotionEffect;
import org.jdom2.Document;
import org.jdom2.Element;
import java.time.Duration;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.parser.FilterParser;
import tc.oc.pgm.kits.ItemParser;
import tc.oc.pgm.kits.Kit;
import tc.oc.pgm.kits.KitParser;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.map.MapModuleFactory;
import tc.oc.pgm.module.ModuleDescription;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

@ModuleDescription(name="Projectile")
public class ProjectileModule implements MapModule {
    public static class Factory extends MapModuleFactory<ProjectileModule> {
        @Override
        public @Nullable ProjectileModule parse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
            final ItemParser itemParser = context.needModule(ItemParser.class);
            FilterParser filterParser = context.needModule(FilterParser.class);
            KitParser kitParser = context.needModule(KitParser.class);

            for(Element projectileElement : XMLUtils.flattenElements(doc.getRootElement(), "projectiles", "projectile")) {
                String name = projectileElement.getAttributeValue("name");
                Double damage = XMLUtils.parseNumber(projectileElement.getAttribute("damage"), Double.class, (Double) null);
                double velocity = XMLUtils.parseNumber(Node.fromChildOrAttr(projectileElement, "velocity"), Double.class, 1.0);
                ClickAction clickAction = XMLUtils.parseEnum(Node.fromAttr(projectileElement, "click"), ClickAction.class, "click action", ClickAction.BOTH);
                Class<? extends Entity> entity = XMLUtils.parseEntityTypeAttribute(projectileElement, "projectile", Arrow.class);
                List<PotionEffect> potionKit = itemParser.parsePotionEffects(projectileElement);
                Filter destroyFilter = filterParser.parseOptionalProperty(projectileElement, "destroy-filter").orElse(null);
                Duration coolDown = XMLUtils.parseDuration(projectileElement.getAttribute("cooldown"));
                boolean throwable = XMLUtils.parseBoolean(projectileElement.getAttribute("throwable"), true);
                Kit kit = kitParser.parseOptionalProperty(projectileElement, "victim-kit").orElse(null);
                Kit shooterKit = kitParser.parseOptionalProperty(projectileElement, "attacker-kit").orElse(null);

                context.features().define(projectileElement, new ProjectileDefinitionImpl(name, damage, velocity, clickAction, entity, potionKit, destroyFilter, coolDown, throwable, kit, shooterKit));
            }

            return null;
        }
    }
}
