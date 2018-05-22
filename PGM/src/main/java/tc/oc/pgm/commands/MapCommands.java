package tc.oc.pgm.commands;

import java.net.URL;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSortedSet;
import com.sk89q.bukkit.util.BukkitWrappedCommandSender;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.CommandSender;
import tc.oc.api.docs.User;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.api.util.Permissions;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.chat.ComponentRenderContext;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.bukkit.chat.UserTextComponent;
import tc.oc.commons.bukkit.commands.PrettyPaginatedResult;
import tc.oc.commons.bukkit.commands.UserFinder;
import tc.oc.commons.bukkit.localization.Translations;
import tc.oc.commons.bukkit.nick.Identity;
import tc.oc.commons.bukkit.nick.IdentityProvider;
import tc.oc.commons.bukkit.nick.UsernameRenderer;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;
import tc.oc.commons.core.commands.CommandFutureCallback;
import tc.oc.commons.core.commands.Commands;
import tc.oc.minecraft.scheduler.SyncExecutor;
import tc.oc.pgm.PGM;
import tc.oc.pgm.PGMTranslations;
import tc.oc.pgm.ffa.FreeForAllModule;
import tc.oc.pgm.map.Contributor;
import tc.oc.pgm.map.MapInfo;
import tc.oc.pgm.map.PGMMap;
import tc.oc.pgm.modules.InfoModule;
import tc.oc.pgm.rotation.RotationManager;
import tc.oc.pgm.rotation.RotationProviderInfo;
import tc.oc.pgm.rotation.RotationState;
import tc.oc.pgm.teams.TeamFactory;

public class MapCommands implements Commands {
    private final UserFinder userFinder;
    private final SyncExecutor syncExecutor;
    private final IdentityProvider identityProvider;
    private final ComponentRenderContext renderer;

    @Inject MapCommands(UserFinder userFinder, IdentityProvider identityProvider, SyncExecutor syncExecutor, ComponentRenderContext renderer) {
        this.userFinder = userFinder;
        this.identityProvider = identityProvider;
        this.syncExecutor = syncExecutor;
        this.renderer = renderer;
    }

    @Command(
        aliases = {"maplist", "maps", "ml"},
        desc = "Shows the maps that are currently loaded",
        usage = "[-a author] [-g gamemode] [page]",
        min = 0,
        max = 2,
        flags = "a:g:",
        help = "Shows all the maps that are currently loaded including ones that are not in the rotation."
    )
    @CommandPermissions("pgm.maplist")
    public void maplist(final CommandContext args, final CommandSender sender) throws CommandException {
        final Identity senderIdentity = identityProvider.createIdentity(sender);
        final MapDoc.Gamemode gamemode = parseGamemode(args.getFlag('g'));
        final String author = args.getFlag('a');
        if(args.getFlag('a') != null) {
            syncExecutor.callback(
                userFinder.findUser(sender, author, UserFinder.Scope.ALL, UserFinder.Default.NULL),
                CommandFutureCallback.onSuccess(sender, result -> {
                    BaseComponent displayMsg = gamemode != null ?
                        new TranslatableComponent("command.map.mapList.displayByBoth",
                            new PlayerComponent(identityProvider.currentIdentity(result.user)),
                            new UserTextComponent(senderIdentity, gamemode.name())) :
                        new TranslatableComponent("command.map.mapList.displayByAuthor",
                            new PlayerComponent(identityProvider.createIdentity(result.user)));
                    displayMaps(sender,
                        displayMsg,
                        getFilteredMaps(gamemode, result.user),
                        args);
                }));
        } else if(gamemode != null) {
            displayMaps(sender,
                new TranslatableComponent("command.map.mapList.displayByGamemode",
                    new UserTextComponent(senderIdentity, gamemode.name())),
                getFilteredMaps(gamemode, null),
                args);
        } else {
            displayMaps(sender,
                new TranslatableComponent("command.map.mapList.title"),
                getFilteredMaps(null, null), args);
        }
    }

    private MapDoc.Gamemode parseGamemode(@Nullable String gamemode) throws CommandException {
        if(gamemode == null) return null;
        try {
            return MapDoc.Gamemode.valueOf(gamemode);
        } catch (IllegalArgumentException e) {
            throw new CommandException("Invalid input " + gamemode);
        }
    }

    /**
     * Returns a list of filtered map with the given parameters.
     * If both parameters are null, this method returns the full set of maps from the repository.
     *
     * @param gamemode Gamemode used to filter the map with - can be null.
     * @param author   An author given from the result of a UserFinder - can be null.
     */
    private Set<PGMMap> getFilteredMaps(@Nullable MapDoc.Gamemode gamemode, @Nullable User author) {
        final Set<PGMMap> maps = ImmutableSortedSet.copyOf(new PGMMap.DisplayOrder(), PGM.get().getMapLibrary().getMaps());
        if(gamemode == null && author == null) return maps;

        return maps.stream().filter(m -> (gamemode == null || m.getDocument().gamemode().contains(gamemode)) &&
            (author == null || m.getDocument().author_uuids().contains(author.uuid())))
            .collect(Collectors.toSet());
    }

    /**
     * Returns a PrettyPaginatedResult of the list of maps given.
     *
     * @param sender CommandSender to display to.
     * @param title  BaseComponent of the title to be provided for display.
     * @param maps   The list of maps to be formatted and displayed.
     * @param args   The arguments of a command.
     */
    private void displayMaps(CommandSender sender, BaseComponent title, Set<PGMMap> maps, CommandContext args) throws CommandException {
        new PrettyPaginatedResult<PGMMap>(renderer.renderLegacy(title, sender)) {
            @Override
            public String format(PGMMap pgmMap, int i) {
                return (i + 1) + ". " + pgmMap.getInfo().getShortDescription(sender);
            }
        }.display(new BukkitWrappedCommandSender(sender), maps, args.getInteger(0, 1) /*page*/);
    }

    private static BaseComponent mapInfoLabel(String key) {
        return new Component(new TranslatableComponent(key), ChatColor.DARK_PURPLE, ChatColor.BOLD).extra(": ");
    }

    @Command(
        aliases = {"mapinfo", "map"},
        desc = "Shows information a certain map",
        usage = "[map name] - defaults to the current map",
        min = 0,
        max = -1,
        help = "Shows information about a map including objective, authors, rules, and more."
    )
    @CommandPermissions("pgm.mapinfo")
    public static List<String> mapinfo(CommandContext args, CommandSender sender) throws CommandException {
        if(args.getSuggestionContext() != null) {
            return CommandUtils.completeMapName(args.getJoinedStrings(0));
        }
        final Audience audience = Audiences.Deprecated.get(sender);
        final PGMMap map;
        if(args.argsLength() > 0) {
            map = CommandUtils.getMap(args.getJoinedStrings(0), sender);
        } else {
            map = CommandUtils.getMatch(sender).getMap();
        }

        final InfoModule infoModule = map.getContext().needModule(InfoModule.class);
        final MapInfo mapInfo = infoModule.getMapInfo();

        audience.sendMessage(mapInfo.getFormattedMapTitle());

        Set<MapDoc.Gamemode> gamemodes = infoModule.getGamemodes();
        if(gamemodes.size() == 1) {
            audience.sendMessage(new Component(
                mapInfoLabel("command.map.mapInfo.gamemode.singular"),
                new Component(new TranslatableComponent(Translations.gamemodeLongName(gamemodes.iterator().next())), ChatColor.GOLD)
            ));
        } else if(!gamemodes.isEmpty()) {
            audience.sendMessage(new Component(
                mapInfoLabel("command.map.mapInfo.gamemode.plural"),
                new Component(Components.join(
                    new Component(" "),
                    Collections2.transform(gamemodes, gamemode -> new TranslatableComponent(Translations.gamemodeShortName(gamemode)))
                ), ChatColor.GOLD)
            ));
        }

        Component edition = new Component(
            mapInfoLabel("command.map.mapInfo.edition"),
            new Component(mapInfo.getLocalizedEdition(), ChatColor.GOLD)
        );

        if(mapInfo.phase() == MapDoc.Phase.DEVELOPMENT) {
            edition.extra(new Component(" (", ChatColor.DARK_GRAY).extra(new TranslatableComponent("map.phase.development")).extra(")"));
        }

        audience.sendMessage(edition);

        audience.sendMessage(new Component(
            mapInfoLabel("command.map.mapInfo.objective"),
            new Component(mapInfo.objective, ChatColor.GOLD)
        ));

        List<Contributor> authors = mapInfo.getNamedAuthors();
        if(authors.size() == 1) {
            audience.sendMessage(new Component(
                mapInfoLabel("command.map.mapInfo.authorSingular"),
                formatContribution(authors.get(0))
            ));
        } else if(!authors.isEmpty()) {
            audience.sendMessage(mapInfoLabel("command.map.mapInfo.authorPlural"));
            for(Contributor author : authors) {
                audience.sendMessage(new Component("  ").extra(formatContribution(author)));
            }
        }

        List<Contributor> contributors = mapInfo.getNamedContributors();
        if(!contributors.isEmpty()) {
            audience.sendMessage(mapInfoLabel("command.map.mapInfo.contributors"));
            for(Contributor contributor : contributors) {
                audience.sendMessage(new Component("  ").extra(formatContribution(contributor)));
            }
        }

        if(mapInfo.rules.size() > 0) {
            audience.sendMessage(mapInfoLabel("command.map.mapInfo.rules"));

            for(int i = 0; i < mapInfo.rules.size(); i++) {
                audience.sendMessage(new Component(
                    new Component((i + 1) + ") ", ChatColor.WHITE),
                    new Component(mapInfo.rules.get(i), ChatColor.GOLD)
                ));
            }
        }

        int maxPlayers = map.getContext()
                            .features()
                            .all(TeamFactory.class)
                            .mapToInt(TeamFactory::getMaxPlayers)
                            .sum();

        FreeForAllModule ffam = map.getContext().getModule(FreeForAllModule.class);
        if(ffam != null) {
            maxPlayers += ffam.getOptions().maxPlayers;
        }

        audience.sendMessage(new Component(
            mapInfoLabel("command.map.mapInfo.playerLimit"),
            new Component(String.valueOf(maxPlayers), ChatColor.GOLD)
        ));

        if(sender.hasPermission(Permissions.MAPDEV)) {
            audience.sendMessage(new Component(mapInfoLabel("command.map.mapInfo.genre"), new Component(mapInfo.getLocalizedGenre(), ChatColor.GOLD)));
            audience.sendMessage(new Component(mapInfoLabel("command.map.mapInfo.proto"), new Component(map.getContext().getProto().toString(), ChatColor.GOLD)));
            audience.sendMessage(new Component(mapInfoLabel("command.map.mapInfo.folder"), new Component(map.getFolder().getRelativePath().toString(), ChatColor.GOLD)));
            audience.sendMessage(new Component(mapInfoLabel("command.map.mapInfo.source"), new Component(map.getSource().getPath().toString(), ChatColor.GOLD)));
        }

        URL xmlLink = map.getFolder().getDescriptionFileUrl();
        if(xmlLink != null) {
            audience.sendMessage(new Component(
                new Component(ChatColor.DARK_PURPLE, ChatColor.BOLD)
                    .extra(new TranslatableComponent("command.map.mapInfo.xml"))
                    .extra(": "),
                ((Component) Components.link(xmlLink))
                    .hoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        new TranslatableComponent("command.map.mapInfo.sourceCode.tip")
                    )

            ));
        }

        return null;
    }

    @Command(
        aliases = {"rotation", "rot", "rota", "maprot", "maprotation"},
        desc = "Shows the current map rotation",
        usage= "[-n name] [page]",
        flags = "n:",
        min = 0,
        max = 1
    )
    @CommandPermissions("pgm.rotation.view")
    public static void rotation(CommandContext args, final CommandSender sender) throws CommandException {
        final RotationState rotation = CommandUtils.getRotation(args.getFlag('n'), sender);
        int page = args.getInteger(0, 1);

        String header = PGMTranslations.get().t("command.map.currentRotation.title", sender);
        String name = PGM.getMatchManager().getRotationManager().getCurrentRotationName();
        if(!name.equalsIgnoreCase("default")) {
            header += " (" + ChatColor.DARK_AQUA + name + ChatColor.RESET + ")";
        }

        new PrettyPaginatedResult<PGMMap>(header) {
            @Override public String format(PGMMap map, int index) {
                ChatColor color = index == rotation.getNextId() ? ChatColor.DARK_AQUA : ChatColor.WHITE;
                return color.toString() + (index + 1) + ". " + map.getInfo().getShortDescription(sender);
            }
        }.display(new BukkitWrappedCommandSender(sender), rotation.getMaps(), page);
    }

    @Command(
        aliases = {"rotations", "rots", "maprotations", "maprots"},
        desc = "Shows the available map rotations",
        usage = "[-p page]",
        flags = "p:",
        min = 0,
        max = 0
    )
    @CommandPermissions("pgm.rotation.list")
    public static void rotations(final CommandContext args, final CommandSender sender) throws CommandException {
        RotationManager manager = PGM.getMatchManager().getRotationManager();
        List<RotationProviderInfo> rotations = manager.getProviders();
        int page = args.getFlagInteger('p', 1);

        new PrettyPaginatedResult<RotationProviderInfo>(PGMTranslations.get().t("command.map.rotationList.title", sender)) {
            @Override public String format(RotationProviderInfo rotationInfo, int index) {
                boolean current = manager.getCurrentRotationName().equals(rotationInfo.name);
                int count = rotationInfo.count;
                return (current ? ChatColor.GOLD : ChatColor.GRAY) + " \u25ba " +
                        (index % 2 == 0 ? ChatColor.AQUA : ChatColor.DARK_AQUA) + rotationInfo.name +
                        (count > 0 ? ChatColor.GRAY + " " +
                                PGMTranslations.get().t("command.map.rotationList.activatesWith", sender,
                                        ChatColor.RED + "" + count + ChatColor.GRAY) : "");
            }
        }.display(new BukkitWrappedCommandSender(sender), rotations, page);
    }

    @Command(
        aliases = {"mapnext", "mn", "nextmap", "nm", "next"},
        desc = "Shows which map is coming up next",
        min = 0,
        max = 0
    )
    @CommandPermissions("pgm.mapnext")
    public static void mapnext(CommandContext args, CommandSender sender) throws CommandException {
        PGMMap next = PGM.getMatchManager().getNextMap();
        sender.sendMessage(ChatColor.DARK_PURPLE + PGMTranslations.get().t("command.map.next.success", sender, next.getInfo().getShortDescription(sender) + ChatColor.DARK_PURPLE));
    }

    private static @Nullable BaseComponent formatContribution(Contributor contributor) {
        BaseComponent c = formatContributor(contributor);
        if(c == null || !contributor.hasContribution()) return c;
        return new Component(c, new Component(ChatColor.GRAY, ChatColor.ITALIC).extra(" - ").extra(contributor.getContribution()));
    }

    private static @Nullable BaseComponent formatContributor(Contributor contributor) {
        Identity identity = contributor.getIdentity();
        if(identity != null) {
            return new PlayerComponent(identity, NameStyle.MAPMAKER);
        }

        String name = contributor.getName();
        if(name != null) {
            return new Component(name, UsernameRenderer.OFFLINE_COLOR);
        }

        return null;
    }
}
