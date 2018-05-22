package tc.oc.pgm.channels;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.commons.bukkit.channels.Channel;
import tc.oc.commons.bukkit.channels.ChannelRouter;
import tc.oc.pgm.events.CompetitorAddEvent;
import tc.oc.pgm.events.CompetitorRemoveEvent;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.PartyAddEvent;
import tc.oc.pgm.events.PartyRemoveEvent;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.MultiPlayerParty;
import tc.oc.pgm.match.Party;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@ListenerScope(MatchScope.LOADED)
public class ChannelMatchModule extends MatchModule implements Listener {

    private final Map<Party, Channel> channels = new HashMap<>();

    @Inject
    ChannelRouter channelRouter;
    @Inject PartyChannel.Factory channelFactory;

    public void create(Party party) {
        if(!channels.containsKey(party) && party instanceof MultiPlayerParty) {
            channels.put(party, channelFactory.create(party));
        }
    }

    public void remove(Party party) {
        channels.remove(party);
    }

    @Override
    public void load() {
        match.getParties().forEach(this::create);
        channelRouter.setTeamChannelFunction(player -> channels.get(match.player(player).map(MatchPlayer::getParty).orElse(null)));
    }

    @Override
    public void unload() {
        channelRouter.setTeamChannelFunction(null);
        match.getParties().forEach(this::remove);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPartyAdd(PartyAddEvent event) {
        create(event.getParty());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPartyRemove(PartyRemoveEvent event) {
        remove(event.getParty());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCompetitorAdd(CompetitorAddEvent event) {
        create(event.getParty());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCompetitorRemove(CompetitorRemoveEvent event) {
        remove(event.getParty());
    }

}
