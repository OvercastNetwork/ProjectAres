package tc.oc.pgm.development;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.sk89q.bukkit.util.BukkitWrappedCommandSender;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.apache.commons.io.FileUtils;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jdom2.Element;
import java.time.Duration;
import tc.oc.api.docs.PlayerId;
import tc.oc.minecraft.scheduler.SyncExecutor;
import tc.oc.api.util.Permissions;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.commands.CommandUtils;
import tc.oc.commons.bukkit.commands.PrettyPaginatedResult;
import tc.oc.commons.bukkit.util.BukkitUtils;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.commands.CommandFutureCallback;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.inspect.Inspection;
import tc.oc.commons.core.inspect.MultiLineTextInspector;
import tc.oc.pgm.PGMTranslations;
import tc.oc.pgm.features.Feature;
import tc.oc.pgm.features.FeatureDefinition;
import tc.oc.pgm.features.FeatureProxy;
import tc.oc.pgm.features.SluggedFeature;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.map.MapConfiguration;
import tc.oc.pgm.map.MapDefinition;
import tc.oc.pgm.map.MapLibrary;
import tc.oc.pgm.map.MapLogRecord;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.map.MapNotFoundException;
import tc.oc.pgm.map.PGMMap;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchManager;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.MatchState;
import tc.oc.pgm.physics.AccelerationPlayerFacet;
import tc.oc.pgm.physics.DebugVelocityPlayerFacet;
import tc.oc.pgm.physics.PlayerForce;
import tc.oc.pgm.xml.Node;

public class MapDevelopmentCommands implements Commands {

    private final MapErrorTracker mapErrorTracker;
    private final Map<String, Boolean> mapEnvironment;
    private final MapLibrary mapLibrary;
    private final MatchManager matchManager;
    private final Audiences audiences;
    private final SyncExecutor syncExecutor;

    @Inject MapDevelopmentCommands(MapErrorTracker mapErrorTracker, MapConfiguration mapConfiguration, MapLibrary mapLibrary, MatchManager matchManager, Audiences audiences, SyncExecutor syncExecutor) {
        this.mapErrorTracker = mapErrorTracker;
        this.mapEnvironment = mapConfiguration.environment();
        this.mapLibrary = mapLibrary;
        this.matchManager = matchManager;
        this.audiences = audiences;
        this.syncExecutor = syncExecutor;
    }

    @Command(
        aliases = {"clearerrors", "clearxmlerrors"},
        desc = "Clears XML errors"
    )
    @CommandPermissions(Permissions.MAPERRORS)
    public void clearErrorsCommand(CommandContext args, CommandSender sender) throws CommandException {
        mapErrorTracker.clearAllErrors();
        sender.sendMessage(ChatColor.GREEN + PGMTranslations.get().t("command.development.clearErrors.success", sender));
    }

    @Command(
        aliases = {"errors", "xmlerrors"},
        usage = "[-p page] [map name]",
        desc = "Reads back XML errors",
        min = 0,
        max = -1,
        flags = "p:"
    )
    @CommandPermissions(Permissions.MAPERRORS)
    public List<String> errorsCommand(CommandContext args, final CommandSender sender) throws CommandException {
        final String mapName = args.argsLength() > 0 ? args.getJoinedStrings(0) : "";
        if(args.getSuggestionContext() != null) {
            return tc.oc.pgm.commands.CommandUtils.completeMapName(mapName);
        }

        Multimap<MapDefinition, MapLogRecord> errors = mapErrorTracker.getErrors();
        PGMMap filterMap = null;
        if(!mapName.isEmpty()) {
            filterMap = tc.oc.pgm.commands.CommandUtils.getMap(mapName, sender);
            Multimap<MapDefinition, MapLogRecord> filtered = ArrayListMultimap.create();
            filtered.putAll(filterMap, errors.get(filterMap));
            errors = filtered;
        }

        new PrettyPaginatedResult<Map.Entry<MapDefinition, MapLogRecord>>(filterMap == null ? "Map Errors (" + errors.keySet().size() + " maps)"
                                                                                            : filterMap.getName() + " Errors") {
            @Override
            public String format(Map.Entry<MapDefinition, MapLogRecord> entry, int index) {
                return entry.getValue().getLegacyFormattedMessage();
            }

            @Override
            public String formatEmpty() {
                return ChatColor.GREEN + PGMTranslations.get().t("command.development.listErrors.noErrors", sender);
            }
        }.display(new BukkitWrappedCommandSender(sender), errors.entries(), args.getFlagInteger('p', 1));
        return null;
    }

    @Command(
        aliases = {"loadnewmaps", "findnewmaps", "newmaps"},
        desc = "Scan for new maps and load them",
        min = 0,
        max = 0
    )
    @CommandPermissions("pgm.loadnewmaps")
    public void loadNewMaps(CommandContext args, CommandSender sender) throws CommandException {
        final Audience audience = audiences.get(sender);
        audience.sendMessage(new Component(new TranslatableComponent("command.loadNewMaps.loading"), ChatColor.WHITE));
        // Clear errors for maps that failed to load, because we want to see those errors again
        mapErrorTracker.clearErrorsExcept(mapLibrary.getMaps());
        try {
            final Set<PGMMap> newMaps = matchManager.loadMapsAndRotations();

            if(newMaps.isEmpty()) {
                audience.sendMessage(new Component(new TranslatableComponent("command.loadNewMaps.noNewMaps"), ChatColor.WHITE));
            } else if(newMaps.size() == 1) {
                audience.sendMessage(new Component(new TranslatableComponent("command.loadNewMaps.foundSingleMap", new Component(newMaps.iterator().next().getInfo().name, ChatColor.YELLOW)), ChatColor.WHITE));
            } else {
                audience.sendMessage(new Component(new TranslatableComponent("command.loadNewMaps.foundMultipleMaps", new Component(Integer.toString(newMaps.size()), ChatColor.AQUA)), ChatColor.WHITE));
            }
        } catch(MapNotFoundException e) {
            audience.sendWarning(new TranslatableComponent("command.loadNewMaps.noMaps"), false);
        }
    }

    @Command(
        aliases = {"matchfeatures", "features"},
        desc = "Lists all features by ID and type",
        min = 0,
        max = 1
    )
    @CommandPermissions(Permissions.MAPDEV)
    public void featuresCommand(CommandContext args, CommandSender sender) throws CommandException {
        final Match match = tc.oc.pgm.commands.CommandUtils.getMatch(sender);
        new PrettyPaginatedResult<Feature>("Match Features") {
            @Override
            public String format(Feature feature, int i) {
                String text = (i + 1) + ". " + ChatColor.RED + feature.getClass().getSimpleName();
                if(feature instanceof SluggedFeature) {
                    text += ChatColor.GRAY + " - " +ChatColor.GOLD + ((SluggedFeature) feature).slug();
                }
                return text;
            }
        }.display(new BukkitWrappedCommandSender(sender),
                  match.features().all().collect(Collectors.toList()),
                  args.getInteger(0, 1));
    }

    @Command(
        aliases = {"mapfeatures", "mapf"},
        desc = "Lists all map features by ID and type",
        usage = "[-a(nonymous)] [-v(erbose)] [-l(ocations)] [-t type] [-i id]",
        flags = "avlt:i:",
        min = 0,
        max = 0
    )
    @CommandPermissions(Permissions.MAPDEV)
    public void mapFeaturesCommand(CommandContext args, CommandSender sender) throws CommandException {
        final PGMMap map = tc.oc.pgm.commands.CommandUtils.getMatch(sender).getMap();
        final boolean verbose = args.hasFlag('v');
        final boolean locate = args.hasFlag('l');
        final boolean anonymous = args.hasFlag('a');
        final Optional<String> typeFilter = CommandUtils.flag(args, 't').map(String::toLowerCase);
        final Optional<String> idFilter = CommandUtils.flag(args, 'i').map(String::toLowerCase);

        MapModuleContext context = map.getContext().orElseThrow(() ->
                new IllegalStateException("The map modules are currently unloaded."));

        Stream<? extends FeatureDefinition> features = context.features().all();
        if(typeFilter.isPresent()) {
            features = features.filter(f -> f.inspectType().toLowerCase().contains(typeFilter.get()));
        }
        if(idFilter.isPresent()) {
            features = features.filter(f -> f instanceof FeatureProxy &&
                                            ((FeatureProxy) f).getId().toLowerCase().contains(idFilter.get()));
        } else if(!anonymous) {
            features = features.filter(f -> f instanceof FeatureProxy);
        }

        features.forEach(feature -> {
            final Component c = new Component(feature.inspectType(), ChatColor.BLUE);

            feature.inspectIdentity().ifPresent(id -> c.extra(" ").extra(new Component(id, ChatColor.YELLOW)));

            if(locate) {
                final Element element = context.features().definitionNode(feature);
                if(element != null) {
                    c.extra(" ").extra(new Component(new Node(element).describeWithLocation(), ChatColor.DARK_AQUA));
                }
            }

            sender.sendMessage(c);

            if(verbose) {
                feature.inspect(new MultiLineTextInspector(), Inspection.defaults())
                       .forEach(line -> sender.sendMessage(new Component("  " + BukkitUtils.escapeColors(line), ChatColor.GOLD)));
            }
        });
    }

    @Command(
        aliases = {"feature", "fl"},
        desc = "Prints information regarding a specific feature",
        min = 1,
        max = -1
    )
    @CommandPermissions(Permissions.MAPDEV)
    public void featureCommand(CommandContext args, CommandSender sender) throws CommandException {
        final String slug = args.getJoinedStrings(0);
        final Optional<Feature<?>> feature = matchManager.getCurrentMatch(sender).features().bySlug(slug);
        if(feature.isPresent()) {
            sender.sendMessage(ChatColor.GOLD + slug + ChatColor.GRAY + " corresponds to: " + ChatColor.WHITE + feature.get().toString());
        } else {
            sender.sendMessage(ChatColor.RED + "No feature by the name of " + ChatColor.GOLD + slug + ChatColor.RED + " was found.");
        }
    }

    @Command(
        aliases = {"velocity", "vel"},
        desc = "Apply a velocity to a player",
        min = 3,
        max = 4
    )
    @CommandPermissions(Permissions.MAPDEV)
    public void velocity(CommandContext args, CommandSender sender) throws CommandException {
        Player target = CommandUtils.getPlayerOrSelf(args, sender, 3);
        Vector velocity = new Vector(args.getDouble(0), args.getDouble(1), args.getDouble(2));
        sender.sendMessage(String.format("Applying velocity (%.2f, %.2f, %.2f) to " + target.getName(sender),
                                         velocity.getX(), velocity.getY(), velocity.getZ()));
        target.setVelocity(velocity);
    }

    @Command(
        aliases = {"impulse", "imp"},
        desc = "Apply an impulse to a player",
        min = 3,
        max = 4
    )
    @CommandPermissions(Permissions.MAPDEV)
    public void impulse(CommandContext args, CommandSender sender) throws CommandException {
        Player target = CommandUtils.getPlayerOrSelf(args, sender, 3);
        Vector impulse = new Vector(args.getDouble(0), args.getDouble(1), args.getDouble(2));
        sender.sendMessage(String.format("Applying impulse (%.2f, %.2f, %.2f) to " + target.getName(sender),
                                         impulse.getX(), impulse.getY(), impulse.getZ()));
        target.applyImpulse(impulse, true);
    }

    @Command(
        aliases = {"accelerate", "acc"},
        desc = "Apply a continuous force to a player for a period of time",
        min = 4,
        max = 5
    )
    @CommandPermissions(Permissions.MAPDEV)
    public void accelerate(CommandContext args, CommandSender sender) throws CommandException {
        final MatchPlayer target = tc.oc.pgm.commands.CommandUtils.getMatchPlayerOrSelf(args, sender, 4);
        final Duration duration = CommandUtils.getDuration(args, 0);
        final Vector accel = new Vector(args.getDouble(1), args.getDouble(2), args.getDouble(3)).multiply(1d / 20d); // per-tick

        sender.sendMessage(String.format("Applying force (%.2f, %.2f, %.2f) to %s for %s",
                                         accel.getX(), accel.getY(), accel.getZ(),
                                         target.getName(sender),
                                         duration));

        final PlayerForce force = target.facet(AccelerationPlayerFacet.class).addForce(accel);

        final Match match = target.getMatch();
        final PlayerId playerId = target.getPlayerId();
        match.getScheduler(MatchScope.LOADED).createDelayedTask(duration, () -> {
            final MatchPlayer target0 = match.getPlayer(playerId);
            if(target0 != null) {
                target0.facet(AccelerationPlayerFacet.class).removeForce(force);
            }
        });
    }

    @Command(
        aliases = {"filter", "fil"},
        desc = "Query a filter by ID with yourself or the given player",
        usage = "filter [player]",
        min = 1,
        max = 2
    )
    @CommandPermissions(Permissions.MAPDEV)
    public void filter(CommandContext args, CommandSender sender) throws CommandException {
        MatchPlayer target = tc.oc.pgm.commands.CommandUtils.getMatchPlayerOrSelf(args, sender, 1);
        String id = args.getString(0);
        Filter filter = tc.oc.pgm.commands.CommandUtils.getFeatureDefinition(id, sender, Filter.class);

        Filter.QueryResponse response = filter.query(target);
        String out = ChatColor.BLUE + id +
                     ChatColor.DARK_GRAY + "(" +
                     ChatColor.GRAY + target.getName(sender) +
                     ChatColor.DARK_GRAY + ") -> ";

        switch(response) {
            case DENY:      out += ChatColor.DARK_RED; break;
            case ABSTAIN:   out += ChatColor.YELLOW; break;
            case ALLOW:     out += ChatColor.GREEN; break;
        }

        sender.sendMessage(out + response.name());
    }

    @Command(
        aliases = {"updatemap", "savemap"},
        desc = "Updates the original map file to changes in game",
        flags = "f"
    )
    @CommandPermissions("pgm.updatemap")
    public void updateMap(CommandContext args, final CommandSender sender) throws CommandException {
        final Match match = matchManager.getCurrentMatch(sender);
        final PGMMap map = match.getMap();
        final Logger logger = map.getLogger();

        if(match.matchState() != MatchState.Idle && !args.hasFlag('f')) {
            sender.sendMessage(ChatColor.RED + PGMTranslations.get().t("command.map.update.running", sender));
        } else {
            World world = match.getWorld();
            logger.info("Saving world");
            world.save();

            final Path worldFolder = world.getWorldFolder().toPath();

            try {
                // Prune void regions from the world
                Path regionFolder = worldFolder.resolve("region");
                File[] regionFiles = regionFolder.toFile().listFiles();
                if(regionFiles == null) {
                    logger.info("No region folder");
                } else {
                    logger.info("Pruning empty regions");
                    for(File file : regionFiles) {
                        Matcher matcher = Pattern.compile("r\\.(-?\\d+).(-?\\d+).mca").matcher(file.getName());
                        if(!matcher.matches()) continue;

                        int regionX = Integer.parseInt(matcher.group(1));
                        int regionZ = Integer.parseInt(matcher.group(2));
                        int minX = regionX << 5;
                        int minZ = regionZ << 5;
                        int maxX = minX + 32;
                        int maxZ = minZ + 32;
                        boolean empty = true;

                        for(int x = minX; x < maxX; x++) {
                            for(int z = minZ; z < maxZ; z++) {
                                if(!world.getChunkAt(x, z).isEmpty()) empty = false;
                            }
                        }

                        if(empty) {
                            logger.info("  empty: " + file.getName());
                            if(!file.delete()) {
                                throw new CommandException(PGMTranslations.get().t("command.map.update.deleteFailed", sender, file.getName()));
                            }
                        } else {
                            logger.info("  non-empty: " + file.getName());
                        }
                    }
                }

                final Path sourceWorldFolder = map.getFolder().getAbsolutePath();
                Path sourceRegionFolder = sourceWorldFolder.resolve("region");
                Path sourceRegionBackup = sourceWorldFolder.resolve("region_backup");

                if(Files.exists(sourceRegionFolder)) {
                    logger.info("Copying source /region to /region_backup");
                    Files.copy(sourceRegionFolder, sourceRegionBackup, StandardCopyOption.REPLACE_EXISTING);
                }

                logger.info("Searching for changed files");
                Files.walkFileTree(worldFolder, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Path relativePath = worldFolder.relativize(file);
                        File sourceFile = sourceWorldFolder.resolve(relativePath).toFile();
                        // Always copy region files (mca)
                        if(!sourceFile.exists() && !file.getFileName().toString().endsWith(".mca")) {
                            logger.info("  skipping: " + relativePath);
                        }
                        else if(FileUtils.contentEquals(file.toFile(), sourceFile)) {
                            logger.info("  unchanged: " + relativePath);
                        }
                        else {
                            logger.info("  changed: " + relativePath);
                            Files.copy(file, sourceFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });

                if(Files.exists(sourceRegionBackup)) {
                    logger.info("Deleting region backup");
                    Files.delete(sourceRegionBackup);
                }
            } catch(IOException e) {
                throw new CommandException(e.toString());
            }

            sender.sendMessage(ChatColor.GREEN + PGMTranslations.get().t("command.map.update.success", sender));
        }
    }

    @Command(
        aliases = {"pushmaps"},
        desc = "Synchronizes ALL loaded maps with the database",
        min = 0,
        max = 0
    )
    @CommandPermissions(Permissions.DEVELOPER)
    public void pushMaps(CommandContext args, final CommandSender sender) throws CommandException {
        Audience audience = audiences.get(sender);
        audience.sendMessage(new Component("Pushing " + mapLibrary.getMaps().size() + " maps..."));

        syncExecutor.callback(
            mapLibrary.pushAllMaps(),
            CommandFutureCallback.onSuccess(sender, args, response ->
                audience.sendMessage(new Component(response.toString()))
            )
        );
    }

    @Command(
        aliases = {"environment", "env"},
        desc = "Get/set map environment variables"
    )
    @CommandPermissions(Permissions.MAPDEV)
    public void environment(CommandContext args, final CommandSender sender) throws CommandException {
        final Audience audience = audiences.get(sender);

        boolean reset = false;
        Map<String, Boolean> vars = new HashMap<>();
        Pattern pattern = Pattern.compile("([A-Za-z0-9_]+)=(true|false)");

        for(int i = 0; i < args.argsLength(); i++) {
            String arg = args.getString(i);
            if("reset".equals(arg)) {
                reset = true;
            } else {
                Matcher matcher = pattern.matcher(arg);
                if(!matcher.matches()) {
                    throw new CommandException("Can't understand variable assignment '" + arg + "'");
                }
                vars.put(matcher.group(1).toLowerCase(), "true".equals(matcher.group(2)));
            }
        }

        if(reset) mapEnvironment.clear();
        mapEnvironment.putAll(vars);

        for(Map.Entry<String, Boolean> entry : mapEnvironment.entrySet()) {
            audience.sendMessage(
                new Component(ChatColor.GRAY)
                    .extra(new Component(entry.getKey(), ChatColor.WHITE))
                    .extra("=")
                    .extra(new Component(String.valueOf(entry.getValue()), ChatColor.BLUE))
            );
        }
    }

    @Command(
        aliases = {"debugvelocity"},
        desc = "Dump debug info about a player's velocity to the console",
        usage = "[player]",
        min = 0,
        max = 1
    )
    @CommandPermissions(Permissions.MAPDEV)
    public void debugVelocity(CommandContext args, CommandSender sender) throws CommandException {
        final MatchPlayer player = tc.oc.pgm.commands.CommandUtils.getMatchPlayerOrSelf(args, sender, 0);
        final DebugVelocityPlayerFacet facet = player.facet(DebugVelocityPlayerFacet.class);
        final boolean enabled = !facet.isEnabled();
        facet.setEnabled(enabled);
        sender.sendMessage(new Component("Velocity debug for " + player.getName() + " is " + (enabled ? "ENABLED" : "DISABLED")));
    }
}