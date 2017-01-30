package tc.oc.commons.bukkit.ticket;

import java.util.Objects;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import tc.oc.api.bukkit.users.OnlinePlayers;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.Ticket;
import tc.oc.api.games.TicketStore;
import tc.oc.api.minecraft.users.UserStore;
import tc.oc.api.model.ModelDispatcher;
import tc.oc.api.model.ModelListener;
import tc.oc.api.servers.ServerStore;
import tc.oc.commons.bukkit.teleport.PlayerServerChanger;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.plugin.PluginFacet;

/**
 * Reacts to changing {@link Ticket}s and joining {@link Player}s, sending players
 * to a different server, if their ticket implies they should be elsewhere.
 *
 * Currently, the dispatch is unconditional, and does not fire a PlayerServerChangeEvent.
 * In the future, the event should be fired and cancellation respected.
 */
@Singleton
public class TicketListener implements PluginFacet, Listener, ModelListener {

    private final Logger logger;
    private final ServerStore servers;
    private final TicketStore tickets;
    private final UserStore userStore;
    private final Server localServer;
    private final OnlinePlayers onlinePlayers;
    private final PlayerServerChanger serverChanger;

    @Inject TicketListener(Loggers loggers, ServerStore servers, TicketStore tickets, UserStore userStore, Server localServer, OnlinePlayers onlinePlayers, PlayerServerChanger serverChanger, ModelDispatcher modelDispatcher) {
        this.logger = loggers.get(getClass());
        this.servers = servers;
        this.tickets = tickets;
        this.userStore = userStore;
        this.localServer = localServer;
        this.onlinePlayers = onlinePlayers;
        this.serverChanger = serverChanger;
        modelDispatcher.subscribe(this);
    }

    private void dispatch(@Nullable Player player, @Nullable Ticket ticket) {
        if(player == null || ticket == null) return;

        if(ticket.server_id() != null && !localServer._id().equals(ticket.server_id())) {
            final Server server = servers.byId(ticket.server_id());
            logger.info("Sending " + player.getName() + " to server " + server.bungee_name() + " to play a game");
            serverChanger.sendPlayerToServer(player, server, true);
        }
    }

    @HandleModel
    private void onTicketUpdate(@Nullable Ticket before, @Nullable Ticket after, Ticket ticket) {
        if(before == null || after == null || !Objects.equals(before.server_id(), after.server_id())) {
            dispatch(onlinePlayers.find(ticket.user()), after);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        dispatch(event.getPlayer(), tickets.tryUser(userStore.playerId(event.getPlayer())));
    }
}
