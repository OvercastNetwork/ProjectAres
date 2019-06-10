package tc.oc.commons.bukkit.commands;

import com.google.common.collect.Sets;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.inject.Inject;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.docs.virtual.UserDoc;
import tc.oc.api.users.UserService;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.chat.HeaderComponent;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.bukkit.nick.IdentityProvider;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.concurrent.Flexecutor;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.commons.core.util.Streams;
import tc.oc.minecraft.protocol.MinecraftVersion;
import tc.oc.minecraft.scheduler.Sync;

/**
 * Commands for miscellaneous purposes.
 */
public class MiscCommands implements Commands {

    private static final Random RANDOM = new Random();

    private final Flexecutor flexecutor;
    private final UserService userService;
    private final BukkitUserStore userStore;
    private final UserFinder userFinder;
    private final IdentityProvider identityProvider;
    private final Audiences audiences;

    @Inject MiscCommands(@Sync Flexecutor flexecutor, UserService userService, BukkitUserStore userStore, UserFinder userFinder, IdentityProvider identityProvider, Audiences audiences) {
        this.flexecutor = flexecutor;
        this.userService = userService;
        this.userStore = userStore;
        this.userFinder = userFinder;
        this.identityProvider = identityProvider;
        this.audiences = audiences;
    }

    @Command(
            aliases = { "playerversion", "pv" },
            desc = "Shows statics on what version players online are using",
            flags = "ad",
            min = 0,
            max = 1
    )
    @CommandPermissions("ocn.version")
    public void listPlayerVersions(final CommandContext args, final CommandSender sender) throws CommandException {
        Audience audience = audiences.get(sender);
        if (args.hasFlag('a')) {
            Map<String, Integer> playerCountVersionMap = new HashMap<>();
            userStore.stream().forEach(player -> {
                String version = MinecraftVersion.describeProtocol(player.getProtocolVersion(), !args.hasFlag('d'));
                playerCountVersionMap.put(version, playerCountVersionMap.getOrDefault(version, 0) + 1);
            });

            audience.sendMessage(new HeaderComponent(new Component(ChatColor.AQUA).translate("list.player.versions.title")));
            for (Map.Entry<String, Integer> entry : playerCountVersionMap.entrySet()) {
                audience.sendMessage(new TranslatableComponent("list.player.versions.message." + (entry.getValue() == 1 ? "singular" : "plural"),
                        ChatColor.AQUA + entry.getValue().toString(),
                        ChatColor.AQUA + entry.getKey(),
                        String.format("%.1f", 100 * entry.getValue() / (double) userStore.count()) + "%"));
            }
        } else {
            Player player = CommandUtils.getPlayerOrSelf(args, sender, 0);
            audience.sendMessage(new TranslatableComponent("list.player.version.singular.message", new PlayerComponent(identityProvider.createIdentity(player)), ChatColor.AQUA + MinecraftVersion.describeProtocol(player.getProtocolVersion(), false)));
        }
    }

    @Command(
            aliases = { "playerlocale", "locale" },
            desc = "Shows statics on what locale players online are in",
            flags = "a",
            min = 0,
            max = 1
    )
    @CommandPermissions("ocn.locale")
    public void listPlayerLocales(final CommandContext args, final CommandSender sender) throws CommandException {
        Audience audience = audiences.get(sender);
        if (args.hasFlag('a')) {
            Map<String, Long> playerLocaleMap = userStore.stream().collect(java.util.stream.Collectors.groupingBy(Player::getLocale, java.util.stream.Collectors.counting()));

            audience.sendMessage(new HeaderComponent(new Component(ChatColor.AQUA).translate("list.player.locales.title")));
            for (Map.Entry<String, Long> entry : playerLocaleMap.entrySet()) {
                audience.sendMessage(new TranslatableComponent("list.player.locales.message." + (entry.getValue() == 1 ? "singular" : "plural"),
                        ChatColor.AQUA + entry.getValue().toString(),
                        ChatColor.AQUA + entry.getKey(),
                        String.format("%.1f", 100 * entry.getValue() / (double) userStore.count()) + "%"));
            }
        } else {
            Player player = CommandUtils.getPlayerOrSelf(args, sender, 0);
            audience.sendMessage(new TranslatableComponent("list.player.locale.singular.message", new PlayerComponent(identityProvider.createIdentity(player)), ChatColor.AQUA + player.getLocale()));
        }
    }

    @Command(
            aliases = { "coinflip" },
            desc = "Flip a Coin",
            flags = "b",
            min = 0,
            max = 0
    )
    @CommandPermissions("coinflip")
    public void coinFlip(final CommandContext args, final CommandSender sender) throws CommandException {
        if (args.hasFlag('b')) {
            Bukkit.broadcastMessage(ChatColor.AQUA + (Math.random() < 0.5 ? "Heads" : "Tails"));
        } else {
            sender.sendMessage(ChatColor.AQUA + (Math.random() < 0.5 ? "Heads" : "Tails"));
        }
    }

    @Command(
            aliases = { "togglegravity" },
            usage = "<player>",
            desc = "Toggle a player's gravity.",
            min = 0,
            max = 1
    )
    @CommandPermissions("togglegravity")
    public void noGravity(final CommandContext args, final CommandSender sender) throws CommandException {
        Player player = CommandUtils.getPlayerOrSelf(args, sender, 0);
        player.setGravity(!player.hasGravity());
    }

    @Command(
            aliases = { "join-friend-tokens" },
            usage = "<player> <concurrent> <limit>",
            desc = "Change the join friend tokens limit for a premium player",
            min = 3
    )
    public void joinFriend(final CommandContext args, final CommandSender sender) throws CommandException {
        if(!(sender instanceof ConsoleCommandSender)) throw new CommandPermissionsException();
        int concurrent = args.getInteger(1, 1);
        int limit = args.getInteger(2, 3);
        flexecutor.callback(
            userFinder.findLocalPlayer(sender, args, 0),
            response -> {
                userService.update(response.user, new UserDoc.FriendTokens() {
                    @Override
                    public int friend_tokens_limit() {
                        return limit;
                    }

                    @Override
                    public int friend_tokens_concurrent() {
                        return concurrent;
                    }
                });
            }
        );
    }

    @Command(
            aliases = { "change-death-screen" },
            usage = "<player> <+1/-1>",
            desc = "Allow a player to change their death screen",
            min = 2
    )
    public void deathScreen(final CommandContext args, final CommandSender sender) throws CommandException {
        if(!(sender instanceof ConsoleCommandSender)) throw new CommandPermissionsException();
        boolean enable = args.getInteger(1, +1) > 0;
        flexecutor.callback(
            userFinder.findLocalPlayer(sender, args, 0),
            response -> {
                if((response.user.death_screen() == null) != enable) {
                    userService.update(response.user, (UserDoc.DeathScreen) () -> enable ? "default" : null);
                }
            }
        );
    }

    @Command(
        aliases = { "vice" },
        desc = "WELCOME BACK VICE!"
    )
    public void vice(final CommandContext args, final CommandSender sender) throws CommandException {
        Player player = CommandUtils.senderToPlayer(sender);
        UUID vice = UUID.fromString("bf331953-4f92-43ee-8abc-7544b8234936");
        if (!(player.isOp() || player.getUniqueId().equals(vice))) throw new CommandPermissionsException();
        Set<Location> fireworkLocs = Sets.newHashSet();
        Location center = player.getLocation();
        if (!player.getUniqueId().equals(vice) && sender.getServer().getPlayer(vice) != null)
            center = sender.getServer().getPlayer(vice).getLocation();
        center = center.clone();
        fireworkLocs.add(center);
        int radius = 5;
        for (int i = 0 - radius; i <= radius; i = i + (radius / 2)) {
            if (i == 0) continue;
            fireworkLocs.add(center.clone().add(i, 0, 0));
            fireworkLocs.add(center.clone().add(0, 0, i));
            fireworkLocs.add(center.clone().add(i, 0, i));
        }
        for (Location location : fireworkLocs) {
            FireworkMeta meta = (FireworkMeta) Bukkit.getItemFactory().getItemMeta(Material.FIREWORK);
            meta.setPower(RANDOM.nextInt(15));
            meta.addEffect(randomRGBEffect());

            Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
            firework.setFireworkMeta(meta);
        }
        sender.getServer().broadcast(new TextComponent(
            ChatColor.RED + "WELCOME " +
            ChatColor.YELLOW + "BACK " +
            ChatColor.GREEN + "VICE" +
            ChatColor.BLUE + "!!")
        );
    }

    private FireworkEffect randomRGBEffect() {
        return FireworkEffect.builder()
            .flicker(RANDOM.nextBoolean())
            .trail(true)
            .with(Type.values()[RANDOM.nextInt(4)])
            .withColor(Color.fromRGB(RANDOM.nextInt(255), RANDOM.nextInt(255), RANDOM.nextInt(255)))
            .withColor(Color.fromRGB(RANDOM.nextInt(255), RANDOM.nextInt(255), RANDOM.nextInt(255)))
            .withColor(Color.fromRGB(RANDOM.nextInt(255), RANDOM.nextInt(255), RANDOM.nextInt(255)))
            .withFade(Color.fromRGB(RANDOM.nextInt(255), RANDOM.nextInt(255), RANDOM.nextInt(255)))
            .build();
    }

    @Command(
            aliases = { "sudo" },
            usage = "<player> [command... (rand|mode|near|color|*)=value]",
            desc = "Run a command as console or another player",
            flags = "cd",
            anyFlags = true,
            min = 1,
            max = -1
    )
    @CommandPermissions("sudo")
    public void sudo(final CommandContext args, final CommandSender sender) throws CommandException {
        Server server = sender.getServer();
        int index = 1;
        CommandSender other = userStore.find(args.getString(0, ""));
        if(other == null) {
            other = args.hasFlag('c') ? server.getConsoleSender() : sender;
            index = 0;
        }
        if(!sender.equals(other) && !sender.hasPermission("sudo.others")) {
            throw new CommandPermissionsException();
        }
        String command = args.getJoinedStrings(index);
        List<String> commands = getPermutations(sender, command);
        String explanation;
        if(commands.size() == 1) {
            explanation = "/" + commands.get(0);
        } else {
            explanation = commands.size() + ChatColor.WHITE.toString() + " commands";
        }
        sender.sendMessage("Executing " + ChatColor.AQUA + explanation + ChatColor.WHITE + " as " + identityProvider.currentIdentity(other).getName(sender));
        for(String cmd : commands) {
            if(commands.size() > 1 && args.hasFlag('d')) {
                sender.sendMessage(" > " + cmd);
            }
            server.dispatchCommand(other, cmd);
        }
    }

    public List<String> getPermutations(CommandSender sender, String command) throws CommandException {
        List<String> permutations = new ArrayList<>();
        getPermutations(sender, command, permutations);
        return permutations;
    }

    public void getPermutations(CommandSender sender, String command, List<String> commands) throws CommandException {
        Matcher matcher = Pattern.compile("\\*|[A-Za-z]{1,}=[A-Za-z0-9_-]{1,}").matcher(command);
        if(matcher.find()) {
            String keyValue = matcher.group();
            for(String name : getPlayers(sender, keyValue).map(player -> player.getName(sender)).collect(Collectors.toImmutableList())) {
                getPermutations(sender, matcher.replaceFirst(name), commands);
            }
        } else {
            commands.add(command);
        }
    }

    public Stream<Player> getPlayers(CommandSender sender, String keyValue) throws CommandException {
        Stream<Player> players = userStore.stream();
        int seperator = keyValue.indexOf("=");
        String key = seperator != -1 ? keyValue.substring(0, seperator) : keyValue;
        String value = seperator != -1 ? keyValue.substring(seperator + 1, keyValue.length()) : "";
        int parsed;
        try {
            parsed = Integer.parseInt(value);
        } catch(NumberFormatException nfe) {
            parsed = -1;
        }
        int valueInt = parsed;
        switch(key) {
            case "rand":
                return players.collect(Collectors.toRandomSubList(valueInt)).stream();
            case "mode":
                return players.filter(p -> p.getGameMode().getValue() == valueInt);
            case "near":
                Location location = CommandUtils.senderToPlayer(sender).getLocation();
                return players.filter(p -> p.getLocation().distance(location) <= valueInt);
            case "color":
                ChatColor color = CommandUtils.getEnum(value, sender, ChatColor.class, ChatColor.WHITE);
                return players.filter(p -> getFuzzyColor(p).equals(color));
            case "perm":
                return players.filter(p -> p.hasPermission(value));
            case "*":
                return Streams.shuffle(players);
            default:
                throw new CommandException("Unrecognized player filter '" + key + "'");
        }
    }

    public ChatColor getFuzzyColor(CommandSender sender) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            Matcher matcher = ChatColor.STRIP_COLOR_PATTERN.matcher(player.getDisplayName(sender));
            String color = null;
            while(matcher.find()) {
                color = matcher.group();
            }
            if(color != null) {
                return ChatColor.getByChar(color.charAt(1));
            }
        }
        return ChatColor.WHITE;
    }

}
