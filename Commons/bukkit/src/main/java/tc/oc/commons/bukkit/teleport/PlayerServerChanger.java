package tc.oc.commons.bukkit.teleport;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.annotation.Nullable;
import javax.inject.Inject;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.Messenger;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.commons.core.plugin.PluginFacet;

public class PlayerServerChanger implements PluginFacet, Listener {

    private static final String PLUGIN_CHANNEL = "BungeeCord";
    private static final String METADATA_KEY = "QuitFuture";

    private final Plugin plugin;
    private final Messenger messenger;
    private final Server localServer;

    @Inject PlayerServerChanger(Plugin plugin, Messenger messenger, Server localServer) {
        this.plugin = plugin;
        this.messenger = messenger;
        this.localServer = localServer;
    }

    @Override
    public void enable() {
        messenger.registerOutgoingPluginChannel(plugin, PLUGIN_CHANNEL);
    }

    @Override
    public void disable() {
        messenger.unregisterOutgoingPluginChannel(plugin, PLUGIN_CHANNEL);
    }

    private ListenableFuture<?> quitFuture(Player player) {
        if(player.isOnline()) {
            final SettableFuture<?> future = SettableFuture.create();
            player.setMetadata(METADATA_KEY, new FixedMetadataValue(plugin, future));
            return future;
        } else {
            return Futures.immediateFuture(null);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        final MetadataValue future = event.getPlayer().getMetadata(METADATA_KEY, plugin);
        if(future != null) {
            ((SettableFuture) future.value()).set(null);
        }
    }

    public ListenableFuture<?> sendPlayerToLobby(Player player, boolean quiet) {
        return sendPlayerToServer(player, (String) null, quiet);
    }

    public ListenableFuture<?> sendPlayerToServer(Player player, @Nullable ServerDoc.BungeeName server, boolean quiet) {
        return sendPlayerToServer(player, server == null ? null : server.bungee_name(), quiet);
    }

    public ListenableFuture<?> sendPlayerToServer(Player player, @Nullable String bungeeName, boolean quiet) {
        if(localServer.bungee_name().equals(bungeeName) || (localServer.role() == ServerDoc.Role.LOBBY && bungeeName == null)) {
            return Futures.immediateFuture(null);
        }

        final ByteArrayOutputStream message = new ByteArrayOutputStream();
        final DataOutputStream out = new DataOutputStream(message);

        try {
            out.writeUTF(quiet ? "ConnectQuiet" : "Connect");
            out.writeUTF(bungeeName == null ? "default" : bungeeName);
        } catch(IOException e) {
            return Futures.immediateFailedFuture(e);
        }

        player.sendPluginMessage(plugin, PLUGIN_CHANNEL, message.toByteArray());
        return quitFuture(player);
    }

    public ListenableFuture<?> kickPlayer(Player player, String message) {
        // Magic color sequence signals Bungee to disconnect the player
        player.kickPlayer(ChatColor.BLACK.toString() + ChatColor.RED + ChatColor.RESET + message);
        return quitFuture(player);
    }
}
