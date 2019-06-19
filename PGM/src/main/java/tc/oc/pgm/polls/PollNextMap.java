package tc.oc.pgm.polls;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.api.docs.User;
import tc.oc.commons.bukkit.tokens.TokenUtil;
import tc.oc.pgm.map.PGMMap;
import tc.oc.pgm.match.MatchManager;

public class PollNextMap extends Poll {
    private final MatchManager mm;
    private final PGMMap nextMap;
    private CommandSender sender;
    private User user;

    public PollNextMap(PollManager pollManager, Server server, CommandSender sender, String initiator, MatchManager mm, PGMMap nextMap) {
        super(pollManager, server, initiator);
        this.mm = mm;
        this.nextMap = nextMap;
        this.sender = sender;
        if (sender instanceof Player) {
            user = TokenUtil.getUser((Player)sender);
        }
    }

    @Override
    public void executeAction() {
        this.mm.setNextMap(this.nextMap);
        if (user != null) {
            TokenUtil.giveMapTokens(user, -1);
        }
    }

    @Override
    public String getActionString() {
        return normalize + "Next map: " + boldAqua + this.nextMap.getInfo().name;
    }

    @Override
    public String getDescriptionMessage() {
        return "to set the next map to " + boldAqua + this.nextMap.getInfo().name;
    }
}
