package tc.oc.pgm.listeners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.commons.bukkit.chat.ListComponent;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.bukkit.util.BukkitUtils;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;
import tc.oc.pgm.core.CoreLeakEvent;
import tc.oc.pgm.destroyable.Destroyable;
import tc.oc.pgm.destroyable.DestroyableContribution;
import tc.oc.pgm.destroyable.DestroyableDestroyedEvent;
import tc.oc.pgm.wool.PlayerWoolPlaceEvent;

public class FormattingListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void playerWoolPlace(final PlayerWoolPlaceEvent event) {
        if (event.getWool().isVisible()) {
            event.getMatch().sendMessage(new TranslatableComponent("match.complete.wool",
                                                                   event.getPlayer().getStyledName(NameStyle.COLOR),
                                                                   BukkitUtils.woolName(event.getWool().getDyeColor()),
                                                                   event.getPlayer().getParty().getComponentName()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void coreLeak(final CoreLeakEvent event) {
        if (event.getCore().isVisible()) {
            event.getMatch().sendMessage(new Component(new TranslatableComponent("match.complete.core",
                                                                                 Components.blank(),
                                                                                 event.getCore().getComponentName(),
                                                                                 event.getCore().getOwner().getComponentName()),
                                                       net.md_5.bungee.api.ChatColor.RED));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void destroyableDestroyed(final DestroyableDestroyedEvent event) {
        Destroyable destroyable = event.getDestroyable();

        if (destroyable.isVisible()) {
            List<DestroyableContribution> sorted = new ArrayList<>(event.getDestroyable().getContributions());
            Collections.sort(sorted, new Comparator<DestroyableContribution>() {
                @Override
                public int compare(DestroyableContribution o1, DestroyableContribution o2) {
                    return Double.compare(o2.getPercentage(), o1.getPercentage()); // reverse
                }
            });

            List<BaseComponent> contributors = new ArrayList<>();
            boolean someExcluded = false;
            for(DestroyableContribution entry : sorted) {
                if(entry.getPercentage() > 0.2) { // 20% necessary to be included
                    contributors.add(new TranslatableComponent(
                        "objective.credit.player.percentage",
                        entry.getPlayerState().getStyledName(NameStyle.COLOR),
                        new Component(String.valueOf(Math.round(entry.getPercentage() * 100)), net.md_5.bungee.api.ChatColor.AQUA)
                    ));
                } else {
                    someExcluded = true;
                }
            }

            BaseComponent credit;
            if(contributors.isEmpty()) {
                credit = someExcluded ? new TranslatableComponent("objective.credit.many") // All contributors < 20%
                                      : new TranslatableComponent("objective.credit.unknown"); // No contributors
            } else {
                if(someExcluded) {
                    contributors.add(new TranslatableComponent("objective.credit.etc")); // Some contributors < 20%
                }
                credit = new ListComponent(contributors);
            }

            event.getMatch().sendMessage(new TranslatableComponent(
                "match.complete.destroyable",
                credit,
                destroyable.getComponentName(),
                destroyable.getOwner().getComponentName()
            ));
        }
    }
}
