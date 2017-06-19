package tc.oc.commons.bukkit.teleport;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import tc.oc.api.docs.Arena;
import tc.oc.api.docs.Game;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.games.GameStore;
import tc.oc.commons.bukkit.chat.ComponentRenderContext;
import tc.oc.commons.bukkit.config.ExternalConfiguration;
import tc.oc.commons.bukkit.configuration.ConfigUtils;
import tc.oc.commons.bukkit.event.ObserverKitApplyEvent;
import tc.oc.commons.bukkit.format.GameFormatter;
import tc.oc.commons.bukkit.format.ServerFormatter;
import tc.oc.commons.bukkit.inventory.Slot;
import tc.oc.commons.bukkit.item.ItemConfigurationParser;
import tc.oc.commons.bukkit.item.RenderedItemBuilder;
import tc.oc.commons.bukkit.listeners.ButtonListener;
import tc.oc.commons.bukkit.listeners.ButtonManager;
import tc.oc.commons.bukkit.listeners.WindowListener;
import tc.oc.commons.bukkit.listeners.WindowManager;
import tc.oc.commons.bukkit.ticket.TicketBooth;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.inject.InnerFactory;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.minecraft.api.configuration.InvalidConfigurationException;

import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowFunction;

@Singleton
public class NavigatorInterface implements PluginFacet, Listener, Commands {

    private final GameStore games;
    private final ServerFormatter serverFormatter = ServerFormatter.light;
    private final GameFormatter gameFormatter;
    private final TicketBooth ticketBooth;
    private final ButtonManager buttonManager;
    private final WindowManager windowManager;
    private final RenderedItemBuilder.Factory itemBuilders;
    private final ComponentRenderContext renderer;
    private final Server localServer;
    private final Navigator navigator;

    private boolean enabled;
    private int height;
    private BaseComponent title = new TranslatableComponent("navigator.title");
    private ItemStack openButtonIcon = new ItemStack(Material.SIGN);
    private Slot.Player openButtonSlot = Slot.Hotbar.forPosition(0);

    private ImmutableMap<Slot.Container, Button> buttons = ImmutableMap.of();
    private final Set<InventoryView> openWindows = new HashSet<>();

    @Inject NavigatorInterface(GameStore games,
                               GameFormatter gameFormatter,
                               TicketBooth ticketBooth,
                               ButtonManager buttonManager,
                               RenderedItemBuilder.Factory itemBuilders,
                               WindowManager windowManager,
                               ComponentRenderContext renderer,
                               Server localServer,
                               Navigator navigator,
                               InnerFactory<NavigatorInterface, Configuration> configFactory) {
        this.games = games;
        this.gameFormatter = gameFormatter;
        this.ticketBooth = ticketBooth;
        this.buttonManager = buttonManager;
        this.itemBuilders = itemBuilders;
        this.windowManager = windowManager;
        this.renderer = renderer;
        this.localServer = localServer;
        this.navigator = navigator;

        configFactory.create(this);
    }

    @Command(
            aliases = { "shownavigatorbuttons" },
            desc = "Print a list of the buttons in the navigator",
            min = 0,
            max = 0
    )
    @CommandPermissions("ocn.developer")
    public void servers(final CommandContext args, final CommandSender sender) throws CommandException {
        sender.sendMessage("Buttons:");
        for (Button button: buttons.values()) {
            sender.sendMessage(button.toString());
        }
    }


    public void setOpenButtonSlot(Slot.Player openButtonSlot) {
        this.openButtonSlot = openButtonSlot;
    }

    private final ButtonListener openButtonListener = (button, clicker, clickType, event) -> {
        if(clickType == ClickType.RIGHT) {
            openWindow(clicker);
            return true;
        }
        return false;
    };

    private ItemStack createOpenButton(Player player) {
        return buttonManager.createButton(openButtonListener,
                                          itemBuilders.create(player, openButtonIcon)
                                                      .flags(ItemFlag.values())
                                                      .name(new Component(title, ChatColor.AQUA, ChatColor.BOLD))
                                                      .get());
    }

    public void giveOpenButton(Player player) {
        openButtonSlot.putItem(player, createOpenButton(player));
    }

    @EventHandler
    public void onObserve(ObserverKitApplyEvent event) {
        if(enabled) {
            giveOpenButton(event.getPlayer());
        }
    }

    private final WindowListener windowListener = new WindowListener() {
        @Override public void windowOpened(InventoryView window) {
            openWindows.add(window);
        }

        @Override public void windowClosed(InventoryView window) {
            openWindows.remove(window);
        }

        @Override
        public boolean windowClicked(InventoryView window, Inventory inventory, ClickType clickType, InventoryType.SlotType slotType, int slotIndex, @Nullable ItemStack item) {
            return true;
        }
    };

    private Inventory createWindow(Player player) {
        final Inventory inventory = Bukkit.createInventory(
            player,
            height * 9,
            renderer.renderLegacy(new Component(title, ChatColor.DARK_AQUA, ChatColor.BOLD), player)
        );
        buttons.values().forEach(handler -> handler.updateWindow(player, inventory));
        return inventory;
    }

    private void openWindow(Player player) {
        if(enabled && !buttons.isEmpty()) {
            windowManager.openWindow(windowListener, player, createWindow(player));
        }
    }

    private void closeAllWindows() {
        ImmutableList.copyOf(openWindows).forEach(InventoryView::close);
        openWindows.clear();
    }

    private void clear() {
        closeAllWindows();
        NavigatorInterface.this.enabled = false;
        NavigatorInterface.this.buttons.values().forEach(Button::release);
        NavigatorInterface.this.buttons = ImmutableMap.of();
    }

    class Configuration extends ExternalConfiguration {

        @Inject public Configuration() {}

        @Override
        protected String configName() {
            return "navigator";
        }

        @Override
        protected String fileName() {
            return "navigator-" + localServer.datacenter();
        }

        @Override
        protected void configChanged(@Nullable ConfigurationSection before, @Nullable ConfigurationSection after) throws InvalidConfigurationException {
            super.configChanged(before, after);
            if(after != null) {
                load(after);
            } else {
                clear();
            }
        }

        void load(ConfigurationSection config) throws InvalidConfigurationException {
            final boolean enabled;
            final BaseComponent title;
            final ItemStack openButtonIcon;
            final Map<Slot.Container, Button> buttons = new HashMap<>();

            try {
                enabled = config.getBoolean("enabled", false);
                title = new TranslatableComponent(config.getString("title", "navigator.title"));
                final ItemConfigurationParser itemParser = new ItemConfigurationParser(config);
                openButtonIcon = itemParser.getItem(config, "icon", () -> new ItemStack(Material.SIGN));

                if(enabled) {
                    final ConfigurationSection buttonSection = config.getSection("buttons");
                    for(String key : buttonSection.getKeys()) {
                        final Button button = new Button(buttonSection.getSection(key), itemParser);
                        buttons.put(button.slot, button);
                    }
                }
            } catch(InvalidConfigurationException e) {
                buttons.values().forEach(Button::release);
                throw e;
            }

            clear();

            NavigatorInterface.this.enabled = enabled;
            NavigatorInterface.this.title = title;
            NavigatorInterface.this.openButtonIcon = openButtonIcon;
            NavigatorInterface.this.buttons = ImmutableMap.copyOf(buttons);
            NavigatorInterface.this.height = buttons.values()
                                                    .stream()
                                                    .mapToInt(button -> button.slot.getRow() + 1)
                                                    .max()
                                                    .orElse(0);
        }
    }

    private class Button implements ButtonListener {

        final Slot.Container slot;
        final ItemStack icon;
        final Navigator.Connector connector;

        final Consumer<Navigator.Connector> observer = c ->
            openWindows.forEach(window -> updateWindow((Player) window.getPlayer(), window.getTopInventory()));

        public String toString() {
            String string = "{";
            string += "slot: {" + slot.getColumn() + ", " + slot.getRow() + "}, ";
            string += "icon: " + icon.getType() + ", ";
            string += "connector: " + "{isConnectable: " + connector.isConnectable() + ", ";
            string += "isVisible: " + connector.isVisible() + ", ";
            if (connector instanceof Navigator.ServerConnector) {
                Server server = ((Navigator.ServerConnector)connector).server;
                string += "server: {";
                string += "bungee_name: " + server.bungee_name() + ", ";
                string += "box: " + server.box() + ", ";
                string += "datacenter: " + server.datacenter() + ", ";
                string += "family: " + server.family() + ", ";
                string += "description: " + server.description() + ", ";
                string += "ip: " + server.ip() + ", ";
                string += "name: " + server.name() + ", ";
                string += "max_players: " + server.max_players() + ", ";
                string += "num_online: " + server.num_online() + ", ";
                string += "_id: " + server._id() + ", ";
                string += "slug: " + server.slug() + ", ";
                string += "priority: " + server.priority() + ", ";
                string += "visibility: " + server.visibility() + ", ";
                string += "online: " + server.online() + ", ";
                string += "running: " + server.running();
                string += "}";
            }
            string += "}";
            return string;
        }

        Button(ConfigurationSection config, ItemConfigurationParser itemParser) throws InvalidConfigurationException {
            this.slot = itemParser.needSlotByPosition(config, null, null, Slot.Container.class);
            this.icon = config.isString("skull") ? itemParser.needSkull(config, "skull")
                                                  : itemParser.needItem(config, "icon");
            this.connector = navigator.combineConnectors(ConfigUtils.needValueOrList(config, "to", String.class).stream()
                                                                    .map(rethrowFunction(token -> parseConnector(config, "to", token)))
                                                                    .collect(Collectors.toList()));
            this.connector.startObserving(observer);
        }

        private Navigator.Connector parseConnector(ConfigurationSection section, String key, String token) throws InvalidConfigurationException {
            final Navigator.Connector connector = navigator.parseConnector(token);
            if(connector == null) {
                throw new InvalidConfigurationException(section, key, "Invalid connector token '" + token + "'");
            }
            return connector;
        }

        void release() {
            buttonManager.unregisterListener(this);
            connector.stopObserving(observer);
            connector.release();
        }

        @Override
        public boolean buttonClicked(ItemStack stack, Player clicker, ClickType clickType, Event event) {
            if(connector.isConnectable()) {
                windowManager.closeWindow(clicker);
                connector.teleport(clicker);
            }
            return true;
        }

        void updateWindow(Player viewer, Inventory inventory) {
            final ItemStack stack = createButton(viewer);
            if(!Objects.equals(stack, slot.getItem(inventory))) {
                slot.putItem(inventory, stack);
            }
        }

        @Nullable ItemStack createButton(Player viewer) {
            final ItemStack icon = createIcon(viewer);
            return icon == null ? null : buttonManager.createButton(this, icon);
        }

        @Nullable ItemStack createIcon(Player viewer) {
            if(!connector.isVisible()) return null;

            final RenderedItemBuilder<?> icon = itemBuilders.create(viewer, this.icon.clone())
                                                            .flags(ItemFlag.values());
            final Object mapped = connector.mappedTo();
            if(Navigator.DEFAULT_MAPPING.equals(mapped)) {
                renderDefault(viewer, icon);
            } else if(mapped instanceof Server) {
                renderServer(viewer, icon, (Server) mapped);
            } else if(mapped instanceof Arena) {
                renderArena(viewer, icon, (Arena) mapped);
            }
            return icon.get();
        }

        void renderDefault(Player viewer, RenderedItemBuilder<?> icon) {
            final Game game = ticketBooth.currentGame(viewer);
            if(game != null) {
                icon.name(gameFormatter.leave(game));
            } else {
                icon.name(new Component(new TranslatableComponent("servers.backToLobby"), ChatColor.AQUA));
            }
        }

        void renderServer(Player viewer, RenderedItemBuilder<?> icon, Server server) {
            icon.name(serverFormatter.name(server));
            serverFormatter.description(server).ifPresent(icon::lore);
            icon.lore(Components.blank());

            if(serverFormatter.isRestarting(server)) {
                icon.lore(serverFormatter.onlineStatus(server));
            } else {
                icon.amount(Math.max(1, server.num_online()));

                if(server.role() == ServerDoc.Role.LOBBY) {
                    icon.lore(gameFormatter.onlineCount(server));
                } else {
                    icon.lore(gameFormatter.playingCount(server));
                    icon.lore(gameFormatter.watchingCount(server));
                    icon.lore(Components.blank());

                    if(server.current_match() != null) {
                        if(server.current_match().map() != null && server.current_match().end() == null) {
                            // Show current map if match is not finished
                            icon.lore(serverFormatter.currentMap(server));
                        }

                        if(server.num_online() > 0 &&
                           server.restart_queued_at() == null &&
                           (server.current_match().start() == null || server.current_match().end() != null)) {
                            // Enchant if server has players, and is not restarting, and is between matches
                            icon.enchant(Enchantment.ARROW_INFINITE, 1);
                        }

                    }

                    if(server.next_map() != null) {
                        icon.lore(serverFormatter.nextMap(server));
                    }
                }
            }
        }

        void renderArena(Player viewer, RenderedItemBuilder<?> icon, Arena arena) {
            final Game game = games.byId(arena.game_id());
            icon.amount(Math.max(1, arena.num_playing() + arena.num_queued()))
                .name(new Component(game.name(), ChatColor.AQUA, ChatColor.BOLD))
                .lore(gameFormatter.description(game))
                .lore(Components.blank())
                .lore(gameFormatter.playingCount(arena))
                .lore(gameFormatter.waitingCount(arena))
                .get();
        }
    }
}
