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
import tc.oc.pgm.mutation.MutationQueue;

import java.util.logging.Logger;

public class DynamicRotationListener implements PluginFacet, Listener {

    private final Logger logger;
    private final Audiences audiences;
    private final OnlinePlayers players;
    private final ConfigurationSection config;
    private final MutationQueue mutationQueue;

    @Inject DynamicRotationListener(Loggers loggers, Audiences audiences, OnlinePlayers players, Configuration config, MutationQueue mutationQueue) {
        this.logger = loggers.get(getClass());
        this.audiences = audiences;
        this.players = players;
        this.config = config.getConfigurationSection("rotation");
        this.mutationQueue = mutationQueue;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMatchEnd(MatchEndEvent event) {
        RotationManager rotationManager = PGM.getMatchManager().getRotationManager();

        // Ignore if dynamic rotations are disabled or if there is only one rotation available
        if (!config.getBoolean("dynamic", false) || rotationManager.getRotations().size() <= 1) return;
        
        // If a mutation was set for the next map, don't change it yet.
        if (!mutationQueue.isEmpty()) return;

        int playerCount = players.count() + Math.round(event.getMatch().getObservingPlayers().size() / 2);

        // Get appropriate rotation
        RotationProviderInfo rotation = rotationManager.getProviders().stream()
                .filter(info -> playerCount >= info.count).findFirst().orElse(null);

        if (rotation == null) {
            logger.warning("No valid rotation was found to accommodate " + playerCount + " players. Missing fallback?");
        } else {
            RotationState rotState = rotation.provider.getRotation(rotation.name);

            String oldRotation = rotationManager.getCurrentRotationName();
            if (rotState == null) {
                logger.warning("'" + rotation.name + "' rotation provider doesn't have a rotation with it's own name");
            } else if (!rotationManager.getCurrentRotationName().equals(rotation.name)) {
                rotationManager.setRotation(rotation.name, rotState);
                rotationManager.setCurrentRotationName(rotation.name);

                logger.info("Changing to \"" + rotation.name + "\" rotation...");
                sendRotationChangeMessage(audiences.all(), oldRotation, rotation.name);
            }
        }
    }

    private void sendRotationChangeMessage(Audience audience, String previous, String current) {
        audience.sendMessage(new HeaderComponent(ChatColor.AQUA, new TranslatableComponent("rotation.change.broadcast.change")));
        audience.sendMessage(new Component(new TranslatableComponent("rotation.change.broadcast.previous", ChatColor.AQUA + previous), ChatColor.DARK_AQUA));
        audience.sendMessage(new Component(new TranslatableComponent("rotation.change.broadcast.current", ChatColor.AQUA + current), ChatColor.DARK_AQUA));
        audience.sendMessage(new Component(new TranslatableComponent("rotation.change.broadcast.info"), ChatColor.WHITE));
        audience.sendMessage(new HeaderComponent(ChatColor.DARK_AQUA));
    }
}
