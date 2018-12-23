package tc.oc.commons.bukkit.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.NestedCommand;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.PluginManager;
import tc.oc.api.util.Permissions;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.commands.NestedCommands;

public class PermissionCommands implements NestedCommands {
    public static class Parent implements Commands {
        @Command(
            aliases = {"permission", "perm"},
            desc = "Commands to query permissions",
            min = 1,
            max = -1
        )
        @NestedCommand({PermissionCommands.class})
        @CommandPermissions(Permissions.DEVELOPER)
        public void perm() {
        }
    }

    void sendPermissionInfo(String name, CommandSender sender) {
        PluginManager pm = sender.getServer().getPluginManager();
        Permission permission = pm.getPermission(name);

        if(permission == null) {
            sender.sendMessage(ChatColor.RED + "Permission " + name + " is unregistered");
        } else {
            sender.sendMessage(ChatColor.GOLD + "Permission " + ChatColor.WHITE + permission.getName());
            sender.sendMessage(ChatColor.WHITE.toString() + ChatColor.ITALIC + permission.getDescription());
            sender.sendMessage(ChatColor.GOLD + "Default: " + ChatColor.WHITE + permission.getDefault());

            boolean first = true;
            for(Permission parent : pm.getPermissions()) {
                Boolean value = parent.getChildren().get(permission.getName());
                if(value != null) {
                    if(first) {
                        first = false;
                        sender.sendMessage(ChatColor.GOLD + "Parents:");
                    }

                    if(value) {
                        sender.sendMessage(ChatColor.GREEN + "  +" + parent.getName());
                    } else {
                        sender.sendMessage(ChatColor.RED + "  -" + parent.getName());
                    }
                }
            }

            first = true;
            for(Map.Entry<String, Boolean> child : permission.getChildren().entrySet()) {
                if(first) {
                    first = false;
                    sender.sendMessage(ChatColor.GOLD + "Children:");
                }

                if(child.getValue()) {
                    sender.sendMessage(ChatColor.GREEN + "  +" + child.getKey());
                } else {
                    sender.sendMessage(ChatColor.RED + "  -" + child.getKey());
                }
            }
        }
    }

    @Command(
        aliases = {"info"},
        desc = "Get detailed info about a permission",
        usage = "<permission>",
        min = 1,
        max = 1
    )
    public void info(CommandContext args, CommandSender sender) throws CommandException {
        sendPermissionInfo(args.getString(0), sender);
    }

    @Command(
        aliases = {"test"},
        desc = "Test for a specific permission",
        usage = "<permission> [player]",
        min = 1,
        max = 2
    )
    public void test(CommandContext args, CommandSender sender) throws CommandException {
        CommandSender player = CommandUtils.getCommandSenderOrSelf(args, sender, 1);
        String perm = args.getString(0);
        if(player.hasPermission(perm)) {
            sender.sendMessage(ChatColor.GREEN + player.getName() + " has permission " + perm);
        } else {
            sender.sendMessage(ChatColor.RED + player.getName() + " does NOT have permission " + perm);
        }
    }

    @Command(
        aliases = {"list"},
        desc = "List all permissions",
        usage = "[player] [prefix]",
        min = 0,
        max = 2
    )
    public void list(CommandContext args, CommandSender sender) throws CommandException {
        CommandSender player = CommandUtils.getCommandSenderOrSelf(args, sender, 0);
        String prefix = args.getString(1, "");

        sender.sendMessage(ChatColor.WHITE + "Permissions for " + player.getName() + ":");

        List<PermissionAttachmentInfo> perms = new ArrayList<>(player.getEffectivePermissions());
        Collections.sort(perms, new Comparator<PermissionAttachmentInfo>() {
            @Override
            public int compare(PermissionAttachmentInfo a, PermissionAttachmentInfo b) {
                return a.getPermission().compareTo(b.getPermission());
            }
        });

        for(PermissionAttachmentInfo perm : perms) {
            if(perm.getPermission().startsWith(prefix)) {
                sender.sendMessage((perm.getValue() ? ChatColor.GREEN : ChatColor.RED) +
                                   "  " + perm.getPermission() +
                                   (perm.getAttachment() == null ? "" : " (" + perm.getAttachment().getPlugin().getName() + ")"));
            }
        }
    }
}
