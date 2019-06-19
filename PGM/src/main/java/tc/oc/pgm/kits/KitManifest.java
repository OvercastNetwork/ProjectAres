package tc.oc.pgm.kits;

import org.bukkit.inventory.ItemStack;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.compose.ComposableManifest;
import tc.oc.pgm.features.FeatureBinder;
import tc.oc.pgm.map.inject.MapScoped;
import tc.oc.pgm.match.MatchPlayerFacetBinder;
import tc.oc.pgm.match.inject.MatchBinders;
import tc.oc.pgm.match.inject.MatchScoped;
import tc.oc.pgm.xml.parser.EnumParserManifest;
import tc.oc.pgm.xml.parser.ParserBinders;

public class KitManifest extends HybridManifest implements ParserBinders, MatchBinders {

    @Override
    protected void configure() {
        // Items
        bind(GlobalItemParser.class);
        bind(ItemParser.class).in(MapScoped.class);
        linkOptional(ItemParser.class);
        bindElementParser(ItemStack.class).to(ItemParser.class);

        // Custom Items
        bind(GrenadeListener.class).in(MatchScoped.class);
        matchListener(GrenadeListener.class);

        bind(KitListener.class).in(MatchScoped.class);
        matchListener(KitListener.class);

        bind(ItemSharingAndLockingListener.class).in(MatchScoped.class);
        matchListener(ItemSharingAndLockingListener.class);

        // Kits
        bind(KitDefinitionParser.class).in(MapScoped.class);
        bind(KitParser.class).in(MapScoped.class);
        linkOptional(KitParser.class);

        final FeatureBinder<Kit> kits = new FeatureBinder<>(binder(), Kit.class);
        kits.bindParser().to(KitParser.class);
        kits.bindDefinitionParser().to(KitDefinitionParser.class);
        kits.installRootParser();

        install(new ComposableManifest<Kit>(){});

        installPlayerModule(binder -> {
            new MatchPlayerFacetBinder(binder)
                .register(KitPlayerFacet.class);
        });

        // KitRules
        final FeatureBinder<KitRule> kitRules = new FeatureBinder<>(binder(), KitRule.class);
        kitRules.installReflectiveParser();
        kitRules.installRootParser();
        install(new EnumParserManifest<>(KitRule.Action.class));
    }
}

