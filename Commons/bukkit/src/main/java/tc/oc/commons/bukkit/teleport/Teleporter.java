package tc.oc.commons.bukkit.teleport;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.users.UserService;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.bukkit.event.PlayerServerChangeEvent;
import tc.oc.commons.bukkit.format.ServerFormatter;
import tc.oc.commons.bukkit.nick.IdentityProvider;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.NullAudience;

@Singleton
public class Teleporter {

    public static final Permission PERMISSION = new Permission("ocn.teleport", PermissionDefault.FALSE);
    public static final Permission PERMISSION_OTHERS = new Permission("ocn.teleport.others", PermissionDefault.OP);
    public static final String CROSS_DATACENTER_PERMISSION = "server.cross-datacenter";

    private final UserService userService;
    private final IdentityProvider identityProvider;
    private final Audiences audiences;
    private final PlayerServerChanger playerServerChanger;
    private final Server localServer;

    @Inject Teleporter(UserService userService, IdentityProvider identityProvider, Audiences audiences, PlayerServerChanger playerServerChanger, Server localServer) {
        this.userService = userService;
        this.identityProvider = identityProvider;
        this.audiences = audiences;
        this.playerServerChanger = playerServerChanger;
        this.localServer = localServer;
    }

    public boolean isConnectable(Server server) {
        return server.online() &&
               server.visibility() != ServerDoc.Visibility.PRIVATE &&
               server.role() != ServerDoc.Role.BUNGEE &&
               server.network().equals(localServer.network()) &&
               server.datacenter().equals(localServer.datacenter());
    }

    public boolean isVisible(Server server) {
        return isConnectable(server) &&
               server.visibility() == ServerDoc.Visibility.PUBLIC;
    }

    public boolean isLocal(@Nullable ServerDoc.Identity destinationServer) {
        return destinationServer == null ? localServer.role() == ServerDoc.Role.LOBBY
                                         : localServer.bungee_name().equals(destinationServer.bungee_name());
    }

    public void showCurrentServer(CommandSender viewer) {
        showCurrentServer(audiences.get(viewer));
    }

    public void showCurrentServer(Audience audience) {
        audience.sendMessage(new Component(
            new TranslatableComponent(
                "command.server.currentServer",
                ServerFormatter.light.nameWithDatacenter(localServer)
            ),
            ChatColor.DARK_PURPLE
        ));
        audience.sendMessage(new Component(new TranslatableComponent("command.server.switchPrompt"), ChatColor.GREEN));
    }

    public void localTeleport(Player traveler, @Nullable Player destinationPlayer) {
        if(destinationPlayer != null && traveler.hasPermission(PERMISSION)) {
            audiences.get(traveler).sendMessage(new Component(
                new TranslatableComponent("command.server.teleporting",
                                          new PlayerComponent(identityProvider.currentIdentity(destinationPlayer), NameStyle.VERBOSE)),
                ChatColor.DARK_PURPLE
            ));
            traveler.teleport(destinationPlayer);
        }
    }

    public void localTeleport(Player traveler, @Nullable UUID destinationPlayer) {
        if(destinationPlayer != null && traveler.hasPermission(PERMISSION)) {
            localTeleport(traveler, traveler.getServer().getPlayer(destinationPlayer));
        }
    }

    public void remoteTeleport(Player traveler, ServerDoc.Identity destinationServer, @Nullable UUID destinationPlayer) {
        if(traveler.hasPermission(PERMISSION)) {
            Player target = destinationPlayer == null ? null : traveler.getServer().getPlayer(destinationPlayer);
            if(target != null) {
                localTeleport(traveler, target);
            } else if (!isLocal(destinationServer)) {
                // TeleportListener will receive this message and call sendToServer
                userService.requestTeleport(traveler.getUniqueId(), destinationServer, destinationPlayer);
            }
        }
    }

    public ListenableFuture<?> remoteTeleport(Player traveler, @Nullable ServerDoc.Identity server) {
        return remoteTeleport(traveler, server, false);
    }

    public ListenableFuture<?> remoteTeleport(Player traveler, @Nullable ServerDoc.Identity server, boolean quiet) {
        return server != null ? remoteTeleport(traveler, server.datacenter(), server.name(), server.bungee_name(), quiet)
                              : sendToLobby(traveler, quiet);
    }

    public ListenableFuture<?> remoteTeleport(Player traveler, @Nullable String datacenter, @Nullable String serverName, @Nullable String bungeeName) {
        return remoteTeleport(traveler, datacenter, serverName, bungeeName, false);
    }

    public ListenableFuture<?> remoteTeleport(Player traveler, @Nullable String datacenter, @Nullable String serverName, @Nullable String bungeeName, boolean quiet) {
        final Audience audience = quiet ? NullAudience.INSTANCE : audiences.get(traveler);

        if(datacenter == null || !traveler.hasPermission(CROSS_DATACENTER_PERMISSION)) {
            datacenter = localServer.datacenter();
        }

        final BaseComponent fullName = ServerFormatter.light.nameWithDatacenter(datacenter, bungeeName, serverName, bungeeName == null);

        if((bungeeName == null && localServer.role() == ServerDoc.Role.LOBBY) ||
           (bungeeName != null && bungeeName.equals(localServer.bungee_name()))) {
            showCurrentServer(audience);
            return Futures.immediateFuture(null);
        }

        PlayerServerChangeEvent event = new PlayerServerChangeEvent(traveler, datacenter, bungeeName, new TranslatableComponent("servers.cannotChange"));
        traveler.getServer().getPluginManager().callEvent(event);

        if(event.isCancelled()) {
            if(event.getCancelMessage() != null) {
                audience.sendWarning(event.getCancelMessage(), false);
            }
            return Futures.immediateCancelledFuture();
        }

        audience.sendMessage(new Component(new TranslatableComponent("command.server.teleporting", fullName), ChatColor.DARK_PURPLE));
        return playerServerChanger.sendPlayerToServer(traveler, bungeeName, quiet);
    }

    public ListenableFuture<?> sendToLobby(Player player, @Nullable String datacenter, boolean quiet) {
        return remoteTeleport(player, datacenter, null, null, quiet);
    }

    public ListenableFuture<?> sendToLobby(Player player, @Nullable String datacenter) {
        return sendToLobby(player, datacenter, false);
    }

    public ListenableFuture<?> sendToLobby(Player player, boolean quiet) {
        return sendToLobby(player, null, quiet);
    }
}
