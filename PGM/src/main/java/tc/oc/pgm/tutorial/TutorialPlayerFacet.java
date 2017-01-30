package tc.oc.pgm.tutorial;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import tc.oc.commons.bukkit.chat.ComponentRenderContext;
import tc.oc.commons.bukkit.event.targeted.TargetedEventHandler;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;
import tc.oc.time.Time;
import tc.oc.pgm.events.ObserverInteractEvent;
import tc.oc.pgm.map.MapInfo;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchPlayerFacet;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.Parties;
import tc.oc.pgm.match.Repeatable;
import tc.oc.commons.bukkit.event.ObserverKitApplyEvent;

public class TutorialPlayerFacet implements MatchPlayerFacet, Listener {

    private static final Material TUTORIAL_ITEM = Material.EMERALD;
    private static final int TUTORIAL_SLOT = 3;

    private final Player bukkit;
    private final PlayerInventory inventory;
    private final MatchPlayer player;
    private final Tutorial tutorial;
    private final MapInfo mapInfo;
    private final ComponentRenderContext renderer;

    private @Nullable TutorialStage currentStage;
    private @Nullable BaseComponent navigation;
    private long lastStageChangeMillis;

    @Inject TutorialPlayerFacet(Player bukkit, PlayerInventory inventory, MatchPlayer player, Tutorial tutorial, MapInfo mapInfo, ComponentRenderContext renderer) {
        this.bukkit = bukkit;
        this.inventory = inventory;
        this.player = player;
        this.tutorial = tutorial;
        this.mapInfo = mapInfo;
        this.renderer = renderer;
    }

    public void setCurrentStage(TutorialStage newStage) {
        if(player.isParticipating() || Objects.equals(currentStage, newStage)) return;

        // Debounce stage changes closer than 0.5 seconds
        final long nowMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
        if(nowMillis < lastStageChangeMillis + 500) return;

        currentStage = newStage;
        navigation = null;
        lastStageChangeMillis = nowMillis;

        if(newStage != null) {
            newStage.execute(player);
        }

        refreshNavigation();
    }

    @TargetedEventHandler(priority = EventPriority.MONITOR)
    public void onApplyKit(ObserverKitApplyEvent event) {
        giveKit(player);
    }

    @TargetedEventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(ObserverInteractEvent event) {
        if(tutorial.hasStages() && event.getClickedItem() != null && event.getClickedItem().getType() == TUTORIAL_ITEM) {
            switch(event.getClickType()) {
                case LEFT:
                    setCurrentStage(tutorial.getPreviousStage(currentStage));
                    break;
                case RIGHT:
                    setCurrentStage(tutorial.getNextStage(currentStage));
                    break;
            }
        }
    }

    private void giveKit(MatchPlayer player) {
        if(tutorial.hasStages() && Parties.isObservingType(player.getParty())) {
            inventory.setItem(TUTORIAL_SLOT, createItem(bukkit));
        }
    }

    private ItemStack createItem(CommandSender viewer) {
        final ItemStack item = new ItemStack(TUTORIAL_ITEM);
        final ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.values());
        meta.setDisplayName(renderer.renderLegacy(new Component(new TranslatableComponent("tutorial.displayName"), ChatColor.GOLD, ChatColor.BOLD), viewer));
        meta.setLore(ImmutableList.of(renderer.renderLegacy(new Component(new TranslatableComponent("tutorial.tooltip", mapInfo.getColoredName()), ChatColor.WHITE), viewer)));
        item.setItemMeta(meta);
        return item;
    }

    @Repeatable(interval = @Time(seconds = 1), scope = MatchScope.LOADED)
    public void refreshNavigation() {
        if(!tutorial.hasStages()) return;

        final ItemStack holding = inventory.getItemInHand();
        if(holding == null || holding.getType() != TUTORIAL_ITEM) return;

        if(currentStage != null) {
            if(navigation == null) {
                navigation = tutorial.renderNavigation(currentStage);
            }
            player.sendHotbarMessage(navigation);
        } else {
            if(navigation != null) {
                navigation = null;
                player.sendHotbarMessage(Components.blank());
            }
        }
    }
}
