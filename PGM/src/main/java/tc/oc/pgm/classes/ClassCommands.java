package tc.oc.pgm.classes;

import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.sk89q.minecraft.util.commands.*;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.docs.User;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.commands.TranslatableCommandException;
import tc.oc.commons.core.formatting.StringUtils;
import tc.oc.pgm.*;
import tc.oc.pgm.commands.CommandUtils;
import tc.oc.pgm.match.MatchFinder;
import tc.oc.pgm.match.MatchPlayer;

@Singleton
public class ClassCommands implements Commands {

    private final MatchFinder matchFinder;
    private final BukkitUserStore userStore;

    @Inject private ClassCommands(MatchFinder matchFinder, BukkitUserStore userStore) {
        this.matchFinder = matchFinder;
        this.userStore = userStore;
    }

    @Command(
        aliases = { "class", "selectclass", "c", "cl", "kit", "klasse" },
        desc = "Selects or views the player class",
        min = 0,
        max = -1
    )
    @CommandPermissions("pgm.class")
    public void selectclass(CommandContext args, CommandSender sender) throws CommandException {
        ClassMatchModule classModule = getClassModule(sender);

        MatchPlayer player = CommandUtils.senderToMatchPlayer(sender);
        final User user = userStore.getUser(player.getBukkit());
        PlayerClass cls = classModule.selectedClass(user);

        if(args.argsLength() == 0) {
            // show current class
            sender.sendMessage(ChatColor.GREEN + PGMTranslations.t("command.class.view.currentClass", player) + " " + ChatColor.GOLD + ChatColor.UNDERLINE + cls.getName());
            sender.sendMessage(ChatColor.DARK_PURPLE + PGMTranslations.t("command.class.view.list", player).replace("'/classes'", ChatColor.GOLD + "'/classes'" + ChatColor.DARK_PURPLE));
        } else {
            if(!sender.hasPermission("pgm.class.select")) {
                throw new CommandPermissionsException();
            }

            String search = args.getJoinedStrings(0);
            PlayerClass result = StringUtils.bestFuzzyMatch(search, classModule.getClasses(), 0.9);
            if(result == null) {
                throw new CommandException(PGMTranslations.t("command.class.select.classNotFound", player));
            }

            if(!cls.canUse(player.getBukkit())) {
                throw new CommandException(PGMTranslations.t("command.class.restricted", player, ChatColor.GOLD, result.getName(), ChatColor.RED));
            }

            try {
                classModule.setPlayerClass(user, result);
            } catch (IllegalStateException e) {
                throw new CommandException(PGMTranslations.t("command.class.stickyClass", player));
            }

            sender.sendMessage(ChatColor.GREEN + PGMTranslations.t("command.class.select.confirm", player, ChatColor.GOLD.toString() + ChatColor.UNDERLINE + result.getName() + ChatColor.GREEN));
            if(player.isParticipating()) {
                sender.sendMessage(ChatColor.GREEN + PGMTranslations.t("command.class.select.nextSpawn", player));
            }
        }
    }

    @Command(
        aliases = { "classlist", "classes", "listclasses", "cls", "kits" },
        desc = "Lists the classes available on this map",
        min = 0,
        max = 1
    )
    @CommandPermissions("pgm.class.list")
    public void listclasses(CommandContext args, CommandSender sender) throws CommandException {
        Player bukkit = tc.oc.commons.bukkit.commands.CommandUtils.senderToPlayer(sender);
        ClassMatchModule classModule = getClassModule(bukkit);

        final PlayerClass senderClass = classModule.selectedClass(userStore.getUser(bukkit));

        sender.sendMessage(StringUtils.dashedChatMessage(ChatColor.GOLD + PGMTranslations.get().t("command.class.list.title", sender), "-", ChatColor.RED.toString()));
        int i = 1;
        boolean doesntHave = false;
        for(PlayerClass cls : classModule.getClasses()) {
            StringBuilder result = new StringBuilder();

            result.append(i++).append(". ");

            if(Objects.equals(cls, senderClass)) {
                result.append(ChatColor.GOLD);
            } else if(cls.canUse(bukkit)) {
                result.append(ChatColor.GREEN);
            } else {
                result.append(ChatColor.RED);
                doesntHave = true;
            }

            if(Objects.equals(cls, senderClass)) result.append(ChatColor.UNDERLINE);
            result.append(cls.getName());

            if(cls.getDescription() != null) {
                result.append(ChatColor.DARK_PURPLE).append(" - ").append(ChatColor.RESET).append(cls.getDescription());
            }

            sender.sendMessage(result.toString());
        }
    }

    private ClassMatchModule getClassModule(CommandSender sender) throws CommandException {
        return matchFinder.needMatch(sender)
                          .module(ClassMatchModule.class)
                          .orElseThrow(() -> new TranslatableCommandException("command.class.notEnabled"));
    }
}
