package tc.oc.pgm.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import tc.oc.pgm.PGM;
import tc.oc.pgm.map.PGMMap;
import tc.oc.pgm.rotation.*;

import com.sk89q.minecraft.util.commands.*;

public class RotationEditCommands {
    public static class RotationEditParent {
        @Command(
            aliases = {"rotationedit", "rotedit", "roted", "editrotation", "editrot", "erot"},
            desc = "Commands for editing the rotation and reloading it",
            min = 1,
            max = -1
        )
        @NestedCommand({RotationEditCommands.class})
        public static void editrot() {
        }
    }

    @Command(
        aliases = {"reload"},
        desc = "Reload the map rotation from it's provider",
        min = 0,
        max = 0
    )
    @CommandPermissions("pgm.rotation.reload")
    public static void reload(CommandContext args, CommandSender sender) throws CommandException {
        boolean success = PGM.getMatchManager().loadRotations();
        if(success) {
            sender.sendMessage(ChatColor.GREEN + "Reloaded the rotation successfully");
        } else {
            sender.sendMessage(ChatColor.RED + "There was an error reloading the rotation - check the server logs");
        }
    }

    @Command(
        aliases = {"append", "a"},
        desc = "Append a map to the end of the rotation",
        usage = "[map name]",
        min = 1,
        max = -1
    )
    @CommandPermissions("pgm.rotation.append")
    public static void append(CommandContext args, CommandSender sender) throws CommandException {
        PGMMap map = CommandUtils.getMap(args.getJoinedStrings(0), sender);

        apply(new AppendTransformation(map));
        sender.sendMessage(ChatColor.DARK_PURPLE + "Appended " + ChatColor.GOLD + map.getInfo().name + ChatColor.DARK_PURPLE + " to the rotation.");
    }

    @Command(
        aliases = {"insert", "i"},
        desc = "Insert a map into the rotation at a certain place",
        usage = "[index] [map name]",
        min = 2,
        max = -1
    )
    @CommandPermissions("pgm.rotation.insert")
    public static void insert(CommandContext args, CommandSender sender) throws CommandException {
        int index = args.getInteger(0);
        PGMMap map = CommandUtils.getMap(args.getJoinedStrings(1), sender);

        apply(new InsertTransformation(map, index - 1));
        sender.sendMessage(ChatColor.GOLD + map.getInfo().name + ChatColor.DARK_PURPLE + " inserted at index " + index);
    }

    @Command(
        aliases = {"remove", "r"},
        desc = "Removes all instances of a given map from the rotation",
        usage = "[map name]",
        min = 1,
        max = -1
    )
    @CommandPermissions("pgm.rotation.remove")
    public static void remove(CommandContext args, CommandSender sender) throws CommandException {
        PGMMap map = CommandUtils.getMap(args.getJoinedStrings(0), sender);

        apply(new RemoveAllTransformation(map));
        sender.sendMessage(ChatColor.DARK_PURPLE + "Removed all instances of " + ChatColor.GOLD + map.getInfo().name + ChatColor.DARK_PURPLE + " from the rotation");
    }

    @Command(
        aliases = {"removeat", "ra"},
        desc = "Removes the map at a specific index from the rotation",
        usage = "[index]",
        min = 1,
        max = 1
    )
    @CommandPermissions("pgm.rotation.removeat")
    public static void removeat(CommandContext args, CommandSender sender) throws CommandException {
        int index = args.getInteger(0);

        apply(new RemoveIndexTransformation(index - 1));
        sender.sendMessage(ChatColor.DARK_PURPLE + "Removed map at index " + index + " from the rotation");
    }

    private static void apply(RotationTransformation transform) throws CommandException {
        RotationManager manager = PGM.getMatchManager().getRotationManager();
        RotationState rotation = transform.apply(manager.getRotation());
        manager.setRotation(rotation);
    }
}
