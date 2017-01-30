package tc.oc.pgm.tnt.license;

import com.sk89q.minecraft.util.commands.*;
import org.bukkit.command.CommandSender;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.docs.User;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.commands.CommandUtils;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.commands.NestedCommands;

import javax.inject.Inject;

public class LicenseCommands implements NestedCommands {

    private final BukkitUserStore userStore;
    private final LicenseBroker licenseBroker;
    private final Audiences audiences;

    @Inject LicenseCommands(BukkitUserStore userStore, LicenseBroker licenseBroker, Audiences audiences) {
        this.userStore = userStore;
        this.licenseBroker = licenseBroker;
        this.audiences = audiences;
    }

    public static class Parent implements Commands {
        @Command(
            aliases = {"tnt"},
            desc = "Manage your TNT license."
        )
        @NestedCommand(value = LicenseCommands.class, executeBody = true)
        public static void tnt(CommandContext args, CommandSender sender) throws CommandException {}
    }

    private User user(CommandSender sender) throws CommandException {
        return userStore.getUser(CommandUtils.senderToPlayer(sender));
    }

    @Command(
            aliases = {"info"},
            desc = "Information about your TNT license.",
            min = 0,
            max = 0
    )
    public void information(CommandContext args, CommandSender sender) throws CommandException {
        licenseBroker.information(user(sender), audiences.get(sender));
    }


    @Command(
        aliases = {"request"},
        desc = "Request a TNT license.",
        min = 0,
        max = 0
    )
    public void request(CommandContext args, CommandSender sender) throws CommandException {
        licenseBroker.request(user(sender), audiences.get(sender));
    }

    @Command(
        aliases = {"revoke"},
        desc = "Revoke your TNT license.",
        min = 0,
        max = 0
    )
    public void revoke(CommandContext args, final CommandSender sender) throws CommandException {
        licenseBroker.revoke(user(sender), LicenseBroker.RevokeReason.COMMAND, false);
    }

}
