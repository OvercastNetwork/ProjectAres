package tc.oc.lobby.bukkit;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.inject.Inject;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.util.RayBlockIntersection;
import tc.oc.api.docs.Arena;
import tc.oc.api.docs.Game;
import tc.oc.api.docs.Server;
import tc.oc.api.games.GameStore;
import tc.oc.commons.bukkit.chat.ComponentRenderContext;
import tc.oc.commons.bukkit.event.BlockPunchEvent;
import tc.oc.commons.bukkit.format.GameFormatter;
import tc.oc.commons.bukkit.format.ServerFormatter;
import tc.oc.commons.bukkit.teleport.Navigator;
import tc.oc.commons.bukkit.util.BlockUtils;
import tc.oc.commons.bukkit.util.ChunkLocation;
import tc.oc.commons.bukkit.util.NMSHacks;
import tc.oc.commons.bukkit.util.Vectors;
import tc.oc.commons.core.chat.ChatUtils;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.commons.core.util.CacheUtils;

public class SignUpdater implements Listener, PluginFacet {

    private static final int SIGN_WIDTH = 90;
    private static final ImmutableSet<Material> PORTAL_MATERIALS = ImmutableSet.of(Material.PORTAL);

    private final Logger logger;
    private final Collection<World> initialWorlds;
    private final ComponentRenderContext renderer;
    private final GameStore games;
    private final GameFormatter.Dark gameFormatter;
    private final Navigator navigator;

    private final Set<ChunkLocation> chunks = new HashSet<>();
    private final Map<Location, SignHandle> signs = new HashMap<>();
    private final Map<Player, SignHandle> hovering = new WeakHashMap<>();

    @Inject SignUpdater(Loggers loggers,
                        Collection<World> initialWorlds,
                        ComponentRenderContext renderer,
                        GameStore games,
                        GameFormatter.Dark gameFormatter,
                        Navigator navigator) {
        this.logger = loggers.get(getClass());
        this.initialWorlds = initialWorlds;
        this.renderer = renderer;
        this.games = games;
        this.gameFormatter = gameFormatter;
        this.navigator = navigator;
    }

    @Override
    public void enable() {
        initialWorlds.forEach(world -> Stream.of(world.getLoadedChunks()).forEach(this::load));
    }

    @Override
    public void disable() {
        hovering.forEach((player, sign) -> sign.hover(player, false));
        hovering.clear();
    }

    private @Nullable SignHandle createSign(Sign blockState) {
        String prev = "";
        final List<Navigator.Connector> connectors = new ArrayList<>();

        for(BaseComponent component : blockState.lines()) {
            String line = component.toPlainText();
            if(line.endsWith("\\")) {
                prev += line.substring(0, line.length() - 1);
            } else {
                final Navigator.Connector connector = navigator.parseConnector(prev + line);
                if(connector != null) {
                    connectors.add(connector);
                }
            }
        }

        return connectors.isEmpty() ? null : new SignHandle(blockState, navigator.combineConnectors(connectors));
    }

    void load(Chunk chunk) {
        if(chunks.add(ChunkLocation.of(chunk))) {
            for(BlockState blockState : chunk.getTileEntities()) {
                if(blockState instanceof Sign) {
                    final SignHandle sign = createSign((Sign) blockState);
                    if(sign != null) {
                        signs.put(blockState.getLocation(), sign);
                    }
                }
            }

            logger.fine(() -> "Loaded chunk " + chunk.getX() + "," + chunk.getZ() +" with " + signs.size());
        }
    }

    @EventHandler
    private void onChunkLoad(ChunkLoadEvent event) {
        load(event.getChunk());
    }

    private Optional<SignHandle> sign(Location location) {
        return Optional.ofNullable(signs.get(location));
    }

    public Optional<SignHandle> nearestSign(Location location) {
        return signs.values().stream()
                    .filter(sign -> sign.connector().isVisible())
                    .min(Comparator.comparing(sign -> sign.distanceSquared(location)));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void move(final PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        Location from = event.getFrom();
        Block toBlock = to.getBlock();
        Block fromBlock = from.getBlock();

        handleLook(player, to);

        if(PORTAL_MATERIALS.contains(toBlock.getType()) && !PORTAL_MATERIALS.contains(fromBlock.getType())) {
            nearestSign(event.getTo()).ifPresent(
                sign -> sign.connector().teleport(player)
            );
        }
    }

    private void handleLook(Player player, Location loc) {
        final RayBlockIntersection hit = player.getWorld().rayTraceBlock(loc.clone().add(0, player.getEyeHeight(), 0), player.getBlockReach(), true, false);
        final SignHandle newSign = hit == null ? null : sign(hit.getBlock().getLocation()).orElse(null);
        final SignHandle oldSign = hovering.get(player);

        if(!Objects.equals(oldSign, newSign)) {
            if(oldSign != null) {
                hovering.remove(player);
                oldSign.hover(player, false);
            }
            if(newSign != null) {
                hovering.put(player, newSign);
                newSign.hover(player, true);
            }
        }
    }

    private void handleBlockClick(Player player, Block block) {
        if(block != null && block.getState() instanceof Sign) {
            sign(block.getLocation()).ifPresent(
                sign -> sign.connector().teleport(player)
            );
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void rightClick(final PlayerInteractEvent event) {
        handleBlockClick(event.getPlayer(), event.getClickedBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void leftClick(final BlockPunchEvent event) {
        handleBlockClick(event.getPlayer(), event.getBlock());
    }

    class SignHandle {
        private final Location location;
        private final MaterialData material;
        private final Navigator.Connector connector;

        private final LoadingCache<BaseComponent, NMSHacks.FakeArmorStand> hoverEntities;

        private final Map<Player, NMSHacks.FakeArmorStand> hovering = new WeakHashMap<>();

        private final Consumer<Navigator.Connector> observer = c -> paint();

        public SignHandle(Sign sign, Navigator.Connector connector) {
            this.location = sign.getLocation();
            this.material = sign.getMaterialData();
            this.connector = connector;

            hoverEntities = CacheUtils.newCache(
                description -> new NMSHacks.FakeArmorStand(this.location.getWorld(), description.toLegacyText())
            );

            connector.startObserving(observer);

            paint();

            logger.fine("Created " + this);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() +
                   "{location=(" + Vectors.format(location.position(), "%.0f") +
                   ") connector=" + connector +
                   "}";
        }

        public double distanceSquared(Location to) {
            return this.location.getWorldId().equals(to.getWorldId()) ? this.location.distanceSquared(to) : Double.POSITIVE_INFINITY;
        }

        public Navigator.Connector connector() {
            return connector;
        }

        public void hover(Player player, boolean over) {
            if(over) {
                final BaseComponent description = connector.description();
                if(description != null) {
                    final NMSHacks.FakeArmorStand entity = hoverEntities.getUnchecked(renderer.render(new Component(description, ChatColor.DARK_AQUA), player));
                    hovering.put(player, entity);
                    entity.spawn(player, BlockUtils.center(location));
                }

            } else {
                final NMSHacks.FakeArmorStand entity = hovering.remove(player);
                if(entity != null) {
                    entity.destroy(player);
                }
            }
        }

        private void paint() {
            final BaseComponent[] lines = new BaseComponent[4];

            if(connector.isVisible()) {
                final Object mapped = connector.mappedTo();
                if(Navigator.DEFAULT_MAPPING.equals(mapped)) {
                    renderDefault(lines);
                } else if(mapped instanceof Server) {
                    renderServer(lines, (Server) mapped);
                } else if(mapped instanceof Arena) {
                    renderArena(lines, (Arena) mapped);
                }
            }

            final BlockState block = location.getBlock().getState();
            if(lines[0] != null) {
                final Sign sign;
                if(block instanceof Sign) {
                    sign = (Sign) block;
                } else {
                    block.setMaterialData(material);
                    block.update(true, false);
                    sign = (Sign) location.getBlock().getState();
                }

                for(int i = 0; i < 4; i++) {
                    sign.setLine(i, renderer.render(lines[i], Bukkit.getConsoleSender()));
                }
                block.update(true, false);

            } else if(block.getMaterial() != Material.AIR) {
                block.setMaterial(Material.AIR);
                block.update(true, false);
            }
        }

        void renderDefault(BaseComponent[] lines) {
            lines[0] = Components.blank();
            lines[1] = new Component(new TranslatableComponent("servers.backToLobby"), ChatColor.DARK_BLUE);
            lines[2] = Components.blank();
            lines[3] = Components.blank();
        }

        void renderServer(BaseComponent[] lines, Server server) {
            final ServerFormatter formatter = ServerFormatter.dark;

            lines[0] = formatter.name(server);

            if(formatter.isRestarting(server)) {
                lines[1] = Components.blank();
                lines[2] = Components.blank();
                lines[3] = formatter.onlineStatus(server);
            } else {
                lines[1] = formatter.playerCounts(server, false);

                if(server.current_match() != null && server.current_match().map() != null) {
                    final ChatColor color = formatter.matchStatusColor(server);
                    final List<String> matchText = ChatUtils.wordWrap(server.current_match().map().name(), SIGN_WIDTH);

                    if(matchText.size() == 1) {
                        lines[2] = Components.blank();
                        lines[3] = new Component(matchText.get(0), color);
                    } else {
                        lines[2] = new Component(matchText.get(0), color);
                        lines[3] = new Component(matchText.get(1), color);
                    }
                } else {
                    lines[2] = Components.blank();
                    lines[3] = Components.blank();
                }
            }
        }

        void renderArena(BaseComponent[] lines, Arena arena) {
            final Game game = games.byId(arena.game_id());

            lines[0] = new Component(game.name(), ChatColor.BLACK, ChatColor.BOLD);
            lines[1] = Components.blank();
            lines[2] = gameFormatter.playingCount(arena);
            lines[3] = gameFormatter.waitingCount(arena);
        }
    }
}
