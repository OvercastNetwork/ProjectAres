package tc.oc.pgm.listing;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableList;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.SuggestException;
import org.bukkit.command.CommandSender;

@Singleton
public class ListingCommands {

    private final ListingService listingService;

    @Inject ListingCommands(ListingService listingService) {
        this.listingService = listingService;
    }

    @Command(
        aliases = "announce",
        usage = "[on|off]",
        desc = "Announce the server to the public listing service",
        min = 0,
        max = 1
    )
    @CommandPermissions("pgm.listing.announce")
    public void announce(CommandContext args, CommandSender sender) throws CommandException, SuggestException {
        listingService.update("on".equals(args.tryString(0, ImmutableList.of("on", "off")).orElse("on")), sender);
    }
}
