package tc.oc.commons.bukkit.respack;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.NestedCommand;
import javax.inject.Inject;
import org.bukkit.command.CommandSender;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.commands.NestedCommands;

public class ResourcePackCommands implements NestedCommands {
    private static final String PERMISSION = "ocn.command.respack";

    public static class Parent implements Commands {
        @Command(
            aliases = {"respack"},
            desc = "Commands to manage custom resource packs",
            min = 1,
            max = -1
        )
        @NestedCommand({ResourcePackCommands.class})
        @CommandPermissions(PERMISSION)
        public void respack() {}
    }

    private final ResourcePackManager manager;

    @Inject ResourcePackCommands(ResourcePackManager manager) {
        this.manager = manager;
    }

    @Command(
        aliases = {"status"},
        desc = "Show info about the custom resource pack",
        min = 0,
        max = 0
    )
    public void status(CommandContext args, CommandSender sender) throws CommandException {
        sender.sendMessage("Custom resource packs are locally " + (manager.isEnabled() ? "ENABLED" : "DISABLED"));
        sender.sendMessage("Fast updating is " + (manager.isFastUpdate() ? "ENABLED" : "DISABLED"));
        if(manager.getUrl() == null) {
            sender.sendMessage("No resource pack is configured for this server");
        } else {
            sender.sendMessage("URL:  " + manager.getUrl());
            sender.sendMessage("SHA1: " + manager.getSha1());
        }
    }

    @Command(
        aliases = {"enable"},
        desc = "Enable the custom resource pack",
        min = 0,
        max = 0
    )
    public void enable(CommandContext args, CommandSender sender) throws CommandException {
        if(manager.isEnabled()) {
            sender.sendMessage("Custom resource pack already enabled");
        } else {
            sender.sendMessage("Enabling custom resource pack");
            manager.setEnabled(true);
        }
    }

    @Command(
        aliases = {"disable"},
        desc = "Disable the custom resource pack",
        min = 0,
        max = 0
    )
    public void disable(CommandContext args, CommandSender sender) throws CommandException {
        if(manager.isEnabled()) {
            sender.sendMessage("Disabling custom resource pack");
            manager.setEnabled(false);
        } else {
            sender.sendMessage("Custom resource pack already disabled");
        }
    }
}
