package tc.oc.pgm.start;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.CommandUsageException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.CommandSender;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.bukkit.chat.ComponentRenderers;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.PGMTranslations;
import tc.oc.pgm.commands.CommandUtils;

import static tc.oc.commons.bukkit.commands.CommandUtils.*;

public class StartCommands {
    private StartCommands() {}

    @Command(
        aliases = {"start", "begin"},
        desc = "Queues the start of the match in a certain amount of seconds",
        usage = "[countdown time] [huddle time]",
        min = 0,
        max = 2
    )
    @CommandPermissions("pgm.start")
    public static void start(CommandContext args, CommandSender sender) throws CommandException {
        Match match = CommandUtils.getMatch(sender);
        StartMatchModule smm = CommandUtils.getMatchModule(StartMatchModule.class, sender);
        switch(match.matchState()) {
            case Idle:
            case Starting:
            case Huddle:
                if(smm.canStart(true)) {
                    smm.forceStartCountdown(getDuration(args, 0), getDuration(args, 1));
                } else {
                    ComponentRenderers.send(sender, new Component(new TranslatableComponent("command.admin.start.unknownState"), ChatColor.RED));
                    for(UnreadyReason reason : smm.getUnreadyReasons(true)) {
                        ComponentRenderers.send(sender, new Component(ChatColor.RED).text(" * ").extra(reason.getReason()));
                    }
                }
                break;

            case Running:
                throw new CommandException(PGMTranslations.get().t("command.admin.start.matchRunning", sender));

            case Finished:
                throw new CommandException(PGMTranslations.get().t("command.admin.start.matchFinished", sender));

            default:
                throw new CommandException(PGMTranslations.get().t("command.admin.start.unknownState", sender));
        }
    }

    @Command(
        aliases = {"autostart"},
        desc = "Enable or disable match auto-start",
        usage = "[on|off]",
        min = 0,
        max = 1
    )
    @CommandPermissions("pgm.start")
    public static void autostart(CommandContext args, CommandSender sender) throws CommandException {
        StartMatchModule smm = CommandUtils.getMatchModule(StartMatchModule.class, sender);

        boolean autostart;
        if(args.argsLength() >= 1) {
            switch(args.getString(0)) {
                case "on": autostart = true; break;
                case "off": autostart = false; break;
                default: throw new CommandUsageException("Invalid", "[on|off]");
            }
        } else {
            autostart = !smm.isAutoStart();
        }

        smm.setAutoStart(autostart);

        if(autostart) {
            sender.sendMessage(ChatColor.GREEN + PGMTranslations.get().t("command.admin.autoStartEnabled", sender));
            smm.autoStartCountdown();
        } else {
            sender.sendMessage(ChatColor.BLUE + PGMTranslations.get().t("command.admin.autoStartDisabled", sender));
        }
    }
}
