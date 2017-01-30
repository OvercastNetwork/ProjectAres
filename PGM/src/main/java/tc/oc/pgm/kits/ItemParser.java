package tc.oc.pgm.kits;

import javax.inject.Inject;

import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.jdom2.Element;
import tc.oc.pgm.features.FeatureDefinitionContext;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.projectile.ProjectileDefinition;
import tc.oc.pgm.projectile.Projectiles;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;
import tc.oc.pgm.xml.parser.Parser;

import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowConsumer;

public class ItemParser extends GlobalItemParser implements MapModule {

    private final FeatureDefinitionContext fdc;

    @Inject private ItemParser(Parser<Material> materialParser, FeatureDefinitionContext fdc) {
        super(materialParser);
        this.fdc = fdc;
    }

    @Override
    public void parseCustomNBT(Element el, ItemMeta meta) throws InvalidXMLException {
        super.parseCustomNBT(el, meta);

        Node.tryAttr(el, "projectile").ifPresent(rethrowConsumer(node -> {
            fdc.reference(node, ProjectileDefinition.class);
            Projectiles.setProjectileId(meta, node.getValue());
        }));
    }
}
