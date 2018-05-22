package tc.oc.commons.bukkit.commands;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.Server;
import tc.oc.commons.bukkit.chat.ComponentRenderers;
import tc.oc.commons.bukkit.chat.ListComponent;
import tc.oc.commons.bukkit.localization.Translations;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.commands.TranslatableCommandException;
import tc.oc.commons.core.formatting.StringUtils;
import tc.oc.commons.core.util.TimeUtils;

public abstract class CommandUtils {

    public static final String CONSOLE_DISPLAY_NAME = ChatColor.GOLD + "❖" + ChatColor.AQUA + "Console";
    public static final BaseComponent CONSOLE_COMPONENT_NAME = new Component(new Component("❖").color(net.md_5.bungee.api.ChatColor.GOLD),
                                                                             new Component("Console").color(net.md_5.bungee.api.ChatColor.AQUA));

    public static Optional<String> flag(CommandContext args, char flag) {
        return Optional.ofNullable(args.getFlag(flag));
    }

    public static CommandException newCommandException(CommandSender sender, BaseComponent message) {
        return new CommandException(ComponentRenderers.toLegacyText(message, sender));
    }

    public static Player senderToPlayer(CommandSender sender) throws CommandException {
        if(sender instanceof Player) {
            return (Player) sender;
        } else {
            throw new CommandException(ComponentRenderers.toLegacyText(new TranslatableComponent("command.onlyPlayers"), sender));
        }
    }

    /**
     * Get an online {@link Player} by exact name
     */
    public static Player getPlayer(CommandContext args, CommandSender sender, int index) throws CommandException {
        if(args.argsLength() > index) {
            Player player = sender.getServer().getPlayerExact(args.getString(index), sender);
            if(player == null) throw new CommandException(ComponentRenderers.toLegacyText(new TranslatableComponent("command.playerNotFound"), sender));
            return player;
        } else {
            throw new CommandException(ComponentRenderers.toLegacyText(new TranslatableComponent("command.specifyPlayer"), sender));
        }
    }

    /**
     * Get an online {@link Player} by exact name, defaulting to sender
     */
    public static Player getPlayerOrSelf(CommandContext args, CommandSender sender, int index) throws CommandException {
        return senderToPlayer(getCommandSenderOrSelf(args, sender, index));
    }

    /**
     * Get an online {@link CommandSender} by exact name, defaulting to sender
     */
    public static CommandSender getCommandSenderOrSelf(CommandContext args, CommandSender sender, int index) throws CommandException {
        if(args.argsLength() > index) {
            Player player = sender.getServer().getPlayerExact(args.getString(index), sender);
            if(player == null) throw new CommandException(ComponentRenderers.toLegacyText(new TranslatableComponent("command.playerNotFound"), sender));
            return player;
        } else {
            return sender;
        }
    }

    /**
     * Get an online {@link Player} by partial name.
     * @throws CommandException if the name does not match any player, or matches multiple players
     */
    public static Player findOnlinePlayer(CommandContext args, CommandSender sender, int index) throws CommandException {
        if(args.argsLength() > index) {
            String name = args.getString(index);
            List<Player> players = sender.getServer().matchPlayer(name, sender);
            switch(players.size()) {
                case 0: throw new CommandException(Translations.get().t("command.playerNotFound", sender));
                case 1: return players.get(0);
                default: throw new CommandException(Translations.get().t("command.multiplePlayersFound", sender));
            }
        } else {
            throw new CommandException(Translations.get().t("command.specifyPlayer", sender));
        }
    }

    public static void assertPermission(Permissible permissible, String permission) throws CommandPermissionsException {
        if(!permissible.hasPermission(permission)) {
            throw new CommandPermissionsException();
        }
    }

    public static void assertPermission(Permissible permissible, Permission permission) throws CommandPermissionsException {
        if(!permissible.hasPermission(permission)) {
            throw new CommandPermissionsException();
        }
    }

    public static int getInteger(CommandContext args, CommandSender sender, int index, int def) throws CommandException {
        try {
            return args.getInteger(index, def);
        }
        catch(NumberFormatException e) {
            throw new CommandException(ComponentRenderers.toLegacyText(new TranslatableComponent("command.error.invalidNumber", args.getString(index)), sender));
        }
    }

    public static @Nullable Duration getDuration(CommandContext args, int index) throws CommandException {
        return getDuration(args, index, null);
    }

    public static Duration getDuration(CommandContext args, int index, Duration def) throws CommandException {
        return getDuration(args.getString(index, null), def);
    }

    public static @Nullable Duration getDuration(String text) throws CommandException {
        return getDuration(text, null);
    }

    public static Duration getDuration(String text, Duration def) throws CommandException {
        if(text == null) {
            return def;
        } else {
            try {
                return TimeUtils.parseDuration(text);
            } catch(DateTimeParseException e) {
                throw new TranslatableCommandException("command.error.invalidTimePeriod", text);
            }
        }
    }

    public static @Nullable <E extends Enum<E>> E getEnum(CommandContext args, CommandSender sender, int index, Class<E> type) throws CommandException {
        return getEnum(args, sender, index, type, null);
    }

    public static <E extends Enum<E>> E getEnum(CommandContext args, CommandSender sender, int index, Class<E> type, E def) throws CommandException {
        return getEnum(args.getString(index, null), sender, type, def);
    }

    public static <E extends Enum<E>> E getEnum(String text, CommandSender sender, Class<E> type, E def) throws CommandException {
        if(text == null) {
            return def;
        } else {
            try {
                return Enum.valueOf(type, text.toUpperCase().replace(' ', '_'));
            } catch(IllegalArgumentException e) {
                throw newCommandException(sender, new TranslatableComponent("command.error.invalidEnum", text));
            }
        }
    }

    public static String getDisplayName(CommandSender target) {
        return getDisplayName(target, null);
    }

    public static String getDisplayName(CommandSender target, CommandSender viewer) {
        if(target instanceof Player) {
            return ((Player) target).getDisplayName(viewer);
        } else {
            return CONSOLE_DISPLAY_NAME;
        }
    }

    public static String getDisplayName(@Nullable PlayerId target) {
        return getDisplayName(target, null);
    }

    public static String getDisplayName(@Nullable PlayerId target, CommandSender viewer) {
        if(target == null) {
            return CONSOLE_DISPLAY_NAME;
        } else {
            Player targetPlayer = Bukkit.getPlayerExact(target.username(), viewer);
            if(targetPlayer == null) {
                return ChatColor.DARK_AQUA + target.username();
            } else {
                return targetPlayer.getDisplayName(viewer);
            }
        }
    }

    public static String getDisplayName(@Nullable String username) {
        return getDisplayName(username, null);
    }

    public static String getDisplayName(@Nullable String username, CommandSender viewer) {
        if(username == null || username.trim().length() == 0 || username.trim().equalsIgnoreCase("CONSOLE")) {
            return CONSOLE_DISPLAY_NAME;
        } else {
            Player targetPlayer = Bukkit.getPlayerExact(username, viewer);
            if(targetPlayer == null) {
                return ChatColor.DARK_AQUA + username;
            } else {
                return targetPlayer.getDisplayName(viewer);
            }
        }
    }

    public static String formatServerPrefix(Server server) {
        return ChatColor.WHITE + "[" +
               ChatColor.GOLD + server.name() +
               ChatColor.WHITE + "]";
    }

    public static void notEnoughArguments(CommandSender sender) throws CommandException {
        throw new CommandException(Translations.get().t("command.error.notEnoughArguments", sender));
    }

    public static <E extends Enum> Map<String, E> enumChoices(Class<E> enumClass) {
        return Stream.of(enumClass.getEnumConstants())
                .collect(Collectors.toMap(e -> e.name().toLowerCase().replaceAll("_", "-"), Function.identity()));
    }

    public static <E extends Enum> List<String> enumChoicesList(Class<E> enumClass) {
        return new ArrayList<>(enumChoices(enumClass).keySet());
    }

    public static @Nullable <E extends Enum> E tryEnum(String text, Class<E> enumClass) {
        return StringUtils.bestFuzzyMatch(text, enumChoices(enumClass), 0.8);
    }

    public static <E extends Enum> E tryEnum(String text, Class<E> enumClass, E def) {
        final E option = tryEnum(text, enumClass);
        return option == null ? def : option;
    }

    public static <E extends Enum> E getEnum(String text, Class<E> enumClass) throws CommandException {
        final E option = tryEnum(text, enumClass);
        if(option != null) {
            return option;
        } else {
            throw new TranslatableCommandException("command.error.invalidOption", text, new ListComponent(enumChoicesList(enumClass), TextComponent::new));
        }
    }

    public static <E extends Enum> List<String> completeEnum(String prefix, Class<E> enumClass) {
        return StringUtils.complete(prefix, enumChoicesList(enumClass));
    }
}
