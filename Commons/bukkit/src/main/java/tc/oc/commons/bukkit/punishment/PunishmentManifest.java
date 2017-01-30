package tc.oc.commons.bukkit.punishment;

import tc.oc.commons.bukkit.settings.SettingBinder;
import tc.oc.commons.core.commands.CommandBinder;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.minecraft.api.event.ListenerBinder;

public class PunishmentManifest extends HybridManifest {
    @Override
    protected void configure() {
        new CommandBinder(binder())
            .register(PunishmentCommands.class);

        new ListenerBinder(binder())
            .bindListener().to(PunishmentEnforcer.class);

        new SettingBinder(publicBinder())
            .addBinding().toInstance(PunishmentMessageSetting.get());
    }
}
