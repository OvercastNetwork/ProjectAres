package tc.oc.pgm.inventory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryClickedEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import tc.oc.commons.bukkit.util.BukkitUtils;
import tc.oc.commons.core.commands.CommandBinder;
import tc.oc.pgm.PGMTranslations;
import tc.oc.pgm.blitz.BlitzMatchModuleImpl;
import tc.oc.pgm.doublejump.DoubleJumpMatchModule;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.ObserverInteractEvent;
import tc.oc.pgm.events.PlayerBlockTransformEvent;
import tc.oc.pgm.events.PlayerPartyChangeEvent;
import tc.oc.pgm.kits.WalkSpeedKit;
import tc.oc.pgm.blitz.BlitzMatchModule;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.Repeatable;
import tc.oc.pgm.match.inject.MatchModuleFixtureManifest;
import tc.oc.pgm.spawns.events.ParticipantSpawnEvent;
import tc.oc.time.Time;

@ListenerScope(MatchScope.LOADED)
public class ViewInventoryMatchModule extends MatchModule implements Listener {

    public static class Manifest extends MatchModuleFixtureManifest<ViewInventoryMatchModule> {
        @Override protected void configure() {
            super.configure();

            new CommandBinder(binder())
                .register(InventoryCommands.class);
        }
    }

    public static final Duration TICK = Duration.ofMillis(50);

    protected final Map<Player, View> views = new HashMap<>();
    protected final Map<Player, Instant> updateQueue = new HashMap<>();

    public static int getInventoryPreviewSlot(int inventorySlot) {
        if(inventorySlot < 9) {
            return inventorySlot + 36; // put hotbar on bottom
        }
        if(inventorySlot < 36) {
            return inventorySlot; // rest of inventory
        }
        // TODO: investigate why this method doesn't work with CraftBukkit's armor slots
        return inventorySlot; // default
    }

    @Repeatable(scope = MatchScope.LOADED, interval = @Time(ticks = 4))
    public void queuedChecks() {
        for(Iterator<Map.Entry<Player, Instant>> iterator = updateQueue.entrySet().iterator(); iterator.hasNext();) {
            final Map.Entry<Player, Instant> entry = iterator.next();
            if(entry.getValue().isAfter(Instant.now())) continue;

            checkMonitoredInventories(entry.getKey());
            iterator.remove();
        }
    }

    @EventHandler
    public void closeMonitoredInventory(final InventoryCloseEvent event) {
        views.remove(event.getActor());
    }

    @EventHandler
    public void playerQuit(final PlayerPartyChangeEvent event) {
        views.remove(event.getPlayer().getBukkit());
    }

    @EventHandler(ignoreCancelled = true)
    public void showInventories(final ObserverInteractEvent event) {
        if(event.getClickType() != ClickType.RIGHT) return;
        if(event.getPlayer().isDead()) return;

        if(event.getClickedParticipant() != null) {
            event.setCancelled(true);
            if(canPreviewInventory(event.getPlayer(), event.getClickedParticipant())) {
                this.previewPlayerInventory(event.getPlayer().getBukkit(), event.getClickedParticipant().getInventory());
            }
        } else if(event.getClickedEntity() instanceof InventoryHolder && !(event.getClickedEntity() instanceof Player)) {
            event.setCancelled(true);
            this.previewInventory(event.getPlayer().getBukkit(), ((InventoryHolder) event.getClickedEntity()).getInventory());
        } else if(event.getClickedBlockState() instanceof InventoryHolder) {
            event.setCancelled(true);
            this.previewInventory(event.getPlayer().getBukkit(), ((InventoryHolder) event.getClickedBlockState()).getInventory());
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void cancelClicks(final InventoryClickEvent event) {
        final View view = views.get(event.getActor());
        if(view != null && event.getInventory().equals(view.preview)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void updateMonitoredClick(final InventoryClickedEvent event) {
        if(event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();

            boolean playerInventory = event.getInventory().getType() == InventoryType.CRAFTING; // cb bug fix
            Inventory inventory;

            if(playerInventory) {
                inventory = player.getInventory();
            } else {
                inventory = event.getInventory();
            }

            invLoop: for(Map.Entry<Player, View> entry : new HashSet<>(this.views.entrySet())) { // avoid ConcurrentModificationException
                final Player viewer = entry.getKey();
                View view = entry.getValue();

                // because a player can only be viewing one inventory at a time,
                // this is how we determine if we have a match
                if(inventory.getViewers().isEmpty() ||
                   view.watched.getViewers().isEmpty() ||
                   inventory.getViewers().size() > view.watched.getViewers().size()) continue invLoop;

                for(int i = 0; i < inventory.getViewers().size(); i++) {
                    if(!inventory.getViewers().get(i).equals(view.watched.getViewers().get(i))) {
                        continue invLoop;
                    }
                }

                // a watched user is in a chest
                if(view.isPlayerInventory() && !playerInventory) {
                    inventory = view.getPlayerInventory().getHolder().getInventory();
                    playerInventory = true;
                }

                if(playerInventory) {
                    this.previewPlayerInventory(viewer, (PlayerInventory) inventory);
                } else {
                    this.previewInventory(viewer, inventory);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateMonitoredInventory(final InventoryClickEvent event) {
        this.scheduleCheck((Player) event.getWhoClicked());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateMonitoredInventory(final InventoryDragEvent event) {
        this.scheduleCheck((Player) event.getWhoClicked());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateMonitoredTransform(final PlayerBlockTransformEvent event) {
        MatchPlayer player = event.getPlayer();
        if(player != null) this.scheduleCheck(player.getBukkit());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateMonitoredPickup(final PlayerPickupItemEvent event) {
        this.scheduleCheck(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateMonitoredDrop(final PlayerDropItemEvent event) {
        this.scheduleCheck(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateMonitoredDamage(final EntityDamageEvent event) {
        if(event.getEntity() instanceof Player) {
            this.scheduleCheck((Player) event.getEntity());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateMonitoredHealth(final EntityRegainHealthEvent event) {
        if(event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if(player.getHealth() == player.getMaxHealth()) return;
            this.scheduleCheck((Player) event.getEntity());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateMonitoredHunger(final FoodLevelChangeEvent event) {
        this.scheduleCheck((Player) event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void updateMonitoredSpawn(final ParticipantSpawnEvent event) {
        // must have this hack so we update player's inventories when they respawn and recieve a kit
        ViewInventoryMatchModule.this.scheduleCheck(event.getPlayer().getBukkit());
    }

    public boolean canPreviewInventory(Player viewer, Player holder) {
        MatchPlayer matchViewer = getMatch().getPlayer(viewer);
        MatchPlayer matchHolder = getMatch().getPlayer(holder);
        return matchViewer != null && matchHolder != null && canPreviewInventory(matchViewer, matchHolder);
    }

    public boolean canPreviewInventory(MatchPlayer viewer, MatchPlayer holder) {
        return viewer.isObserving() && holder.isSpawned();
    }

    protected void scheduleCheck(Player updater) {
        updateQueue.computeIfAbsent(updater, player -> Instant.now().plus(TICK));
    }

    protected void checkMonitoredInventories(Player updater) {
        views.forEach((viewer, view) -> {
            if(view.isPlayerInventory() && updater.equals(view.getPlayerInventory().getHolder())) {
                previewPlayerInventory(viewer, view.getPlayerInventory());
            }
        });
    }

    protected void previewPlayerInventory(Player viewer, PlayerInventory inventory) {
        if(viewer == null) { return; }

        Player holder = (Player) inventory.getHolder();
        // Ensure that the title of the inventory is <= 32 characters long to appease Minecraft's restrictions on inventory titles
        String title = StringUtils.substring(holder.getDisplayName(viewer), 0, 32);

        Inventory preview = Bukkit.getServer().createInventory(viewer, 45, title);

        // handle inventory mapping
        for(int i = 0; i <= 35; i++) {
            preview.setItem(getInventoryPreviewSlot(i), inventory.getItem(i));
        }

        MatchPlayer matchHolder = this.match.getPlayer(holder);
        if (matchHolder != null && matchHolder.isParticipating()) {
            BlitzMatchModule module = matchHolder.getMatch().getMatchModule(BlitzMatchModuleImpl.class);
            if(module != null && module.activated() && module.lives(matchHolder).isPresent()) {
                int livesLeft = module.livesCount(matchHolder);
                ItemStack lives = new ItemStack(Material.EGG, livesLeft);
                ItemMeta lifeMeta = lives.getItemMeta();
                lifeMeta.addItemFlags(ItemFlag.values());
                String key = livesLeft == 1 ? "match.blitz.livesRemaining.singularLives" : "match.blitz.livesRemaining.pluralLives";
                lifeMeta.setDisplayName(ChatColor.GREEN + PGMTranslations.get().t(key, viewer, ChatColor.AQUA + String.valueOf(livesLeft) + ChatColor.GREEN));
                lives.setItemMeta(lifeMeta);
                preview.setItem(4, lives);
            }

            List<String> specialLore = new ArrayList<>();

            if(holder.getAllowFlight()) {
                specialLore.add(ChatColor.LIGHT_PURPLE + PGMTranslations.get().t("specialAbility.flying", viewer));
            }

            DoubleJumpMatchModule djmm = matchHolder.getMatch().getMatchModule(DoubleJumpMatchModule.class);
            if(djmm != null && djmm.hasKit(matchHolder)) {
                specialLore.add(ChatColor.LIGHT_PURPLE + PGMTranslations.get().t("specialAbility.doubleJump", viewer));
            }

            double knockbackResistance = holder.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).getValue();
            if(knockbackResistance > 0) {
                specialLore.add(ChatColor.LIGHT_PURPLE + PGMTranslations.get().t("specialAbility.knockbackResistance", viewer, (int) Math.ceil(knockbackResistance * 100)));
            }

            double knockbackReduction = holder.getKnockbackReduction();
            if(knockbackReduction > 0) {
                specialLore.add(ChatColor.LIGHT_PURPLE + PGMTranslations.get().t("specialAbility.knockbackReduction", viewer, (int) Math.ceil(knockbackReduction * 100)));
            }

            double walkSpeed = holder.getWalkSpeed();
            if(walkSpeed != WalkSpeedKit.BUKKIT_DEFAULT) {
                specialLore.add(ChatColor.LIGHT_PURPLE + PGMTranslations.get().t("specialAbility.walkSpeed", viewer, String.format("%.1f", walkSpeed / WalkSpeedKit.BUKKIT_DEFAULT)));
            }


            if(!specialLore.isEmpty()) {
                ItemStack special = new ItemStack(Material.NETHER_STAR);
                ItemMeta specialMeta = special.getItemMeta();
                specialMeta.addItemFlags(ItemFlag.values());
                specialMeta.setDisplayName(ChatColor.AQUA.toString() + ChatColor.ITALIC + PGMTranslations.get().t("player.inventoryPreview.specialAbilities", viewer));
                specialMeta.setLore(specialLore);
                special.setItemMeta(specialMeta);
                preview.setItem(5, special);
            }
        }

        // potions
        boolean hasPotions = holder.getActivePotionEffects().size() > 0;
        ItemStack potions = new ItemStack(hasPotions? Material.POTION : Material.GLASS_BOTTLE);
        ItemMeta potionMeta = potions.getItemMeta();
        potionMeta.addItemFlags(ItemFlag.values());
        potionMeta.setDisplayName(ChatColor.AQUA.toString() + ChatColor.ITALIC + PGMTranslations.get().t("player.inventoryPreview.potionEffects", viewer));
        List<String> lore = Lists.newArrayList();
        if(hasPotions) {
            for(PotionEffect effect : holder.getActivePotionEffects()) {
                lore.add(ChatColor.YELLOW + BukkitUtils.potionEffectTypeName(effect.getType()) + " " + (effect.getAmplifier() + 1));
            }
        } else {
            lore.add(ChatColor.YELLOW + PGMTranslations.get().t("player.inventoryPreview.noPotionEffects", viewer));
        }
        potionMeta.setLore(lore);
        potions.setItemMeta(potionMeta);
        preview.setItem(6, potions);

        // hunger and health
        ItemStack hunger = new ItemStack(Material.COOKED_BEEF, holder.getFoodLevel());
        ItemMeta hungerMeta = hunger.getItemMeta();
        hungerMeta.addItemFlags(ItemFlag.values());
        hungerMeta.setDisplayName(ChatColor.AQUA.toString() + ChatColor.ITALIC + PGMTranslations.get().t("player.inventoryPreview.hungerLevel", viewer));
        hungerMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        hunger.setItemMeta(hungerMeta);
        preview.setItem(7, hunger);

        ItemStack health = new ItemStack(Material.REDSTONE, (int) holder.getHealth());
        ItemMeta healthMeta = health.getItemMeta();
        healthMeta.addItemFlags(ItemFlag.values());
        healthMeta.setDisplayName(ChatColor.AQUA.toString() + ChatColor.ITALIC + PGMTranslations.get().t("player.inventoryPreview.healthLevel", viewer));
        healthMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        health.setItemMeta(healthMeta);
        preview.setItem(8, health);

        // set armor manually because craftbukkit is a derp
        preview.setItem(0, inventory.getHelmet());
        preview.setItem(1, inventory.getChestplate());
        preview.setItem(2, inventory.getLeggings());
        preview.setItem(3, inventory.getBoots());

        this.showInventoryPreview(viewer, inventory, preview);
    }

    public void previewInventory(Player viewer, Inventory realInventory) {
        if(viewer == null) { return; }

        if(realInventory instanceof PlayerInventory) {
            previewPlayerInventory(viewer, (PlayerInventory) realInventory);
        }else {
            Inventory fakeInventory;
            if(realInventory instanceof DoubleChestInventory) {
                if(realInventory.hasCustomName()) {
                    fakeInventory = Bukkit.createInventory(viewer, realInventory.getSize(), realInventory.getName());
                } else {
                    fakeInventory = Bukkit.createInventory(viewer, realInventory.getSize());
                }
            } else {
                if(realInventory.hasCustomName()) {
                    fakeInventory = Bukkit.createInventory(viewer, realInventory.getType(), realInventory.getName());
                } else {
                    fakeInventory = Bukkit.createInventory(viewer, realInventory.getType());
                }
            }
            fakeInventory.setContents(realInventory.contents());

            this.showInventoryPreview(viewer, realInventory, fakeInventory);
        }
    }

    protected void showInventoryPreview(Player viewer, Inventory realInventory, Inventory fakeInventory) {
        if(viewer == null) return;

        View view = views.get(viewer);
        if(view != null && view.watched.equals(realInventory) && view.preview.getSize() == fakeInventory.getSize()) {
            view.preview.setContents(fakeInventory.contents());
        } else {
            view = new View(realInventory, fakeInventory);
            views.put(viewer, view);
            viewer.openInventory(fakeInventory);
        }
    }

    private static class View {
        final Inventory watched;
        final Inventory preview;

        View(Inventory watched, Inventory preview) {
            this.watched = watched;
            this.preview = preview;
        }

        boolean isPlayerInventory() {
            return this.watched instanceof PlayerInventory;
        }

        PlayerInventory getPlayerInventory() {
            return (PlayerInventory) this.watched;
        }
    }
}
