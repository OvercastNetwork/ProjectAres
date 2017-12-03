package tc.oc.commons.bukkit.ticket;

import java.util.List;
import javax.inject.Inject;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import org.bukkit.command.CommandSender;
import tc.oc.api.docs.Game;
import tc.oc.commons.bukkit.commands.CommandUtils;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.formatting.StringUtils;

public class TicketCommands implements Commands {

    private final TicketBooth ticketBooth;

    @Inject TicketCommands(TicketBooth ticketBooth) {
        this.ticketBooth = ticketBooth;
    }

    @Command(
        aliases = { "games" },
        desc = "List all the games you can play",
        min = 0,
        max = 0
    )
    public void games(final CommandContext args, final CommandSender sender) throws CommandException {
        ticketBooth.showGames(sender);
    }

    @Command(
        aliases = { "play", "replay" },
        desc = "Play a game",
        usage = "[game]",
        min = 0,
        max = -1
    )
    public List<String> play(final CommandContext args, final CommandSender sender) throws CommandException {
        final String name = args.argsLength() > 0 ? args.getRemainingString(0) : "";
        if(args.getSuggestionContext() != null) {
            return StringUtils.complete(name, ticketBooth.allGames(sender).stream().map(Game::name));
        }

        ticketBooth.playGame(CommandUtils.senderToPlayer(sender), name);
        return null;
    }

    @Command(
        aliases = { "quit" },
        desc = "Leave the game you are currently playing, or waiting to play",
        min = 0,
        max = 0
    )
    public void leave(final CommandContext args, final CommandSender sender) throws CommandException {
        ticketBooth.leaveGame(CommandUtils.senderToPlayer(sender), true);
    }

    @Command(
        aliases = { "watch" },
        desc = "Spectate a game",
        usage = "[game]",
        min = 0,
        max = -1
    )
    public List<String> watch(final CommandContext args, final CommandSender sender) throws CommandException {
        final String name = args.argsLength() > 0 ? args.getRemainingString(0) : "";
        if(args.getSuggestionContext() != null) {
            return StringUtils.complete(name, ticketBooth.allGames(sender).stream().map(Game::name));
        }

        ticketBooth.watchGame(CommandUtils.senderToPlayer(sender), name);
        return null;
    }
}
