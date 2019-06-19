package tc.oc.commons.bukkit.broadcast;

import com.google.inject.TypeLiteral;
import java.util.List;
import tc.oc.commons.bukkit.broadcast.model.BroadcastPrefix;
import tc.oc.commons.bukkit.broadcast.model.BroadcastSchedule;
import tc.oc.commons.bukkit.settings.SettingBinder;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.plugin.PluginFacetBinder;
import tc.oc.commons.core.reflect.TypeLiterals;
import tc.oc.parse.DocumentWatcher;
import tc.oc.parse.EnumParserManifest;
import tc.oc.parse.ParserTypeLiterals;

public class BroadcastManifest extends HybridManifest implements TypeLiterals, ParserTypeLiterals {
    @Override
    protected void configure() {
        installFactory(new TypeLiteral<DocumentWatcher.Factory<List<BroadcastSchedule>>>(){});

        bind(BroadcastFormatter.class);

        install(new EnumParserManifest<>(BroadcastPrefix.class));
        bind(DocumentParser(List(BroadcastSchedule.class))).to(BroadcastParser.class);

        new PluginFacetBinder(binder()).register(BroadcastScheduler.class);

        bind(BroadcastSettings.class);
        final SettingBinder settings = new SettingBinder(publicBinder());
        settings.addBinding().toInstance(BroadcastSettings.TIPS);
        settings.addBinding().toInstance(BroadcastSettings.NEWS);
        settings.addBinding().toInstance(BroadcastSettings.FACTS);
        settings.addBinding().toInstance(BroadcastSettings.RANDOM);
    }
}
