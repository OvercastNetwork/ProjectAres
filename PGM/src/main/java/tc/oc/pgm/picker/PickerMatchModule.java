package tc.oc.pgm.picker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import me.anxuiz.settings.bukkit.PlayerSettings;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerLocaleChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.MaterialData;
import tc.oc.commons.bukkit.chat.ComponentRenderContext;
import tc.oc.commons.bukkit.event.ObserverKitApplyEvent;
import tc.oc.commons.bukkit.item.ItemUtils;
import tc.oc.commons.bukkit.item.StringItemTag;
import tc.oc.commons.core.chat.ChatUtils;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.formatting.StringUtils;
import tc.oc.pgm.PGMTranslations;
import tc.oc.pgm.blitz.BlitzEvent;
import tc.oc.pgm.classes.ClassMatchModule;
import tc.oc.pgm.classes.ClassModule;
import tc.oc.pgm.classes.PlayerClass;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.MatchBeginEvent;
import tc.oc.pgm.events.MatchEndEvent;
import tc.oc.pgm.events.ObserverInteractEvent;
import tc.oc.pgm.events.PlayerJoinMatchEvent;
import tc.oc.pgm.events.PlayerPartyChangeEvent;
import tc.oc.pgm.join.JoinMatchModule;
import tc.oc.pgm.join.JoinRequest;
import tc.oc.pgm.join.JoinResult;
import tc.oc.pgm.blitz.BlitzMatchModule;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.spawns.events.DeathKitApplyEvent;
import tc.oc.pgm.teams.Team;
import tc.oc.pgm.teams.TeamMatchModule;
import tc.oc.pgm.teams.TeamModule;

import static com.google.common.base.Preconditions.checkState;

@ListenerScope(MatchScope.LOADED)
public class PickerMatchModule extends MatchModule implements Listener {

    private static final StringItemTag ITEM_TAG = new StringItemTag(PickerMatchModule.class.getSimpleName(), null);

    private static final String OPEN_BUTTON_PREFIX = ChatColor.GREEN + ChatColor.BOLD.toString();
    private static final int OPEN_BUTTON_SLOT = 2;

    private static final int LORE_WIDTH_PIXELS = 120;
    private static final int WIDTH = 9; // Inventory width in slots (for readability)

    enum Button {
        AUTO_JOIN(Material.CHAINMAIL_HELMET),
        TEAM_JOIN(Material.LEATHER_HELMET),
        JOIN(Material.LEATHER_HELMET),
        LEAVE(Material.LEATHER_BOOTS),
        ;

        public final Material material;

        Button(Material material) {
            this.material = material;
        }

        public boolean matches(MaterialData material) {
            return this.material.equals(material.getItemType());
        }
    }

    private final ComponentRenderContext renderer;
    private final JoinMatchModule jmm;
    private final BlitzMatchModule bmm;
    private final boolean hasTeams;
    private final boolean hasClasses;

    private final Set<MatchPlayer> picking = new HashSet<>();

    @Inject PickerMatchModule(ComponentRenderContext renderer, JoinMatchModule jmm, BlitzMatchModule bmm, Optional<TeamModule> teamModule, Optional<ClassModule> classModule) {
        this.renderer = renderer;
        this.jmm = jmm;
        this.bmm = bmm;
        this.hasTeams = teamModule.isPresent();
        this.hasClasses = classModule.isPresent();
    }

    protected boolean settingEnabled(MatchPlayer player) {
        return PlayerSettings.getManager(player.getBukkit()).getValue(PickerSettings.PICKER, Boolean.class);
    }

    private boolean hasJoined(MatchPlayer joining) {
        return joining.isParticipatingType() || jmm.isQueuedToJoin(joining);
    }

    private boolean canChooseMultipleTeams(MatchPlayer joining) {
        return getChoosableTeams(joining).size() > 1;
    }

    private Set<Team> getChoosableTeams(MatchPlayer joining) {
        TeamMatchModule tmm = getMatch().getMatchModule(TeamMatchModule.class);
        if(tmm == null) return Collections.emptySet();

        Set<Team> teams = new HashSet<>();
        for(Team team : tmm.getTeams()) {
            JoinResult result = tmm.queryJoin(joining, JoinRequest.user(team));
            if(result != null && result.isVisible()) {
                teams.add(team);
            }
        }

        return teams;
    }

    /**
     * Does the player have any use for the picker?
     */
    private boolean canUse(MatchPlayer player) {
        if(player == null) return false;

        // Player is eliminated from Blitz
        if(bmm.activated() && getMatch().isRunning()) return false;

        // Player is not observing or dead
        if(!(player.isObserving() || player.isDead())) return false;

        return jmm.queryJoin(player, JoinRequest.user()).isVisible();
    }

    /**
     * Does the player have any use for the picker dialog? If the player can join,
     * but there is nothing to pick (i.e. FFA without classes) then this returns
     * false, while {@link #canUse} returns true.
     */
    private boolean canOpenWindow(MatchPlayer player) {
        return canUse(player) && (hasClasses || canChooseMultipleTeams(player));
    }

    private boolean isPicking(MatchPlayer player) {
        return picking.contains(player);
    }

    private void refreshCountsAll() {
        for(MatchPlayer player : ImmutableSet.copyOf(picking)) {
            refreshWindow(player);
        }
    }

    private String getWindowTitle(MatchPlayer player) {
        checkState(hasTeams || hasClasses); // Window should not open if there is nothing to pick

        String key;
        if(hasTeams && hasClasses) {
            key = "teamClass.picker.title";
        } else if(hasTeams) {
            key = "teamSelection.picker.title";
        } else {
            key = "class.picker.title";
        }

        return ChatColor.DARK_RED + PGMTranslations.t(key, player);
    }

    private ItemStack createJoinButton(final MatchPlayer player) {
        ItemStack stack = new ItemStack(Button.JOIN.material);

        ItemMeta meta = stack.getItemMeta();
        meta.addItemFlags(ItemFlag.values());

        String key;
        if(!canOpenWindow(player)) {
            key = "ffa.picker.displayName";
        } else if(hasTeams && hasClasses) {
            key = "teamClass.picker.displayName";
        } else if(hasTeams) {
            key = "teamSelection.picker.displayName";
        } else if(hasClasses) {
            key = "class.picker.displayName";
        } else {
            key = "ffa.picker.displayName";
        }

        meta.setDisplayName(OPEN_BUTTON_PREFIX + PGMTranslations.t(key, player));
        meta.setLore(Lists.newArrayList(ChatColor.DARK_PURPLE + PGMTranslations.t("teamSelection.picker.tooltip", player)));

        stack.setItemMeta(meta);
        ITEM_TAG.set(stack, "join");
        return stack;
    }

    private ItemStack createLeaveButton(final MatchPlayer player) {
        ItemStack stack = new ItemStack(Button.LEAVE.material);

        ItemMeta meta = stack.getItemMeta();
        meta.addItemFlags(ItemFlag.values());
        meta.setDisplayName(OPEN_BUTTON_PREFIX + PGMTranslations.t("leave.picker.displayName", player));
        meta.setLore(Lists.newArrayList(ChatColor.DARK_PURPLE + PGMTranslations.t("leave.picker.tooltip", player)));

        stack.setItemMeta(meta);
        ITEM_TAG.set(stack, "leave");
        return stack;
    }

    public void refreshKit(final MatchPlayer player) {
        if(canUse(player)) {
            logger.fine("Giving kit to " + player);

            ItemStack stack;
            if(hasJoined(player) && !canOpenWindow(player)) {
                stack = createLeaveButton(player);
            } else {
                stack = createJoinButton(player);
            }

            player.getInventory().setItem(OPEN_BUTTON_SLOT, stack);
        } else if(ITEM_TAG.has(player.getInventory().getItem(OPEN_BUTTON_SLOT))) {
            player.getInventory().setItem(OPEN_BUTTON_SLOT, null);
        }
    }

    private void refreshKitAll() {
        for(MatchPlayer player : getMatch().getObservingPlayers()) {
            refreshKit(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void join(PlayerJoinMatchEvent event) {
        final MatchPlayer player = event.getPlayer();
        player.nextTick(() -> {
            if(settingEnabled(player) && canOpenWindow(player)) {
                showWindow(player);
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void checkInventoryClick(InventoryClickEvent event) {
        if(event.getCurrentItem() == null ||
           event.getCurrentItem().getItemMeta() == null ||
           event.getCurrentItem().getItemMeta().getDisplayName() == null) return;

        match.player(event.getActor()).ifPresent(player -> {
            if(!this.picking.contains(player)) return;

            this.handleInventoryClick(
                player,
                ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()),
                event.getCurrentItem().getData()
            );
            event.setCancelled(true);
        });
    }

    @EventHandler
    public void handleLocaleChange(final PlayerLocaleChangeEvent event) {
        final MatchPlayer player = getMatch().getPlayer(event.getPlayer());
        if(player != null) refreshKit(player);
    }

    @EventHandler
    public void closeMonitoredInventory(final InventoryCloseEvent event) {
        this.picking.remove(getMatch().getPlayer((Player) event.getPlayer()));
    }

    @EventHandler
    public void rightClickIcon(final ObserverInteractEvent event) {
        if(event.getClickType() != ClickType.RIGHT) return;

        MatchPlayer player = event.getPlayer();
        if(!canUse(player)) return;

        ItemStack hand = event.getClickedItem();
        if(ItemUtils.isNothing(hand)) return;

        String displayName = hand.getItemMeta().getDisplayName();
        if(displayName == null) return;

        if(hand.getType() == Button.JOIN.material) {
            event.setCancelled(true);
            if(canOpenWindow(player)) {
                showWindow(player);
            } else {
                // If there is nothing to pick, just join immediately
                jmm.requestJoin(player, JoinRequest.user());
            }
        } else if(hand.getType() == Button.LEAVE.material) {
            event.setCancelled(true);
            jmm.requestObserve(player);
        }
    }

    @EventHandler
    public void giveKitToObservers(ObserverKitApplyEvent event) {
        match.player(event.getPlayer())
             .ifPresent(this::refreshKit);
    }

    @EventHandler
    public void giveKitToDead(final DeathKitApplyEvent event) {
        refreshKit(event.getPlayer());
    }

    @EventHandler
    public void teamSwitch(final PlayerPartyChangeEvent event) {
        refreshCountsAll();
        refreshKit(event.getPlayer());

        if(event.getNewParty() == null) {
            picking.remove(event.getPlayer());
        }
    }

    @EventHandler
    public void matchBegin(final MatchBeginEvent event) {
        refreshCountsAll();
        refreshKitAll();
    }

    @EventHandler
    public void matchEnd(final MatchEndEvent event) {
        refreshCountsAll();
        refreshKitAll();
    }

    @EventHandler
    public void blitzEnable(final BlitzEvent event) {
        refreshCountsAll();
        refreshKitAll();
    }

    /**
     * Open the window for the given player, or refresh its contents
     * if they already have it open, and return the current contents.
     *
     * If the window is currently open but too small to hold the current
     * contents, it will be closed and reopened.
     *
     * If the player is not currently allowed to have the window open,
     * close any window they have open and return null.
     */
    private @Nullable Inventory showWindow(MatchPlayer player) {
        if(!checkWindow(player)) return null;

        ItemStack[] contents = createWindowContents(player);
        Inventory inv = getOpenWindow(player);
        if(inv != null && inv.getSize() < contents.length) {
            inv = null;
            closeWindow(player);
        }
        if(inv == null) {
            inv = openWindow(player, contents);
        } else {
            inv.setContents(contents);
        }
        return inv;
    }

    /**
     * If the given player currently has the window open, refresh its contents
     * and return the updated inventory. The window will be closed and reopened
     * if it is too small to hold the current contents.
     *
     * If the window is open but should be closed, close it and return null.
     *
     * If the player does not have the window open, return null.
     */
    private @Nullable Inventory refreshWindow(MatchPlayer player) {
        if(!checkWindow(player)) return null;

        Inventory inv = getOpenWindow(player);
        if(inv != null) {
            ItemStack[] contents = createWindowContents(player);
            if(inv.getSize() < contents.length) {
                closeWindow(player);
                inv = openWindow(player, contents);
            } else {
                inv.setContents(contents);
            }
        }
        return inv;
    }

    /**
     * Return true if the given player is currently allowed to have an open window.
     * If they are not allowed, close any window they have open, and return false.
     */
    private boolean checkWindow(MatchPlayer player) {
        if(!player.isOnline()) return false;

        if(!canOpenWindow(player)) {
            closeWindow(player);
            return false;
        }

        return true;
    }

    /**
     * Return the inventory of the given player's currently open window,
     * or null if the player does not have the window open.
     */
    private @Nullable Inventory getOpenWindow(MatchPlayer player) {
        if(picking.contains(player)) {
            return player.getBukkit().getOpenInventory().getTopInventory();
        }
        return null;
    }

    /**
     * Close any window that is currently open for the given player
     */
    private void closeWindow(MatchPlayer player) {
        if(picking.contains(player)) {
            player.getBukkit().closeInventory();
        }
    }

    /**
     * Open a new window for the given player displaying the given contents
     */
    private Inventory openWindow(MatchPlayer player, ItemStack[] contents) {
        closeWindow(player);
        Inventory inv = getMatch().getServer().createInventory(player.getBukkit(),
                                                               contents.length,
                                                               StringUtils.truncate(getWindowTitle(player), 32));
        inv.setContents(contents);
        player.getBukkit().openInventory(inv);
        picking.add(player);
        return inv;
    }

    /**
     * Generate current picker contents for the given player
     */
    private ItemStack[] createWindowContents(final MatchPlayer player) {
        final List<ItemStack> slots = new ArrayList<>();

        final Set<Team> teams = getChoosableTeams(player);
        if(!teams.isEmpty()) {
            // Auto-join button at start of row
            if(teams.size() > 1) {

                final JoinResult autoResult = jmm.queryJoin(player, JoinRequest.user());
                if(autoResult.isVisible()) {
                    slots.add(createAutoJoinButton(player));
                }
            }

            // Team buttons
            if(teams.size() > 1 || !hasJoined(player)) {
                for(Team team : teams) {
                    slots.add(createTeamJoinButton(player, team));
                }
            }
        }

        // Skip to next empty row
        while(slots.size() % WIDTH != 0) slots.add(null);

        // Class buttons
        if(hasClasses) {
            for(PlayerClass cls : getMatch().getMatchModule(ClassMatchModule.class).getClasses()) {
                slots.add(createClassButton(player, cls));
            }
        }

        // Pad last row to width
        while(slots.size() % WIDTH != 0) slots.add(null);

        if(hasJoined(player)) {
            // Put leave button in first empty slot of the last column
            for(int slot = WIDTH - 1;; slot += WIDTH) {
                while(slots.size() <= slot) {
                    slots.add(null);
                }

                if(slots.get(slot) == null) {
                    slots.set(slot, createLeaveButton(player));
                    break;
                }
            }
        }

        return slots.toArray(new ItemStack[slots.size()]);
    }

    private ItemStack createClassButton(MatchPlayer viewer, PlayerClass cls) {
        ItemStack item = cls.getIcon().toItemStack(1);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.values());

        meta.setDisplayName((cls.canUse(viewer.getBukkit()) ? ChatColor.GREEN : ChatColor.RED) + cls.getName());
        if(getMatch().getMatchModule(ClassMatchModule.class).selectedClass(viewer.getDocument()).equals(cls)) {
            meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
        }

        List<String> lore = Lists.newArrayList();
        if(cls.getLongDescription() != null) {
            ChatUtils.wordWrap(ChatColor.GOLD + cls.getLongDescription(), LORE_WIDTH_PIXELS, lore);
        } else if(cls.getDescription() != null) {
            lore.add(ChatColor.GOLD + cls.getDescription());
        }

        if(!cls.canUse(viewer.getBukkit())) {
            lore.add(ChatColor.RED + PGMTranslations.t("class.picker.restricted", viewer));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createAutoJoinButton(MatchPlayer viewer) {
        ItemStack autojoin = new ItemStack(Button.AUTO_JOIN.material);

        ItemMeta autojoinMeta = autojoin.getItemMeta();
        autojoinMeta.addItemFlags(ItemFlag.values());
        autojoinMeta.setDisplayName(ChatColor.GRAY.toString() + ChatColor.BOLD + PGMTranslations.t("teamSelection.picker.autoJoin.displayName", viewer));
        autojoinMeta.setLore(Lists.newArrayList(this.getTeamSizeDescription(viewer.getMatch().getParticipatingPlayers().size(),
                                                                            viewer.getMatch().getMaxPlayers()),
                                                ChatColor.AQUA + PGMTranslations.t("teamSelection.picker.autoJoin.tooltip", viewer)));
        autojoin.setItemMeta(autojoinMeta);

        return autojoin;
    }

    private ItemStack createTeamJoinButton(final MatchPlayer player, final Team team) {
        ItemStack item = new ItemStack(Button.TEAM_JOIN.material);
        String capacityMessage = this.getTeamSizeDescription(team.getPlayers().size(), team.getMaxPlayers());
        List<String> lore = Lists.newArrayList(capacityMessage);

        final JoinResult result = jmm.queryJoin(player, JoinRequest.user(team));
        if(result.isAllowed()) {
            final String label = result.isRejoin() ? "teamSelection.picker.clickToRejoin"
                                                   : "teamSelection.picker.clickToJoin";
            lore.add(renderer.renderLegacy(new Component(new TranslatableComponent(label), ChatColor.GREEN), player.getBukkit()));
        } else if(result.message().isPresent()) {
            lore.add(renderer.renderLegacy(new Component(result.message().get(), ChatColor.RED), player.getBukkit()));
            result.extra().forEach(line -> {
                lore.add(renderer.renderLegacy(line, player.getBukkit()));
            });
        }

        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.addItemFlags(ItemFlag.values());
        meta.setColor(team.getFullColor());
        meta.setDisplayName(team.getColor().toString() + ChatColor.BOLD + team.getName());
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.values()); // Hides a lot more than potion effects
        item.setItemMeta(meta);

        return item;
    }

    private String getTeamSizeDescription(final int num, final int max) {
        return (num >= max ? ChatColor.RED : ChatColor.GREEN).toString() + num + ChatColor.GOLD + " / " + ChatColor.RED + max;
    }

    private void handleInventoryClick(final MatchPlayer player, final String name, final MaterialData material) {
        player.playSound(Sound.UI_BUTTON_CLICK, 1, 2);

        if(hasClasses) {
            ClassMatchModule cmm = player.getMatch().needMatchModule(ClassMatchModule.class);
            PlayerClass cls = cmm.getPlayerClass(name);

            if(cls != null && cls.getIcon().equals(material)) {
                if(!cls.canUse(player.getBukkit())) return;

                if(!Objects.equals(cls, cmm.selectedClass(player.getDocument()))) {
                    if(cmm.getCanChangeClass(player.getPlayerId())) {
                        cmm.setPlayerClass(player.getPlayerId(), cls);
                        player.sendMessage(ChatColor.GOLD + PGMTranslations.t("command.class.select.confirm", player, ChatColor.GREEN + name));
                        scheduleRefresh(player);
                    } else {
                        player.sendMessage(ChatColor.RED + PGMTranslations.t("command.class.stickyClass", player));
                    }
                }

                if(!canChooseMultipleTeams(player) && !hasJoined(player)) {
                    this.scheduleClose(player);
                    this.scheduleJoin(player, null);
                }

                return;
            }
        }

        if(hasTeams && Button.TEAM_JOIN.matches(material)) {
            Team team = player.getMatch().needMatchModule(TeamMatchModule.class).bestFuzzyMatch(name, 1);
            if(team != null) {
                this.scheduleClose(player);
                this.scheduleJoin(player, team);
            }

        } else if(Button.AUTO_JOIN.matches(material)) {
            this.scheduleClose(player);
            this.scheduleJoin(player, null);

        } else if(Button.LEAVE.matches(material)) {
            this.scheduleClose(player);
            this.scheduleLeave(player);
        }
    }

    private void scheduleClose(final MatchPlayer player) {
        player.nextTick(() -> {
            player.getBukkit().getOpenInventory().getTopInventory().clear();
            player.getBukkit().closeInventory();
        });
    }

    private void scheduleJoin(final MatchPlayer player, @Nullable final Team team) {
        player.nextTick(() -> {
            if(team == null) {
                if(hasJoined(player)) return;
            } else {
                if(player.getParty().equals(team)) return;
            }

            jmm.requestJoin(player, JoinRequest.user(team));
        });
    }

    private void scheduleLeave(final MatchPlayer player) {
        player.nextTick(() -> {
            if(!hasJoined(player)) return;
            jmm.requestObserve(player);
        });
    }

    private void scheduleRefresh(final MatchPlayer viewer) {
        viewer.nextTick(() -> refreshWindow(viewer));
    }
}
