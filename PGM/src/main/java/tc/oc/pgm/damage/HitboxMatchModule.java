package tc.oc.pgm.damage;

import javax.inject.Inject;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerUseUnknownEntityEvent;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchScope;

@ListenerScope(MatchScope.RUNNING)
public class HitboxMatchModule extends MatchModule implements Listener {

    final TIntObjectMap<HitboxPlayerFacet> facets = new TIntObjectHashMap<>();

    @Inject HitboxMatchModule(Match match) {
        super(match);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHit(PlayerUseUnknownEntityEvent event) {
        final HitboxPlayerFacet facet = facets.get(event.getEntityId());
        if(facet != null) {
            facet.onUse(event.getPlayer(), event.isAttack(), event.getHand());
        }
    }
}
