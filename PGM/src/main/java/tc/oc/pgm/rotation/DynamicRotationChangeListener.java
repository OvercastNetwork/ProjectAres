package tc.oc.pgm.rotation;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import tc.oc.commons.bukkit.chat.HeaderComponent;
import tc.oc.commons.core.chat.Component;
import tc.oc.pgm.Config;
import tc.oc.pgm.PGM;
import tc.oc.pgm.cycle.CycleMatchModule;
import tc.oc.pgm.events.MatchEndEvent;
import tc.oc.pgm.match.Match;

public class DynamicRotationChangeListener implements Listener {


    @EventHandler
    public void onMatchEnd(MatchEndEvent event) {
        RotationManager rotationManager = PGM.getMatchManager().getRotationManager();

        // Ignore if dynamic rotations are disabled or if there is only one rotation available
        if (!Config.getConfiguration().getBoolean("rotation.dynamic") || rotationManager.getRotations().size() == 1) return;

        // Number of players we can assume is active
        int playersOnline = event.getMatch().getServer().getOnlinePlayers().size();

        // Get appropriate rotation
        RotationCategory appr = getAppropriateRotationCategory(playersOnline, rotationManager);

        if (appr != null && !rotationManager.getCurrentRotationName().equals(appr.toString().toLowerCase())) {
            rotationManager.setRotation(appr.toString().toLowerCase(), rotationManager.getRotation(appr.toString().toLowerCase()));
            rotationManager.setCurrentRotationName(appr.toString().toLowerCase());
            CycleMatchModule cmm = event.getMatch().needMatchModule(CycleMatchModule.class);
            cmm.startCountdown(cmm.getConfig().countdown());

            PGM.get().getLogger().info("[Dynamic Rotations] Changing to \"" + appr.toString().toLowerCase() + "\" rotation...");
            sendRotationChangeMessage(event.getMatch());
        }
    }

    /**
     * Returns appropriate rotation looking at how many players (participating) are online.
     *
     * @param players Current participant player count.
     * @param rotationManager The {@link RotationManager}
     * @return any of {@link RotationCategory}
     */
    private RotationCategory getAppropriateRotationCategory(int players, RotationManager rotationManager) {
        Configuration config = Config.getConfiguration();
        int medium = config.getInt("rotation.providers.file.medium.count");
        int mega = config.getInt("rotation.providers.file.mega.count");

        if (players > medium && players <= mega && rotationManager.getRotation("medium") != null) return RotationCategory.MEDIUM;
        if (players > mega && rotationManager.getRotation("mega") != null) return RotationCategory.MEGA;

        return RotationCategory.MINI;
    }

    private void sendRotationChangeMessage(Match match) {
        match.sendMessage(new HeaderComponent(ChatColor.RED));
        match.sendMessage(new Component(new TranslatableComponent("rotation.change.broadcast.title"), ChatColor.GOLD));
        match.sendMessage(new Component(new TranslatableComponent("rotation.change.broadcast.info"), ChatColor.YELLOW));
        match.sendMessage(new HeaderComponent(ChatColor.RED));
    }
}
