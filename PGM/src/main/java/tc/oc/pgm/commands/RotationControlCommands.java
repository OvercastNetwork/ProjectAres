package tc.oc.pgm.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import tc.oc.pgm.PGM;
import tc.oc.pgm.rotation.RotationManager;

import com.sk89q.minecraft.util.commands.*;

public class RotationControlCommands {
    public static class RotationControlParent {
        @Command(
            aliases = {"rotationcontrol", "rotcontrol", "rotcon", "controlrotation", "controlrot", "crot"},
            desc = "Commands for controlling the rotation",
            min = 1,
            max = -1
        )
        @NestedCommand({RotationControlCommands.class})
        public static void rotationcontrol() {
        }
    }

    @Command(
        aliases = {"set", "s"},
        desc = "Sets the current rotation",
        min = 1,
        max = -1
    )
    @CommandPermissions("pgm.rotation.set")
    public static void info(final CommandContext args, final CommandSender sender) throws CommandException {
        RotationManager manager = PGM.getMatchManager().getRotationManager();

        String name = args.getJoinedStrings(0);
        CommandUtils.getRotation(name, sender);

        manager.setCurrentRotationName(name);
        sender.sendMessage(ChatColor.GRAY + "Current rotation set to " + ChatColor.AQUA + name);
    }
}
