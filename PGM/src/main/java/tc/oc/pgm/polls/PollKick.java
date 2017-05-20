package tc.oc.pgm.polls;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class PollKick extends Poll {
    private final String player;

    public PollKick(PollManager pollManager, Server server, String initiator, String player) {
        super(pollManager, server, initiator);
        this.player = player;
    }

    @Override
    public void executeAction() {
        Player player = this.server.getPlayerExact(this.player);
        if(player != null) player.kickPlayer(ChatColor.DARK_RED + "You were poll-kicked.");
        this.server.broadcastMessage(ChatColor.RED + this.player + ChatColor.YELLOW + " has been kicked.");
    }

    @Override
    public String getActionString() {
        return normalize + "Kick: " + boldAqua + this.player;
    }

    @Override
    public String getDescriptionMessage() {
        return "to kick " + boldAqua +  this.player;
    }
}
