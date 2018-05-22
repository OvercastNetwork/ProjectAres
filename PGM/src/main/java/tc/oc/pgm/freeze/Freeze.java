package tc.oc.pgm.freeze;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.sk89q.minecraft.util.commands.CommandException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import tc.oc.commons.bukkit.channels.admin.AdminChannel;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.chat.BukkitSound;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.bukkit.freeze.FrozenPlayer;
import tc.oc.commons.bukkit.freeze.PlayerFreezer;
import tc.oc.commons.bukkit.nick.IdentityProvider;
import tc.oc.commons.bukkit.util.OnlinePlayerMapAdapter;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;
import tc.oc.commons.core.commands.ComponentCommandException;
import tc.oc.commons.core.plugin.PluginFacet;

@Singleton
public class Freeze implements PluginFacet {

    public static final String PERMISSION = "projectares.freeze";

    private static final BukkitSound FREEZE_SOUND = new BukkitSound(Sound.ENTITY_ENDERDRAGON_GROWL, 1f, 1f);
    private static final BukkitSound THAW_SOUND = new BukkitSound(Sound.ENTITY_ENDERDRAGON_GROWL, 1f, 2f);

    private final IdentityProvider identityProvider;
    private final PlayerFreezer playerFreezer;
    private final Audiences audiences;
    private final FreezeConfig config;
    private final OnlinePlayerMapAdapter<FrozenPlayer> frozenPlayers;
    private final AdminChannel adminChannel;

    @Inject Freeze(IdentityProvider identityProvider, PlayerFreezer playerFreezer, Audiences audiences, FreezeConfig config, OnlinePlayerMapAdapter<FrozenPlayer> frozenPlayers, AdminChannel adminChannel) {
        this.identityProvider = identityProvider;
        this.playerFreezer = playerFreezer;
        this.audiences = audiences;
        this.config = config;
        this.frozenPlayers = frozenPlayers;
        this.adminChannel = adminChannel;
    }

    @Override
    public void enable() {
        frozenPlayers.enable();
    }

    @Override
    public void disable() {
        frozenPlayers.disable();
    }

    public boolean enabled() {
        return config.enabled();
    }

    public boolean isFrozen(Entity player) {
        return player instanceof Player && frozenPlayers.containsKey(player);
    }

    public void setFrozen(@Nullable CommandSender freezer, Player freezee, boolean frozen) throws CommandException {
        if(!freezee.equals(freezer) && freezee.hasPermission("projectares.freeze.exempt")) {
            throw new ComponentCommandException(new TranslatableComponent(
                "command.freeze.exempt",
                new PlayerComponent(identityProvider.currentIdentity(freezee), NameStyle.VERBOSE)
            ));
        }

        final PlayerComponent freezerComponent = new PlayerComponent(identityProvider.createIdentity(freezer), NameStyle.VERBOSE);
        final PlayerComponent freezeeComponent = new PlayerComponent(identityProvider.createIdentity(freezee), NameStyle.VERBOSE);
        final Audience freezeeAudience = audiences.get(freezee);

        final FrozenPlayer frozenPlayer = frozenPlayers.get(freezee);
        if(frozen && frozenPlayer == null) {
            frozenPlayers.put(freezee, playerFreezer.freeze(freezee));

            final BaseComponent freezeeMessage = new Component(new TranslatableComponent("freeze.frozen", freezerComponent), ChatColor.RED);

            freezeeAudience.playSound(FREEZE_SOUND);
            freezeeAudience.sendWarning(freezeeMessage, false);
            freezeeAudience.showTitle(Components.blank(), freezeeMessage, 5, 9999, 5);

            removeEntities(freezee.getLocation(), config.tntVictimRadius());

            if(freezer instanceof Player) {
                removeEntities(((Player) freezer).getLocation(), config.tntSenderRadius());
            }

            adminChannel.sendMessage(new TranslatableComponent("freeze.frozen.broadcast", freezeeComponent, freezerComponent));
        } else if(!frozen && frozenPlayer != null) {
            frozenPlayer.thaw();
            frozenPlayers.remove(freezee);

            freezeeAudience.hideTitle();
            freezeeAudience.playSound(THAW_SOUND);
            freezeeAudience.sendMessage(new Component(new TranslatableComponent("freeze.unfrozen", freezerComponent), ChatColor.GREEN));
            adminChannel.sendMessage(new TranslatableComponent("freeze.unfrozen.broadcast", freezeeComponent, freezerComponent));
        }
    }

    // Borrowed from WorldEdit
    private void removeEntities(Location origin, double radius) {
        if(radius <= 0) return;

        double radiusSq = radius * radius;
        for(Entity ent : origin.getWorld().getEntities()) {
            if(origin.distanceSquared(ent.getLocation()) > radiusSq)
                continue;

            if(ent instanceof TNTPrimed) {
                ent.remove();
            }
        }
    }

    /** Toggle the player's frozen status.
     * @return Boolean indicating whether the player is now frozen or not.
     */
    public boolean toggleFrozen(CommandSender freezer, Player freezee) throws CommandException {
        if(enabled()) {
            boolean frozen = !isFrozen(freezee);
            setFrozen(freezer, freezee, frozen);
            return frozen;
        } else {
            return false;
        }
    }
}
