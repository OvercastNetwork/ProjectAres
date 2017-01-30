package tc.oc.commons.bukkit.commands;

import com.google.inject.TypeLiteral;
import org.bukkit.command.CommandSender;
import tc.oc.commons.core.commands.CommandRegistryImpl;
import tc.oc.commons.core.commands.CommandRegistry;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.minecraft.api.event.ListenerBinder;

public class BukkitCommandManifest extends HybridManifest {

    @Override
    protected void configure() {
        bind(BukkitCommandRegistry.class);
        bind(CommandRegistry.class).to(BukkitCommandRegistry.class);
        bind(new TypeLiteral<CommandRegistryImpl<CommandSender>>(){}).to(BukkitCommandRegistry.class);

        new ListenerBinder(binder())
            .bindListener().to(BukkitCommandRegistry.class);
    }
}
