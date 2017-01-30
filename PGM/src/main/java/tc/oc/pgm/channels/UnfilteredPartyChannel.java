package tc.oc.pgm.channels;

import com.github.rmsy.channels.impl.SimpleChannel;
import com.google.common.base.Preconditions;
import org.bukkit.permissions.Permission;
import tc.oc.pgm.match.Party;

public class UnfilteredPartyChannel extends SimpleChannel implements PartyChannel {

    private Party party;

    public UnfilteredPartyChannel(String format, final Permission permission, Party party) {
        super(format, permission);
        this.party = Preconditions.checkNotNull(party, "party");
    }

    @Override
    public Party getParty() {
        return this.party;
    }
}
