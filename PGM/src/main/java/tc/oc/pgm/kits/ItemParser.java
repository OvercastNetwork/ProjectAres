package tc.oc.pgm.kits;

import javax.inject.Inject;
import javax.inject.Provider;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.jdom2.Element;
import tc.oc.pgm.features.FeatureDefinitionContext;
import tc.oc.pgm.kits.tag.ItemTags;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.projectile.ProjectileDefinition;
import tc.oc.pgm.projectile.Projectiles;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;
import tc.oc.pgm.xml.parser.Parser;

import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowConsumer;

public class ItemParser extends GlobalItemParser implements MapModule {

    private final FeatureDefinitionContext fdc;
    private final MapModuleContext context;

    @Inject private ItemParser(Parser<Material> materialParser, FeatureDefinitionContext fdc, Provider<MapModuleContext> contextProvider) {
        super(materialParser);
        this.fdc = fdc;
        context = contextProvider.get();
    }

    @Override
    public void parseCustomNBT(Element el, ItemMeta meta) throws InvalidXMLException {
        super.parseCustomNBT(el, meta);

        Node.tryAttr(el, "projectile").ifPresent(rethrowConsumer(node -> {
            fdc.reference(node, ProjectileDefinition.class);
            Projectiles.setProjectileId(meta, node.getValue());
        }));

        Node.tryAttr(el, "victim-kit").ifPresent(rethrowConsumer(node -> {
            fdc.reference(node, Kit.class);
            ItemTags.KIT.set(meta, node.getValue());
        }));

        Node.tryAttr(el, "attacker-kit").ifPresent(rethrowConsumer(node -> {
            fdc.reference(node, Kit.class);
            ItemTags.HITTER_KIT.set(meta, node.getValue());
        }));
    }
}
