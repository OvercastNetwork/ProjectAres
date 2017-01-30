package tc.oc.pgm.channels;

import com.google.common.base.Preconditions;
import org.bukkit.permissions.Permission;
import tc.oc.chatmoderator.channels.SimpleFilteredChannel;
import tc.oc.pgm.match.Party;

public class FilteredPartyChannel extends SimpleFilteredChannel implements PartyChannel {

    private final Party party;

    public FilteredPartyChannel(String format, final Permission permission, final Party party, int minimumScoreNoSend, float partial) {
        super(format, permission, minimumScoreNoSend, partial);
        this.party = Preconditions.checkNotNull(party, "party");
    }

    @Override
    public Party getParty() {
        return this.party;
    }
}
