package tc.oc.pgm.channels;

import com.github.rmsy.channels.Channel;
import tc.oc.pgm.match.Party;

public interface PartyChannel extends Channel {
    Party getParty();
}
