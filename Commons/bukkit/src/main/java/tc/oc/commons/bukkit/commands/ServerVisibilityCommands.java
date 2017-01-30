package tc.oc.commons.bukkit.commands;

import java.util.EnumSet;
import java.util.logging.Logger;
import javax.inject.Inject;

import com.google.common.util.concurrent.ListenableFuture;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.minecraft.MinecraftService;
import tc.oc.minecraft.scheduler.SyncExecutor;
import tc.oc.commons.bukkit.event.WhitelistStateChangeEvent;
import tc.oc.commons.bukkit.whitelist.Whitelist;
import tc.oc.commons.core.commands.CommandFutureCallback;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.logging.Loggers;

public class ServerVisibilityCommands implements Listener, Commands {

    private static final String VISIBILITY_PERMISSION = "server.visibility";
    private static final EnumSet<ServerDoc.Visibility> OPTIONS = EnumSet.of(ServerDoc.Visibility.PUBLIC, ServerDoc.Visibility.UNLISTED);

    private final Logger logger;
    private final MinecraftService minecraftService;
    private final SyncExecutor syncExecutor;
    private final Whitelist whitelist;

    @Inject ServerVisibilityCommands(Loggers loggers, MinecraftService minecraftService, SyncExecutor syncExecutor, Whitelist whitelist) {
        this.whitelist = whitelist;
        this.logger = loggers.get(getClass());
        this.minecraftService = minecraftService;
        this.syncExecutor = syncExecutor;
    }

    private static String coloredVisibility(ServerDoc.Visibility visibility) {
        switch(visibility) {
            case PRIVATE: return ChatColor.BLUE + "private";
            case UNLISTED: return ChatColor.YELLOW + "unlisted";
            case PUBLIC: return ChatColor.GREEN + "public";
            default: return ChatColor.RED + "unknown";
        }
    }

    private static void reportVisibility(CommandSender sender, ServerDoc.Visibility visibility) {
        sender.sendMessage("Server visibility: " + coloredVisibility(visibility));
    }

    // Too bad Gson refuses to serialize anonymous classes
    private static class Info implements ServerDoc.Visible {
        private final ServerDoc.Visibility visibility;
        public Info(ServerDoc.Visibility visibility) {this.visibility = visibility; }
        @Override public ServerDoc.Visibility visibility() { return visibility; }
    }

    private ListenableFuture<Server> setVisibility(final ServerDoc.Visibility visibility) {
        return minecraftService.updateLocalServer(new Info(visibility));
    }

    @Command(
        aliases = { "visibility" },
        desc = "Show or change the visibility type of this server",
        usage = "[public|unlisted]",
        min = 0,
        max = 1
    )
    @CommandPermissions(VISIBILITY_PERMISSION)
    public void visibility(final CommandContext args, final CommandSender sender) throws CommandException {
        final Server local = minecraftService.getLocalServer();
        if(args.argsLength() < 1) {
            reportVisibility(sender, local.visibility());
        } else {
            final ServerDoc.Visibility visibility;
            try {
                visibility = ServerDoc.Visibility.valueOf(args.getString(0).toUpperCase());
                if(!OPTIONS.contains(visibility)) throw new IllegalArgumentException();
            } catch(IllegalArgumentException e) {
                throw new CommandException("Invalid visibility type '" + args.getString(0) + "'");
            }

            if(visibility == ServerDoc.Visibility.PUBLIC && whitelist.isEnabled()) {
                throw new CommandException("Cannot set visibility to 'public' while whitelist is enabled");
            }

            syncExecutor.callback(
                setVisibility(visibility),
                CommandFutureCallback.onSuccess(sender, args, response -> {
                    logger.info("Server visibility set to " + response.visibility() + " by " + sender.getName());
                    reportVisibility(sender, response.visibility());
                })
            );
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void hideAbandonedServer(PlayerQuitEvent event) {
        final Server local = minecraftService.getLocalServer();

        if(local.startup_visibility() != ServerDoc.Visibility.PUBLIC &&      // If server was initially not public and
           local.visibility() == ServerDoc.Visibility.PUBLIC &&              // server was made public and
           event.getPlayer().hasPermission(VISIBILITY_PERMISSION)) {    // someone with perms to do that is leaving...

            // ...check if there is still someone online with that permission
            for(Player player : event.getPlayer().getServer().getOnlinePlayers()) {
                // If someone else with perms is online, we're cool
                if(player != event.getPlayer() && player.hasPermission(VISIBILITY_PERMISSION)) return;
            }

            // If nobody with perms is online, make the server non-public again
            logger.info("Reverting server visibility to " + local.startup_visibility() + " because nobody with permissions is online");
            setVisibility(local.startup_visibility());
        }
    }

    @EventHandler
    public void hideWhitelistedServer(WhitelistStateChangeEvent event) {
        final Server local = minecraftService.getLocalServer();

        if(local.startup_visibility() != ServerDoc.Visibility.PUBLIC &&
           local.visibility() == ServerDoc.Visibility.PUBLIC &&
           event.isEnabled()) {

            logger.info("Reverting server visibility to " + local.startup_visibility() + " because whitelist is enabled");
            setVisibility(local.startup_visibility());
        }
    }
}
