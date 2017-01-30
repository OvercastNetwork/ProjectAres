package tc.oc.pgm.destroyable;

import tc.oc.commons.core.commands.CommandBinder;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.features.FeatureBinder;
import tc.oc.pgm.map.inject.MapScoped;

public class DestroyableManifest extends HybridManifest {

    @Override
    protected void configure() {
        bind(DestroyableParser.class).in(MapScoped.class);

        final FeatureBinder<DestroyableFactory> destroyable = new FeatureBinder<>(binder(), DestroyableFactory.class);
        destroyable.bindDefinitionParser().to(DestroyableParser.class);
        destroyable.installRootParser();
        destroyable.installMatchModule(DestroyableMatchModule.class);

        bind(DestroyableCommands.class);
        new CommandBinder(binder())
            .register(DestroyableCommands.class);
    }
}
