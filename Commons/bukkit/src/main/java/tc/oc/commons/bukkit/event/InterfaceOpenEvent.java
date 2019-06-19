package tc.oc.commons.bukkit.event;


import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import tc.oc.commons.bukkit.gui.Interface;

public class InterfaceOpenEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Interface gui;
    private final Player player;

    public InterfaceOpenEvent(Interface gui, Player player) {
        this.gui = gui;
        this.player = player;
    }

    public Interface getInterface() {
        return this.gui;
    }

    public Player getPlayer() {
        return this.player;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
