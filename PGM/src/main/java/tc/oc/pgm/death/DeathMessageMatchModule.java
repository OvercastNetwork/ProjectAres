package tc.oc.pgm.death;

import javax.inject.Inject;

import me.anxuiz.settings.SettingManager;
import me.anxuiz.settings.bukkit.PlayerSettings;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import tc.oc.commons.bukkit.nick.Identity;
import tc.oc.commons.bukkit.nick.IdentityProvider;
import tc.oc.commons.bukkit.settings.SettingBinder;
import tc.oc.commons.core.chat.Component;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.MatchPlayerDeathEvent;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.inject.MatchModuleFixtureManifest;
import tc.oc.pgm.module.ModuleDescription;

@ModuleDescription(name = "Death Messages")
@ListenerScope(MatchScope.RUNNING)
public class DeathMessageMatchModule extends MatchModule implements Listener {

    public static class Manifest extends MatchModuleFixtureManifest<DeathMessageMatchModule> {
        @Override protected void configure() {
            super.configure();

            final SettingBinder settings = new SettingBinder(publicBinder());
            settings.addBinding().toInstance(DeathMessageSetting.get());
            settings.addBinding().toInstance(HighlightDeathMessageSetting.get());
        }
    }

    private final IdentityProvider identityProvider;

    @Inject DeathMessageMatchModule(Match match, IdentityProvider identityProvider) {
        super(match);
        this.identityProvider = identityProvider;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onVanillaDeath(final PlayerDeathEvent event) {
        event.setDeathMessage(null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleDeathBroadcast(MatchPlayerDeathEvent event) {
        if(!event.getMatch().isRunning()) return;

        DeathMessageBuilder builder = new DeathMessageBuilder(event.getVictim(), event.getDamageInfo(), logger);
        Component message = new Component(builder.getMessage(), ChatColor.GRAY);

        if(event.isPredicted()) {
            message.extra(new Component(" "), new TranslatableComponent("death.predictedSuffix"));
        }

        final Identity victim = identityProvider.currentIdentity(event.getVictim().getBukkit());
        final Identity killer = event.getKiller() == null ? null : event.getKiller().getIdentity();

        for(MatchPlayer viewer : event.getMatch().getPlayers()) {
            final Player bukkit = viewer.getBukkit();
            final SettingManager settingManager = PlayerSettings.getManager(bukkit);
            final DeathMessageSetting.Options dms = settingManager.getValue(DeathMessageSetting.get(), DeathMessageSetting.Options.class);

            if(dms.isAllowed(victim.familiarity(bukkit)) || (killer != null && dms.isAllowed(killer.familiarity(bukkit)))) {
                if(event.isInvolved(viewer) && settingManager.getValue(HighlightDeathMessageSetting.get(), Boolean.class)) {
                    viewer.sendMessage(new Component(message, ChatColor.BOLD));
                } else {
                    viewer.sendMessage(message);
                }
            }
        }
    }
}
