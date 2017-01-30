package tc.oc.pgm.antigrief;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import com.google.common.collect.Lists;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerAttackEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import tc.oc.commons.bukkit.channels.AdminChannel;
import tc.oc.commons.bukkit.chat.ComponentRenderers;
import tc.oc.commons.bukkit.chat.ListComponent;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.bukkit.event.ObserverKitApplyEvent;
import tc.oc.commons.core.inject.Proxied;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.pgm.PGMTranslations;
import tc.oc.pgm.match.MatchManager;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.ParticipantState;
import tc.oc.pgm.tnt.TNTMatchModule;
import tc.oc.pgm.tracker.EntityResolver;

public class DefuseListener implements PluginFacet, Listener {
    public static final Material DEFUSE_ITEM = Material.SHEARS;
    public static final int DEFUSE_SLOT = 4;

    private final MatchManager mm;
    private final EntityResolver entityResolver;
    private final AdminChannel adminChannel;

    @Inject DefuseListener(MatchManager mm, @Proxied EntityResolver entityResolver, AdminChannel adminChannel) {
        this.mm = mm;
        this.entityResolver = entityResolver;
        this.adminChannel = adminChannel;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void rightClickDefuse(final PlayerInteractEntityEvent event) {
        ItemStack hand = event.getPlayer().getItemInHand();
        if(hand == null || hand.getType() != DEFUSE_ITEM) return;

        this.participantDefuse(event.getPlayer(), event.getRightClicked());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void leftClickDefuse(final PlayerAttackEntityEvent event) {
        this.participantDefuse(event.getPlayer(), event.getLeftClicked());
    }

    private void participantDefuse(Player player, Entity entity) {
        if(!AntiGrief.Defuse.enabled()) return;

        // check tnt
        if(!(entity instanceof TNTPrimed)) return;

        TNTMatchModule tntmm = mm.getMatch(player.getWorld()).getMatchModule(TNTMatchModule.class);
        if(tntmm != null && !tntmm.getProperties().friendlyDefuse) return;

        MatchPlayer clicker = this.mm.getPlayer(player);
        if(clicker == null || !clicker.canInteract()) return;

        // check water
        Block block = entity.getLocation().getBlock();
        if(block != null && (block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER)) {
            clicker.sendMessage(ChatColor.RED + PGMTranslations.t("defuse.water", clicker));
            return;
        }

        // check owner
        MatchPlayer owner = this.mm.getPlayer(entityResolver.getOwner(entity));
        if(owner == null || (owner != clicker && owner.getParty() == clicker.getParty())) { // cannot defuse own TNT
            // defuse TNT
            entity.remove();
            if(owner != null) {
                this.notifyDefuse(clicker, entity, ChatColor.RED + PGMTranslations.t("defuse.player", clicker, owner.getDisplayName(clicker) + ChatColor.RED));
                adminChannel.broadcast(clicker.getDisplayName() +
                                       ChatColor.WHITE + " defused " +
                                       owner.getDisplayName()
                                       + ChatColor.WHITE + "'s " +
                                       ChatColor.DARK_RED + "TNT");
            } else {
                this.notifyDefuse(clicker, entity, ChatColor.RED + PGMTranslations.t("defuse.world", clicker));
            }
        }
    }

    private void notifyDefuse(MatchPlayer clicker, Entity entity, String message) {
        clicker.sendMessage(message);
        for(Player viewer : Bukkit.getOnlinePlayers()) {
            viewer.playSound(entity.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1, 1);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void checkDefuse(final PlayerInteractEvent event) {
        if(!AntiGrief.Defuse.enabled()) return;
        ItemStack hand = event.getPlayer().getItemInHand();
        if(hand == null || hand.getType() != DEFUSE_ITEM) return;

        MatchPlayer clicker = this.mm.getPlayer(event.getPlayer());
        if(clicker != null && clicker.isObserving() && clicker.getBukkit().hasPermission("pgm.defuse")) {
            if(event.getAction() == Action.RIGHT_CLICK_AIR) {
                this.obsTntDefuse(clicker.getBukkit(), event.getPlayer().getLocation());
            } else if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                this.obsTntDefuse(clicker.getBukkit(), event.getClickedBlock().getLocation());
            }
        }
    }

    private void obsTntDefuse(Player player, Location loc) {
        List<ParticipantState> owners = this.removeTnt(loc, 5.0);
        if(owners != null && !owners.isEmpty()) {
            ComponentRenderers.send(
                player,
                new TranslatableComponent(
                    "defuse.player",
                    new ListComponent(
                        Lists.transform(owners, owner -> owner.getStyledName(NameStyle.COLOR))
                    )
                )
            );
        }
    }

    // Original code borrowed from WorldEdit
    private List<ParticipantState> removeTnt(Location origin, double radius) {
        if(radius <= 0) return null;

        List<ParticipantState> owners = new ArrayList<>();
        double radiusSq = radius * radius;
        for(Entity ent : origin.getWorld().getEntities()) {
            if(origin.distanceSquared(ent.getLocation()) > radiusSq) continue;

            if(ent instanceof TNTPrimed) {
                ParticipantState player = entityResolver.getOwner(ent);

                if(player != null) {
                    owners.add(player);
                }
                ent.remove();

            }
        }

        List<ParticipantState> uniqueOwners = new ArrayList<>();
        for(ParticipantState player : owners) {
            if(!uniqueOwners.contains(player)) {
                uniqueOwners.add(player);
            }
        }
        return uniqueOwners;
    }

    @EventHandler
    public void giveKit(final ObserverKitApplyEvent event) {
        final MatchPlayer player = mm.getPlayer(event.getPlayer());
        if(player == null) return;
        if(!player.isObservingType()) return;
        if(!player.getBukkit().hasPermission("pgm.defuse")) return;

        ItemStack shears = new ItemStack(DEFUSE_ITEM);

        // TODO: Update information if locale changes
        ItemMeta meta = shears.getItemMeta();
        meta.addItemFlags(ItemFlag.values());
        meta.setDisplayName(PGMTranslations.t("defuse.displayName", player));
        meta.setLore(Lists.newArrayList(ChatColor.GRAY + PGMTranslations.t("defuse.tooltip", player)));
        shears.setItemMeta(meta);

        event.getPlayer().getInventory().setItem(DEFUSE_SLOT, shears);
    }
}
