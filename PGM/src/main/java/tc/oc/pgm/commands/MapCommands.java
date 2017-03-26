package tc.oc.pgm.commands;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import com.sk89q.bukkit.util.BukkitWrappedCommandSender;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.CommandSender;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.api.util.Permissions;
import tc.oc.commons.bukkit.chat.BukkitAudiences;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.bukkit.commands.PrettyPaginatedResult;
import tc.oc.commons.bukkit.localization.Translations;
import tc.oc.commons.bukkit.nick.Identity;
import tc.oc.commons.bukkit.nick.UsernameRenderer;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;
import tc.oc.pgm.Config;
import tc.oc.pgm.PGM;
import tc.oc.pgm.PGMTranslations;
import tc.oc.pgm.ffa.FreeForAllModule;
import tc.oc.pgm.map.Contributor;
import tc.oc.pgm.map.MapInfo;
import tc.oc.pgm.map.PGMMap;
import tc.oc.pgm.match.MatchManager;
import tc.oc.pgm.match.Party;
import tc.oc.pgm.modules.InfoModule;
import tc.oc.pgm.rotation.RotationManager;
import tc.oc.pgm.rotation.RotationState;
import tc.oc.pgm.teams.TeamFactory;
import tc.oc.pgm.teams.TeamModule;

public class MapCommands {
    @Command(
        aliases = {"maplist", "maps", "ml"},
        desc = "Shows the maps that are currently loaded",
        usage = "[page]",
        min = 0,
        max = 1,
        help = "Shows all the maps that are currently loaded including ones that are not in the rotation."
    )
    @CommandPermissions("pgm.maplist")
    public static void maplist(CommandContext args, final CommandSender sender) throws CommandException {
        final Set<PGMMap> maps = ImmutableSortedSet.copyOf(new PGMMap.DisplayOrder(), PGM.getMatchManager().getMaps());

        new PrettyPaginatedResult<PGMMap>(PGMTranslations.get().t("command.map.mapList.title", sender)) {
            @Override public String format(PGMMap map, int index) {
                return (index + 1) + ". " + map.getInfo().getShortDescription(sender);
            }
        }.display(new BukkitWrappedCommandSender(sender), maps, args.getInteger(0, 1) /* page */);
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
        final Audience audience = BukkitAudiences.getAudience(sender);
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

        new PrettyPaginatedResult<PGMMap>(PGMTranslations.get().t("command.map.currentRotation.title", sender)) {
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
        Map<String, RotationState> rotations = manager.getRotations();
        int page = args.getFlagInteger('p', 1);

        new PrettyPaginatedResult<String>(PGMTranslations.get().t("command.map.rotationList.title", sender)) {
            @Override public String format(String rotationName, int index) {
                int activation = Config.getConfiguration().getInt("rotation.providers.file." + rotationName + ".count");
                return (index % 2 == 0 ? ChatColor.AQUA : ChatColor.DARK_AQUA) + rotationName + (activation > 0 ? ChatColor.GRAY + " " + PGMTranslations.get().t("command.map.rotationList.activatesWith", sender, ChatColor.RED + "" + activation + ChatColor.GRAY) : "");
            }
        }.display(new BukkitWrappedCommandSender(sender), Lists.newArrayList(rotations.keySet()), page);
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
