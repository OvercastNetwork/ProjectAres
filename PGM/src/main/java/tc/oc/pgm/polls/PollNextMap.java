package tc.oc.pgm.polls;

import org.bukkit.ChatColor;
import org.bukkit.Server;

import tc.oc.pgm.map.PGMMap;
import tc.oc.pgm.match.MatchManager;


public class PollNextMap extends Poll {
    private final MatchManager mm;
    private final PGMMap nextMap;

    public PollNextMap(PollManager pollManager, Server server, String initiator, MatchManager mm, PGMMap nextMap) {
        super(pollManager, server, initiator);
        this.mm = mm;
        this.nextMap = nextMap;
    }

    @Override
    public void executeAction() {
        this.mm.setNextMap(this.nextMap);
    }

    @Override
    public String getActionString(ChatColor neutral) {
        return "to set the next map to " + ChatColor.GOLD + this.nextMap.getInfo().name;
    }
}
