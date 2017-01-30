package tc.oc.commons.bungee.sessions;

import net.md_5.bungee.api.ChatColor;

public enum SessionState {
    ABSENT(ChatColor.YELLOW),
    ONLINE(ChatColor.GREEN),
    OFFLINE(ChatColor.RED);

    SessionState(ChatColor color) {
        this.color = color;
    }

    public ChatColor getColor() {
        return this.color;
    }

    public String toString() {
        return color + super.toString();
    }

    private final ChatColor color;
}
