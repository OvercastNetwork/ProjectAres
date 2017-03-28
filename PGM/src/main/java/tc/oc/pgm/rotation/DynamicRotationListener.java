package tc.oc.pgm.rotation;

import com.google.inject.Inject;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.api.bukkit.users.OnlinePlayers;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.chat.HeaderComponent;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.pgm.PGM;
import tc.oc.pgm.events.MatchEndEvent;

import java.util.logging.Logger;

public class DynamicRotationListener implements PluginFacet, Listener {
    
    private final Logger logger;
    private final Audiences audiences;
    private final OnlinePlayers players;
    private final ConfigurationSection config;
    
    @Inject DynamicRotationListener(Loggers loggers, Audiences audiences, OnlinePlayers players, Configuration config) {
        this.logger = loggers.get(getClass());
        this.audiences = audiences;
        this.players = players;
        this.config = config.getConfigurationSection("rotation");
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onMatchEnd(MatchEndEvent event) {
        RotationManager rotationManager = PGM.getMatchManager().getRotationManager();

        // Ignore if dynamic rotations are disabled or if there is only one rotation available
        if (!config.getBoolean("dynamic", false) || rotationManager.getRotations().size() <= 1) return;

        int playerCount = players.count();
        
        // Get appropriate rotation
        RotationProviderInfo rotation = rotationManager.getProviders().stream()
                .filter(info -> playerCount >= info.count).findFirst().orElse(null);

        if (rotation == null) {
            logger.warning("No valid rotation was found to accommodate " + playerCount + " players. Missing fallback?");
        } else {
            RotationState rotState = rotation.provider.getRotation(rotation.name);

            if (rotState == null) {
                logger.warning("'" + rotation.name + "' rotation provider doesn't have a rotation with it's own name");
            } else if (!rotationManager.getCurrentRotationName().equals(rotation.name)) {
                rotationManager.setRotation(rotation.name, rotState);
                rotationManager.setCurrentRotationName(rotation.name);
    
                logger.info("Changing to \"" + rotation.name + "\" rotation...");
                sendRotationChangeMessage(audiences.localServer());
            }
        }
    }

    private void sendRotationChangeMessage(Audience audience) {
        audience.sendMessage(new HeaderComponent(ChatColor.RED));
        audience.sendMessage(new Component(new TranslatableComponent("rotation.change.broadcast.title"), ChatColor.GOLD));
        audience.sendMessage(new Component(new TranslatableComponent("rotation.change.broadcast.info"), ChatColor.YELLOW));
        audience.sendMessage(new HeaderComponent(ChatColor.RED));
    }
}
