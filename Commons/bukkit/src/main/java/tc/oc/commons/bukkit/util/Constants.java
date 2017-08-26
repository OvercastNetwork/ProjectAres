package tc.oc.commons.bukkit.util;

import org.bukkit.ChatColor;

public enum Constants {

    PREFIX(ChatColor.GOLD.toString() + ChatColor.BOLD),
    SUBTEXT(ChatColor.AQUA.toString()),
    CLICK(ChatColor.RED.toString() + ChatColor.BOLD),
    ERROR(ChatColor.RED.toString());

    private final String location;

    Constants(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return location;
    }

}
