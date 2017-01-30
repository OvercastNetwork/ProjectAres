package tc.oc.pgm.loot;

import org.bukkit.inventory.ItemStack;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.compose.ComposableManifest;
import tc.oc.pgm.features.FeatureBinder;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.inject.MatchBinders;
import tc.oc.pgm.match.inject.MatchScoped;
import tc.oc.pgm.xml.parser.ParserBinders;

public class LootManifest extends HybridManifest implements MatchBinders, ParserBinders {

    @Override
    protected void configure() {
        final FeatureBinder<Loot> loot = new FeatureBinder<>(binder(), Loot.class);
        loot.installReflectiveParser();
        loot.installRootParser();

        final FeatureBinder<Filler> fill = new FeatureBinder<>(binder(), Filler.class);
        fill.installReflectiveParser();
        fill.installRootParser();

        final FeatureBinder<Cache> cache = new FeatureBinder<>(binder(), Cache.class);
        cache.installReflectiveParser();
        cache.installRootParser();

        install(new ComposableManifest<ItemStack>(){}); // Parser<Composition<ItemStack>>

        bind(FillListener.class).in(MatchScoped.class);
        matchListener(FillListener.class, MatchScope.LOADED);
    }
}
