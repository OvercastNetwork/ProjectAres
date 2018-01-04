package tc.oc.commons.bukkit;

import javax.inject.Inject;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;
import tc.oc.api.util.Permissions;
import tc.oc.bukkit.analytics.BukkitPlayerReporter;
import tc.oc.bukkit.analytics.LatencyReporter;
import tc.oc.bukkit.analytics.TickReporter;
import tc.oc.commons.bukkit.broadcast.BroadcastManifest;
import tc.oc.commons.bukkit.channels.AdminChatManifest;
import tc.oc.commons.bukkit.chat.ComponentRenderContext;
import tc.oc.commons.bukkit.chat.ComponentRendererRegistry;
import tc.oc.commons.bukkit.chat.ComponentRenderers;
import tc.oc.commons.bukkit.chat.FullNameRenderer;
import tc.oc.commons.bukkit.chat.NameRenderer;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.bukkit.chat.PlayerComponentRenderer;
import tc.oc.commons.bukkit.chat.TextComponentRenderer;
import tc.oc.commons.bukkit.chat.TranslatableComponentRenderer;
import tc.oc.commons.bukkit.chat.UserTextComponent;
import tc.oc.commons.bukkit.chat.UserTextComponentRenderer;
import tc.oc.commons.bukkit.commands.*;
import tc.oc.commons.bukkit.debug.LeakListener;
import tc.oc.commons.bukkit.event.targeted.TargetedEventManifest;
import tc.oc.commons.bukkit.flairs.FlairConfiguration;
import tc.oc.commons.bukkit.format.ServerFormatter;
import tc.oc.commons.bukkit.freeze.PlayerFreezer;
import tc.oc.commons.bukkit.inject.BukkitPluginManifest;
import tc.oc.commons.bukkit.inject.ComponentRendererModule;
import tc.oc.commons.bukkit.listeners.AppealAlertListener;
import tc.oc.commons.bukkit.listeners.ButtonManager;
import tc.oc.commons.bukkit.listeners.InactivePlayerListener;
import tc.oc.commons.bukkit.listeners.LocaleListener;
import tc.oc.commons.bukkit.listeners.LoginListener;
import tc.oc.commons.bukkit.listeners.PermissionGroupListener;
import tc.oc.commons.bukkit.listeners.PlayerMovementListener;
import tc.oc.commons.bukkit.listeners.WindowManager;
import tc.oc.commons.bukkit.localization.BukkitTranslator;
import tc.oc.commons.bukkit.localization.BukkitTranslatorImpl;
import tc.oc.commons.bukkit.localization.LocalizationManifest;
import tc.oc.commons.bukkit.localization.Translations;
import tc.oc.commons.bukkit.localization.Translator;
import tc.oc.commons.bukkit.logging.MapdevLogger;
import tc.oc.commons.bukkit.nick.IdentityProvider;
import tc.oc.commons.bukkit.nick.IdentityProviderImpl;
import tc.oc.commons.bukkit.nick.NicknameCommands;
import tc.oc.commons.bukkit.nick.PlayerAppearanceChanger;
import tc.oc.commons.bukkit.nick.PlayerAppearanceListener;
import tc.oc.commons.bukkit.nick.PlayerOrder;
import tc.oc.commons.bukkit.nick.PlayerOrderCache;
import tc.oc.commons.bukkit.punishment.PunishmentManifest;
import tc.oc.commons.bukkit.raindrops.RaindropManifest;
import tc.oc.commons.bukkit.report.ReportManifest;
import tc.oc.commons.bukkit.respack.ResourcePackCommands;
import tc.oc.commons.bukkit.respack.ResourcePackListener;
import tc.oc.commons.bukkit.respack.ResourcePackManager;
import tc.oc.commons.bukkit.restart.RestartCommands;
import tc.oc.commons.bukkit.sessions.SessionListener;
import tc.oc.commons.bukkit.settings.SettingManifest;
import tc.oc.commons.bukkit.stats.StatsCommands;
import tc.oc.commons.bukkit.stats.StatsManifest;
import tc.oc.commons.bukkit.suspend.SuspendListener;
import tc.oc.commons.bukkit.tablist.PlayerTabEntry;
import tc.oc.commons.bukkit.tablist.TabRender;
import tc.oc.commons.bukkit.teleport.Navigator;
import tc.oc.commons.bukkit.teleport.NavigatorInterface;
import tc.oc.commons.bukkit.teleport.NavigatorManifest;
import tc.oc.commons.bukkit.teleport.PlayerServerChanger;
import tc.oc.commons.bukkit.teleport.TeleportCommands;
import tc.oc.commons.bukkit.teleport.TeleportListener;
import tc.oc.commons.bukkit.teleport.Teleporter;
import tc.oc.commons.bukkit.ticket.TicketBooth;
import tc.oc.commons.bukkit.ticket.TicketCommands;
import tc.oc.commons.bukkit.ticket.TicketDisplay;
import tc.oc.commons.bukkit.ticket.TicketListener;
import tc.oc.commons.bukkit.tokens.TokenManifest;
import tc.oc.commons.bukkit.trophies.TrophyCase;
import tc.oc.commons.bukkit.trophies.TrophyCommands;
import tc.oc.commons.bukkit.users.JoinMessageManifest;
import tc.oc.commons.bukkit.util.PlayerStates;
import tc.oc.commons.bukkit.util.PlayerStatesImpl;
import tc.oc.commons.bukkit.whisper.WhisperManifest;
import tc.oc.commons.bukkit.whitelist.Whitelist;
import tc.oc.commons.bukkit.whitelist.WhitelistCommands;
import tc.oc.commons.core.CommonsCoreManifest;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.plugin.PluginFacetBinder;
import tc.oc.minecraft.api.event.Enableable;
import tc.oc.minecraft.api.event.ListenerBinder;

public final class CommonsBukkitManifest extends HybridManifest {
    @Override
    protected void configure() {
        install(new CommonsCoreManifest());
        install(new BukkitPluginManifest());
        install(new TargetedEventManifest());

        install(new SettingManifest());
        install(new WhisperManifest());
        install(new JoinMessageManifest());
        install(new AdminChatManifest());
        install(new BroadcastManifest());
        install(new LocalizationManifest());
        install(new NavigatorManifest());
        install(new RaindropManifest());
        install(new ReportManifest());
        install(new TokenManifest());
        install(new StatsManifest());
        install(new PunishmentManifest());

        // These are already bound as facets, so they only need to be exposed
        expose(PlayerFreezer.class);
        expose(PlayerServerChanger.class);
        expose(LeakListener.class);
        expose(TicketDisplay.class);
        expose(TicketListener.class);

        bindAndExpose(FlairConfiguration.class);
        bindAndExpose(PlayerAppearanceChanger.class);
        bindAndExpose(UserFinder.class);
        bindAndExpose(Teleporter.class);
        bindAndExpose(TicketBooth.class);
        bindAndExpose(MapdevLogger.class);
        bindAndExpose(TrophyCase.class);

        bindAndExpose(Translator.class).to(Translations.class);
        bindAndExpose(BukkitTranslator.class).to(BukkitTranslatorImpl.class);
        bindAndExpose(PlayerOrder.Factory.class).to(PlayerOrderCache.class);
        bindAndExpose(IdentityProvider.class).to(IdentityProviderImpl.class);
        bindAndExpose(ResourcePackManager.class).to(ResourcePackListener.class);
        bindAndExpose(NameRenderer.class).to(FullNameRenderer.class);
        bindAndExpose(ComponentRenderContext.class).to(ComponentRendererRegistry.class);
        bindAndExpose(PlayerStates.class).to(PlayerStatesImpl.class);

        installAndExpose(new ComponentRendererModule() {
            @Override
            protected void configure() {
                bindComponent(TextComponent.class).to(TextComponentRenderer.class);
                bindComponent(Component.class).to(TextComponentRenderer.class);
                bindComponent(TranslatableComponent.class).to(TranslatableComponentRenderer.class);
                bindComponent(PlayerComponent.class).to(PlayerComponentRenderer.class);
                bindComponent(UserTextComponent.class).to(UserTextComponentRenderer.class);
            }
        });

        final PluginFacetBinder facets = new PluginFacetBinder(binder());
        facets.register(TicketCommands.class);
        facets.register(PlayerMovementListener.class);
        facets.register(ButtonManager.class);
        facets.register(IdentityProviderImpl.class);
        facets.register(InactivePlayerListener.class);
        facets.register(LeakListener.class);
        facets.register(LocaleListener.class);
        facets.register(LoginListener.class);
        facets.register(MiscCommands.class);
        facets.register(Navigator.class);
        facets.register(NavigatorInterface.class);
        facets.register(NicknameCommands.class);
        facets.register(PermissionCommands.class);
        facets.register(PermissionCommands.Parent.class);
        facets.register(PermissionGroupListener.class);
        facets.register(PlayerAppearanceListener.class);
        facets.register(PlayerFreezer.class);
        facets.register(PlayerOrderCache.class);
        facets.register(PlayerServerChanger.class);
        facets.register(ResourcePackCommands.class);
        facets.register(ResourcePackCommands.Parent.class);
        facets.register(ResourcePackListener.class);
        facets.register(RestartCommands.class);
        facets.register(ServerCommands.class);
        facets.register(ServerVisibilityCommands.class);
        facets.register(SessionListener.class);
        facets.register(SkinCommands.class);
        facets.register(SkinCommands.Parent.class);
        facets.register(TeleportCommands.class);
        facets.register(TeleportListener.class);
        facets.register(TicketDisplay.class);
        facets.register(TicketListener.class);
        facets.register(TrophyCommands.class);
        facets.register(TrophyCommands.Parent.class);
        facets.register(TraceCommands.class);
        facets.register(TraceCommands.Parent.class);
        facets.register(UserCommands.class);
        facets.register(Whitelist.class);
        facets.register(WhitelistCommands.class);
        facets.register(WhitelistCommands.Parent.class);
        facets.register(WindowManager.class);
        facets.register(AppealAlertListener.class);
        facets.register(SuspendListener.class);
        facets.register(GroupCommands.Parent.class);

        // DataDog
        facets.register(TickReporter.class);
        facets.register(BukkitPlayerReporter.class);
        facets.register(LatencyReporter.class);

        // Hall of shame
        requestStaticInjection(ComponentRenderers.class);
        requestStaticInjection(PlayerTabEntry.class);
        requestStaticInjection(TabRender.class);
        requestStaticInjection(ServerFormatter.class);

        new ListenerBinder(binder())
            .bindListener().to(RegisterConsolePermissions.class);
    }

    static class RegisterConsolePermissions implements Enableable {

        @Inject Plugin plugin;
        @Inject ConsoleCommandSender console;

        PermissionAttachment attachment;

        @Override
        public void enable() {
            attachment = console.addAttachment(plugin, Permissions.CONSOLE, true);
        }

        @Override
        public void disable() {
            if(attachment != null) {
                attachment.remove();
            }
        }
    }
}
